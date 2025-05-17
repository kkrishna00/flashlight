package com.stringsAttached.flashlight.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import androidx.core.content.ContextCompat.getSystemService

class TorchToggleReceiver : BroadcastReceiver() {
    private lateinit var cameraManager: CameraManager
    private lateinit var cameraId: String

    override fun onReceive(context: Context, intent: Intent?) {

//        cameraManager = getSystemService(context, CameraManager::class.java) as CameraManager
//
//        cameraId = cameraManager.cameraIdList.firstOrNull { id ->
//            cameraManager.getCameraCharacteristics(id)
//                .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
//        } ?: return

        if (intent?.action == "TOGGLE_TORCH") {
            val torchServiceIntent = Intent(context, TorchService::class.java)
            torchServiceIntent.action = "TOGGLE_TORCH"
            context.startService(torchServiceIntent)
        }
    }
}
