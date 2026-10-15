package com.duress.app.pipeline

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.duress.app.R
import com.duress.app.data.DuressPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SosTrackingService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default)
    private var trackingJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_TRACKING) {
            stopTracking()
            return START_NOT_STICKY
        }

        startForegroundNotification()
        startPeriodicTracking()
        return START_NOT_STICKY
    }

    private fun startForegroundNotification() {
        try {
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.location_tracking_service_name))
                .setContentText(getString(R.string.location_tracking_notification))
                .setSmallIcon(R.drawable.ic_widget_sos)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val serviceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                } else {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                }
                startForeground(NOTIFICATION_ID, notification, serviceType)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            // Failure-isolated
        }
    }

    private fun startPeriodicTracking() {
        trackingJob?.cancel()
        trackingJob = serviceScope.launch {
            val prefs = DuressPreferences(applicationContext)
            val startTime = System.currentTimeMillis()
            val totalDurationMs = 15 * 60 * 1000L // 15 minutes
            val refreshIntervalMs = 60 * 1000L    // 60 seconds

            var updateCount = 1

            while (isActive && (System.currentTimeMillis() - startTime) < totalDurationMs) {
                delay(refreshIntervalMs)
                if (!isActive) break

                try {
                    val config = prefs.pipelineConfigFlow.first()
                    if (!config.actionSmsEnabled || !config.actionLocationTrackingEnabled) {
                        break
                    }

                    val location = LocationHelper.getFreshLocationWithFallback(applicationContext, 8000L)
                    val mapsUrl = LocationHelper.formatMapsUrl(location)

                    updateCount++
                    val updateMessage = "SOS Location Update #$updateCount:\n$mapsUrl"

                    val customConfig = config.copy(
                        customSmsTemplate = updateMessage
                    )
                    SmsSender.sendSosSms(applicationContext, customConfig, mapsUrl)
                } catch (_: Exception) {}
            }

            stopTracking()
        }
    }

    private fun stopTracking() {
        try {
            trackingJob?.cancel()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        } catch (_: Exception) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        trackingJob?.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.location_tracking_service_name),
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = getString(R.string.location_tracking_notification)
                    setShowBadge(false)
                }
                val nm = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                nm?.createNotificationChannel(channel)
            } catch (_: Exception) {}
        }
    }

    companion object {
        private const val CHANNEL_ID = "duress_live_sos_channel"
        private const val NOTIFICATION_ID = 9110

        const val ACTION_START_TRACKING = "com.duress.app.ACTION_START_TRACKING"
        const val ACTION_STOP_TRACKING = "com.duress.app.ACTION_STOP_TRACKING"

        fun start(context: Context) {
            try {
                val intent = Intent(context, SosTrackingService::class.java).apply {
                    action = ACTION_START_TRACKING
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (_: Exception) {}
        }

        fun stop(context: Context) {
            try {
                val intent = Intent(context, SosTrackingService::class.java).apply {
                    action = ACTION_STOP_TRACKING
                }
                context.startService(intent)
            } catch (_: Exception) {}
        }
    }
}
