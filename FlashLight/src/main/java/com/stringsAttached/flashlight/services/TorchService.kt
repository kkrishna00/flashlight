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
    private lateinit var cameraManager: CameraManager
    private lateinit var cameraId: String

    override fun onCreate() {
        super.onCreate()
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraId = cameraManager.cameraIdList.firstOrNull { id ->
            cameraManager.getCameraCharacteristics(id)
                .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        } ?: return
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "TOGGLE_TORCH" -> toggleTorch()
            "STOP_SERVICE" -> {
                stopSelf()
            }
            "UPDATE_NOTIFICATION" -> {
                isTorchOn = intent.getBooleanExtra("TORCH_STATE", false)
                showNotification()
            }
        }
        createNotificationChannel(this)
        showNotification()
        return START_STICKY
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "torch_channel",
                "Torch Controls",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
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

    private fun showNotification() {
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

        val notification = NotificationCompat.Builder(this, "torch_channel")
            .setContentTitle("Flashlight")
            .setContentText(if (isTorchOn) "Torch is ON" else "Torch is OFF")
            .setSmallIcon(R.drawable.torch_set_logo_design__2_) // your custom icon
            .addAction(
                if (isTorchOn) R.drawable.torch_set_logo_design__2_ else R.drawable.torch_set_logo_design__2_,
                if (isTorchOn) "Turn OFF" else "Turn ON",
                pendingToggleIntent
            )
            .addAction(com.google.android.gms.ads.impl.R.drawable.admob_close_button_white_circle_black_cross, "Stop", pendingStopIntent)
            .setOngoing(true)
            .build()

        startForeground(1, notification)
    }

    override fun onBind(intent: Intent?) = null
}
