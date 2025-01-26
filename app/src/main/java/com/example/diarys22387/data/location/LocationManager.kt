package com.example.diarys22387.data.location

import android.content.Context
import android.location.Geocoder
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Singleton
class LocationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun getAddressFromLocation(latitude: Double, longitude: Double): String? {
        return suspendCoroutine { continuation ->
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                        val address = addresses.firstOrNull()?.let { addr ->
                            buildString {
                                append(addr.thoroughfare ?: "")
                                if (addr.thoroughfare != null && addr.subThoroughfare != null) {
                                    append(", ")
                                }
                                append(addr.subThoroughfare ?: "")
                                if (addr.locality != null) {
                                    if (isNotEmpty()) append(", ")
                                    append(addr.locality)
                                }
                            }
                        }
                        continuation.resume(address)
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    val address = addresses?.firstOrNull()?.let { addr ->
                        buildString {
                            append(addr.thoroughfare ?: "")
                            if (addr.thoroughfare != null && addr.subThoroughfare != null) {
                                append(", ")
                            }
                            append(addr.subThoroughfare ?: "")
                            if (addr.locality != null) {
                                if (isNotEmpty()) append(", ")
                                append(addr.locality)
                            }
                        }
                    }
                    continuation.resume(address)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                continuation.resume(null)
            }
        }
    }
} 