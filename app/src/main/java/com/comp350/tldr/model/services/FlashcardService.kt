package com.comp350.tldr.model.services

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.*
import android.view.*
import android.widget.*
import android.graphics.PixelFormat
import com.comp350.tldr.classicstuff.Question
import com.google.firebase.auth.FirebaseAuth
import java.util.*

class FlashcardService : Service() {
    private val serviceIdentifier = "FlashcardService"
    private lateinit var windowManager: WindowManager
    private val flashcardViews = ArrayList<View>(3)
    private var timer: Timer? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPrefs: android.content.SharedPreferences

    private var currentTopic = "Python"
    private var intervalMs: Long = 60000
    private var gears = 0

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

    private val cleanCodeQuestions = listOf(
        Question("What common programmer experience is described as \"wading\"?",
            listOf("Struggling through bad code", "Debugging hardware issues", "Brainstorming ideas", "Designing UI/UX"), 0),
        Question("What does LeBlanc's Law state?",
            listOf("Later equals never", "Bugs are inevitable", "Clean code is slow code", "Deadlines overrule quality"), 0),
        Question("What is a major consequence of a messy codebase over time?",
            listOf("Decreasing productivity", "Lower memory usage", "Fewer bugs", "Better performance"), 0),
        Question("What quality does Michael Feathers say defines clean code?",
            listOf("It looks like someone cared", "It's fast to write", "It avoids using functions", "It uses the latest framework"), 0),
        Question("What metaphor do Dave Thomas and Andy Hunt use to describe messy code?",
            listOf("Broken windows", "A house of cards", "A tangled web", "A leaking faucet"), 0),
        Question("According to Ron Jeffries, what is a key sign of clean code?",
            listOf("It minimizes duplication", "It uses long variable names", "It has no comments", "It's optimized for performance"), 0),
        Question("What does the \"Boy Scout Rule\" in programming advocate for?",
            listOf("Always improve the code you touch", "Code must be rewritten monthly", "Add at least one new feature per commit", "Avoid touching old code"), 0),
        Question("What makes a function name good?",
            listOf("It describes what the function does", "It's short", "It includes the return type", "It starts with a verb"), 0),
        Question("What is a code smell?",
            listOf("A sign of poor design", "A compiler warning", "A syntax error", "A performance bottleneck"), 0),
        Question("What is the Single Responsibility Principle?",
            listOf("A class should have only one reason to change", "Code should be written by a single person", "Functions should have only one parameter", "Tests should check only one thing"), 0),
        Question("What is the primary purpose of a name in code?",
            listOf("To reveal the intent of the variable, function, or class", "To shorten the code", "To confuse other programmers", "To pass compiler checks"), 0),
        Question("What should you do if a name requires a comment to explain it?",
            listOf("Rename it to reveal intent", "Keep it as is", "Shorten the name", "Add more comments instead"), 0),
        Question("What term describes names that suggest false meanings?",
            listOf("Disinformation", "Redirection", "Inference", "Compression"), 0),
        Question("Why are single-letter names generally discouraged?",
            listOf("They are not searchable or meaningful", "They are difficult to type", "They take up too much space", "They slow down compilation"), 0),
        Question("What is a \"noise word\" in a name?",
            listOf("A redundant or meaningless addition to the name", "A word that makes the name funnier", "A technical term that describes functionality", "A requirement in Java programming"), 0),
        Question("Which of the following is a better practice?",
            listOf("Naming classes with noun phrases", "Using m_ prefixes for member variables", "Using Hungarian Notation in modern code", "Naming classes with verbs"), 0),
        Question("What is the \"Boy Scout Rule\" applied to naming?",
            listOf("Leave names cleaner and more understandable than you found them", "Always add a joke to every name", "Encode types into every name", "Keep names short at all costs"), 0),
        Question("When should single-letter variables like i, j, or k be used?",
            listOf("Only in small local scopes, like short loops", "Always, to save space", "Never, under any circumstances", "In function names"), 0),
        Question("What naming mistake leads to mental mapping problems?",
            listOf("Using single-letter names that require extra mental translation", "Long, descriptive names", "Using consistent technical terms", "Using searchable constants"), 0),
        Question("When should you use problem domain names?",
            listOf("When no technical term exists for the concept", "To impress managers", "To make code unreadable to other developers", "To encode types and scopes"), 0),


                Question("What is the first and most important rule for writing functions?",
        listOf("They should be small", "They must be highly optimized", "They should be long and detailed", "They must use recursion"), 0),

    Question("What does \"Do One Thing\" mean for functions?",
    listOf("Functions should focus on a single task or responsibility", "Functions should only use one data type", "Functions should handle both setup and teardown", "Functions should process one line of code"), 0),

    Question("What is the Stepdown Rule?",
    listOf("Code should read top-to-bottom, dropping one level of abstraction at a time", "Each function should be lower performance than the one before", "Code should be indented as much as possible", "Every function must contain a loop"), 0),

    Question("What should you do instead of using switch statements across your code?",
    listOf("Use polymorphism to replace them", "Ignore them", "Expand them with more cases", "Always return error codes"), 0),

    Question("How many arguments should a function ideally have?",
    listOf("One or two at most", "Three or more", "Exactly four", "As many as needed"), 0),

    Question("Why are flag arguments considered bad practice?",
    listOf("They make code less readable and imply multiple behaviors", "They make code faster", "They save memory", "They improve compile times"), 0),

    Question("What is a dangerous side effect of a function?",
    listOf("Changing unrelated system state without clear intention", "Writing to a log file", "Printing to the console", "Adding extra whitespace"), 0),

    Question("When should you prefer exceptions over returning error codes?",
    listOf("When you want to separate happy path logic from error handling", "Only for fatal errors", "When you want faster performance", "When memory usage is a concern"), 0),

    Question("What principle does \"Don't Repeat Yourself\" (DRY) target?",
    listOf("Preventing duplicate logic in the codebase", "Reducing function arguments", "Increasing error handling", "Adding more abstraction layers"), 0),

    Question("How does using descriptive function names help?",
    listOf("It makes comments unnecessary", "It increases performance", "It reduces the need for refactoring", "It hides the implementation details"), 0),
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
            "SHOW_NOW" -> displayAllFlashcards()
        }

