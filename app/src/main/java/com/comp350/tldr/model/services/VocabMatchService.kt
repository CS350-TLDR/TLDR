package com.comp350.tldr.model.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Timer
import kotlin.math.abs
import com.google.firebase.auth.FirebaseAuth

class VocabMatchService : Service() {
    private val serviceIdentifier = "VocabMatchService"
    private lateinit var windowManager: WindowManager
    private val cards = mutableListOf<View>()
    private val cardPairs = mutableMapOf<String, String>()
    private var vocabCoroutineJob: Job? = null
    private var pixelFont: Typeface? = null
    private var currentTopic = "Python"

    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPrefs: android.content.SharedPreferences
    private var gears = 0

    private val samplePythonQuestions = listOf(
        "What are variables used for?" to "To store data",
        "What is the correct form to name a variable with multiple words?" to "snake_case",
        "What keyword is used to define a function in Python?" to "def",
        "How do you print text in Python?" to "print(\"Hello\")",
        "What is a valid variable name in Python?" to "my_variable",
        "What is the correct way to start a comment in Python?" to "# This is a comment",
        "How do you add an item to the end of a list in Python?" to "list.append(item)",
        "What does the strip() method do in Python?" to "Removes whitespace from the beginning and end",
        "What data type stores whole numbers?" to "int",
        "What symbol is used for single-line comments?" to "#",
        "How do you define a string variable?" to "name = \"Alice\"",
        "What does len(list) return?" to "Length of the list",
        "How do you create a list?" to "my_list = [1, 2, 3]",
        "How do you check equality in Python?" to "==",
        "What function displays output?" to "print()",
        "How do you start a for loop?" to "for x in list:",
        "Which keyword ends a function and sends back a value?" to "return",
        "How do you define a function with parameters?" to "def my_func(x, y):",
        "How do you access the first element of a list?" to "list[0]",
        "How do you import a module?" to "import module"
    )

    private val cleanCodeQuestions = listOf(
        "What does the Boy Scout Rule suggest?" to "Leave the code cleaner than you found it",
        "What is a common sign of bad code?" to "Duplication",
        "What does LeBlanc's law state?" to "Later equals never",
        "Why should functions be small?" to "They're easier to understand, test, and reuse",
        "What naming convention should be used for variables?" to "Clear, intention-revealing names",
        "What is a code smell?" to "A surface indication of deeper problems",
        "What does the Single Responsibility Principle state?" to "A class should have only one reason to change",
        "What is the problem with comments?" to "They often compensate for unclear code",
        "What is meant by 'Technical Debt'?" to "Future costs from taking shortcuts now",
        "What is a side effect?" to "When a function changes something outside its scope",
        "Why should error handling be separated from normal logic?" to "To improve code clarity and readability",
        "What is the DRY principle?" to "Don't Repeat Yourself",
        "Why are meaningful names important?" to "They make code self-documenting",
        "What is 'primitive obsession'?" to "Overuse of primitive data types instead of custom objects",
        "What are the benefits of test-driven development?" to "It ensures code works and supports refactoring",
        "What should a good function do?" to "Do one thing, do it well, do it only",
        "What is continuous refactoring?" to "Constantly improving code without changing behavior",
        "What is a good rule for function arguments?" to "Zero to two arguments is ideal",
        "What makes comments dangerous?" to "They can become outdated while code changes",
        "Why does code formatting matter?" to "It improves readability and signals professionalism",
        "What is the primary purpose of a name in code?" to "To reveal the intent of the variable, function, or class",
        "What should you do if a name requires a comment?" to "Rename it to reveal intent",
        "What term describes names that suggest false meanings?" to "Disinformation",
        "Why are single-letter names generally discouraged?" to "They are not searchable or meaningful",
        "What is a \"noise word\" in a name?" to "A redundant or meaningless addition",
        "How should classes be named?" to "With noun phrases",
        "What is the \"Boy Scout Rule\" applied to naming?" to "Leave names cleaner than you found them",
        "When should single-letter variables be used?" to "Only in small local scopes, like short loops",
        "What causes mental mapping problems?" to "Names that require extra mental translation",
        "When should you use problem domain names?" to "When no technical term exists for the concept"
    )


    private val correctMatches = mutableSetOf<String>()
    private val matchedCards = mutableSetOf<View>()
    private var intervalMs: Long = 60000L
    private var timer: Timer? = null
    private var waitingForNextSet = false
    private val handler = Handler(Looper.getMainLooper())
    private var gearsEarned = 0
    private var totalPairs = 0

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(serviceIdentifier, "VocabMatchService created")
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        auth = FirebaseAuth.getInstance()
        sharedPrefs = getSharedPreferences("tldr_prefs", Context.MODE_PRIVATE)
        loadGears()

