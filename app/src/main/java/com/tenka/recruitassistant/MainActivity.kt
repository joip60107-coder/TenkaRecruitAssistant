
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
    "火屬性",
    "水屬性",
    "風屬性",
    "光屬性",
    "闇屬性",
    "攻擊者",
    "守護者",
    "治療者",
    "妨礙者",
    "輔助者",
    "人類",
    "魔族",
    "亞人",
    "小體型",
    "中體型",
    "貧乳",
    "美乳",
    "巨乳",
    "士兵",
    "菁英",
    "領袖",
    "輸出",
    "保護",
    "防禦",
    "回復",
    "干擾",
    "支援",
    "削弱",
    "爆發力",
    "生存力",
    "越戰越強",
    "群體攻擊",
    "回擊"
)
private var detectedTags: List<String> = emptyList()
    private var waitingForPermission = false
    private var isPickingImage = false
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
    val currentTags = detectedTags

    if (currentTags.isEmpty()) {
        result.text = "請先選擇截圖並辨識標籤。"
        return@setOnClickListener
    }

    for (i in currentTags.indices) {
        combinations.add(currentTags[i])

        for (j in i + 1 until currentTags.size) {
            combinations.add(
                "${currentTags[i]} + ${currentTags[j]}"
            )

            for (k in j + 1 until currentTags.size) {
                combinations.add(
                    "${currentTags[i]} + ${currentTags[j]} + ${currentTags[k]}"
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
    val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
        type = "image/*"
    }
    startActivityForResult(intent, 2001)
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
                val normalizedText = recognizedText.replace(
    "\\s+".toRegex(), ""
)

val foundTags = tags.filter { tag ->
    normalizedText.contains(
        tag.replace("\\s+".toRegex(), "")
    )
}
detectedTags = foundTags
status.text = if (foundTags.isNotEmpty()) {
    "辨識到的標籤：\n" +
    foundTags.joinToString("\n") { "・$it" }
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

        if (waitingForPermission &&
            Settings.canDrawOverlays(this)
        ) {
            waitingForPermission = false
            startFloatingService()
        }
    }
override fun onStop() {
    super.onStop()

    if (!isPickingImage) {
        val showIntent = Intent(this, FloatingService::class.java).apply {
            action = FloatingService.ACTION_SHOW
        }
        startService(showIntent)
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
