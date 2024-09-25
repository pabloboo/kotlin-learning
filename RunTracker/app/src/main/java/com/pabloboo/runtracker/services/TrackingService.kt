package com.pabloboo.runtracker.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.lifecycleScope
import com.pabloboo.runtracker.utils.Constants.ACTION_PAUSE_SERVICE
import com.pabloboo.runtracker.utils.Constants.ACTION_START_OR_RESUME_SERVICE
import com.pabloboo.runtracker.utils.Constants.ACTION_STOP_SERVICE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class TrackingService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    Timber.d("Started or resumed service")
                    startTracking()
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

    private fun startTracking() {
        // Lógica para iniciar el seguimiento
        //lifecycleScope.launch(Dispatchers.IO) {
            // Ejecuta el seguimiento en un hilo de fondo
        //}
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
}
