
package com.tenka.recruitassistant

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.media.projection.MediaProjectionManager
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private val tags = listOf(
        "火屬性",
        "群體攻擊",
        "小體型",
        "魔族",
        "防禦"
    )

    private var waitingForPermission = false
    private lateinit var status: TextView
    private lateinit var projectionManager: MediaProjectionManager
private val SCREEN_CAPTURE_REQUEST = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        projectionManager =
    getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 32, 24, 24)
            setBackgroundColor(Color.rgb(25, 28, 36))
        }

        val title = TextView(this).apply {
            text = "天下布魔｜全境徵才助手"
            textSize = 23f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }
        root.addView(title)

        val subtitle = TextView(this).apply {
            text = "\n五個徵才標籤組合分析"
            textSize = 17f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
        }
        root.addView(subtitle)

        status = TextView(this).apply {
            text = "\n懸浮按鈕尚未啟用"
            textSize = 16f
            setTextColor(Color.rgb(180, 220, 180))
            setPadding(0, 12, 0, 12)
        }
        root.addView(status)

        val floatingButton = Button(this).apply {
            text = "啟用懸浮按鈕「徵」"
            textSize = 18f
        }
        root.addView(
            floatingButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        floatingButton.setOnClickListener {
            if (Settings.canDrawOverlays(this)) {
                startFloatingService()
            } else {
                waitingForPermission = true
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            }
        }

        val tagTitle = TextView(this).apply {
            text = "\n目前測試標籤"
            textSize = 19f
            setTextColor(Color.WHITE)
        }
        root.addView(tagTitle)

        tags.forEach { tag ->
            val item = TextView(this).apply {
                text = "• $tag"
                textSize = 18f
                setTextColor(Color.WHITE)
                setPadding(8, 10, 8, 10)
            }
            root.addView(item)
        }

        val analyzeButton = Button(this).apply {
            text = "分析全部組合"
            textSize = 18f
        }
        root.addView(
            analyzeButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        val result = TextView(this).apply {
            text = "\n按下按鈕即可分析組合。"
            textSize = 16f
            setTextColor(Color.rgb(180, 220, 180))
            setPadding(0, 16, 0, 16)
        }
        root.addView(result)

        analyzeButton.setOnClickListener {
            val combinations = mutableListOf<String>()

            for (i in tags.indices) {
                combinations.add(tags[i])

                for (j in i + 1 until tags.size) {
                    combinations.add(
                        "${tags[i]} + ${tags[j]}"
                    )

                    for (k in j + 1 until tags.size) {
                        combinations.add(
                            "${tags[i]} + ${tags[j]} + ${tags[k]}"
                        )
                    }
                }
            }

            result.text = buildString {
                append("分析完成！共 ${combinations.size} 種組合\n\n")
                combinations.forEachIndexed { index, item ->
                    append("${index + 1}. $item\n")
                }
            }
        }

        val scroll = ScrollView(this)
        scroll.addView(root)
        setContentView(scroll)
    }

    override fun onResume() {
        super.onResume()

        if (waitingForPermission &&
            Settings.canDrawOverlays(this)
        ) {
            waitingForPermission = false
            startFloatingService()
        }
    }

    private fun startFloatingService() {
        val intent = Intent(this, FloatingService::class.java)
        startService(intent)

        status.text = "\n懸浮按鈕已啟動！可以返回遊戲測試。"
        Toast.makeText(
            this,
            "懸浮按鈕已啟動",
            Toast.LENGTH_SHORT
        ).show()
    }
}
