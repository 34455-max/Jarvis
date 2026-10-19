package com.example.ai

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiApiClient {
    private const val TAG = "GeminiApiClient"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    fun getEffectiveApiKey(customKey: String?): String {
        return when {
            !customKey.isNullOrBlank() && customKey != "MY_GEMINI_API_KEY" -> customKey.trim()
            try {
                val configKey = BuildConfig.GEMINI_API_KEY
                configKey.isNotBlank() && configKey != "MY_GEMINI_API_KEY"
            } catch (e: Exception) { false } -> BuildConfig.GEMINI_API_KEY
            else -> ""
        }
    }

    suspend fun generateContent(
        apiKey: String,
        systemInstruction: String,
        conversationHistory: List<Pair<String, String>>, // role ("user" | "model") to text
        userMessage: String
    ): Result<String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is not configured. Please set GEMINI_API_KEY in the Secrets panel or Settings.")
            )
        }

        try {
            val root = JSONObject()

            // System instruction
            if (systemInstruction.isNotBlank()) {
                val sysObj = JSONObject()
                val sysParts = JSONArray().apply {
                    put(JSONObject().apply { put("text", systemInstruction) })
                }
                sysObj.put("parts", sysParts)
                root.put("systemInstruction", sysObj)
            }

            // Contents array
            val contentsArray = JSONArray()

            // Include last 8 turns of conversation for contextual continuity
            val recentTurns = conversationHistory.takeLast(8)
            for ((role, text) in recentTurns) {
                if (text.isNotBlank()) {
                    val turnObj = JSONObject()
                    turnObj.put("role", if (role.equals("model", ignoreCase = true) || role.equals("jarvis", ignoreCase = true)) "model" else "user")
                    val parts = JSONArray().apply {
                        put(JSONObject().apply { put("text", text) })
                    }
                    turnObj.put("parts", parts)
                    contentsArray.put(turnObj)
                }
            }

            // Current user turn
            val currentTurn = JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", userMessage) })
                })
            }
            contentsArray.put(currentTurn)
            root.put("contents", contentsArray)

            // Generation config
            val config = JSONObject().apply {
                put("temperature", 0.7)
                put("topP", 0.95)
                put("maxOutputTokens", 1024)
            }
            root.put("generationConfig", config)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = root.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (!response.isSuccessful) {
                val code = response.code
                val errMessage = when (code) {
                    400 -> "API request format error or invalid parameter."
                    403 -> "Invalid API Key or unauthorized quota access."
                    429 -> "Gemini API rate limit exceeded. Please wait a moment."
                    500, 503 -> "Gemini service temporarily unavailable. Falling back to local assistant."
                    else -> "API Error ($code): ${response.message}"
                }
                Log.w(TAG, "Gemini API failed with code $code: $responseBody")
                return@withContext Result.failure(Exception(errMessage))
            }

            if (responseBody.isNullOrBlank()) {
                return@withContext Result.failure(Exception("Empty response from AI service."))
            }

            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext Result.failure(Exception("No response generated."))
            }

            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val generatedText = parts?.optJSONObject(0)?.optString("text")

            if (generatedText.isNullOrBlank()) {
                return@withContext Result.failure(Exception("Empty message content."))
            }

            Result.success(generatedText.trim())
        } catch (e: Exception) {
            Log.e(TAG, "Network or parsing exception during Gemini call", e)
            Result.failure(e)
        }
    }
}
