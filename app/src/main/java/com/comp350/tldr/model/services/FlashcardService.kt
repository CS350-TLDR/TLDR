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
import androidx.core.content.res.ResourcesCompat
import com.comp350.tldr.R
import com.comp350.tldr.classicstuff.Question
import com.google.firebase.auth.FirebaseAuth
import java.util.*

class FlashcardService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPrefs: android.content.SharedPreferences
    private var backgroundView: View? = null
    private var flashcardView: View? = null
    private var arrowContainer: View? = null
    private var clearButtonView: View? = null
    private var topicTextView: View? = null
    private var currentTopic = "Python"
    private var currentQuestionIndex = 0
    private var currentQuestions: List<Question> = emptyList()
    private var timer: Timer? = null
    private var intervalMs: Long = 60000
    private var pixelFont: Typeface? = null

    // Window dimensions scaled to 2/3 of original size
    private val windowWidth = 693  // Original was 1040
    private val windowHeight = 587 // Original was 880

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        auth = FirebaseAuth.getInstance()
        sharedPrefs = getSharedPreferences("tldr_prefs", Context.MODE_PRIVATE)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        pixelFont = ResourcesCompat.getFont(this, R.font.rainyhearts)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent ?: return START_NOT_STICKY
        if (intent.action == "STOP_SERVICE") {
            stopSelf()
            return START_NOT_STICKY
        }
        currentTopic = intent.getStringExtra("topic") ?: "Python"
        intervalMs = intent.getLongExtra("interval", 60000)
        startFlashcardCycle()
        return START_NOT_STICKY
    }

    private fun startFlashcardCycle() {
        timer?.cancel()
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                Handler(Looper.getMainLooper()).post {
                    currentQuestionIndex = 0
                    loadNewFlashcards()
                }
            }
        }, 0, intervalMs)
    }

    private fun loadNewFlashcards() {
        val allQuestions = getQuestionsForCurrentTopic().shuffled()
        currentQuestions = allQuestions.take(5)
        removeAllViews()
        showFlashcardOverlay()
    }

    private fun showFlashcardOverlay() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#2E2E2E"))
                cornerRadius = 40f  // Scaled from 60f
            }
            layoutParams = LinearLayout.LayoutParams(windowWidth, windowHeight)
            setPadding(0, 20, 0, 20)  // Scaled from 30
        }

        val topic = TextView(this).apply {
            text = "Topic: $currentTopic"
            setTextColor(Color.WHITE)
            textSize = 19f  // Scaled from 28f
            gravity = Gravity.CENTER
            typeface = pixelFont
            setPadding(0, 7, 0, 20)  // Scaled from 10, 0, 30
        }
        layout.addView(topic)
        topicTextView = topic

        val flashcard = createFlashcardView(currentQuestionIndex)
        layout.addView(flashcard)
        flashcardView = flashcard

        val arrows = createNavigationArrows()
        layout.addView(arrows)

        // Add Clear button at the bottom of the main layout
        val clearButton = Button(this).apply {
            text = "Clear"
            textSize = 11f  // Scaled from 16f
            typeface = pixelFont
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply {
                setColor(Color.DKGRAY)
                cornerRadius = 27f  // Scaled from 40f
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
                topMargin = 20  // Add some space above the button
            }
            setOnClickListener { removeAllViews() }
        }
        layout.addView(clearButton)
        clearButtonView = clearButton

        // Add drag capability
        setupDragBehavior(layout)

        val params = WindowManager.LayoutParams(
            windowWidth,
            WindowManager.LayoutParams.WRAP_CONTENT,  // Allow height to adjust to content
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = 100
        }

        windowManager.addView(layout, params)
        backgroundView = layout
    }

    private fun setupDragBehavior(view: View) {
        var lastX = 0
        var lastY = 0

        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastX = event.rawX.toInt()
                    lastY = event.rawY.toInt()
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val params = v.layoutParams as WindowManager.LayoutParams
                    val dx = event.rawX.toInt() - lastX
                    val dy = event.rawY.toInt() - lastY

                    params.x += dx
                    params.y += dy

                    lastX = event.rawX.toInt()
                    lastY = event.rawY.toInt()

                    windowManager.updateViewLayout(v, params)
                    true
                }
                else -> false
            }
        }
    }

    private fun createFlashcardView(index: Int): View {
        val question = currentQuestions[index % currentQuestions.size]

        // Scale card size to 2/3 of original
        val cardWidth = 653  // Scaled from 980
        val cardHeight = 373 // Scaled from 560

        val card = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(cardWidth, cardHeight)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#4B89DC"))
                cornerRadius = 27f  // Scaled from 40f
                setStroke(3, Color.DKGRAY)  // Scaled from 4
            }
        }

        val cardFront = TextView(this).apply {
            text = question.text
            setTextColor(Color.BLACK)
            textSize = 15f  // Scaled from 22f
            gravity = Gravity.CENTER
            setPadding(40, 27, 40, 27)  // Scaled from 60, 40, 60, 40
        }

        val cardBack = TextView(this).apply {
            text = "Answer: ${question.options[question.correctAnswerIndex]}"
            setTextColor(Color.BLACK)
            textSize = 15f  // Scaled from 22f
            gravity = Gravity.CENTER
            setPadding(40, 27, 40, 27)  // Scaled from 60, 40, 60, 40
            visibility = View.GONE
        }

        card.addView(cardFront)
        card.addView(cardBack)

        card.setOnClickListener {
            val showingFront = cardFront.visibility == View.VISIBLE
            cardFront.visibility = if (showingFront) View.GONE else View.VISIBLE
            cardBack.visibility = if (showingFront) View.VISIBLE else View.GONE
        }

        return card
    }

    private fun createNavigationArrows(): View {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        val buttonParams = LinearLayout.LayoutParams(147, 93).apply {  // Scaled from 220, 140
            setMargins(27, 13, 27, 13)  // Scaled from 40, 20, 40, 20
        }

        val leftArrow = Button(this).apply {
            text = "←"
            textSize = 28f  // Scaled from 42f
            typeface = pixelFont
            layoutParams = buttonParams
            setPadding(0, -23, 0, 0)  // Scaled from 0, -35, 0, 0
            gravity = Gravity.CENTER
            setOnClickListener {
                currentQuestionIndex = (currentQuestionIndex - 1 + currentQuestions.size) % currentQuestions.size
                (backgroundView as? LinearLayout)?.removeView(flashcardView)
                flashcardView = createFlashcardView(currentQuestionIndex)
                (backgroundView as? LinearLayout)?.addView(flashcardView, 1)
            }
        }

        val rightArrow = Button(this).apply {
            text = "→"
            textSize = 28f  // Scaled from 42f
            typeface = pixelFont
            layoutParams = buttonParams
            setPadding(0, -23, 0, 0)  // Scaled from 0, -35, 0, 0
            gravity = Gravity.CENTER
            setOnClickListener {
                currentQuestionIndex = (currentQuestionIndex + 1) % currentQuestions.size
                (backgroundView as? LinearLayout)?.removeView(flashcardView)
                flashcardView = createFlashcardView(currentQuestionIndex)
                (backgroundView as? LinearLayout)?.addView(flashcardView, 1)
            }
        }

        container.addView(leftArrow)
        container.addView(rightArrow)
        arrowContainer = container
        return container
    }

    private fun removeAllViews() {
        backgroundView?.let { try { windowManager.removeView(it) } catch (_: Exception) {} }
        backgroundView = null
        flashcardView = null
        arrowContainer = null
        topicTextView = null
        clearButtonView = null
    }

    override fun onDestroy() {
        timer?.cancel()
        removeAllViews()
        super.onDestroy()
    }

    private fun getQuestionsForCurrentTopic(): List<Question> {
        return if (currentTopic == "Clean Code") cleanCodeQuestions else pythonQuestions
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

        // Chapter 4: Comments
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

        // Chapter 5: Formatting
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

        // Chapter 6: Objects and Data Structures
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

        // Chapter 7: Error Handling
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

        // Chapter 8: Boundaries
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

        // Chapter 9: Unit Tests
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
            listOf("There is no relationship", "Dirty tests lead to dirty code", "Dirty code leads to dirty tests", "Both B and C"), 3)
    )
}