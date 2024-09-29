package com.pabloboo.runtracker.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.pabloboo.runtracker.R
import com.pabloboo.runtracker.ui.MainActivity
import com.pabloboo.runtracker.utils.Constants.ACTION_PAUSE_SERVICE
import com.pabloboo.runtracker.utils.Constants.ACTION_SHOW_TRACKING_SCREEN
import com.pabloboo.runtracker.utils.Constants.ACTION_START_OR_RESUME_SERVICE
import com.pabloboo.runtracker.utils.Constants.ACTION_STOP_SERVICE
import com.pabloboo.runtracker.utils.Constants.NOTIFICATION_CHANNEL_ID
import com.pabloboo.runtracker.utils.Constants.NOTIFICATION_CHANNEL_NAME
import com.pabloboo.runtracker.utils.Constants.NOTIFICATION_ID
import timber.log.Timber

class TrackingService : Service() {

    private var isFirstRun = true

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun) {
                        startForegroundService()
                        isFirstRun = false
                    } else {
                        Timber.d("Resumed service")
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.d("Paused service")
                    pauseTracking()
                }
                ACTION_STOP_SERVICE -> {
                    Timber.d("Stopped service")
                    stopTracking()
                    stopSelf()
                }
            }
        }
        return START_STICKY // Para reiniciar el servicio si se detiene
    }

    private fun pauseTracking() {
        // Lógica para pausar el seguimiento
    }

    private fun stopTracking() {
        // Lógica para detener el seguimiento
    }

    override fun onBind(intent: Intent?): IBinder? {
        // Este servicio no se vincula a una actividad, así que retornamos null
        return null
    }

    private fun startForegroundService() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_run)
            .setContentTitle("Run Tracker")
            .setContentText("00:00:00")
            .setContentIntent(getMainActivityPendingIntent())

        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java).also {
            it.action = ACTION_SHOW_TRACKING_SCREEN
            it.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        },
        FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW)
        notificationManager.createNotificationChannel(channel)
    }
}
