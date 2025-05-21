package com.stringsAttached.flashlight.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.stringsAttached.flashlight.R

class TorchService : Service() {
    private var isTorchOn = false
    private var hasStartedForeground = false
    private lateinit var cameraManager: CameraManager
    private lateinit var cameraId: String
    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        cameraId = cameraManager.cameraIdList.firstOrNull { id ->
            cameraManager.getCameraCharacteristics(id)
                .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        } ?: return
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "TOGGLE_TORCH" -> toggleTorch()
            "STOP_SERVICE" -> stopSelf()
            "UPDATE_NOTIFICATION" -> {
                isTorchOn = intent.getBooleanExtra("TORCH_STATE", false)
            }
        }

        createNotificationChannel()
        updateNotification()
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "torch_channel",
                "Torch Controls",
                NotificationManager.IMPORTANCE_LOW // set LOW to reduce feedback
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun toggleTorch() {
        isTorchOn = !isTorchOn
        try {
            cameraManager.setTorchMode(cameraId, isTorchOn)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun buildNotification(): NotificationCompat.Builder {
        val toggleIntent = Intent(this, TorchToggleReceiver::class.java).apply {
            action = "TOGGLE_TORCH"
        }
        val pendingToggleIntent = PendingIntent.getBroadcast(
            this, 0, toggleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, TorchService::class.java).apply {
            action = "STOP_SERVICE"
        }
        val pendingStopIntent = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, "torch_channel")
            .setContentTitle("Flashlight")
            .setContentText(if (isTorchOn) "Torch is ON" else "Torch is OFF")
            .setSmallIcon(R.drawable.torch_set_logo_design__2_)
            .addAction(
                R.drawable.torch_set_logo_design__2_,
                if (isTorchOn) "Turn OFF" else "Turn ON",
                pendingToggleIntent
            )
            .addAction(
                R.drawable.torch_set_logo_design,
                "Stop",
                pendingStopIntent
            )
            .setOngoing(true)
    }

    private fun updateNotification() {
        val notification = buildNotification().build()
        if (!hasStartedForeground) {
            startForeground(1, notification)
            hasStartedForeground = true
        } else {
            notificationManager.notify(1, notification)
        }
    }

    override fun onBind(intent: Intent?) = null
}

