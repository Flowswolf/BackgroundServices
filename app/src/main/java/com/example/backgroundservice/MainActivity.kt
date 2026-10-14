package com.example.backgroundservice // Sesuaikan nama package kamu

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var statusTextView: TextView
    private lateinit var btnStart: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        statusTextView = findViewById(R.id.statusTextView)
        btnStart = findViewById(R.id.btnStart)

        btnStart.setOnClickListener {
            if (hasAllPermissions()) {
                startTrackerService()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest,
                    REQUEST_CODE_PERMISSIONS
                )
            }
        }

        handleIntent(intent)
    }

    private fun startTrackerService() {
        val serviceIntent = Intent(this, TrackerService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
        statusTextView.text = "Background service sedang berjalan..."
    }

    private fun hasAllPermissions(): Boolean {
        return permissionsToRequest.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            var allPermissionsGranted = true
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false
                    break
                }
            }
            if (allPermissionsGranted) {
                val serviceIntent = Intent(this, TrackerService::class.java)
                ContextCompat.startForegroundService(this, serviceIntent)
                statusTextView.text = "Background service sedang berjalan..."
            } else {
                statusTextView.text = "Izin ditolak, service tidak dapat dijalankan."
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == ACTION_SERVICE_STOPPED_JUMP) {
            statusTextView.text = "Background services sudah berhenti karena Anda melompat"
        }
    }

    companion object {
        const val ACTION_SERVICE_STOPPED_JUMP =
            "com.example.backgroundservice.ACTION_SERVICE_STOPPED_JUMP" // Sesuaikan nama package kamu
        const val REQUEST_CODE_PERMISSIONS = 101
    }

    private val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            android.Manifest.permission.ACTIVITY_RECOGNITION,
            android.Manifest.permission.POST_NOTIFICATIONS,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    } else {
        arrayOf(
            android.Manifest.permission.ACTIVITY_RECOGNITION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }
}