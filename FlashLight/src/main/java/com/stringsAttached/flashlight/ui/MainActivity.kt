package com.stringsAttached.flashlight.ui

import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.stringsAttached.flashlight.R
import com.stringsAttached.flashlight.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var cameraManager: CameraManager

    private lateinit var cameraId: String

    private var isChecked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupUI()
    }

    private fun setupUI() {
        val isFlashAvailable = applicationContext.packageManager
            .hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)
        if (!isFlashAvailable) {
            showNoFlashError()
        }
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            cameraId = cameraManager.cameraIdList[0]
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
        binding.flashLight.setOnClickListener {
            isChecked = !isChecked
            switchFlashLight(isChecked)
        }
    }

    private fun showNoFlashError() {
        val alert = AlertDialog.Builder(this)
            .create()
        alert.setTitle("Oops!")
        alert.setMessage("Flash not available in this device...")
        alert.setButton(DialogInterface.BUTTON_POSITIVE, "OK") { _, _ -> finish() }
        alert.show()
    }

    private fun switchFlashLight(status: Boolean) {
        try {
            cameraManager.setTorchMode(cameraId, status)
            if (status) {
                binding.root.setBackgroundColor(this.resources.getColor(R.color.white))
                binding.title.visibility = View.GONE
            } else {
                binding.title.visibility = View.VISIBLE
                binding.root.setBackgroundColor(this.resources.getColor(R.color.black))
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }
}