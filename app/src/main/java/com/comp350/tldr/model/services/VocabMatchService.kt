package com.comp350.tldr.model.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
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
import com.comp350.tldr.R
import java.util.Timer
import com.google.firebase.auth.FirebaseAuth
import java.util.TimerTask

class VocabMatchService : Service() {
    private val serviceIdentifier = "VocabMatchService"
    private lateinit var windowManager: WindowManager
    private val cards = mutableListOf<View>()
    private val cardPairs = mutableMapOf<String, String>()
    private var pixelFont: Typeface? = null
    private var currentTopic = "Python"

    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPrefs: android.content.SharedPreferences
    private var gears = 0
    private var backgroundView: View? = null

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

        startRefreshTimer()
    }

    private fun startRefreshTimer() {
        timer?.cancel()
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                Handler(Looper.getMainLooper()).post {
                    Log.d(serviceIdentifier, "Timer triggered at interval: $intervalMs ms")
                    if (waitingForNextSet) {
                        refreshCards()
                        waitingForNextSet = false
                    }
                }
            }
        }, intervalMs, intervalMs) // Use the specified interval
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

        showBackgroundLayer()

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
            textSize = 16f
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

    private val dragTouchListener = View.OnTouchListener { v, event ->
        if (matchedCards.contains(v)) return@OnTouchListener false

        val params = v.layoutParams as WindowManager.LayoutParams
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                v.setTag(R.id.initial_x, params.x)
                v.setTag(R.id.initial_y, params.y)
                v.setTag(R.id.touch_x, event.rawX)
                v.setTag(R.id.touch_y, event.rawY)
                true
            }
            MotionEvent.ACTION_MOVE -> {
                val initialX = v.getTag(R.id.initial_x) as Int
                val initialY = v.getTag(R.id.initial_y) as Int
                val touchX = v.getTag(R.id.touch_x) as Float
                val touchY = v.getTag(R.id.touch_y) as Float

                params.x = initialX + (event.rawX - touchX).toInt()
                params.y = initialY + (event.rawY - touchY).toInt()
                windowManager.updateViewLayout(v, params)
                true
            }
            MotionEvent.ACTION_UP -> {
                checkForMatch(v)
                true
            }
            else -> false
        }
    }


    private fun createCard(label: String): View {
        val cardWidth = 500  // consistent width in pixels
        val cardHeight = 200 // consistent height in pixels

        val outerFrame = FrameLayout(this).apply {
            setPadding(6, 6, 6, 6)
            tag = label
            layoutParams = FrameLayout.LayoutParams(cardWidth, cardHeight)
        }

        val roundedBackground = GradientDrawable().apply {
            setColor(Color.parseColor("#4B89DC"))
            cornerRadius = 32f
        }

        val innerFrame = FrameLayout(this).apply {
            background = roundedBackground
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            id = 1001
        }

        val textView = TextView(this).apply {
            text = label
            setTextColor(Color.WHITE)
            textSize = 20f
            setPadding(16, 16, 16, 16)
            gravity = Gravity.CENTER
            setLineSpacing(0f, 1.2f)
            pixelFont?.let { typeface = it }
        }

        innerFrame.addView(textView)
        outerFrame.addView(innerFrame)
        outerFrame.setOnTouchListener(dragTouchListener)
        return outerFrame
    }


    private fun showBackgroundLayer() {
        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels

        val drawable = GradientDrawable().apply {
            setColor(Color.parseColor("#AA000000")) // Semi-transparent black
            cornerRadius = 48f // Adjust as needed for roundness
        }

        backgroundView = FrameLayout(this).apply {
            background = drawable
        }

        val params = WindowManager.LayoutParams(
            screenWidth,
            screenHeight,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }

        try {
            windowManager.addView(backgroundView, params)
        } catch (e: Exception) {
            Log.e(serviceIdentifier, "Error showing background layer", e)
        }
    }


    private fun checkForMatch(draggedCard: View) {
        val draggedParams = draggedCard.layoutParams as WindowManager.LayoutParams
        val draggedLeft = draggedParams.x
        val draggedTop = draggedParams.y
        val draggedRight = draggedLeft + draggedCard.width
        val draggedBottom = draggedTop + draggedCard.height

        for (card in cards) {
            if (card == draggedCard || matchedCards.contains(card)) continue

            val targetParams = card.layoutParams as WindowManager.LayoutParams
            val targetLeft = targetParams.x
            val targetTop = targetParams.y
            val targetRight = targetLeft + card.width
            val targetBottom = targetTop + card.height

            val intersects = draggedRight > targetLeft &&
                    draggedLeft < targetRight &&
                    draggedBottom > targetTop &&
                    draggedTop < targetBottom

            if (intersects) {
                val draggedText = draggedCard.tag as String
                val targetText = card.tag as String
                val isMatch = cardPairs[draggedText] == targetText || cardPairs[targetText] == draggedText

                val draggedInner = draggedCard.findViewById<FrameLayout>(1001)
                val targetInner = card.findViewById<FrameLayout>(1001)

                val draggedDrawable = draggedInner?.background as? GradientDrawable
                val targetDrawable = targetInner?.background as? GradientDrawable

                if (isMatch) {
                    draggedDrawable?.setColor(Color.GREEN)
                    targetDrawable?.setColor(Color.GREEN)
                    correctMatches.add(draggedText)
                    correctMatches.add(targetText)
                    matchedCards.add(draggedCard)
                    matchedCards.add(card)
                    gears++
                    gearsEarned++
                    saveGears()

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
                    // Keep rounded corners and just update the drawable's color
                    draggedDrawable?.setColor(Color.RED)
                    targetDrawable?.setColor(Color.RED)

                    Toast.makeText(this, "Incorrect Match", Toast.LENGTH_SHORT).show()

                    handler.postDelayed({
                        draggedDrawable?.setColor(Color.parseColor("#4B89DC"))
                        targetDrawable?.setColor(Color.parseColor("#4B89DC"))
                    }, 500)
                }

                break // stop after first match check
            }
        }
    }


    private fun showGearPopup(earned: Int) {

        // Remove the dimmed background when done
        backgroundView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                Log.e(serviceIdentifier, "Error removing background in popup", e)
            }
            backgroundView = null
        }

        val popup = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
            gravity = Gravity.CENTER

            background = GradientDrawable().apply {
                setColor(Color.parseColor("#4B89DC"))
                cornerRadius = 48f
            }
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

        backgroundView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                Log.e(serviceIdentifier, "Error removing background", e)
            }
            backgroundView = null
        }

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
    }

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
        "When should you use problem domain names?" to "When no technical term exists for the concept",
        "What is the most important rule for writing functions?" to "They should be small",
        "What does \"Do One Thing\" mean for functions?" to "Focus on a single task or responsibility",
        "What is the Stepdown Rule in code?" to "Code should read top-to-bottom, reducing abstraction level",
        "What should replace switch statements across code?" to "Use polymorphism instead",
        "How many arguments should a function ideally have?" to "One or two at most",
        "Why are flag arguments considered bad practice?" to "They make code less readable and imply multiple behaviors",
        "What is a dangerous side effect of a function?" to "Changing unrelated system state without clear intention",
        "When should you prefer exceptions over error codes?" to "To separate happy path logic from error handling",
        "What principle does DRY target?" to "Preventing duplicate logic in the codebase",
        "How does using descriptive function names help?" to "It makes comments unnecessary",

        // Chapter 4: Comments
        "What does the author say is the main problem with comments?" to "They lie as code evolves",
        "According to the author, what should you do instead of commenting bad code?" to "Rewrite the code",
        "What type of comment does the author consider acceptable?" to "Legal comments",
        "Why does the author say that \"comments are always failures\"?" to "They indicate a failure to express yourself in code",
        "What should you do instead of using a comment to explain confusing code?" to "Make the code so clear it doesn't need comments",
        "Which of these is described as a good use for comments?" to "Warning of consequences",
        "What does the author recommend for TODOs?" to "Keep them updated and scan them regularly",
        "What problem does the author identify with commented-out code?" to "People are afraid to delete it",
        "What is a \"noise comment\" according to the author?" to "A comment that states the obvious",
        "What practice should replace the need for many comments according to Clean Code?" to "Better variable naming",

        // Chapter 5: Formatting
        "What is the main purpose of code formatting according to the author?" to "Communication",
        "What is the \"newspaper metaphor\" in code formatting?" to "The highest level concepts should be at the top, with details below",
        "What does vertical openness between concepts help with?" to "Separating unrelated concepts",
        "What does the author say about horizontal alignment of variable declarations?" to "It's not useful and may highlight the wrong things",
        "What rule does the author suggest for indentation?" to "The team should agree on a style and be consistent",
        "According to the author, how big should source files be?" to "Hundreds of lines at most",
        "How should related concepts be positioned in code?" to "Vertically close to each other",
        "What is a good practice for variable declarations?" to "Declare them as close to their usage as possible",
        "Which style convention does the author present as most important?" to "Consistency across the team",
        "What does the author say about the rules for formatting?" to "They are too important to ignore and too important to treat religiously",

        // Chapter 6: Objects and Data Structures
        "What is the key difference between objects and data structures?" to "Objects hide data and expose behavior, data structures expose data",
        "What is the Law of Demeter also known as?" to "Principle of Least Knowledge",
        "What is a violation of the Law of Demeter?" to "Calling methods on objects returned from other methods",
        "What does the author call a class that's half object and half data structure?" to "A hybrid",
        "According to the chapter, what is a DTO?" to "Data Transfer Object: a class with public variables and no functions",
        "What is the issue with hybrids according to the author?" to "They're the worst of both worlds - hard to add functions and data structures",
        "What is data abstraction according to the chapter?" to "Hiding implementation behind an interface",
        "What does the author say about the complementary nature of objects and data structures?" to "Things easy for OO are hard for procedural code and vice versa",
        "What is an Active Record according to the chapter?" to "A data structure with navigational methods like save and find",
        "What is the recommended approach to Active Records?" to "Treat them as data structures and create separate objects with business rules",

        // Chapter 7: Error Handling
        "What approach does the author prefer for handling errors?" to "Exceptions",
        "What is the recommended approach to writing try-catch-finally blocks?" to "Write them first",
        "What does the author say about checked exceptions in Java?" to "They violate the Open/Closed Principle",
        "What should exceptions provide according to the author?" to "Context to determine source and location of error",
        "What is the Special Case pattern used for?" to "Eliminating the need for special case code",
        "According to the author, what should you never return from methods?" to "Null",
        "What is suggested as an alternative to returning null?" to "Both throwing exceptions and returning special case objects",
        "What does the author suggest about passing null as a parameter?" to "Avoid passing null whenever possible",
        "What problem does returning null create according to the chapter?" to "It creates extra work and possible errors for callers",
        "What approach to error handling increases coupling?" to "Using error codes",

        // Chapter 8: Boundaries
        "What are 'boundaries' in the context of this chapter?" to "Interfaces between our code and third-party code",
        "What is the tension described at boundaries?" to "Between providers who want general interfaces and users who want specific interfaces",
        "What does the author recommend when using third-party APIs like Map?" to "Hide them behind your own interfaces",
        "What are 'learning tests'?" to "Tests that explore and verify our understanding of third-party APIs",
        "According to the author, why are learning tests worth the effort?" to "They verify third-party code works as expected and flag changes in new versions",
        "What pattern is suggested for code that doesn't exist yet?" to "Define the interface you wish you had",
        "What is an Adapter in the context of boundaries?" to "A design pattern to make incompatible interfaces work together",
        "What benefit does the author mention about creating your own interface?" to "It gives you more control and provides a convenient seam for testing",
        "What does the author suggest about clean boundaries?" to "Code at boundaries needs clear separation and tests",
        "According to the chapter, what's better to depend on?" to "Something you control rather than something you don't",

        // Chapter 9: Unit Tests
        "According to the author, what enables the '-ilities' (maintainability, flexibility, etc.)?" to "Unit tests",
        "What does the author say is the most important aspect of clean tests?" to "Readability",
        "What is the BUILD-OPERATE-CHECK pattern in tests?" to "A pattern where tests are split into sections that build data, operate on it, and check results",
        "What does the author say about the 'one assert per test' rule?" to "It's a good guideline but not an absolute rule",
        "What does F.I.R.S.T. stand for in the context of clean tests?" to "Fast, Independent, Repeatable, Self-validating, Timely",
        "Why should tests be fast according to the chapter?" to "So developers will run them frequently",
        "What does it mean for tests to be independent?" to "They should not depend on each other",
        "What is a 'domain-specific testing language'?" to "A set of functions and utilities that make tests more convenient to write and read",
        "According to the author, should test code follow the same quality standards as production code?" to "Yes, with some specific exceptions for efficiency",
        "What does the author say about the relationship between dirty tests and dirty code?" to "Both B and C"
    )
}