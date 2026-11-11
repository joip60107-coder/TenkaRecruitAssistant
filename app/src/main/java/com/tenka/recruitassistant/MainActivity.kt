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
            text = "\n五個徵才標籤組合分析"
            textSize = 17f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
        }
        root.addView(subtitle)

        val tagTitle = TextView(this).apply {
            text = "\n目前標籤"
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

        val button = Button(this).apply {
            text = "分析全部組合"
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
            text = "\n按下按鈕即可分析組合。"
            textSize = 16f
            setTextColor(Color.rgb(180, 220, 180))
            setPadding(0, 16, 0, 16)
        }
        root.addView(result)

        button.setOnClickListener {
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
}
