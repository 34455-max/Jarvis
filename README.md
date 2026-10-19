# JARVIS — Personal AI Assistant for Android

JARVIS is a native Android AI assistant application built with Kotlin, Jetpack Compose, Android Speech APIs, and Google Gemini.

---

## 🚀 Quick Install (No Build Required)

An already-compiled, ready-to-install Android APK is included in this package:
- **File**: `JARVIS.apk` (23 MB)

### How to Install on Your Android Phone:
1. Copy `JARVIS.apk` to your Android phone (via USB cable, Google Drive, WhatsApp, or Bluetooth).
2. Open your phone's **Files** or **Downloads** app.
3. Tap **`JARVIS.apk`**.
4. If prompted with *"For your security, your phone is not allowed to install unknown apps from this source"*, tap **Settings** and enable **Allow from this source**.
5. Tap **Install** and then **Open**.

---

## 🎙️ Core Features

1. **Natural Multilingual Voice & Chat**
   - Push-to-talk microphone with real-time waveform visualizer.
   - Speech-to-text input and natural Text-to-Speech (TTS) spoken output.
   - Supports **English**, **Kiswahili (Swahili)**, and **Kikamba**.
   - Tap the glowing arc-reactor orb or mic icon to talk.

2. **Dedicated Message Assistant**
   - Decodes message intent and emotional tone.
   - Generates 4 natural reply styles: *Formal & Professional*, *Friendly & Warm*, *Short & Direct*, and *Polite Decline / Firm*.
   - One-tap actions to copy or open directly in WhatsApp or Gmail.

3. **Tasks, Deadlines & "Important Now" Section**
   - Persistent Room database storage that survives app restarts.
   - "Important Now" banner highlights overdue and critical priorities.
   - Voice commands (e.g., *"Remind me to study at 7 PM"*) schedule tasks automatically.

4. **Android Permissions & Emergency Shortcut**
   - Individual controls for Microphone, Notifications, and Notification Ingestion.
   - Emergency dialer shortcut with a safety confirmation modal before launching the phone dialer.

---

## 🛠️ Building from Source (Optional)

If you have Android Studio or Gradle installed on your computer:
```bash
# Clone or extract this folder, then run:
gradle :app:assembleDebug
```
The output APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 🔑 AI Engine Configuration
- By default, JARVIS includes an on-device offline conversational engine.
- To enable live **Gemini 2.5 Flash** cloud conversations:
  1. Open the app on your phone.
  2. Go to **Settings > Gemini 2.5 Flash Engine**.
  3. Paste your Gemini API key and tap **Save API Key**.
