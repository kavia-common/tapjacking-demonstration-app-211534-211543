package org.example.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.TextView
import androidx.core.app.NotificationCompat

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "tapjacking_overlay"
            val channel = NotificationChannel(channelId, "Overlay", NotificationManager.IMPORTANCE_MIN)
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
            val notification: Notification = NotificationCompat.Builder(this, channelId)
                .setContentTitle("Overlay running")
                .setContentText("Tapjacking overlay is active")
                .setSmallIcon(android.R.drawable.ic_menu_view)
                .build()
            startForeground(1001, notification)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        removeOverlay()

        val safeMode = intent?.getBooleanExtra("safeMode", true) ?: true
        val alpha = intent?.getFloatExtra("opacity", 0.5f) ?: 0.5f

        addOverlay(safeMode, alpha)

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        removeOverlay()
    }

    private fun addOverlay(safeMode: Boolean, alpha: Float) {
        val layoutInflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = layoutInflater.inflate(R.layout.overlay_view, null)
        overlayView = view

        // Apply requested opacity to scrim
        view.alpha = 1.0f // overall view alpha intact; use background alpha through color
        val root = view.findViewById<View>(R.id.overlayRoot)
        root.background.alpha = (alpha * 255).toInt()

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            WindowManager.LayoutParams.TYPE_PHONE

        val flags = if (safeMode) {
            // Consume all touches to avoid passthrough
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        } else {
            // Allow taps to pass through outside focused views
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            type,
            flags,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START

        // Close control always consumes taps and dismisses overlay
        val btnClose = view.findViewById<TextView>(R.id.btnCloseOverlay)
        btnClose.setOnClickListener {
            stopSelf()
        }
        // Ensure close button always intercepts taps
        btnClose.setOnTouchListener { _, _ -> false } // allow click listener

        // For safe mode, intercept all touches
        if (safeMode) {
            view.setOnTouchListener { _, _ -> true } // consume all
            // But allow close button to receive
            btnClose.isClickable = true
            btnClose.isFocusable = true
            btnClose.bringToFront()
        } else {
            // Passthrough outside views – keep not touch modal so touches outside focused views pass
            view.setOnTouchListener(null)
        }

        try {
            windowManager.addView(view, params)
        } catch (_: Exception) {
            // If addView fails (permission revoked), stop service
            stopSelf()
        }
    }

    private fun removeOverlay() {
        overlayView?.let {
            try {
                windowManager.removeView(it)
            } catch (_: Exception) { }
            overlayView = null
        }
    }
}
