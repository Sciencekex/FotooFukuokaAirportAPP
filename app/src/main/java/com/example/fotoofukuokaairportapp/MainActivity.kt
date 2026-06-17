package com.example.fotoofukuokaairportapp

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File

class MainActivity : AppCompatActivity() {

    companion object {
        private const val ALBUM_FOLDER = "/BoardAlbum/"
        private const val SLIDESHOW_INTERVAL_MS = 5 * 60 * 1000L // 5分钟
        private const val FADE_DURATION_MS = 1500L // 渐变持续时间1.5秒
        private const val PERMISSION_REQUEST_CODE = 1001
    }

    private lateinit var imageAlbum: ImageView
    private lateinit var imageAlbumOverlay: ImageView
    private lateinit var textAlbumHint: TextView
    private lateinit var webViewSchedule: WebView

    private val handler = Handler(Looper.getMainLooper())
    private var slideshowRunnable: Runnable? = null
    private var imageFiles: MutableList<File> = mutableListOf()
    private var currentImageIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ---- 锁死布局尺寸（必须在 setContentView 之前） ----
        // FLAG_LAYOUT_NO_LIMITS 让布局从一开始就占满 2400×1080 物理像素，
        // 不会因状态栏/导航栏的出现或消失而产生 H=1036→H=1080 的 44px 跳变
        @Suppress("DEPRECATION")
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        setContentView(R.layout.activity_main)

        imageAlbum = findViewById(R.id.imageAlbum)
        imageAlbumOverlay = findViewById(R.id.imageAlbumOverlay)
        textAlbumHint = findViewById(R.id.textAlbumHint)
        webViewSchedule = findViewById(R.id.webViewSchedule)

