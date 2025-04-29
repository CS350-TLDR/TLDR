package com.comp350.tldr.model.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.*
import android.view.*
import android.widget.*
import com.comp350.tldr.classicstuff.Question
import com.google.firebase.auth.FirebaseAuth
import java.util.*

class TriviaService : Service() {
    private val serviceIdentifier = "TriviaService"
    private lateinit var windowManager: WindowManager
    private var floatingView: View? = null
    private var resultsView: View? = null
    private var timer: Timer? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPrefs: android.content.SharedPreferences

    private var currentTopic = "Python"
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

    private val cleanCodeQuestions = listOf(
        Question("Why does the author argue that we will never be rid of code?",
            listOf("Code is outdated technology", "Requirements can never be abstracted", "Code specifies requirements in executable detail", "Business people can't write code"), 2),
        Question("What common programmer experience is described as \"wading\"?",
            listOf("Debugging hardware issues", "Struggling through bad code", "Brainstorming ideas", "Designing UI/UX"), 1),
        Question("What does LeBlanc's Law state?",
            listOf("Bugs are inevitable", "Later equals never", "Clean code is slow code", "Deadlines overrule quality"), 1),
        Question("What is a major consequence of a messy codebase over time?",
            listOf("Lower memory usage", "Fewer bugs", "Decreasing productivity", "Better performance"), 2),
        Question("What typically triggers a \"Grand Redesign in the Sky\"?",
            listOf("Managerial demand for innovation", "Budget overflow", "Developer frustration with messy code", "A new CEO"), 2),
        Question("What quality does Michael Feathers say defines clean code?",
            listOf("It's fast to write", "It looks like someone cared", "It avoids using functions", "It uses the latest framework"), 1),
        Question("What metaphor do Dave Thomas and Andy Hunt use to describe messy code?",
            listOf("A house of cards", "Broken windows", "A tangled web", "A leaking faucet"), 1),
        Question("According to Ron Jeffries, what is a key sign of clean code?",
            listOf("It uses long variable names", "It has no comments", "It minimizes duplication", "It's optimized for performance"), 2),
        Question("What does the \"Boy Scout Rule\" in programming advocate for?",
            listOf("Code must be rewritten monthly", "Add at least one new feature per commit", "Always improve the code you touch", "Avoid touching old code"), 2),

        Question("What analogy does the author use to describe the path to writing clean code?",
            listOf("A puzzle", "A paint-by-numbers kit", "Painting a picture", "Baking a cake"), 2),
            Question("What is the primary purpose of a name in code?",
                listOf("To shorten the code", "To reveal the intent of the variable, function, or class", "To confuse other programmers", "To pass compiler checks"), 1),

            Question("What should you do if a name requires a comment to explain it?",
                listOf("Keep it as is", "Shorten the name", "Rename it to reveal intent", "Add more comments instead"), 2),

            Question("What term describes names that suggest false meanings?",
                listOf("Disinformation", "Redirection", "Inference", "Compression"), 0),

            Question("Why are single-letter names generally discouraged?",
                listOf("They are difficult to type", "They are not searchable or meaningful", "They take up too much space", "They slow down compilation"), 1),

            Question("What is a \"noise word\" in a name?",
                listOf("A word that makes the name funnier", "A redundant or meaningless addition to the name", "A technical term that describes functionality", "A requirement in Java programming"), 1),

            Question("Which of the following is a better practice?",
                listOf("Using m_ prefixes for member variables", "Using Hungarian Notation in modern code", "Naming classes with noun phrases", "Naming classes with verbs"), 2),

            Question("What is the \"Boy Scout Rule\" applied to naming?",
                listOf("Always add a joke to every name", "Leave names cleaner and more understandable than you found them", "Encode types into every name", "Keep names short at all costs"), 1),

            Question("When should single-letter variables like i, j, or k be used?",
                listOf("Always, to save space", "Only in small local scopes, like short loops", "Never, under any circumstances", "In function names"), 1),

            Question("What naming mistake leads to mental mapping problems?",
                listOf("Long, descriptive names", "Using single-letter names that require extra mental translation", "Using consistent technical terms", "Using searchable constants"), 1),

            Question("When should you use problem domain names?",
                listOf("When no technical term exists for the concept", "To impress managers", "To make code unreadable to other developers", "To encode types and scopes"), 0),

                    Question("What is the first and most important rule for writing functions?",
        listOf("They must be highly optimized", "They should be long and detailed", "They should be small", "They must use recursion"), 2),

    Question("What does \"Do One Thing\" mean for functions?",
    listOf("Functions should only use one data type", "Functions should handle both setup and teardown", "Functions should focus on a single task or responsibility", "Functions should process one line of code"), 2),

    Question("What is the Stepdown Rule?",
    listOf("Each function should be lower performance than the one before", "Code should read top-to-bottom, dropping one level of abstraction at a time", "Code should be indented as much as possible", "Every function must contain a loop"), 1),

    Question("What should you do instead of using switch statements across your code?",
    listOf("Ignore them", "Use polymorphism to replace them", "Expand them with more cases", "Always return error codes"), 1),

    Question("How many arguments should a function ideally have?",
    listOf("Three or more", "One or two at most", "Exactly four", "As many as needed"), 1),

    Question("Why are flag arguments considered bad practice?",
    listOf("They make code faster", "They make code less readable and imply multiple behaviors", "They save memory", "They improve compile times"), 1),

    Question("What is a dangerous side effect of a function?",
    listOf("Changing unrelated system state without clear intention", "Writing to a log file", "Printing to the console", "Adding extra whitespace"), 0),

    Question("When should you prefer exceptions over returning error codes?",
    listOf("Only for fatal errors", "When you want to separate happy path logic from error handling", "When you want faster performance", "When memory usage is a concern"), 1),

    Question("What principle does \"Don't Repeat Yourself\" (DRY) target?",
    listOf("Reducing function arguments", "Preventing duplicate logic in the codebase", "Increasing error handling", "Adding more abstraction layers"), 1),

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
            "SHOW_NOW" -> showRandomQuiz()
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
                        showRandomQuiz()
                    } catch (e: Exception) {
                    }
                }
            }
        }, 0)

        timer?.schedule(object : TimerTask() {
            override fun run() {
                Handler(Looper.getMainLooper()).post {
                    try {
                        showRandomQuiz()
                    } catch (e: Exception) {
                    }
                }
            }
        }, intervalMs, intervalMs)
    }

    private fun getQuestionsForCurrentTopic(): List<Question> {
        return when (currentTopic) {
            "Clean Code" -> cleanCodeQuestions
            else -> pythonQuestions
        }
    }

    private fun showRandomQuiz() {
        val questions = getQuestionsForCurrentTopic()
        val question = questions.randomOrNull() ?: return
        removeAllViews()
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
        setBackgroundColor(Color.BLACK)
        setPadding(24, 24, 24, 24)
    }

    private fun createStatsBar(): TextView = TextView(this).apply {
        text = "Accuracy: ${calculateAccuracy()}% | Gears: $gears"
        setTextColor(Color.WHITE)
        textSize = 16f
        setPadding(0, 0, 0, 16)
    }

    private fun createQuestionText(text: String): TextView = TextView(this).apply {
        this.text = text
        setTextColor(Color.WHITE)
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
                setTextColor(Color.WHITE)
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
            setBackgroundColor(Color.parseColor(darkBlueColor))
            setTextColor(Color.WHITE)
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
            setBackgroundColor(Color.parseColor(blueColor))
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = 8
            }
            setOnClickListener { removeAllViews() }
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
            setTextColor(Color.parseColor(if (isCorrect) "#4CAF50" else "#F44336"))
            textSize = 24f
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 24)
        }

        val answerText = TextView(this).apply {
            text = "The correct answer is: $correctAnswer"
            setTextColor(Color.WHITE)
            textSize = 18f
            setPadding(0, 8, 0, 32)
        }

        val continueButton = Button(this).apply {
            text = "Continue"
            setBackgroundColor(Color.parseColor(blueColor))
            setTextColor(Color.WHITE)
            setOnClickListener { removeResultView() }
        }

        layout.apply {
            addView(resultText)
            addView(answerText)
            addView(continueButton)
        }

        return layout
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

    private fun removeAllViews() {
        removeFloatingView()
        removeResultView()
    }

    override fun onDestroy() {
        timer?.cancel()
        removeAllViews()
        super.onDestroy()
    }

    private fun <T> List<T>.randomOrNull(): T? {
        return if (isEmpty()) null else random()
    }
}