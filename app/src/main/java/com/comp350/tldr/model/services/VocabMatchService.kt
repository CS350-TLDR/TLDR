package com.comp350.tldr.model.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import kotlin.math.abs
import kotlin.random.Random
import java.util.*

class VocabMatchService : Service() {
    private lateinit var windowManager: WindowManager
    private val cards = mutableListOf<View>()
    private val cardPairs = mutableMapOf<String, String>()

    private val sampleQuestions = listOf(
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

    private val correctMatches = mutableSetOf<String>()
    private val refreshInterval = 60000L
    private var timer: Timer? = null
    private var waitingForNextSet = false
    private val handler = Handler()
    private var gearsEarned = 0

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        startRefreshTimer()
        handler.postDelayed({ refreshCards() }, refreshInterval)
    }

    private fun startRefreshTimer() {
        timer?.cancel()
        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                Handler(mainLooper).post {
                    if (waitingForNextSet) {
                        refreshCards()
                        waitingForNextSet = false
                    }
                }
            }
        }, refreshInterval, refreshInterval)
    }

    private fun refreshCards() {
        removeAllCards()
        correctMatches.clear()
        gearsEarned = 0

        val shuffled = sampleQuestions.shuffled().take(4)
        val questions = shuffled.map { it.first }
        val answers = shuffled.map { it.second }
        val allItems = (questions + answers).shuffled()
        val screenWidth = resources.displayMetrics.widthPixels

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
            Log.e("VocabMatchService", "Error showing clear button", e)
        }
    }

    private fun createCard(label: String): View {
        val layout = FrameLayout(this).apply {
            setBackgroundColor(Color.DKGRAY)
            setPadding(8, 8, 8, 8)
            tag = label
        }

        val textView = TextView(this).apply {
            text = label
            setTextColor(Color.WHITE)
            textSize = 16f
            setPadding(24, 24, 24, 24)
        }

        val helperText = TextView(this).apply {
            text = "Drag to match"
            setTextColor(Color.LTGRAY)
            textSize = 12f
            setPadding(24, 0, 24, 8)
        }

        val container = FrameLayout(this)
        container.addView(helperText)
        container.addView(textView)
        layout.addView(container)

        val touchListener = object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
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

        layout.setOnTouchListener(touchListener)
        return layout
    }

    private fun checkForMatch(draggedCard: View) {
        val draggedText = draggedCard.tag as String
        val draggedParams = draggedCard.layoutParams as WindowManager.LayoutParams

        for (card in cards) {
            if (card == draggedCard) continue

            val targetParams = card.layoutParams as WindowManager.LayoutParams

            if (abs(draggedParams.x - targetParams.x) < 400 && abs(draggedParams.y - targetParams.y) < 200) {
                val targetText = card.tag as String
                val isMatch = cardPairs[draggedText] == targetText || cardPairs[targetText] == draggedText

                if (isMatch) {
                    draggedCard.setBackgroundColor(Color.GREEN)
                    card.setBackgroundColor(Color.GREEN)
                    correctMatches.add(draggedText)
                    correctMatches.add(targetText)
                    gearsEarned++

                    Toast.makeText(this, "Correct Match! (+1 Gear)", Toast.LENGTH_SHORT).show()

                    if (correctMatches.size == 8) {
                        showGearPopup(gearsEarned)
                        removeAllCards()
                        waitingForNextSet = true
                    }
                } else {
                    draggedCard.setBackgroundColor(Color.RED)
                    card.setBackgroundColor(Color.RED)
                    Toast.makeText(this, "Incorrect Match", Toast.LENGTH_SHORT).show()
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
        }

        val message = TextView(this).apply {
            text = "You earned $earned gears!"
            textSize = 20f
            setTextColor(Color.YELLOW)
            gravity = Gravity.CENTER
        }

        val closeButton = Button(this).apply {
            text = "Continue"
            setOnClickListener {
                try {
                    windowManager.removeView(popup)
                } catch (e: Exception) {
                    Log.e("VocabMatchService", "Error removing popup", e)
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
            Log.e("VocabMatchService", "Error showing popup", e)
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
            Log.e("VocabMatchService", "Error adding card", e)
        }
    }

    private fun removeAllCards() {
        for (card in cards) {
            try {
                windowManager.removeView(card)
            } catch (e: Exception) {
                Log.e("VocabMatchService", "Error removing card", e)
            }
        }
        cards.clear()
    }

    override fun onDestroy() {
        timer?.cancel()
        removeAllCards()
        super.onDestroy()
    }
}
