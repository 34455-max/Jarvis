package com.example

import android.app.Application
import com.example.data.JarvisDatabase
import com.example.data.repository.JarvisRepository
import com.example.service.JarvisNotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class JarvisApplication : Application() {

    lateinit var database: JarvisDatabase
        private set

    lateinit var repository: JarvisRepository
        private set

    lateinit var notificationHelper: JarvisNotificationHelper
        private set

    private val applicationScope = CoroutineScope(Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        database = JarvisDatabase.getDatabase(this)
        repository = JarvisRepository(database)
        notificationHelper = JarvisNotificationHelper(this)

        applicationScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }
}
