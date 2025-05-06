package com.comp350.tldr.model.services

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.*
import android.view.*
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import com.comp350.tldr.R
import com.google.firebase.auth.FirebaseAuth
import java.util.*

class VideoService : Service() {
    private val serviceIdentifier = "VideoService"
    private lateinit var windowManager: WindowManager
    private var videoView: View? = null
    private var timer: Timer? = null
    private var videoViewComponent: VideoView? = null
    private var mediaController: MediaController? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPrefs: android.content.SharedPreferences

    private var currentTopic = "Python"
    private var intervalMs: Long = 60000
    private var gears = 0
    private val blueColor = "#2196F3"

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        auth = FirebaseAuth.getInstance()
        sharedPrefs = getSharedPreferences("tldr_prefs", Context.MODE_PRIVATE)
        loadGears()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent ?: return START_NOT_STICKY

        when (intent.action) {
            "START_SERVICE" -> handleStart(intent)
            "STOP_SERVICE" -> stopSelf()
            "SHOW_NOW" -> showVideoPopup()
        }

        return START_NOT_STICKY
    }

    private fun handleStart(intent: Intent) {
        intervalMs = intent.getLongExtra("interval", 60000)
        currentTopic = intent.getStringExtra("topic") ?: "Python"

        timer?.cancel()
        timer = Timer()

        timer?.schedule(object : TimerTask() {
            override fun run() {
                Handler(Looper.getMainLooper()).post {
                    try {
                        showVideoPopup()
                    } catch (e: Exception) {
                    }
                }
            }
        }, 0)

        timer?.schedule(object : TimerTask() {
            override fun run() {
                Handler(Looper.getMainLooper()).post {
                    try {
                        showVideoPopup()
                    } catch (e: Exception) {
                    }
                }
            }
        }, intervalMs, intervalMs)
    }

    private fun getVideoResources(): List<Int> {
        return when (currentTopic) {
            "Clean Code" -> listOf(R.raw.cc1, R.raw.cc2, R.raw.cc3, R.raw.cc4, R.raw.cc5, R.raw.cc6, R.raw.cc7, R.raw.cc8, R.raw.cc9)
            else -> listOf(R.raw.pythonbasics, R.raw.oop_vs_functional, R.raw.drake, R.raw.joerogan)
        }
    }

    private fun createRoundedBackground(colorHex: String): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 32f
            setColor(Color.parseColor(colorHex))
        }
    }

    private fun createVideoLayout(): View {
        val context = this

        val outerLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#4B89DC"))
            outlineProvider = ViewOutlineProvider.BACKGROUND
            clipToOutline = true
            setPadding(48, 32, 48, 32)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val topicText = TextView(context).apply {
            text = currentTopic
            setTextColor(Color.WHITE)
            textSize = 20f
            typeface = ResourcesCompat.getFont(context, R.font.rainyhearts)
            setShadowLayer(4f, 2f, 2f, Color.BLACK)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 16
            }
        }

        videoViewComponent = VideoView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        mediaController = MediaController(context)
        videoViewComponent?.setMediaController(mediaController)

        val closeButton = TextView(context).apply {
            text = "X"
            textSize = 20f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.TRANSPARENT)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.END
                topMargin = 16
            }
            setOnClickListener { removeVideoView() }
        }

        outerLayout.addView(topicText)
        outerLayout.addView(videoViewComponent)
        outerLayout.addView(closeButton)

        try {
            val videoResIds = getVideoResources()
            val randomVideoResId = videoResIds.random()
            val videoUri = Uri.parse("android.resource://$packageName/$randomVideoResId")
            videoViewComponent?.setVideoURI(videoUri)

            videoViewComponent?.setOnPreparedListener {
                videoViewComponent?.viewTreeObserver?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        videoViewComponent?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
                        mediaController?.setAnchorView(videoViewComponent)
                        mediaController?.show(0)
                    }
                })
                videoViewComponent?.start()
            }

            videoViewComponent?.setOnCompletionListener {
                gears++
                saveGears()
                removeVideoView()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        outerLayout.setOnTouchListener(createTouchListener(outerLayout))
        return outerLayout
    }



    @SuppressLint("ClickableViewAccessibility")
    private fun createResizeListener(view: View): View.OnTouchListener {
        return object : View.OnTouchListener {
            private var initialWidth: Int = 0
            private var initialHeight: Int = 0
            private var initialTouchX: Float = 0f
            private var initialTouchY: Float = 0f
            private var aspectRatio: Float = 16f / 9f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        val params = view.layoutParams as WindowManager.LayoutParams

                        if (params.width == WindowManager.LayoutParams.WRAP_CONTENT) {
                            view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
                            initialWidth = view.measuredWidth
                            initialHeight = view.measuredHeight

                            params.width = initialWidth
                            params.height = initialHeight
                            windowManager.updateViewLayout(view, params)
                        } else {
                            initialWidth = params.width
                            initialHeight = params.height
                        }

                        aspectRatio = initialWidth.toFloat() / initialHeight.toFloat()

                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        try {
                            val params = view.layoutParams as WindowManager.LayoutParams

                            val deltaX = event.rawX - initialTouchX

                            val newWidth = Math.max(400, initialWidth + deltaX.toInt())
                            val newHeight = (newWidth / aspectRatio).toInt()

                            params.width = newWidth
                            params.height = Math.max(300, newHeight)

                            windowManager.updateViewLayout(view, params)
                            return true
                        } catch (e: Exception) {
                            return false
                        }
                    }
                }
                return false
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createTouchListener(view: View): View.OnTouchListener {
        return object : View.OnTouchListener {
            private var initialX: Int = 0
            private var initialY: Int = 0
            private var initialTouchX: Float = 0f
            private var initialTouchY: Float = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        try {
                            val params = view.layoutParams as WindowManager.LayoutParams
                            initialX = params.x
                            initialY = params.y
                            initialTouchX = event.rawX
                            initialTouchY = event.rawY
                            return true
                        } catch (e: Exception) {
                            return false
                        }
                    }
                    MotionEvent.ACTION_MOVE -> {
                        try {
                            val params = view.layoutParams as WindowManager.LayoutParams
                            params.x = initialX + (event.rawX - initialTouchX).toInt()
                            params.y = initialY + (event.rawY - initialTouchY).toInt()
                            windowManager.updateViewLayout(view, params)
                            return true
                        } catch (e: Exception) {
                            return false
                        }
                    }
                }
                return false
            }
        }
    }

    private fun showVideoPopup() {
        removeVideoView()
        videoView = createVideoLayout()

        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 100
        }


        windowManager.addView(videoView, params)

        Handler(Looper.getMainLooper()).postDelayed({
            try {
                videoViewComponent?.start()
                mediaController?.show(0) // ✅ show it manually
            } catch (e: Exception) {
            }
        }, 500)


    }

    private fun loadGears() {
        val userId = auth.currentUser?.uid
        val prefs = if (userId != null) getSharedPreferences("user_${userId}_prefs", Context.MODE_PRIVATE) else sharedPrefs
        gears = prefs.getInt("gears", 0)
    }

    private fun saveGears() {
        val userId = auth.currentUser?.uid
        val prefs = if (userId != null) getSharedPreferences("user_${userId}_prefs", Context.MODE_PRIVATE) else sharedPrefs
        prefs.edit().putInt("gears", gears).apply()
    }

    private fun removeVideoView() {
        videoView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
            }
        }
        videoView = null
    }

    override fun onDestroy() {
        timer?.cancel()
        removeVideoView()
        super.onDestroy()
    }
}