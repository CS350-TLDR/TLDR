package com.comp350.tldr.model.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.MediaController
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import com.comp350.tldr.R
import com.comp350.tldr.classicstuff.Question
import com.google.firebase.auth.FirebaseAuth
import java.util.Timer
import java.util.TimerTask
import kotlin.random.Random

class PopQuizService : Service() {
    private val TAG = "PopQuizService"

    // Window manager for drawing over other apps
    private lateinit var windowManager: WindowManager

    // Root views for our floating UI
    private var floatingView: View? = null
    private var resultsView: View? = null
    private var videoView: View? = null

    // Timer for scheduling popups
    private var timer: Timer? = null

    // Popup data
    private var currentTopic = "Python"
    private var currentActivity = "Trivia"
    private var intervalMs: Long = 60000 // Default 1 minute
    private var gears = 0  // Currency (now called Gears) earned through correct answers

    // Button colors
    private val darkBlueColor = "#1A237E" // Submit button
    private val blueColor = "#2196F3" // Close button color

    // Firebase Auth for user account
    private lateinit var auth: FirebaseAuth

    // Shared preferences for persistent storage
    private lateinit var sharedPrefs: android.content.SharedPreferences

    // Questions repository based on topic
    private val pythonQuestions = listOf(
        Question(
            "What are variables used for?",
            listOf("To store data", "To print messages", "To create loops", "To define classes"),
            0
        ),
        Question(
            "What is the correct form to name a variable with multiple words?",
            listOf("snake_case", "PascalCase", "camelCase", "UPPER_CASE"),
            0
        ),
        Question(
            "What keyword is used to define a function in Python?",
            listOf("function", "def", "define", "func"),
            1
        ),
        Question(
            "How do you print text in Python?",
            listOf(
                "echo(\"Hello\")",
                "console.log(\"Hello\")",
                "System.out.println(\"Hello\")",
                "print(\"Hello\")"
            ),
            3
        ),
        Question(
            "What is the correct way to start a comment in Python?",
            listOf(
                "// This is a comment",
                "<!-- This is a comment -->",
                "# This is a comment",
                "/* This is a comment */"
            ),
            2
        ),
        Question(
            "What data type would you use to store a whole number in Python?",
            listOf("float", "str", "int", "bool"),
            2
        ),
        Question(
            "How do you create a list in Python?",
            listOf(
                "my_list = (1, 2, 3)",
                "my_list = [1, 2, 3]",
                "my_list = {1, 2, 3}",
                "my_list = <1, 2, 3>"
            ),
            1
        ),
        Question(
            "Which of these is NOT a Python data type?",
            listOf("list", "dictionary", "array", "tuple"),
            2
        ),
        Question(
            "How do you check the length of a list in Python?",
            listOf(
                "list.size()",
                "size(list)",
                "len(list)",
                "list.length"
            ),
            2
        ),
        Question(
            "What will print(type(42)) display in Python?",
            listOf(
                "<class 'int'>",
                "<class 'string'>",
                "<class 'float'>",
                "<class 'number'>"
            ),
            0
        )
    )