        // ---- 禁用根布局及 decorView 的 Force Dark ----
        // 防止 MIUI 在视图层级上扫描时，从父容器向下传递反色策略
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.decorView.isForceDarkAllowed = false
            findViewById<View>(android.R.id.content)?.isForceDarkAllowed = false
        }

        // 全屏沉浸模式
        setupImmersiveMode()
        // 屏幕常亮
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // ---- 延迟 WebView 初始化：让第一帧布局先渲染完，减少 Davey! 卡顿 ----
        // WebView 内核加载耗时 200-400ms，放到 post 中避免阻塞 onCreate
        handler.post {
            setupWebView()
        }

        // ---- 调试：监控 WebView 布局尺寸变化 ----
        webViewSchedule.viewTreeObserver.addOnGlobalLayoutListener {
            val w = webViewSchedule.width
            val h = webViewSchedule.height
            val vis = webViewSchedule.visibility
            val location = IntArray(2)
            webViewSchedule.getLocationOnScreen(location)
            val rect = android.graphics.Rect()
            webViewSchedule.getGlobalVisibleRect(rect)
            android.util.Log.e("LAYOUT_DEBUG", """
                [WebView 布局状态变更]
                - 宽高尺寸: W=${w}px, H=${h}px
                - 屏幕坐标: X=${location[0]}, Y=${location[1]}
                - 可见状态: Visibility=$vis (0=VISIBLE, 4=INVISIBLE, 8=GONE)
                - 实际可见矩形: L=${rect.left}, T=${rect.top}, R=${rect.right}, B=${rect.bottom}
            """.trimIndent())
        }

        // 检查并申请权限，然后加载图片
        checkAndRequestPermissions()
    }

    // ==================== 全屏沉浸模式 ====================

    private fun setupImmersiveMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // Android 9+ 刘海屏适配：内容延伸到切口区域
            window.attributes = window.attributes.apply {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ 使用新 API
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            // Android 10 及以下：Sticky Immersive
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            )
        }

        // 触摸后重新进入 Sticky Immersive（兼容性保障）
        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                handler.postDelayed({ reapplyImmersive() }, 500)
            }
        }
    }

    private fun reapplyImmersive() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            )
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) reapplyImmersive()
    }

    // ==================== WebView 配置 ====================

    private fun setupWebView() {
        webViewSchedule.apply {
            setBackgroundColor(0x00000000)
            setLayerType(View.LAYER_TYPE_HARDWARE, null)

            // ---- 照搬参考项目 FukuokaAirportBoard.apk 的 WebView 配置 ----
            // 核心：setInitialScale(100) 让 WebView 以物理像素渲染，配合
            // setUseWideViewPort 实现 CSS 像素与物理像素 1:1 映射
            settings.apply {
                javaScriptEnabled = true
                allowFileAccess = true
                allowContentAccess = true
                allowFileAccessFromFileURLs = true
                allowUniversalAccessFromFileURLs = true
                domStorageEnabled = true
                databaseEnabled = true
                cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK

                // === 参考项目已验证的视口/缩放配置 ===
                setUseWideViewPort(true)
                setLoadWithOverviewMode(false)
                setSupportZoom(false)
                setBuiltInZoomControls(false)
                textZoom = 100
                layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                }
            }
            // 关键：100% 初始缩放，不做任何 DPR 补偿
            setInitialScale(100)

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    isVerticalScrollBarEnabled = false
                    isHorizontalScrollBarEnabled = false
                }

                override fun onReceivedError(
                    view: WebView?,
                    errorCode: Int,
                    description: String?,
                    failingUrl: String?
                ) {
                    loadDataWithBaseURL(
                        null,
                        "<html><body style='background:#0D1B2A;color:#8899AA;font-family:sans-serif;" +
                        "display:flex;align-items:center;justify-content:center;height:100vh;margin:0;'>" +
                        "<div style='text-align:center;padding:20px;'>" +
                        "<h3>时刻表加载失败</h3>" +
                        "<p>${description ?: "未知错误"}</p>" +
                        "<p style='font-size:12px;color:#556677;'>$failingUrl</p>" +
                        "</div></body></html>",
                        "text/html", "UTF-8", null
                    )
                }
            }

            loadUrl("file:///android_asset/fukuoka/index.html")
        }
    }

    // ==================== 权限处理 ====================

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ 使用细粒度媒体权限
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                    PERMISSION_REQUEST_CODE
                )
            } else {
                loadAlbumImages()
            }
        } else {
            // Android 12 及以下
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )
            } else {
                loadAlbumImages()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadAlbumImages()
            } else {
                Toast.makeText(this, "需要存储权限才能读取相册图片", Toast.LENGTH_LONG).show()
            }
        }
    }

    // ==================== 图片加载与轮播 ====================

    private fun loadAlbumImages() {
        val albumDir = File(
            Environment.getExternalStorageDirectory().toString() + ALBUM_FOLDER
        )

        if (!albumDir.exists()) {
            albumDir.mkdirs()
            Toast.makeText(
                this,
                "请在 ${albumDir.absolutePath} 目录下放入图片文件",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // 扫描支持的图片格式
        val supportedExtensions = listOf("jpg", "jpeg", "png", "bmp", "webp")
        imageFiles.clear()
        albumDir.listFiles()?.forEach { file ->
            if (file.isFile && supportedExtensions.contains(file.extension.lowercase())) {
                imageFiles.add(file)
            }
        }

        if (imageFiles.isEmpty()) {
            Toast.makeText(
                this,
                "相册目录为空，请放入图片文件",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // Shuffle 随机乱序
        imageFiles.shuffle()

        // 显示第一张图片
        if (imageFiles.isNotEmpty()) {
            textAlbumHint.visibility = View.GONE
            displayImage(imageFiles[0], imageAlbum)
            startSlideshow()
        }
    }

    private fun displayImage(file: File, targetView: ImageView) {
        try {
            val options = BitmapFactory.Options().apply {
                // 先解码尺寸，避免 OOM
                inJustDecodeBounds = true
                BitmapFactory.decodeFile(file.absolutePath, this)

                // 计算采样率，目标宽度约 1440px（左侧区域）
                val targetWidth = 1440
                inSampleSize = calculateInSampleSize(outWidth, outHeight, targetWidth, 1080)
                inJustDecodeBounds = false
            }

            val bitmap = BitmapFactory.decodeFile(file.absolutePath, options)
            targetView.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            // 如果图片加载失败，跳过并尝试下一张
            showNextImageWithFade()
        }
    }

    private fun calculateInSampleSize(
        rawWidth: Int, rawHeight: Int,
        reqWidth: Int, reqHeight: Int
    ): Int {
        var inSampleSize = 1
        if (rawHeight > reqHeight || rawWidth > reqWidth) {
            val halfHeight = rawHeight / 2
            val halfWidth = rawWidth / 2
            while ((halfHeight / inSampleSize) >= reqHeight &&
                (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    // ==================== 轮播控制（渐变动画） ====================

    private fun startSlideshow() {
        stopSlideshow()
        slideshowRunnable = object : Runnable {
            override fun run() {
                showNextImageWithFade()
                // 调度下一轮
                slideshowRunnable?.let {
                    handler.postDelayed(it, SLIDESHOW_INTERVAL_MS)
                }
            }
        }
        handler.postDelayed(slideshowRunnable!!, SLIDESHOW_INTERVAL_MS)
    }

    private fun stopSlideshow() {
        slideshowRunnable?.let { handler.removeCallbacks(it) }
        slideshowRunnable = null
    }

    private fun showNextImageWithFade() {
        if (imageFiles.isEmpty()) return

        currentImageIndex = (currentImageIndex + 1) % imageFiles.size

        // 如果到了列表末尾，重新 shuffle 并从头开始
        if (currentImageIndex == 0) {
            imageFiles.shuffle()
        }

        val nextFile = imageFiles[currentImageIndex]
        val overlay = imageAlbumOverlay
        val main = imageAlbum

        // 将下一张图片加载到 overlay 层
        displayImage(nextFile, overlay)

        // overlay 渐显，main 层保持不变（overlay 覆盖在上面）
        overlay.visibility = View.VISIBLE
        val fadeIn = ObjectAnimator.ofFloat(overlay, "alpha", 0f, 1f).apply {
            duration = FADE_DURATION_MS
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    // 动画结束后，将 overlay 的图片设为主层，重置 overlay
                    main.setImageDrawable(overlay.drawable)
                    overlay.visibility = View.GONE
                    overlay.alpha = 0f
                    overlay.setImageBitmap(null) // 释放 bitmap
                }
            })
        }
        fadeIn.start()
    }

    // ==================== 生命周期管理 ====================

    override fun onResume() {
        super.onResume()
        webViewSchedule.onResume()
        reapplyImmersive()
        if (imageFiles.isNotEmpty() && slideshowRunnable == null) {
            startSlideshow()
        }
    }

    override fun onPause() {
        webViewSchedule.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        stopSlideshow()
        webViewSchedule.apply {
            stopLoading()
            destroy()
        }
        super.onDestroy()
    }
}
