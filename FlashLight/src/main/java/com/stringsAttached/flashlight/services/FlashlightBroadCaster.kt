package com.stringsAttached.flashlight.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TorchToggleReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == "TOGGLE_TORCH") {
            val torchServiceIntent = Intent(context, TorchService::class.java)
            torchServiceIntent.action = "TOGGLE_TORCH"
            context.startService(torchServiceIntent)
        }
    }
}
