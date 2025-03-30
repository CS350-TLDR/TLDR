package com.comp350.tldr

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Handler
import android.os.IBinder
import android.os.Looper
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
import com.comp350.tldr.unusedfeatures.Question
import java.util.Timer
import java.util.TimerTask
import kotlin.random.Random

class PopQuizService : Service() {
    private val TAG = "PopQuizService"

    // Window manager for drawing over other apps
    private lateinit var windowManager: WindowManager

    // Root view for our floating UI
    private var floatingView: View? = null
    private var resultsView: View? = null

    // Timer for scheduling quizzes
    private var timer: Timer? = null

    // Quiz data
    private var currentTopic = "Python Basics"
    private var intervalMs: Long = 120000 // Default 2 minutes
    private var maxQuestions: Int = 5 // Default max questions
    private var questionCounter: Int = 0 // Track questions in current session

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

    // User stats
    private var correctAnswers = 0
    private var totalAnswered = 0
    private var sessionScore = 0

    override fun onBind(intent: Intent?): IBinder? {
        return null // Not a bound service
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "PopQuizService created")
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
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
                maxQuestions = intent.getIntExtra("maxQuestions", 5)

                // Reset session counters
                questionCounter = 0
                sessionScore = 0

                Log.d(TAG, "Starting service with topic: $currentTopic, interval: $intervalMs ms, maxQuestions: $maxQuestions")

