package com.example.diarys22387.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationManager @Inject constructor(
    private val context: Context
) {
    private val fusedClient by lazy { LocationServices.getFusedLocationProviderClient(context) }

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val background = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        return (fine == PackageManager.PERMISSION_GRANTED && background == PackageManager.PERMISSION_GRANTED)
    }

    suspend fun getLastKnownLocation(): Location? {
        return try {
            if (!hasLocationPermission()) null
            else fusedClient.lastLocation.await()
        } catch (se: SecurityException) {
            null
        }
    }

    fun getAddressFromCoords(lat: Double, lon: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            addresses?.firstOrNull()?.locality ?: "Unknown place"
        } catch (e: Exception) {
            e.printStackTrace()
            "Unknown place"
        }
    }
}
