package com.pabloboo.runtracker.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.pabloboo.runtracker.R
import com.pabloboo.runtracker.utils.Constants.ACTION_START_SERVICE
import com.pabloboo.runtracker.utils.Constants.ACTION_STOP_SERVICE
import com.pabloboo.runtracker.utils.Constants.FASTEST_LOCATION_INTERVAL
import com.pabloboo.runtracker.utils.Constants.LOCATION_UPDATE_INTERVAL
import com.pabloboo.runtracker.utils.Constants.NOTIFICATION_CHANNEL_ID
import com.pabloboo.runtracker.utils.Constants.NOTIFICATION_CHANNEL_NAME
import com.pabloboo.runtracker.utils.Constants.NOTIFICATION_ID
import com.pabloboo.runtracker.utils.Constants.TIMER_UPDATE_INTERVAL
import com.pabloboo.runtracker.utils.TrackingUtility
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

typealias Polyline = MutableList<LatLng>

@AndroidEntryPoint
class TrackingService : Service(), LifecycleOwner {

    private var serviceKilled = false

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    private lateinit var lifecycleRegistry: LifecycleRegistry

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder

    private lateinit var currentNotificationBuilder: NotificationCompat.Builder

    private val timeRunInSeconds = MutableLiveData<Long>()

    companion object {
        val isTracking = MutableLiveData<Boolean>()

        private val _pathPoints = MutableLiveData<Polyline>()
        val pathPoints: LiveData<Polyline>
            get() = _pathPoints

        val timeRunInMillis = MutableLiveData<Long>()
    }

    private fun postInitialValues() {
        isTracking.postValue(false)
        _pathPoints.postValue(mutableListOf())
        timeRunInSeconds.postValue(0)
        timeRunInMillis.postValue(0)
    }

    override fun onCreate() {
        super.onCreate()

        lifecycleRegistry = LifecycleRegistry(this)
        lifecycleRegistry.currentState = Lifecycle.State.STARTED

        currentNotificationBuilder = baseNotificationBuilder
        postInitialValues()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        isTracking.observe(this) {
            updateLocationTracking(it)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_SERVICE -> {
                    startForegroundService()
                }
                ACTION_STOP_SERVICE -> {
                    Timber.d("Stopped service")
                    killService()
                }
            }
        }
        return START_STICKY // Restart the service if it's killed
    }

    // Timer logic
    private var timeRun = 0L
    private var timeStarted = 0L
    private var lastSecondTimestamp = 0L

    private fun startTimer() {
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!) {
                // Time difference between now and when the timer started
                timeRun = System.currentTimeMillis() - timeStarted
                timeRunInMillis.postValue(timeRun)
                // If the time difference is greater than a second
                if (timeRun >= lastSecondTimestamp + 1000L) {
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)
                    lastSecondTimestamp += 1000L
                }
                delay(TIMER_UPDATE_INTERVAL)
            }
        }
    }

    // Update notification logic
    private fun updateNotificationTrackingState() {
        val intent = Intent(this, TrackingService::class.java).apply {
            action = ACTION_STOP_SERVICE
        }
        val pendingIntent = PendingIntent.getService(this, 1, intent, FLAG_UPDATE_CURRENT or FLAG_MUTABLE)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (!serviceKilled) {
            currentNotificationBuilder = baseNotificationBuilder
                .addAction(R.drawable.ic_pause, "Stop", pendingIntent)

            notificationManager.notify(NOTIFICATION_ID, currentNotificationBuilder.build())
        }
    }

    // Location tracking logic
    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if(isTracking) {
            if(TrackingUtility.hasLocationPermissions(this)) {
                val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, LOCATION_UPDATE_INTERVAL)
                    .setMinUpdateIntervalMillis(FASTEST_LOCATION_INTERVAL)
                    .build()

                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)
            if (isTracking.value!!) {
                p0.locations.let { locations ->
                    for (location in locations) {
                        addPathPoint(location)
                        Timber.d("New Location: ${location.latitude}, ${location.longitude}")
                    }
                }
            }
        }
    }

    private fun addPathPoint(location: Location?) {
        location?.let {
            val pos = LatLng(location.latitude, location.longitude)
            _pathPoints.value?.apply {
                add(pos)
                _pathPoints.postValue(this)
            }
        }
    }

    private fun stopTracking() {
        // Logic to stop tracking
    }

    override fun onBind(intent: Intent?): IBinder? {
        // This service is not bound to an activity, so we return null
        return null
    }

    private fun startForegroundService() {
        startTimer()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        startForeground(NOTIFICATION_ID, baseNotificationBuilder.build())

        timeRunInSeconds.observe(this) {
            if (!serviceKilled) {
                val notification = currentNotificationBuilder
                    .setContentText(TrackingUtility.getFormattedStopWatchTime(it * 1000L))
                notificationManager.notify(NOTIFICATION_ID, notification.build())
            }
        }

        updateNotificationTrackingState()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW)
        notificationManager.createNotificationChannel(channel)
    }

    // Stop the service
    private fun killService() {
        serviceKilled = true
        postInitialValues()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
}
