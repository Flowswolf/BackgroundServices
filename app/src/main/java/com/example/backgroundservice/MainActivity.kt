package com.example.backgroundservice // Sesuaikan nama package kamu

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract
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
    private lateinit var btnLatihan: Button

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
        btnLatihan = findViewById(R.id.btnLatihan)

        btnStart.setOnClickListener {
            if (hasAllPermissions()) {
                startTrackerService(isLatihan = false)
            } else {
                requestPermissions()
            }
        }
        btnLatihan.setOnClickListener {
            if (hasAllPermissions()) {
                startTrackerService(isLatihan = true)
            } else {
                requestPermissions()
            }
        }

        handleIntent(intent)
    }

    private fun startTrackerService(isLatihan: Boolean) {
        val serviceIntent = Intent(this, TrackerService::class.java).apply {
            putExtra(TrackerService.EXTRA_IS_LATIHAN, isLatihan)
        }
        ContextCompat.startForegroundService(this, serviceIntent)

        if (isLatihan) {
            statusTextView.text = "Latihan dimulai! Silakan melompat lebih dari 2 kali..."
        } else {
            statusTextView.text = "Background service sedang berjalan..."
        }
    }

    private fun hasAllPermissions(): Boolean {
        return permissionsToRequest.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            permissionsToRequest,
            REQUEST_CODE_PERMISSIONS
        )
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
        when (intent?.action) {
            ACTION_SERVICE_STOPPED_JUMP -> {
                statusTextView.text = "Background services sudah berhenti karena Anda melompat"
            }
            ACTION_LATIHAN_SELESAI -> {
                val totalJumps = intent.getIntExtra(EXTRA_JUMP_COUNT, 0)
                statusTextView.text = "Latihan Selesai! 🎉\nTotal Lompatan: $totalJumps kali"

                // Otomatis buka form tambah event ke Kalender
                addCalendarEvent(totalJumps)
            }
        }
    }

    private fun addCalendarEvent(totalJumps: Int) {
        val calendarIntent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, "Latihan Melompat Selesai")
            putExtra(CalendarContract.Events.DESCRIPTION, "Berhasil menyelesaikan latihan melompat sebanyak $totalJumps kali hari ini.")
            putExtra(CalendarContract.Events.EVENT_LOCATION, "Rumah / Tempat Latihan")
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, System.currentTimeMillis())
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, System.currentTimeMillis() + (30 * 60 * 1000)) // 30 menit
        }

        try {
            startActivity(calendarIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    companion object {
        const val ACTION_SERVICE_STOPPED_JUMP =
            "com.example.backgroundservice.ACTION_SERVICE_STOPPED_JUMP"
        const val ACTION_LATIHAN_SELESAI =
            "com.example.backgroundservice.ACTION_LATIHAN_SELESAI"
        const val EXTRA_JUMP_COUNT = "extra_jump_count"
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