package com.comp350.tldr

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import kotlin.random.Random
import kotlinx.coroutines.*

class PopLessonService : Service() {
    private val serviceIdentifier = "PopLessonService"

    private var lessonCoroutineJob: Job? = null //To keep track of the coroutine

    // Window manager for drawing over other apps
    private lateinit var windowManager: WindowManager

    private lateinit var layoutInflater: LayoutInflater // For inflating views

    // Root view for our floating UI
    private var floatingView: View? = null


    // Default Lesson Selection Data
    private var currentTopic = "Python Basics"
    private var intervalMs: Long = 120000 // Default 2 minutes



    // Lesson Information
    private val pythonLessons = listOf(
        Lesson(
            "What are variables used for?",
            listOf("To store data", "", "", ""),
            0
        ),
        Lesson(
            "What is the correct form to name a variable with multiple words?",
            listOf("snake_case", "", "", ""),
            0
        ),
        Lesson(
            "What keyword is used to define a function in Python?",
            listOf("", "def", "", ""),
            1
        ),
        Lesson(
            "How do you print text in Python?",
            listOf(
                "",
                "",
                "",
                "print(\"Hello\")"
            ),
            3
        ),
        Lesson(
            "What is the correct way to start a comment in Python?",
            listOf(
                "",
                "",
                "# This is a comment",
                ""
            ),
            2
        ),
        Lesson(
            "What data type would you use to store a whole number in Python?",
            listOf("", "", "int", ""),
            2
        ),
        Lesson(
            "How do you create a list in Python?",
            listOf(
                "",
                "my_list = [1, 2, 3]",
                "",
                ""
            ),
            1
        ),
        Lesson(
            "Which of these is NOT a Python data type?",
            listOf("list", "dictionary", "array", "tuple"),
            2
        ),
        Lesson(
            "How do you check the length of a list in Python?",
            listOf(
                "",
                "",
                "len(list)",
                ""
            ),
            2
        ),
        Lesson(
            "What will print(type(42)) display in Python?",
            listOf(
                "<class 'int'>",
                "",
                "",
                ""
            ),
            0
        )
    )

    override fun onBind(intent: Intent?): IBinder? {
        return null // Not a bound service
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(serviceIdentifier, "PopQuizService created")
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        layoutInflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            return START_NOT_STICKY
        }
        when (intent.action) {
            "START_SERVICE" -> {
                // Get parameters
                currentTopic = intent.getStringExtra("topic") ?: "Python Basics"
                intervalMs = intent.getLongExtra("interval", 120000)

                Log.d(serviceIdentifier, "Starting service with topic: $currentTopic, interval: $intervalMs ms")

                // reusing quiz code to start service
                startQuizScheduler()
            }
            "STOP_SERVICE" -> {
                Log.d(serviceIdentifier, "Stopping service")
                stopSelf()
            }
        }

        return START_STICKY
    }

    private fun initializeFloatingView() {
        if (floatingView == null) {
            val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            floatingView = inflater.inflate(R.layout.floating_lesson, null)

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
            )
            params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            params.y = 100

            try {
                windowManager.addView(floatingView, params)
            } catch (e: Exception) {
                Log.e(serviceIdentifier, "Error adding floating view", e)
            }
        }

    }

    private fun showFloatingQuiz(lesson: Lesson) {
        //Ensure the view is added
        initializeFloatingView()

        addViewToWindowManager(floatingView!!)
        // Find views in the layout
        val robotImage = floatingView?.findViewById<ImageView>(R.id.robotImage)
        val lessonText = floatingView?.findViewById<TextView>(R.id.lessonText)
        val radioGroup = floatingView?.findViewById<RadioGroup>(R.id.answersRadioGroup)
        val closeButton = floatingView?.findViewById<Button>(R.id.closeButton)

        val closeButtonBig = floatingView?.findViewById<Button>(R.id.submitButton)


        // Set the lesson text
        lessonText?.text = lesson.text


        // Handle close button click
        closeButton?.setOnClickListener {
            removeFloatingView()
        }

        // Handle close button click
        closeButtonBig?.setOnClickListener {
            removeFloatingView()
        }
    }

    private fun addViewToWindowManager(view: View, yOffset: Int = 100) {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        params.y = yOffset

        try {
            windowManager.addView(view, params)
        } catch (e: Exception) {
            Log.e(serviceIdentifier, "Error adding view", e)
        }
    }


    private fun startQuizScheduler() {
        // Cancel any existing job
        lessonCoroutineJob?.cancel()

        // Create a new job
        lessonCoroutineJob = CoroutineScope(Dispatchers.Main).launch {
            delay(5000) //First quiz after 5 seconds
            while (isActive) {

                showRandomLesson()

                delay(intervalMs) //Repeat at interval
            }
        }

        // Show a toast indicating active mode
        val intervalText = if (intervalMs < 60000) "${intervalMs / 1000} seconds" else "${intervalMs / 60000} minutes"
        Toast.makeText(
            this@PopLessonService,
            "Pop Lesson mode activated! Expect lessons every $intervalText",
            Toast.LENGTH_SHORT
        ).show()
    }
    override fun onDestroy() {
        super.onDestroy()
        Log.d(serviceIdentifier, "Service destroyed")

        // Clean up
        lessonCoroutineJob?.cancel()
        // ...
    }

    private fun showRandomLesson() {
        // Get lessons for the current topic
        val lessonTopic = when (currentTopic) {
            "Python Basics" -> pythonLessons
            "Variables & Data Types" -> pythonLessons.filter { it.text.contains("variable", ignoreCase = true) }
            "Control Flow" -> pythonLessons
            else -> pythonLessons
        }

        // Select a random lesson
        val lessonIndex = Random.nextInt(lessonTopic.size)
        val lessonDisplay = lessonTopic[lessonIndex]

        // Show the floating quiz UI
        showFloatingQuiz(lessonDisplay)
    }

    private fun removeFloatingView() {
        if (floatingView != null) {
            try {
                windowManager.removeView(floatingView)
                floatingView = null
            } catch (e: Exception) {
                Log.e(serviceIdentifier, "Error removing floating view", e)
            }
        }
    }
}

