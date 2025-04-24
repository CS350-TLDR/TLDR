package com.comp350.tldr.model.services

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.graphics.PixelFormat
import android.net.Uri
import android.os.*
import android.view.*
import android.widget.*
import com.comp350.tldr.R
import com.comp350.tldr.classicstuff.Question
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.*

class PopupService : Service() {
    private val serviceIdentifier = "PopQuizService"

    private lateinit var windowManager: WindowManager
    private var floatingView: View? = null
    private var resultsView: View? = null
    private var videoView: View? = null
    private var timer: Timer? = null
    private val flashcardViews = ArrayList<View>(3)
    private var popServiceCoroutineJob: Job? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPrefs: android.content.SharedPreferences

    private var currentTopic = "Python"
    private var currentActivity = "Trivia"
    private var intervalMs: Long = 60000
    private var gears = 0
    private var correctAnswers = 0
    private var totalAnswered = 0

    private val darkBlueColor = "#1A237E"
    private val blueColor = "#2196F3"

    private val pythonQuestions = listOf(
        Question("What are variables used for?", listOf("To store data", "To print messages", "To create loops", "To define classes"), 0),
        Question("What is the correct form to name a variable with multiple words?", listOf("snake_case", "PascalCase", "camelCase", "UPPER_CASE"), 0),
        Question("Which statement correctly creates a variable x with value 5?", listOf("x = 5", "int x = 5", "var x = 5", "define x = 5"), 0),
        Question("What is the output of print(type(10))?", listOf("<class 'int'>", "<class 'str'>", "<class 'float'>", "<class 'number'>"), 0),
        Question("What is a function in Python?", listOf("A reusable block of code", "A variable type", "A loop construct", "A special character"), 0),
        Question("How do you create a list in Python?", listOf("my_list = [1, 2, 3]", "my_list = (1, 2, 3)", "my_list = {1, 2, 3}", "my_list = <1, 2, 3>"), 0),
        Question("What does the len() function do?", listOf("Returns the length of an object", "Returns the largest value", "Formats a string", "Creates a new line"), 0),
        Question("How do you add an element to a list?", listOf("my_list.append(element)", "my_list.add(element)", "my_list.insert(element)", "my_list.push(element)"), 0),
        Question("What symbol is used for comments in Python?", listOf("#", "//", "/*", "<!-->"), 0),
        Question("Which data type is immutable?", listOf("Tuple", "List", "Dictionary", "Set"), 0)
    )

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
            "SHOW_NOW" -> {
                when (currentActivity) {
                    "Trivia" -> showRandomQuiz()
                    "Video" -> showVideoPopup()
                    "Flashcards" -> displayAllFlashcards()
                }
            }
        }

        return START_NOT_STICKY
    }
//vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
//private fun startPopupServiceScheduler() {
//    // Cancel any existing job
//    popServiceCoroutineJob?.cancel()
//
//    // Create a new job
//    popServiceCoroutineJob = CoroutineScope(Dispatchers.Main).launch {
//
//
//        while (isActive) {
//
//            delay(intervalMs) //Repeat at interval
//        }
//    }
//}
//==================================
    private fun handleStart(intent: Intent) {
        currentTopic = intent.getStringExtra("topic") ?: "Python"
        currentActivity = intent.getStringExtra("activity") ?: "Trivia"
        intervalMs = intent.getLongExtra("interval", 60000)

        // Cancel any existing timers
        timer?.cancel()
        timer = Timer()

        //startPopupServiceScheduler()

        when (currentActivity) {
            "Flashcards" -> {
                removeAllOverlays()

                timer?.schedule(object : TimerTask() {
                    override fun run() {
                        Handler(Looper.getMainLooper()).post {
                            try {
                                displayAllFlashcards()
                            } catch (e: Exception) {
                            }
                        }
                    }
                }, 0)

                timer?.scheduleAtFixedRate(object : TimerTask() {
                    override fun run() {
                        Handler(Looper.getMainLooper()).post {
                            try {
                                refreshAllFlashcards()
                            } catch (e: Exception) {
                            }
                        }
                    }
                }, 10000 + intervalMs, intervalMs)
            }
            else -> {
                timer?.schedule(object : TimerTask() {
                    override fun run() {
                        Handler(Looper.getMainLooper()).post {
                            try {
                                when (currentActivity) {
                                    "Trivia" -> showRandomQuiz()
                                    "Video" -> showVideoPopup()
                                    "VocabMatch" -> {
                                    }
                                }
                            } catch (e: Exception) {
                            }
                        }
                    }
                }, 0)

                timer?.scheduleAtFixedRate(object : TimerTask() {
                    override fun run() {
                        Handler(Looper.getMainLooper()).post {
                            try {
                                when (currentActivity) {
                                    "Trivia" -> showRandomQuiz()
                                    "Video" -> showVideoPopup()
                                    "VocabMatch" -> {
                                    }
                                }
                            } catch (e: Exception) {
                            }
                        }
                    }
                }, 0 + intervalMs, intervalMs)
            }
        }
    }

    private fun displayAllFlashcards() {
        removeAllFlashcards()

        val screenWidth = resources.displayMetrics.widthPixels

        for (i in 0 until 3) {
            val question = pythonQuestions.random()
            val card = createResizableFlashCardView(question)

            val xPos = (screenWidth / 6) + (i * screenWidth / 12)
            val yPos = 100 + (i * 80)

            addFlashcardOverlay(card, xPos, yPos)
            flashcardViews.add(card)
        }
    }

    private fun refreshAllFlashcards() {
        val positions = flashcardViews.map {
            val params = it.layoutParams as WindowManager.LayoutParams
            Pair(params.x, params.y)
        }

        removeAllFlashcards()

        for (i in 0 until 3) {
            val question = pythonQuestions.random()
            val card = createResizableFlashCardView(question)

            val xPos = if (i < positions.size) positions[i].first else 100 + (i * 50)
            val yPos = if (i < positions.size) positions[i].second else 100 + (i * 80)

            addFlashcardOverlay(card, xPos, yPos)
            flashcardViews.add(card)
        }
    }

    private fun createResizableFlashCardView(question: Question): View {
        val layout = FrameLayout(this).apply {
            setBackgroundColor(AndroidColor.parseColor("#333333"))
            setPadding(2, 2, 2, 2)
        }

        val contentLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(AndroidColor.BLACK)
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val titleBar = TextView(this).apply {
            setBackgroundColor(AndroidColor.parseColor("blue"))
            setTextColor(AndroidColor.WHITE)
            gravity = Gravity.CENTER
            textSize = 12f
            setPadding(8, 4, 8, 4)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val instructionsText = TextView(this).apply {
            text = "Tap card to flip"
            setTextColor(AndroidColor.LTGRAY)
            textSize = 10f
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val cardFront = TextView(this).apply {
            text = question.text
            setTextColor(AndroidColor.WHITE)
            textSize = 18f
            setPadding(16, 16, 16, 16)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val cardBack = TextView(this).apply {
            text = "Answer: ${question.options[question.correctAnswerIndex]}"
            setTextColor(AndroidColor.YELLOW)
            textSize = 18f
            setPadding(16, 16, 16, 16)
            gravity = Gravity.CENTER
            visibility = View.GONE
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        contentLayout.addView(titleBar)
        contentLayout.addView(instructionsText)
        contentLayout.addView(cardFront)
        contentLayout.addView(cardBack)

        layout.addView(contentLayout)

        addResizeHandles(layout)

        titleBar.setOnTouchListener(createTouchListener(layout))

        val clickListener = View.OnClickListener {
            if (cardFront.visibility == View.VISIBLE) {
                cardFront.visibility = View.GONE
                cardBack.visibility = View.VISIBLE
            } else {
                cardFront.visibility = View.VISIBLE
                cardBack.visibility = View.GONE
            }
        }

        cardFront.setOnClickListener(clickListener)
        cardBack.setOnClickListener(clickListener)

        return layout
    }

    private fun addResizeHandles(layout: FrameLayout) {
        val resizeHandle = View(this).apply {
            setBackgroundColor(AndroidColor.parseColor("#4B89DC"))
            layoutParams = FrameLayout.LayoutParams(30, 30).apply {
                gravity = Gravity.BOTTOM or Gravity.END
            }
        }

        layout.addView(resizeHandle)

        resizeHandle.setOnTouchListener(createResizeListener(layout))
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

    private fun addFlashcardOverlay(view: View, xOffset: Int, yOffset: Int) {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = xOffset
            y = yOffset
        }

        try {
            windowManager.addView(view, params)
        } catch (e: Exception) {
        }
    }

    private fun removeAllFlashcards() {
        for (view in flashcardViews) {
            try {
                windowManager.removeView(view)
            } catch (e: Exception) {
            }
        }
        flashcardViews.clear()
    }

    private fun showRandomQuiz() {
        val question = pythonQuestions.randomOrNull() ?: return
        removeAllOverlays()
        floatingView = createQuizView(question)
        addOverlay(floatingView!!)
    }

    private fun createQuizView(question: Question): View {
        val layout = createBaseLayout()

        layout.addView(createStatsBar())
        layout.addView(createQuestionText(question.text))

        val radioGroup = createAnswerOptions(question.options)
        layout.addView(radioGroup)

        layout.addView(createQuizButtons(radioGroup, question))

        return layout
    }

    private fun createBaseLayout(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setBackgroundColor(AndroidColor.BLACK)
        setPadding(24, 24, 24, 24)
    }

    private fun createStatsBar(): TextView = TextView(this).apply {
        text = "Accuracy: ${calculateAccuracy()}% | Gears: $gears"
        setTextColor(AndroidColor.WHITE)
        textSize = 16f
        setPadding(0, 0, 0, 16)
    }

    private fun createQuestionText(text: String): TextView = TextView(this).apply {
        this.text = text
        setTextColor(AndroidColor.WHITE)
        textSize = 18f
        setPadding(0, 16, 0, 24)
    }

    private fun createAnswerOptions(options: List<String>): RadioGroup {
        val group = RadioGroup(this).apply {
            orientation = RadioGroup.VERTICAL
            setPadding(0, 0, 0, 24)
        }

        options.forEachIndexed { index, option ->
            val button = RadioButton(this).apply {
                id = index
                text = option
                textSize = 16f
                setTextColor(AndroidColor.WHITE)
                setPadding(0, 8, 0, 8)
            }
            group.addView(button)
        }

        return group
    }

    private fun createQuizButtons(group: RadioGroup, question: Question): LinearLayout {
        val buttonLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val submit = Button(this).apply {
            text = "Submit"
            setBackgroundColor(AndroidColor.parseColor(darkBlueColor))
            setTextColor(AndroidColor.WHITE)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginEnd = 8
            }
            setOnClickListener {
                val selectedId = group.checkedRadioButtonId
                if (selectedId != -1) {
                    totalAnswered++
                    val correct = selectedId == question.correctAnswerIndex
                    if (correct) {
                        correctAnswers++
                        gears++
                        saveGears()
                    }
                    showResult(correct, question.options[question.correctAnswerIndex])
                }
            }
        }

        val close = Button(this).apply {
            text = "Close"
            setBackgroundColor(AndroidColor.parseColor(blueColor))
            setTextColor(AndroidColor.WHITE)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = 8
            }
            setOnClickListener { removeAllOverlays() }
        }

        buttonLayout.addView(submit)
        buttonLayout.addView(close)
        return buttonLayout
    }

    private fun showResult(isCorrect: Boolean, correctAnswer: String) {
        removeFloatingView()
        resultsView = createResultView(isCorrect, correctAnswer)
        addOverlay(resultsView!!)

        Handler(Looper.getMainLooper()).postDelayed({ removeResultView() }, 5000)
    }

    private fun createResultView(isCorrect: Boolean, correctAnswer: String): View {
        val layout = createBaseLayout()

        layout.addView(createStatsBar())

        val resultText = TextView(this).apply {
            text = if (isCorrect) "Correct! (+1 Gear)" else "Wrong!"
            setTextColor(AndroidColor.parseColor(if (isCorrect) "#4CAF50" else "#F44336"))
            textSize = 24f
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 24)
        }

        val answerText = TextView(this).apply {
            text = "The correct answer is: $correctAnswer"
            setTextColor(AndroidColor.WHITE)
            textSize = 18f
            setPadding(0, 8, 0, 32)
        }

        val continueButton = Button(this).apply {
            text = "Continue"
            setBackgroundColor(AndroidColor.parseColor(blueColor))
            setTextColor(AndroidColor.WHITE)
            setOnClickListener { removeResultView() }
        }

        layout.apply {
            addView(resultText)
            addView(answerText)
            addView(continueButton)
        }

        return layout
    }

    private var videoViewComponent: VideoView? = null
    private fun createVideoLayout(): View {
        val layout = FrameLayout(this).apply {
            setBackgroundColor(AndroidColor.parseColor("#333333"))
            setPadding(4, 4, 4, 4)
        }

        val contentLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(AndroidColor.BLACK)
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        val topBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setBackgroundColor(AndroidColor.parseColor("#444444"))
        }

        val title = TextView(this).apply {
            setTextColor(AndroidColor.WHITE)
            textSize = 32f
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setPadding(24, 16, 16, 16)
        }

        val close = Button(this).apply {
            text = "X"
            textSize = 24f
            minWidth = 60
            minHeight = 60
            setTextColor(AndroidColor.WHITE)
            setBackgroundColor(AndroidColor.parseColor(blueColor))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener { removeAllOverlays() }
        }

        topBar.addView(title)
        topBar.addView(close)
        contentLayout.addView(topBar)

        val video = VideoView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0
            ).apply {
                weight = 1f
                topMargin = 16
            }
        }

        videoViewComponent = video

        try {
            val videoResIds = listOf(R.raw.pythonbasics, R.raw.oop_vs_functional, R.raw. drake, R.raw.joeRogan)
            val randomVideoResId = videoResIds.random()
            val videoUri = Uri.parse("android.resource://$packageName/$randomVideoResId")

            video.setVideoURI(videoUri)
            video.setMediaController(MediaController(this).apply {
                setAnchorView(video)
            })

            video.setOnCompletionListener {
                gears++
                saveGears()
                removeAllOverlays()
            }

            contentLayout.addView(video)
        } catch (e: Exception) {
        }

        layout.addView(contentLayout)

        val resizeHandle = View(this).apply {
            setBackgroundColor(AndroidColor.parseColor("#4B89DC"))
            layoutParams = FrameLayout.LayoutParams(60, 60).apply {
                gravity = Gravity.BOTTOM or Gravity.END
            }
        }

        layout.addView(resizeHandle)

        topBar.setOnTouchListener(createTouchListener(layout))
        resizeHandle.setOnTouchListener(createResizeListener(layout))

        return layout
    }

    private fun showVideoPopup() {
        removeAllOverlays()
        videoView = createVideoLayout()

        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        val params = WindowManager.LayoutParams(
            screenWidth / 2,
            screenHeight / 2,
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
            } catch (e: Exception) {
            }
        }, 500)
    }

    private fun addOverlay(view: View) {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }
        windowManager.addView(view, params)
    }

    private fun calculateAccuracy(): Int = if (totalAnswered == 0) 0 else (correctAnswers * 100) / totalAnswered

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

    private fun removeFloatingView() {
        floatingView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
            }
        }
        floatingView = null
    }

    private fun removeResultView() {
        resultsView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
            }
        }
        resultsView = null
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

    private fun removeAllOverlays() {
        removeFloatingView()
        removeResultView()
        removeVideoView()
        removeAllFlashcards()
    }

    override fun onDestroy() {
        timer?.cancel()
        removeAllOverlays()
        super.onDestroy()
    }

    private fun <T> List<T>.randomOrNull(): T? {
        return if (isEmpty()) null else random()
    }
}
