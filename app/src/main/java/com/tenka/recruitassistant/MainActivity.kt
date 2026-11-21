
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
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions

class MainActivity : Activity() {

    private val textRecognizer by lazy {
        TextRecognition.getClient(
            ChineseTextRecognizerOptions.Builder().build()
        )
    }

    private val tags = listOf(
        "火屬性", "水屬性", "風屬性", "光屬性", "闇屬性",
        "攻擊者", "守護者", "治療者", "妨礙者", "輔助者",
        "人類", "魔族", "亞人", "小體型", "中體型",
        "貧乳", "美乳", "巨乳", "士兵", "菁英", "領袖",
        "輸出", "保護", "防禦", "回復", "干擾", "支援",
        "削弱", "爆發力", "生存力", "越戰越強",
        "群體攻擊", "回擊"
    )

    private var detectedTags: List<String> = emptyList()
    private var waitingForPermission = false
    private var isPickingImage = false

    private lateinit var status: TextView
    private lateinit var projectionManager: MediaProjectionManager

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
            text = "\n徵才標籤組合與角色分析"
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
                val permissionIntent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(permissionIntent)
            }
        }

        val hourTitle = TextView(this).apply {
            text = "\n選擇徵才時間"
            textSize = 18f
            setTextColor(Color.WHITE)
        }
        root.addView(hourTitle)

        val hourSpinner = Spinner(this)
        val hourAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listOf("3 小時", "9 小時")
        )
        hourAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )
        hourSpinner.adapter = hourAdapter
        root.addView(hourSpinner)

        val tagTitle = TextView(this).apply {
            text = "\nOCR 辨識結果"
            textSize = 19f
            setTextColor(Color.WHITE)
        }
        root.addView(tagTitle)

        val tagResult = TextView(this).apply {
            text = "尚未辨識標籤，請先選擇遊戲截圖。"
            textSize = 17f
            setTextColor(Color.WHITE)
            setPadding(8, 12, 8, 12)
        }
        root.addView(tagResult)

        val analyzeButton = Button(this).apply {
            text = "分析角色與標籤組合"
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
            text = "\n等待分析..."
            textSize = 16f
            setTextColor(Color.rgb(180, 220, 180))
            setPadding(0, 16, 0, 16)
        }
        root.addView(result)

        analyzeButton.setOnClickListener {

            val currentTags = detectedTags.take(5)

            if (currentTags.isEmpty()) {
                result.text = "請先選擇截圖並辨識標籤。"
                return@setOnClickListener
            }

            val selectedHour =
                if (hourSpinner.selectedItemPosition == 0) 3 else 9

            val combinations = mutableListOf<List<String>>()

            for (i in currentTags.indices) {
                combinations.add(listOf(currentTags[i]))

                for (j in i + 1 until currentTags.size) {
                    combinations.add(
                        listOf(currentTags[i], currentTags[j])
                    )

                    for (k in j + 1 until currentTags.size) {
                        combinations.add(
                            listOf(
                                currentTags[i],
                                currentTags[j],
                                currentTags[k]
                            )
                        )
                    }
                }
            }

            try {
                result.text = buildString {

                    append("招募時間：${selectedHour} 小時\n")
                    append("辨識標籤：${currentTags.size} 個\n")
                    append("組合數量：${combinations.size} 種\n")
                    append("====================\n\n")

                    combinations.forEachIndexed { index, combo ->

                        val matched =
                            RecruitmentDatabase.findMatching(
                                this@MainActivity,
                                combo,
                                selectedHour
                            )

                        append("${index + 1}. ")
                        append(combo.joinToString(" + "))
                        append("\n")

                        if (matched.isEmpty()) {
                            append("   沒有符合角色\n")
                        } else {
                            append("   符合角色 ${matched.size} 名：\n")

                            matched.forEach { character ->
                                val rarityName = when (character.rarity) {
                                    3 -> "SSR"
                                    2 -> "SR"
                                    1 -> "R"
                                    else -> "N"
                                }

                                append(
                                    "   ・${character.name}（$rarityName）\n"
                                )
                            }
                        }

                        append("\n")
                    }
                }

            } catch (e: Exception) {
                result.text =
                    "讀取角色資料失敗：${e.message}\n請確認 characters.json 是否存在。"
            }
        }

        val scroll = ScrollView(this)
        scroll.addView(root)
        setContentView(scroll)

        if (intent.getBooleanExtra("start_capture", false)) {
            intent.removeExtra("start_capture")
            openScreenshotPicker()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        if (intent.getBooleanExtra("start_capture", false)) {
            intent.removeExtra("start_capture")
            openScreenshotPicker()
        }
    }

    private var latestScreenshotUri: Uri? = null

    private fun openScreenshotPicker() {
        isPickingImage = true

        val pickerIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }

        startActivityForResult(pickerIntent, 2001)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 2001) {
            isPickingImage = false
        }

        if (requestCode == 2001 && resultCode == RESULT_OK) {

            latestScreenshotUri = data?.data

            if (latestScreenshotUri != null) {

                status.text = "正在辨識截圖..."

                try {
                    val image = InputImage.fromFilePath(
                        this,
                        latestScreenshotUri!!
                    )

                    textRecognizer.process(image)
                        .addOnSuccessListener { visionText ->

                            val recognizedText = visionText.text

                            val normalizedText =
                                recognizedText.replace(
                                    "\\s+".toRegex(),
                                    ""
                                )

                            val foundTags = tags.filter { tag ->
                                normalizedText.contains(
                                    tag.replace(
                                        "\\s+".toRegex(),
                                        ""
                                    )
                                )
                            }.take(5)

                            detectedTags = foundTags

                            status.text = if (foundTags.isNotEmpty()) {
                                "辨識到 ${foundTags.size} 個標籤"
                            } else {
                                "沒有辨識到標籤。\n\nOCR 原始文字：\n$recognizedText"
                            }
                        }
                        .addOnFailureListener {
                            status.text = "辨識失敗，請重新選取截圖。"
                        }

                } catch (e: Exception) {
                    status.text = "讀取截圖失敗：${e.message}"
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (
            waitingForPermission &&
            Settings.canDrawOverlays(this)
        ) {
            waitingForPermission = false
            startFloatingService()
        }
    }

    override fun onStop() {
        super.onStop()

        if (!isPickingImage) {
            val showIntent = Intent(
                this,
                FloatingService::class.java
            ).apply {
                action = FloatingService.ACTION_SHOW
            }

            startService(showIntent)
        }
    }

    private fun startFloatingService() {
        val serviceIntent = Intent(
            this,
            FloatingService::class.java
        )

        startService(serviceIntent)

        status.text = "\n懸浮按鈕已啟動！可以返回遊戲測試。"

        Toast.makeText(
            this,
            "懸浮按鈕已啟動",
            Toast.LENGTH_SHORT
        ).show()
    }
}