        try {
            pixelFont = resources.getFont(resources.getIdentifier("rainyhearts", "font", packageName))
            Log.d(serviceIdentifier, "Pixel font loaded successfully")
        } catch (e: Exception) {
            Log.e(serviceIdentifier, "Failed to load pixel font", e)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent ?: return START_NOT_STICKY

        when (intent.action) {
            "START_SERVICE" -> handleStart(intent)
            "STOP_SERVICE" -> stopSelf()
            "SHOW_NOW" -> {
                Log.d(serviceIdentifier, "SHOW_NOW action received - showing cards immediately")
                Toast.makeText(this, "Showing VocabMatch cards now", Toast.LENGTH_SHORT).show()
                refreshCards()
            }
        }

        return START_NOT_STICKY
    }

    private fun handleStart(intent: Intent) {
        intervalMs = intent.getLongExtra("interval", 60000L)
        currentTopic = intent.getStringExtra("topic") ?: "Python"

        timer?.cancel()
        timer = Timer()
        handler.post { refreshCards() }
        startVocabScheduler()
    }

    private fun formatIntervalForDisplay(intervalMs: Long): String {
        return when (intervalMs) {
            60000L -> "1 minute"
            300000L -> "5 minutes"
            600000L -> "10 minutes"
            1800000L -> "30 minutes"
            3600000L -> "1 hour"
            7200000L -> "2 hours"
            else -> "${intervalMs / 60000} minutes"
        }
    }

