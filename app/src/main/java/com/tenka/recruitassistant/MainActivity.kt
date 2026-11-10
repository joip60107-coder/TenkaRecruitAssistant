
package com.tenka.recruitassistant

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {

    private val tags = listOf(
        "火屬性",
        "群體攻擊",
        "小體型",
        "魔族",
        "防禦"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
            text = "\n辨識遊戲中的五個徵才標籤，分析最佳組合。"
            textSize = 16f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
        }
        root.addView(subtitle)

        val tagTitle = TextView(this).apply {
            text = "\n測試標籤"
            textSize = 19f
            setTextColor(Color.WHITE)
        }
        root.addView(tagTitle)

        tags.forEach { tag ->
            val item = TextView(this).apply {
                text = "　• $tag"
                textSize = 18f
                setTextColor(Color.WHITE)
                setPadding(8, 12, 8, 12)
            }
            root.addView(item)
        }

        val button = Button(this).apply {
            text = "開始分析（測試）"
            textSize = 18f
        }
        root.addView(
            button,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        val result = TextView(this).apply {
            text = "\n目前是主畫面測試版。\n後續會加入畫面截取、中文 OCR，以及 25 種標籤組合分析。"
            textSize = 16f
            setTextColor(Color.rgb(180, 220, 180))
        }
        root.addView(result)

        button.setOnClickListener {
            result.text = "\n測試成功！\n目前讀取到 ${tags.size} 個測試標籤。\n下一階段將加入組合分析。"
        }

        val scroll = ScrollView(this)
        scroll.addView(root)
        setContentView(scroll)
    }
}
