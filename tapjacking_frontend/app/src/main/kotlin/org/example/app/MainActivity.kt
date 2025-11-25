package org.example.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.*

class MainActivity : Activity() {

    private lateinit var btnShowOverlay: Button
    private lateinit var btnGrantPermission: Button
    private lateinit var permissionStatus: TextView
    private lateinit var overlayRunningText: TextView
    private lateinit var switchSafeDemo: Switch

    private val prefs by lazy {
        getSharedPreferences("tapjacking_prefs", Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnShowOverlay = findViewById(R.id.btnShowOverlay)
        btnGrantPermission = findViewById(R.id.btnGrantPermission)
        permissionStatus = findViewById(R.id.permissionStatus)
        overlayRunningText = findViewById(R.id.overlayRunningText)
        switchSafeDemo = findViewById(R.id.switchSafeDemo)

        // Restore preferences
        val safeMode = prefs.getBoolean("safeMode", true)
        switchSafeDemo.isChecked = safeMode

        updatePermissionStateUI()
        updateOverlayRunning(false)

        btnShowOverlay.setOnClickListener {
            if (!canDrawOverlays(this)) {
                Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Toggle start/stop if already running is not tracked via service binding, we keep simple: always (re)start
            startOverlayService()
            updateOverlayRunning(true)
        }

        btnGrantPermission.setOnClickListener {
            openOverlayPermissionSettings()
        }

        switchSafeDemo.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("safeMode", isChecked).apply()
            // If overlay already running, restart to apply
            stopOverlayService()
            if (canDrawOverlays(this)) {
                startOverlayService()
                updateOverlayRunning(true)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStateUI()
    }

    private fun updatePermissionStateUI() {
        val granted = canDrawOverlays(this)
        permissionStatus.text = if (granted) getString(R.string.permission_granted) else getString(R.string.permission_denied)
        btnGrantPermission.visibility = if (granted) View.GONE else View.VISIBLE
    }

    private fun updateOverlayRunning(running: Boolean) {
        overlayRunningText.text = if (running) getString(R.string.overlay_running) else getString(R.string.overlay_not_running)
    }

    // PUBLIC_INTERFACE
    fun canDrawOverlays(context: Context): Boolean {
        /** Returns whether the app can draw overlays on top of other apps. */
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    // PUBLIC_INTERFACE
    fun openOverlayPermissionSettings() {
        /** Opens the system overlay permission screen for this application. */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        } else {
            Toast.makeText(this, getString(R.string.permission_granted), Toast.LENGTH_SHORT).show()
        }
    }

    private fun startOverlayService() {
        val intent = Intent(this, OverlayService::class.java).apply {
            putExtra("safeMode", switchSafeDemo.isChecked)
            putExtra("opacity", 0.5f)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopOverlayService() {
        val intent = Intent(this, OverlayService::class.java)
        stopService(intent)
    }
}