    // Stats tracking
    private var correctAnswers = 0
    private var totalAnswered = 0

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "PopQuizService created")

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize shared preferences
        sharedPrefs = getSharedPreferences("tldr_prefs", Context.MODE_PRIVATE)

        // Load gears from Firebase (if logged in) or from shared preferences
        loadGears()

        // Initialize window manager
        try {
            windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            Log.d(TAG, "Window manager initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get window manager", e)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand called: ${intent?.action}")

        if (intent == null) {
            return START_NOT_STICKY
        }

        try {
            when (intent.action) {
                "START_SERVICE" -> {
                    // Get parameters
                    currentTopic = intent.getStringExtra("topic") ?: "Python"
                    currentActivity = intent.getStringExtra("activity") ?: "Trivia"
                    intervalMs = intent.getLongExtra("interval", 60000)
                    val testMode = intent.getBooleanExtra("test_mode", false)

                    // Start scheduling popups
                    startScheduler()

                    // If test mode, show popup immediately
                    if (testMode) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            if (currentActivity == "Trivia") {
                                showRandomQuiz()
                            } else if (currentActivity == "Video") {
                                showVideoPopup()
                            }
                        }, 1000)
                    }
                }
                "STOP_SERVICE" -> {
                    stopSelf()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onStartCommand", e)
        }

        return START_STICKY
    }

    private fun startScheduler() {
        // Cancel any existing timer
        timer?.cancel()

        // Create a new timer
        timer = Timer()

        // Schedule popups at the specified interval
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                Handler(Looper.getMainLooper()).post {
                    try {
                        if (currentActivity == "Trivia") {
                            showRandomQuiz()
                        } else if (currentActivity == "Video") {
                            showVideoPopup()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in timer task", e)
                    }
                }
            }
        }, intervalMs, intervalMs)
    }

    private fun showRandomQuiz() {
        // Get questions for the current topic
        val questions = pythonQuestions

        if (questions.isEmpty()) {
            return
        }

        // Select a random question
        val questionIndex = Random.nextInt(questions.size)
        val question = questions[questionIndex]

        // Create custom quiz view
        createCustomQuizView(question)
    }

    private fun createCustomQuizView(question: Question) {
        // Remove any existing views
        removeAllOverlays()

        try {
            // Create custom view
            val customLayout = LinearLayout(this)
            customLayout.orientation = LinearLayout.VERTICAL
            customLayout.setBackgroundColor(Color.BLACK) // Change to BLACK background
            customLayout.setPadding(24, 24, 24, 24)

            // Add stats bar at the top
            val statsText = TextView(this)
            statsText.text = "Accuracy: ${calculateAccuracy()}% | Gears: $gears"
            statsText.setTextColor(Color.WHITE)
            statsText.textSize = 16f
            statsText.setPadding(0, 0, 0, 16)
            customLayout.addView(statsText)

            // Add question text
            val questionText = TextView(this)
            questionText.text = question.text
            questionText.setTextColor(Color.WHITE)
            questionText.textSize = 18f
            questionText.setPadding(0, 16, 0, 24)
            customLayout.addView(questionText)

            // Create radio group
            val radioGroup = RadioGroup(this)
            radioGroup.orientation = RadioGroup.VERTICAL
            radioGroup.setPadding(0, 0, 0, 24)

            // Add radio options
            question.options.forEachIndexed { index, option ->
                val radioButton = RadioButton(this)
                radioButton.id = index
                radioButton.text = option
                radioButton.textSize = 16f
                radioButton.setTextColor(Color.WHITE)
                radioButton.setPadding(0, 8, 0, 8)
                radioGroup.addView(radioButton)
            }
            customLayout.addView(radioGroup)

            // Button layout
            val buttonLayout = LinearLayout(this)
            buttonLayout.orientation = LinearLayout.HORIZONTAL

            // Submit button - Dark Blue
            val submitButton = Button(this)
            submitButton.text = "Submit"
            submitButton.setBackgroundColor(Color.parseColor(darkBlueColor))
            submitButton.setTextColor(Color.WHITE)
            val submitParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            submitParams.marginEnd = 8
            submitButton.layoutParams = submitParams

            // Set up submission logic
            submitButton.setOnClickListener {
                val selectedId = radioGroup.checkedRadioButtonId
                if (selectedId != -1) {
                    val isCorrect = selectedId == question.correctAnswerIndex
                    totalAnswered++

                    if (isCorrect) {
                        correctAnswers++
                        // Add gears for correct answer
                        gears++
                        saveGears()
                    }

                    createCustomResultView(isCorrect, question.options[question.correctAnswerIndex])
                } else {
                    Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show()
                }
            }

            // Close button - Blue
            val closeButton = Button(this)
            closeButton.text = "Close"
            closeButton.setBackgroundColor(Color.parseColor(blueColor))
            closeButton.setTextColor(Color.WHITE)
            val closeParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            closeParams.marginStart = 8
            closeButton.layoutParams = closeParams

            closeButton.setOnClickListener {
                removeAllOverlays()
            }

            buttonLayout.addView(submitButton)
            buttonLayout.addView(closeButton)
            customLayout.addView(buttonLayout)

            // Add to window
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )

            params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            params.y = 100

            floatingView = customLayout
            windowManager.addView(floatingView, params)

        } catch (e: Exception) {
            Log.e(TAG, "Error creating custom quiz view", e)
        }
    }

    private fun createCustomResultView(isCorrect: Boolean, correctAnswer: String) {
        // Remove quiz view
        removeFloatingView()

        try {
            // Create custom result view
            val resultLayout = LinearLayout(this)
            resultLayout.orientation = LinearLayout.VERTICAL
            resultLayout.setBackgroundColor(Color.BLACK) // Change to BLACK background
            resultLayout.setPadding(24, 24, 24, 24)

            // Stats text at the top
            val statsText = TextView(this)
            statsText.text = "Accuracy: ${calculateAccuracy()}% | Gears: $gears"
            statsText.setTextColor(Color.WHITE)
            statsText.textSize = 16f
            statsText.setPadding(0, 0, 0, 16)
            resultLayout.addView(statsText)

            // Result text
            val resultText = TextView(this)
            if (isCorrect) {
                resultText.text = "Correct! (+1 Gear)"
                resultText.setTextColor(Color.parseColor("#4CAF50"))
            } else {
                resultText.text = "Wrong!"
                resultText.setTextColor(Color.parseColor("#F44336"))
            }
            resultText.textSize = 24f
            resultText.gravity = Gravity.CENTER
            resultText.setPadding(0, 16, 0, 24)
            resultLayout.addView(resultText)

            // Correct answer text
            val answerText = TextView(this)
            answerText.text = "The correct answer is: $correctAnswer"
            answerText.setTextColor(Color.WHITE)
            answerText.textSize = 18f
            answerText.setPadding(0, 8, 0, 32)
            resultLayout.addView(answerText)

            // Continue button - Blue
            val continueButton = Button(this)
            continueButton.text = "Continue"
            continueButton.setBackgroundColor(Color.parseColor(blueColor))
            continueButton.setTextColor(Color.WHITE)

            continueButton.setOnClickListener {
                removeResultView()
            }

            resultLayout.addView(continueButton)

            // Set up window parameters
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )

            params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            params.y = 100

            // Add to window manager
            resultsView = resultLayout
            windowManager.addView(resultsView, params)

            // Auto-dismiss after 5 seconds
            Handler(Looper.getMainLooper()).postDelayed({
                removeResultView()
            }, 5000)

        } catch (e: Exception) {
            Log.e(TAG, "Error showing custom result", e)
        }
    }

    private fun showVideoPopup() {
        // Remove any existing views
        removeAllOverlays()

        try {
            // Create a custom video layout
            val videoLayout = LinearLayout(this)
            videoLayout.orientation = LinearLayout.VERTICAL
            videoLayout.setBackgroundColor(Color.BLACK)
            videoLayout.setPadding(16, 16, 16, 16)

            // Top bar with title and close button
            val topBar = LinearLayout(this)
            topBar.orientation = LinearLayout.HORIZONTAL

            // Title
            val titleText = TextView(this)
            titleText.text = "Python Video"
            titleText.setTextColor(Color.WHITE)
            titleText.textSize = 18f
            val titleParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            titleText.layoutParams = titleParams
            topBar.addView(titleText)

            // Close button - Blue
            val closeButton = Button(this)
            closeButton.text = "X"
            closeButton.setTextColor(Color.WHITE)
            closeButton.setBackgroundColor(Color.parseColor(blueColor))
            val buttonParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            buttonParams.width = 60
            buttonParams.height = 40
            closeButton.layoutParams = buttonParams

            closeButton.setOnClickListener {
                removeAllOverlays()
            }
            topBar.addView(closeButton)

            videoLayout.addView(topBar)

            // Video view - doubled size from original
            val videoViewComponent = VideoView(this)
            val videoParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            // Doubled size for video
            videoParams.width = 640 // doubled from 320
            videoParams.height = 480 // doubled from 240
            videoParams.topMargin = 8
            videoViewComponent.layoutParams = videoParams

            // Get video URI
            try {
                val videoUri = Uri.parse("android.resource://$packageName/${R.raw.pythonbasics}")
                videoViewComponent.setVideoURI(videoUri)

                // Create and attach media controller
                val mediaController = MediaController(this)
                mediaController.setAnchorView(videoViewComponent)
                videoViewComponent.setMediaController(mediaController)

                // Set completion listener
                videoViewComponent.setOnCompletionListener {
                    // Add gears for watching the full video
                    gears++
                    saveGears()
                    Toast.makeText(this, "Video completed! (+1 Gear)", Toast.LENGTH_SHORT).show()
                    removeAllOverlays()
                }

                // Set error listener
                videoViewComponent.setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "Video error: what=$what, extra=$extra")
                    Toast.makeText(this, "Error playing video", Toast.LENGTH_SHORT).show()
                    true // Return true to indicate the error was handled
                }

                videoLayout.addView(videoViewComponent)

                // Set up window parameters - Top Left position
                val params = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
                )

                params.gravity = Gravity.TOP or Gravity.START // Top left corner
                params.x = 50 // Small margin from left edge
                params.y = 50 // Small margin from top edge

                // Add to window manager
                videoView = videoLayout
                windowManager.addView(videoView, params)

                // Start playing after a short delay
                Handler(Looper.getMainLooper()).postDelayed({
                    try {
                        videoViewComponent.start()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error starting video", e)
                    }
                }, 500)

            } catch (e: Exception) {
                Log.e(TAG, "Error setting up video URI", e)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error showing video popup", e)
        }
    }

    private fun loadGears() {
        // First check if user is logged in
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // User is logged in, get user-specific gears
            val userId = currentUser.uid
            val userPrefs = getSharedPreferences("user_${userId}_prefs", Context.MODE_PRIVATE)
            gears = userPrefs.getInt("gears", 0)
        } else {
            // User not logged in, use device-wide prefs
            gears = sharedPrefs.getInt("gears", 0)
        }
    }

    private fun saveGears() {
        // First check if user is logged in
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // User is logged in, save to user-specific prefs
            val userId = currentUser.uid
            val userPrefs = getSharedPreferences("user_${userId}_prefs", Context.MODE_PRIVATE)
            userPrefs.edit().putInt("gears", gears).apply()
        } else {
            // User not logged in, save to device-wide prefs
            sharedPrefs.edit().putInt("gears", gears).apply()
        }
    }

    private fun calculateAccuracy(): Int {
        return if (totalAnswered > 0) {
            (correctAnswers.toFloat() / totalAnswered * 100).toInt()
        } else {
            0
        }
    }

    private fun removeFloatingView() {
        if (floatingView != null) {
            try {
                windowManager.removeView(floatingView)
            } catch (e: Exception) {
                Log.e(TAG, "Error removing floating view", e)
            }
            floatingView = null
        }
    }

    private fun removeResultView() {
        if (resultsView != null) {
            try {
                windowManager.removeView(resultsView)
            } catch (e: Exception) {
                Log.e(TAG, "Error removing result view", e)
            }
            resultsView = null
        }
    }

    private fun removeVideoView() {
        if (videoView != null) {
            try {
                windowManager.removeView(videoView)
            } catch (e: Exception) {
                Log.e(TAG, "Error removing video view", e)
            }
            videoView = null
        }
    }

    private fun removeAllOverlays() {
        removeFloatingView()
        removeResultView()
        removeVideoView()
    }

    override fun onDestroy() {
        // Clean up
        timer?.cancel()
        timer = null
        removeAllOverlays()

        super.onDestroy()
    }
}