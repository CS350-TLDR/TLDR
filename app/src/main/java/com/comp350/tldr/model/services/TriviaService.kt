package com.comp350.tldr.model.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.*
import android.view.*
import android.widget.*
import com.comp350.tldr.classicstuff.Question
import com.google.firebase.auth.FirebaseAuth
import java.util.*
import androidx.core.content.res.ResourcesCompat
import com.comp350.tldr.R


class TriviaService : Service() {
    private val serviceIdentifier = "TriviaService"
    private lateinit var windowManager: WindowManager
    private var floatingView: View? = null
    private var resultsView: View? = null
    private var closeButtonView: View? = null

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

    private fun getRainyHeartsFont(): Typeface? {
        return ResourcesCompat.getFont(this, R.font.rainyhearts)
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

        val quizView = createQuizView(question)
        floatingView = quizView
        addOverlay(floatingView!!)

        // Wait until the quiz layout is laid out so we can place the Close button properly
        floatingView!!.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                floatingView!!.viewTreeObserver.removeOnGlobalLayoutListener(this)

                // Now we know the height of the quiz overlay, so we can position the Close button below it
                closeButtonView = Button(this@TriviaService).apply {
                    text = "Close"
                    background = createRoundedBackground(blueColor)
                    setTextColor(Color.WHITE)
                    textSize = 20f
                    typeface = getRainyHeartsFont()
                    setOnClickListener { removeAllViews() }
                }

                // Offset the button to be placed just below the floatingView
                val location = IntArray(2)
                floatingView!!.getLocationOnScreen(location)
                val quizBottomY = location[1] + floatingView!!.height

                val params = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
                ).apply {
                    x = 0
                    y = quizBottomY + 5
                    gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                }