        return START_NOT_STICKY
    }

    private fun handleStart(intent: Intent) {
        intervalMs = intent.getLongExtra("interval", 60000)
        currentTopic = intent.getStringExtra("topic") ?: "Python"

        timer?.cancel()
        timer = Timer()

        removeAllFlashcards()

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

    private fun getQuestionsForCurrentTopic(): List<Question> {
        return when (currentTopic) {
            "Clean Code" -> cleanCodeQuestions
            else -> pythonQuestions
        }
    }

    private fun displayAllFlashcards() {
        removeAllFlashcards()

        val screenWidth = resources.displayMetrics.widthPixels
        val questions = getQuestionsForCurrentTopic()

        for (i in 0 until 3) {
            val question = questions.random()
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
        val questions = getQuestionsForCurrentTopic()

        for (i in 0 until 3) {
            val question = questions.random()
            val card = createResizableFlashCardView(question)

            val xPos = if (i < positions.size) positions[i].first else 100 + (i * 50)
            val yPos = if (i < positions.size) positions[i].second else 100 + (i * 80)

            addFlashcardOverlay(card, xPos, yPos)
            flashcardViews.add(card)
        }
    }

    private fun createResizableFlashCardView(question: Question): View {
        val layout = FrameLayout(this).apply {
            setBackgroundColor(Color.parseColor("#333333"))
            setPadding(2, 2, 2, 2)
        }

        val contentLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.BLACK)
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val titleBar = TextView(this).apply {
            text = currentTopic
            setBackgroundColor(Color.parseColor("blue"))
            setTextColor(Color.WHITE)
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
            setTextColor(Color.LTGRAY)
            textSize = 10f
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val cardFront = TextView(this).apply {
            text = question.text
            setTextColor(Color.WHITE)
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
            setTextColor(Color.YELLOW)
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
            setBackgroundColor(Color.parseColor("#4B89DC"))
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
        removeAllFlashcards()
        super.onDestroy()
    }
}