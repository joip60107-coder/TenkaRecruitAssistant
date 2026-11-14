
package com.tenka.recruitassistant

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView

class FloatingService : Service() {

    companion object {
        const val ACTION_HIDE = "com.tenka.recruitassistant.HIDE"
        const val ACTION_SHOW = "com.tenka.recruitassistant.SHOW"
    }

    private lateinit var windowManager: WindowManager
    private var floatingButton: TextView? = null
    private var isButtonVisible = false
    private lateinit var layoutParams: WindowManager.LayoutParams

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createFloatingButton()
        showFloatingButton()
    }

    private fun createFloatingButton() {
        val button = TextView(this).apply {
            text = "徵"
            textSize = 22f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.rgb(45, 105, 190))
                setStroke(3, Color.WHITE)
            }
            elevation = 8f

            setOnClickListener {
                hideFloatingButton()

                val intent = Intent(
                    this@FloatingService,
                    MainActivity::class.java
                ).apply {
                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                    )
                    putExtra("start_capture", true)
                }

                startActivity(intent)
            }
        }

        floatingButton = button

        val size = (56 * resources.displayMetrics.density).toInt()

        layoutParams = WindowManager.LayoutParams(
            size,
            size,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 20
            y = 300
        }
    }

    private fun showFloatingButton() {
        if (!isButtonVisible && floatingButton != null) {
            windowManager.addView(floatingButton, layoutParams)
            isButtonVisible = true
        }
    }

    private fun hideFloatingButton() {
        if (isButtonVisible && floatingButton != null) {
            windowManager.removeView(floatingButton)
            isButtonVisible = false
        }
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        when (intent?.action) {
            ACTION_HIDE -> hideFloatingButton()
            ACTION_SHOW -> showFloatingButton()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        hideFloatingButton()
        floatingButton = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