                windowManager.addView(closeButtonView, params)
            }
        })
    }



    private fun createQuizView(question: Question): View {
        val layout = createBaseLayout()

        layout.addView(createStatsBar())
        layout.addView(createDivider())
        layout.addView(createQuestionText(question.text))

        val radioGroup = createAnswerOptions(question.options)
        layout.addView(radioGroup)

        val submitButton = createSubmitButton(radioGroup, question)
        val submitWrapper = LinearLayout(this).apply {
            gravity = Gravity.CENTER
            addView(submitButton)
        }
        layout.addView(submitWrapper)

        return layout
    }

    private fun createBaseLayout(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 64f
            setColor(Color.parseColor("#4B89DC"))
        }

        setPadding(48, 48, 48, 48)
    }

    private fun createDivider(): View {
        return View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                2  // Height of the divider in pixels
            ).apply {
                setMargins(0, 16, 0, 16)
            }
            setBackgroundColor(Color.WHITE)
        }
    }

    private fun createStatsBar(): TextView = TextView(this).apply {
        text = "Accuracy: ${calculateAccuracy()}% | Gears: $gears"
        setTextColor(Color.WHITE)
        textSize = 22f
        typeface = getRainyHeartsFont()
        setPadding(0, 0, 0, 16)
    }

    private fun createQuestionText(text: String): TextView = TextView(this).apply {
        this.text = text
        setTextColor(Color.WHITE)
        textSize = 22f
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
                textSize = 20f
                setTextColor(Color.WHITE)
                setPadding(0, 8, 0, 8)
            }
            group.addView(button)
        }

        return group
    }

    private fun createRoundedBackground(colorHex: String): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 32f
            setColor(Color.parseColor(colorHex))
        }
    }

    private fun createSubmitButton(group: RadioGroup, question: Question): Button {
        return Button(this).apply {
            text = "Submit"
            textSize = 20f
            typeface = getRainyHeartsFont()
            background = createRoundedBackground(darkBlueColor)
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
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

                    removeCloseButton()

                    showResult(correct, question.options[question.correctAnswerIndex])
                }
            }
        }
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
        layout.addView(createDivider())

        val resultText = TextView(this).apply {
            text = if (isCorrect) "Correct! (+1 Gear)" else "Wrong!"
            setTextColor(Color.parseColor(if (isCorrect) "#4CAF50" else "#a32618"))
            textSize = 32f
            typeface = getRainyHeartsFont()
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 24)
        }

        val answerText = TextView(this).apply {
            text = "The correct answer is: $correctAnswer"
            setTextColor(Color.WHITE)
            textSize = 20f
            setPadding(0, 8, 0, 32)
        }

        val continueButton = Button(this).apply {
            text = "Continue"
            textSize = 20f
            typeface = getRainyHeartsFont()
            background = createRoundedBackground(blueColor)
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

    private fun addOverlay(view: View, yOffsetDp: Int = 0) {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
            if (yOffsetDp != 0) {
                val scale = resources.displayMetrics.density
                y = (yOffsetDp * scale).toInt()
            }
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

    private fun removeCloseButton() {
        closeButtonView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {}
        }
        closeButtonView = null
    }


    private fun removeAllViews() {
        removeFloatingView()
        removeResultView()
        removeCloseButton()
    }

    override fun onDestroy() {
        timer?.cancel()
        removeAllViews()
        super.onDestroy()
    }

    private fun <T> List<T>.randomOrNull(): T? {
        return if (isEmpty()) null else random()
    }

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

        Question("What does the author say is the main problem with comments?",
            listOf("They take up too much space", "They lie as code evolves", "They're difficult to write clearly", "They slow down program execution"), 1),
        Question("According to the author, what should you do instead of commenting bad code?",
            listOf("Add more detailed comments", "Rewrite the code", "Use better variable names", "Add more unit tests"), 1),
        Question("What type of comment does the author consider acceptable?",
            listOf("Journal comments", "Commented-out code", "Legal comments", "Closing brace comments"), 2),
        Question("Why does the author say that \"comments are always failures\"?",
            listOf("They're never read by other programmers", "They indicate a failure to express yourself in code", "They're always poorly written", "They increase technical debt"), 1),
        Question("What should you do instead of using a comment to explain confusing code?",
            listOf("Add more comments", "Make the code so clear it doesn't need comments", "Remove the code entirely", "Add a warning message"), 1),
        Question("Which of these is described as a good use for comments?",
            listOf("Explaining your thought process", "Marking sections of code", "Warning of consequences", "Summarizing complex algorithms"), 2),
        Question("What does the author recommend for TODOs?",
            listOf("Never use them", "Keep them updated and scan them regularly", "Convert them to user stories", "Only use them in development branches"), 1),
        Question("What problem does the author identify with commented-out code?",
            listOf("It makes compilation slower", "People are afraid to delete it", "It confuses version control systems", "It makes the indentation inconsistent"), 1),
        Question("What is a \"noise comment\" according to the author?",
            listOf("A comment that's too long", "A comment that states the obvious", "A comment with incorrect information", "A comment with poor grammar"), 1),
        Question("What practice should replace the need for many comments according to Clean Code?",
            listOf("Better variable naming", "More unit tests", "Code reviews", "Pair programming"), 0),

        Question("What is the main purpose of code formatting according to the author?",
            listOf("To make code smaller", "To make code run faster", "Communication", "To satisfy IDE requirements"), 2),
        Question("What is the \"newspaper metaphor\" in code formatting?",
            listOf("Code should have headlines and bylines", "Code should be black and white", "The highest level concepts should be at the top, with details below", "Each module should be on a separate page"), 2),
        Question("What does vertical openness between concepts help with?",
            listOf("Compiler optimization", "Separating unrelated concepts", "Fitting more code on screen", "Making code more colorful"), 1),
        Question("What does the author say about horizontal alignment of variable declarations?",
            listOf("It's required for good code", "It's a useful technique", "It's not useful and may highlight the wrong things", "It's only useful in certain languages"), 2),
        Question("What rule does the author suggest for indentation?",
            listOf("Use 2 spaces always", "Use 4 spaces always", "Use tabs not spaces", "The team should agree on a style and be consistent"), 3),
        Question("According to the author, how big should source files be?",
            listOf("As small as possible", "Hundreds of lines at most", "As large as needed", "At least 1000 lines"), 1),
        Question("How should related concepts be positioned in code?",
            listOf("In alphabetical order", "Vertically close to each other", "In order of complexity", "In chronological order of creation"), 1),
        Question("What is a good practice for variable declarations?",
            listOf("At the beginning of the file", "Declare them as close to their usage as possible", "In a separate file", "In alphabetical order"), 1),
        Question("Which style convention does the author present as most important?",
            listOf("Using specific numbers of spaces", "Following exact line length limits", "Consistency across the team", "Following his personal preferences"), 2),
        Question("What does the author say about the rules for formatting?",
            listOf("They should be debated extensively", "They are trivial and don't matter", "They are too important to ignore and too important to treat religiously", "They vary too much by language to be standardized"), 2),



        Question("What is the key difference between objects and data structures?",
            listOf("Objects are faster than data structures", "Objects hide data and expose behavior, data structures expose data", "Objects are for OOP, data structures for functional programming", "Objects use methods, data structures don't"), 1),
        Question("What is the Law of Demeter also known as?",
            listOf("Law of Maintainability", "Don't talk to strangers", "Principle of Least Knowledge", "The Abstraction Rule"), 2),
        Question("What is a violation of the Law of Demeter?",
            listOf("Having too many classes", "Calling methods on objects returned from other methods", "Having public variables", "Using inheritance"), 1),
        Question("What does the author call a class that's half object and half data structure?",
            listOf("A module", "A facade", "A hybrid", "An adapter"), 2),
        Question("According to the chapter, what is a DTO?",
            listOf("Data Transfer Object: a class with public variables and no functions", "Database Transaction Object", "Data Type Object", "Delegated Task Object"), 0),
        Question("What is the issue with hybrids according to the author?",
            listOf("They're too complex", "They're the worst of both worlds - hard to add functions and data structures", "They're not object-oriented enough", "They use too much memory"), 1),
        Question("What is data abstraction according to the chapter?",
            listOf("Hiding implementation behind an interface", "Creating complex data structures", "Simplifying data models", "Converting between data types"), 0),
        Question("What does the author say about the complementary nature of objects and data structures?",
            listOf("They should never be used together", "They're essentially the same thing", "Things easy for OO are hard for procedural code and vice versa", "Data structures are obsolete in modern programming"), 2),
        Question("What is an Active Record according to the chapter?",
            listOf("A special database record", "A data structure with navigational methods like save and find", "An object that actively updates itself", "A design pattern for validation"), 1),
        Question("What is the recommended approach to Active Records?",
            listOf("Never use them", "Treat them as objects with business methods", "Treat them as data structures and create separate objects with business rules", "Convert them to DTOs"), 2),



        Question("What approach does the author prefer for handling errors?",
            listOf("Return codes", "Global error flags", "Exceptions", "Logging"), 2),
        Question("What is the recommended approach to writing try-catch-finally blocks?",
            listOf("Write them last", "Avoid them completely", "Write them first", "Only use them in unit tests"), 2),
        Question("What does the author say about checked exceptions in Java?",
            listOf("They are essential for robust code", "They are worth their price", "They violate the Open/Closed Principle", "They should be used extensively"), 2),
        Question("What should exceptions provide according to the author?",
            listOf("Stack traces only", "Error codes", "Context to determine source and location of error", "Line numbers"), 2),
        Question("What is the Special Case pattern used for?",
            listOf("Handling unique errors", "Optimizing performance", "Creating custom exceptions", "Eliminating the need for special case code"), 3),
        Question("According to the author, what should you never return from methods?",
            listOf("Interfaces", "Strings", "Null", "Exceptions"), 2),
        Question("What is suggested as an alternative to returning null?",
            listOf("Return empty collections", "Throw exceptions", "Return special case objects", "Both throwing exceptions and returning special case objects"), 3),
        Question("What does the author suggest about passing null as a parameter?",
            listOf("It's a useful technique", "Always check for null parameters", "Avoid passing null whenever possible", "Only in private methods"), 2),
        Question("What problem does returning null create according to the chapter?",
            listOf("Performance issues", "It creates extra work and possible errors for callers", "Memory leaks", "It's not object-oriented"), 1),
        Question("What approach to error handling increases coupling?",
            listOf("Using exceptions", "Using the Special Case pattern", "Using null checks", "Using error codes"), 3),




        Question("What are 'boundaries' in the context of this chapter?",
            listOf("Lines between methods", "Limits on code complexity", "Interfaces between our code and third-party code", "Security measures"), 2),
        Question("What is the tension described at boundaries?",
            listOf("Between performance and readability", "Between providers who want general interfaces and users who want specific interfaces", "Between testers and developers", "Between managers and programmers"), 1),
        Question("What does the author recommend when using third-party APIs like Map?",
            listOf("Use them directly throughout your code", "Hide them behind your own interfaces", "Avoid them entirely", "Only use open-source implementations"), 1),
        Question("What are 'learning tests'?",
            listOf("Tests written to evaluate a programmer's skills", "Tests that teach programming concepts", "Tests that explore and verify our understanding of third-party APIs", "Tests that learn from user behavior"), 2),
        Question("According to the author, why are learning tests worth the effort?",
            listOf("They make you a better programmer", "They verify third-party code works as expected and flag changes in new versions", "They're required by Agile methodologies", "They increase test coverage statistics"), 1),
        Question("What pattern is suggested for code that doesn't exist yet?",
            listOf("Skip it and come back later", "Write extensive TODOs", "Define the interface you wish you had", "Use reflection"), 2),
        Question("What is an Adapter in the context of boundaries?",
            listOf("A design pattern to make incompatible interfaces work together", "A hardware component", "A test fixture", "A type of comment"), 0),
        Question("What benefit does the author mention about creating your own interface?",
            listOf("It makes your code run faster", "It gives you more control and provides a convenient seam for testing", "It impresses clients", "It lets you skip documentation"), 1),
        Question("What does the author suggest about clean boundaries?",
            listOf("They should be eliminated", "Code at boundaries needs clear separation and tests", "They should be commented extensively", "They're only important in large systems"), 1),
        Question("According to the chapter, what's better to depend on?",
            listOf("The newest libraries", "Something you control rather than something you don't", "Open-source code", "Code with the most community support"), 1),

        Question("According to the author, what enables the '-ilities' (maintainability, flexibility, etc.)?",
            listOf("Documentation", "Good architecture", "Unit tests", "Code reviews"), 2),
        Question("What does the author say is the most important aspect of clean tests?",
            listOf("Speed", "Coverage", "Reliability", "Readability"), 3),
        Question("What is the BUILD-OPERATE-CHECK pattern in tests?",
            listOf("A way to organize CI/CD pipelines", "A test naming convention", "A pattern where tests are split into sections that build data, operate on it, and check results", "A pattern for integration tests"), 2),
        Question("What does the author say about the 'one assert per test' rule?",
            listOf("It should never be violated", "It's a good guideline but not an absolute rule", "It makes tests too verbose", "It only applies to integration tests"), 1),
        Question("What does F.I.R.S.T. stand for in the context of clean tests?",
            listOf("Fast, Independent, Repeatable, Self-validating, Timely", "Functional, Integrated, Robust, Systematic, Thorough", "Focus, Isolation, Reusability, Specificity, Testing", "Fundamental, Important, Relevant, Significant, Trustworthy"), 0),
        Question("Why should tests be fast according to the chapter?",
            listOf("To impress clients", "So developers will run them frequently", "To reduce CI/CD costs", "To use less memory"), 1),
        Question("What does it mean for tests to be independent?",
            listOf("They should be written by a separate team", "They should run in separate processes", "They should not depend on each other", "They should not depend on the code they test"), 2),
        Question("What is a 'domain-specific testing language'?",
            listOf("A special programming language for tests", "A set of functions and utilities that make tests more convenient to write and read", "A formal specification language", "A way to let domain experts write tests"), 1),
        Question("According to the author, should test code follow the same quality standards as production code?",
            listOf("Yes, with some specific exceptions for efficiency", "No, test code can be messy", "Only if you have time", "Only for critical components"), 0),
        Question("What does the author say about the relationship between dirty tests and dirty code?",
            listOf("There is no relationship", "Dirty tests lead to dirty code", "Dirty code leads to dirty tests", "Both B and C"), 3),
    )
}