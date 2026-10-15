package com.duress.app.pipeline

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

object LocationHelper {

    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        return fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    fun getLastKnownLocation(context: Context): Location? {
        if (!hasLocationPermission(context)) return null
        return try {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
            val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER)

            var bestLocation: Location? = null
            for (provider in providers) {
                if (lm.isProviderEnabled(provider)) {
                    val loc = lm.getLastKnownLocation(provider) ?: continue
                    if (bestLocation == null || loc.time > bestLocation.time) {
                        bestLocation = loc
                    }
                }
            }
            bestLocation
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Attempts to acquire fresh location with timeout (e.g. 8 seconds).
     * Falls back to last known location.
     */
    @SuppressLint("MissingPermission")
    suspend fun getFreshLocationWithFallback(context: Context, timeoutMs: Long = 8000L): Location? {
        if (!hasLocationPermission(context)) return null

        val lastKnown = getLastKnownLocation(context)
        // If last known is less than 60 seconds old, it's fresh enough
        if (lastKnown != null && System.currentTimeMillis() - lastKnown.time < 60_000L) {
            return lastKnown
        }

        val fresh = withTimeoutOrNull(timeoutMs) {
            suspendCancellableCoroutine<Location?> { continuation ->
                try {
                    val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                    if (lm == null) {
                        continuation.resume(null)
                        return@suspendCancellableCoroutine
                    }

                    val provider = when {
                        lm.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
                        lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
                        else -> null
                    }

                    if (provider == null) {
                        continuation.resume(lastKnown)
                        return@suspendCancellableCoroutine
                    }

                    val listener = object : LocationListener {
                        override fun onLocationChanged(location: Location) {
                            try {
                                lm.removeUpdates(this)
                            } catch (_: Exception) {}
                            if (continuation.isActive) {
                                continuation.resume(location)
                            }
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                        override fun onProviderEnabled(provider: String) {}
                        override fun onProviderDisabled(provider: String) {}
                    }

                    lm.requestLocationUpdates(provider, 0L, 0f, listener, Looper.getMainLooper())
                    continuation.invokeOnCancellation {
                        try {
                            lm.removeUpdates(listener)
                        } catch (_: Exception) {}
                    }
                } catch (e: Exception) {
                    if (continuation.isActive) continuation.resume(lastKnown)
                }
            }
        }

        return fresh ?: lastKnown
    }

    fun formatMapsUrl(location: Location?): String {
        return if (location != null) {
            "https://maps.google.com/?q=${location.latitude},${location.longitude}"
        } else {
            "(Location unavailable/acquiring GPS...)"
        }
    }
}