                // Start scheduling quizzes
                startQuizScheduler()
            }
            "STOP_SERVICE" -> {
                Log.d(TAG, "Stopping service")
                stopSelf()
            }
        }

        return START_STICKY
    }

    private fun startQuizScheduler() {
        // Cancel any existing timer
        timer?.cancel()

        // Create a new timer
        timer = Timer()

        // Schedule quizzes at the specified interval
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                // Run on the main thread since we're updating UI
                Handler(Looper.getMainLooper()).post {
                    if (questionCounter < maxQuestions) {
                        showRandomQuiz()
                        questionCounter++
                    } else {
                        // Show session results when max questions reached
                        showSessionResults()
                        // Reset counter for next session
                        questionCounter = 0
                        sessionScore = 0
                    }
                }
            }
        }, 5000, intervalMs) // First quiz after 5 seconds, then repeat at interval

        // Show a toast indicating active mode
        val intervalText = if (intervalMs < 60000) "${intervalMs / 1000} seconds" else "${intervalMs / 60000} minutes"
        Toast.makeText(
            this,
            "Pop Quiz mode activated! Expect $maxQuestions questions every $intervalText",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showRandomQuiz() {
        // Get questions for the current topic
        val questions = when (currentTopic) {
            "Python Basics" -> pythonQuestions
            "Variables & Data Types" -> pythonQuestions.filter { it.text.contains("variable", ignoreCase = true) }
            "Control Flow" -> pythonQuestions // TODO: Add specific control flow questions
            else -> pythonQuestions
        }

        // Select a random question
        val questionIndex = Random.nextInt(questions.size)
        val question = questions[questionIndex]

        // Show the floating quiz UI
        showFloatingQuiz(question)
    }

    private fun showFloatingQuiz(question: Question) {
        // Remove any existing view
        removeFloatingView()

        // Inflate the floating quiz layout
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        floatingView = inflater.inflate(R.layout.floating_quiz, null)

        // Set up the layout parameters for the floating window
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        )

        // Position at the top of the screen
        params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        params.y = 100 // Some offset from the top

        // Find views in the layout
        val robotImage = floatingView?.findViewById<ImageView>(R.id.robotImage)
        val questionText = floatingView?.findViewById<TextView>(R.id.questionText)
        val radioGroup = floatingView?.findViewById<RadioGroup>(R.id.answersRadioGroup)
        val submitButton = floatingView?.findViewById<Button>(R.id.submitButton)
        val closeButton = floatingView?.findViewById<Button>(R.id.closeButton)
        val progressText = floatingView?.findViewById<TextView>(R.id.progressText)

        // Set the question text
        questionText?.text = question.text

        // Update progress text
        progressText?.text = "Question ${questionCounter + 1} of $maxQuestions"

        // Add radio buttons for each answer option
        question.options.forEachIndexed { index, option ->
            val radioButton = RadioButton(this)
            radioButton.id = index
            radioButton.text = option
            radioButton.textSize = 16f
            radioGroup?.addView(radioButton)
        }

        // Handle submit button click
        submitButton?.setOnClickListener {
            val selectedId = radioGroup?.checkedRadioButtonId
            if (selectedId != -1 && selectedId != null) {
                val isCorrect = selectedId == question.correctAnswerIndex
                totalAnswered++

                if (isCorrect) {
                    correctAnswers++
                    sessionScore++
                    showAnswerResult(true, question.options[question.correctAnswerIndex])
                } else {
                    showAnswerResult(false, question.options[question.correctAnswerIndex])
                }
            } else {
                Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle close button click
        closeButton?.setOnClickListener {
            removeFloatingView()
        }

        // Add the view to the window manager
        try {
            windowManager.addView(floatingView, params)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding floating view", e)
        }
    }

    private fun showAnswerResult(isCorrect: Boolean, correctAnswer: String) {
        // Remove quiz view
        removeFloatingView()

        // Inflate result view
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        resultsView = inflater.inflate(R.layout.floating_quiz_result, null)

        // Set up layout parameters
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        )

        // Position at the top of the screen
        params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        params.y = 100

        // Find views
        val resultText = resultsView?.findViewById<TextView>(R.id.resultText)
        val robotImage = resultsView?.findViewById<ImageView>(R.id.robotImage)
        val correctAnswerText = resultsView?.findViewById<TextView>(R.id.correctAnswerText)
        val continueButton = resultsView?.findViewById<Button>(R.id.continueButton)
        val progressText = resultsView?.findViewById<TextView>(R.id.progressText)

        // Update views
        resultText?.text = if (isCorrect) "Correct!" else "Wrong!"
        resultText?.setTextColor(if (isCorrect)
            resources.getColor(android.R.color.holo_green_dark, null)
        else
            resources.getColor(android.R.color.holo_red_dark, null)
        )

        correctAnswerText?.text = "The correct answer is: $correctAnswer"
        progressText?.text = "Score: $sessionScore/$maxQuestions"

        // Handle continue button
        continueButton?.setOnClickListener {
            removeResultView()
        }

        // Add view to window manager
        try {
            windowManager.addView(resultsView, params)

            // Auto-dismiss after 5 seconds
            Handler(Looper.getMainLooper()).postDelayed({
                removeResultView()
            }, 5000)

        } catch (e: Exception) {
            Log.e(TAG, "Error adding results view", e)
        }
    }

    private fun showSessionResults() {
        // Remove any existing views
        removeFloatingView()
        removeResultView()

        // Inflate session results view
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        resultsView = inflater.inflate(R.layout.floating_session_results, null)

        // Set up layout parameters
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        )

        // Position at the top of the screen
        params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        params.y = 100

        // Find views
        val sessionTitleText = resultsView?.findViewById<TextView>(R.id.sessionTitleText)
        val sessionScoreText = resultsView?.findViewById<TextView>(R.id.sessionScoreText)
        val dismissButton = resultsView?.findViewById<Button>(R.id.dismissButton)

        // Update views
        sessionTitleText?.text = "Quiz Session Complete!"
        sessionScoreText?.text = "You scored $sessionScore out of $maxQuestions"

        // Handle dismiss button
        dismissButton?.setOnClickListener {
            removeResultView()
        }

        // Add view to window manager
        try {
            windowManager.addView(resultsView, params)

            // Auto-dismiss after 10 seconds
            Handler(Looper.getMainLooper()).postDelayed({
                removeResultView()
            }, 10000)

        } catch (e: Exception) {
            Log.e(TAG, "Error adding session results view", e)
        }
    }

    private fun removeFloatingView() {
        if (floatingView != null) {
            try {
                windowManager.removeView(floatingView)
                floatingView = null
            } catch (e: Exception) {
                Log.e(TAG, "Error removing floating view", e)
            }
        }
    }

    private fun removeResultView() {
        if (resultsView != null) {
            try {
                windowManager.removeView(resultsView)
                resultsView = null
            } catch (e: Exception) {
                Log.e(TAG, "Error removing result view", e)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")

        // Clean up
        timer?.cancel()
        timer = null
        removeFloatingView()
        removeResultView()

        // Show user's stats
        if (totalAnswered > 0) {
            val accuracy = (correctAnswers.toFloat() / totalAnswered) * 100
            Toast.makeText(
                this,
                "Pop Quiz session ended. Overall Accuracy: ${accuracy.toInt()}% (${correctAnswers}/${totalAnswered})",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}