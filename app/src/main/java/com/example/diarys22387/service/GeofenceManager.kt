package com.example.diarys22387.service

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.example.diarys22387.data.model.Note
import com.google.android.gms.location.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeofenceManager @Inject constructor(
    private val context: Context
) {
    private val geofencingClient: GeofencingClient by lazy {
        LocationServices.getGeofencingClient(context)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun addGeofenceForNote(note: Note) {
        try {
            if (!hasLocationPermission()) return
            if (note.latitude == null || note.longitude == null) return

            val geofence = Geofence.Builder()
                .setRequestId(note.id)
                .setCircularRegion(note.latitude, note.longitude, 1000f)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build()

            val request = GeofencingRequest.Builder()
                .addGeofence(geofence)
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .build()

            geofencingClient.addGeofences(request, geofencePendingIntent)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun removeGeofence(noteId: String) {
        if (!hasLocationPermission()) return
        geofencingClient.removeGeofences(listOf(noteId))
    }

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java).apply {
            action = "com.example.diarys22387.ACTION_GEOFENCE_EVENT"
        }
        PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val background = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        return fine == PackageManager.PERMISSION_GRANTED && background == PackageManager.PERMISSION_GRANTED
    }
}