    private fun startVocabScheduler() {
        vocabCoroutineJob?.cancel()
        vocabCoroutineJob = CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(
                this@VocabMatchService,
                "Vocab Match Activated!",
                Toast.LENGTH_SHORT
            ).show()

            while (isActive) {
                if (waitingForNextSet) {
                    refreshCards()
                    waitingForNextSet = false
                }
                delay(intervalMs)
            }
        }
    }

    private fun getQuestionsForTopic(): List<Pair<String, String>> {
        return when (currentTopic) {
            "Clean Code" -> cleanCodeQuestions
            else -> samplePythonQuestions
        }
    }

    private fun refreshCards() {
        removeAllCards()
        correctMatches.clear()
        matchedCards.clear()
        gearsEarned = 0

        val questionList = getQuestionsForTopic()
        val shuffled = questionList.shuffled().take(4)
        val questions = shuffled.map { it.first }
        val answers = shuffled.map { it.second }
        val allItems = (questions + answers).shuffled()
        val screenWidth = resources.displayMetrics.widthPixels

        totalPairs = shuffled.size

        allItems.forEachIndexed { index, label ->
            val card = createCard(label)
            val isLeft = index % 2 == 0
            val x = if (isLeft) screenWidth / 10 else screenWidth * 6 / 10
            val y = 300 + (index * 200) + if (isLeft) -50 else 50
            addCard(card, x, y)
        }

        cardPairs.clear()
        shuffled.forEach { (q, a) -> cardPairs[q] = a }

        showClearButton()
    }

    private fun showClearButton() {
        val button = Button(this).apply {
            text = "Clear"
            textSize = 8f
            setPadding(8, 4, 8, 4)

            // Apply pixel font if available
            pixelFont?.let { typeface = it }

            setOnClickListener {
                removeAllCards()
                Toast.makeText(this@VocabMatchService, "Cards cleared", Toast.LENGTH_SHORT).show()
            }
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.END
            x = 30
            y = 30
        }

        try {
            windowManager.addView(button, params)
            cards.add(button)
        } catch (e: Exception) {
            Log.e(serviceIdentifier, "Error showing clear button", e)
        }
    }

    private fun createCard(label: String): View {
        val outerFrame = FrameLayout(this).apply {
            setBackgroundColor(Color.BLACK)
            setPadding(6, 6, 6, 6)
            tag = label
        }

        val innerFrame = FrameLayout(this).apply {
            setBackgroundColor(Color.parseColor("#4B89DC"))
            setPadding(8, 8, 8, 8)
            id = 1001 // Give it an ID so we can find it later
        }

        val textView = TextView(this).apply {
            text = label
            setTextColor(Color.WHITE)
            textSize = 16f
            setPadding(24, 24, 24, 24)
            gravity = Gravity.CENTER

            // Apply pixel font if available
            pixelFont?.let { typeface = it }
        }

        innerFrame.addView(textView)
        outerFrame.addView(innerFrame)

        val touchListener = object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if (matchedCards.contains(v)) {
                    return false
                }

                val params = v.layoutParams as WindowManager.LayoutParams
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager.updateViewLayout(v, params)
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        checkForMatch(v)
                        return true
                    }
                }
                return false
            }
        }

        outerFrame.setOnTouchListener(touchListener)
        return outerFrame
    }

    private fun checkForMatch(draggedCard: View) {
        val draggedText = draggedCard.tag as String
        val draggedParams = draggedCard.layoutParams as WindowManager.LayoutParams

        for (card in cards) {
            if (card == draggedCard || matchedCards.contains(card)) continue

            val targetParams = card.layoutParams as WindowManager.LayoutParams

            if (abs(draggedParams.x - targetParams.x) < 400 && abs(draggedParams.y - targetParams.y) < 200) {
                val targetText = card.tag as String
                val isMatch = cardPairs[draggedText] == targetText || cardPairs[targetText] == draggedText

                if (isMatch) {
                    correctMatches.add(draggedText)
                    correctMatches.add(targetText)
                    matchedCards.add(draggedCard)
                    matchedCards.add(card)
                    gearsEarned++


                    gears++
                    saveGears()

                    val draggedInnerFrame = draggedCard.findViewById<FrameLayout>(1001)
                    val targetInnerFrame = card.findViewById<FrameLayout>(1001)

                    draggedInnerFrame?.setBackgroundColor(Color.GREEN)
                    targetInnerFrame?.setBackgroundColor(Color.GREEN)

                    Toast.makeText(this, "Correct Match! (+1 Gear)", Toast.LENGTH_SHORT).show()

                    handler.postDelayed({
                        try {
                            windowManager.removeView(draggedCard)
                            windowManager.removeView(card)
                            cards.remove(draggedCard)
                            cards.remove(card)

                            if (correctMatches.size == totalPairs * 2) {
                                showGearPopup(gearsEarned)
                                waitingForNextSet = true
                            }
                        } catch (e: Exception) {
                            Log.e(serviceIdentifier, "Error removing matched cards", e)
                        }
                    }, 500)

                } else {
                    val draggedInnerFrame = draggedCard.findViewById<FrameLayout>(1001)
                    val targetInnerFrame = card.findViewById<FrameLayout>(1001)

                    draggedInnerFrame?.setBackgroundColor(Color.RED)
                    targetInnerFrame?.setBackgroundColor(Color.RED)

                    Toast.makeText(this, "Incorrect Match", Toast.LENGTH_SHORT).show()

                    handler.postDelayed({
                        draggedInnerFrame?.setBackgroundColor(Color.parseColor("#4B89DC"))
                        targetInnerFrame?.setBackgroundColor(Color.parseColor("#4B89DC"))
                    }, 500)
                }
                break
            }
        }
    }

    private fun showGearPopup(earned: Int) {
        val popup = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
            setBackgroundColor(Color.BLACK)
            gravity = Gravity.CENTER
        }

        val title = TextView(this).apply {
            text = "Vocab Match Completed!"
            textSize = 26f
            setTextColor(Color.WHITE)
            setPadding(0, 0, 0, 24)
            gravity = Gravity.CENTER
            pixelFont?.let { typeface = it }
        }

        val message = TextView(this).apply {
            text = "You earned $earned gears!"
            textSize = 20f
            setTextColor(Color.YELLOW)
            gravity = Gravity.CENTER
            pixelFont?.let { typeface = it }
        }

        val closeButton = Button(this).apply {
            text = "Continue"
            pixelFont?.let { typeface = it }

            setOnClickListener {
                try {
                    windowManager.removeView(popup)
                } catch (e: Exception) {
                    Log.e(serviceIdentifier, "Error removing popup", e)
                }
            }
        }

        popup.addView(title)
        popup.addView(message)
        popup.addView(closeButton)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        try {
            windowManager.addView(popup, params)
        } catch (e: Exception) {
            Log.e(serviceIdentifier, "Error showing popup", e)
        }
    }

    private fun addCard(view: View, x: Int, y: Int) {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            this.x = x
            this.y = y
        }

        try {
            windowManager.addView(view, params)
            cards.add(view)
        } catch (e: Exception) {
            Log.e(serviceIdentifier, "Error adding card", e)
        }
    }

    private fun removeAllCards() {
        for (card in cards) {
            try {
                windowManager.removeView(card)
            } catch (e: Exception) {
                Log.e(serviceIdentifier, "Error removing card", e)
            }
        }
        cards.clear()
        matchedCards.clear()
        waitingForNextSet = true
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

    override fun onDestroy() {
        timer?.cancel()
        removeAllCards()
        Log.d(serviceIdentifier, "Vocab Match Service destroyed")
        super.onDestroy()
        vocabCoroutineJob?.cancel()
    }
}