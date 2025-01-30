package com.example.diarys22387

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.lifecycle.lifecycleScope
import com.example.diarys22387.data.DemoDataInitializer
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.*
import javax.inject.Inject

@HiltAndroidApp
class DiarysApp : Application() {

    @Inject
    lateinit var demoInitializer: DemoDataInitializer

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        
        FirebaseApp.initializeApp(this)
        
        appScope.launch(Dispatchers.IO) {
            try {
                demoInitializer.loadDemoDataIfNeeded()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        createNotificationChannel()
    }

    override fun onTerminate() {
        super.onTerminate()
        appScope.cancel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "geofence_channel",
                "Geofence Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for nearby notes"
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
