package com.comp350.tldr.controllers

import android.content.Context
import android.content.Intent
import com.comp350.tldr.models.Question
import com.comp350.tldr.models.QuizResult
import com.comp350.tldr.model.services.PopQuizService

class QuizController(private val context: Context) {
    private val userController = UserController(context)

    // Question repositories
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
        // Additional questions from the original code
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

    fun getQuestionsByTopic(topic: String): List<Question> {
        return when (topic.lowercase()) {
            "python" -> pythonQuestions
            else -> emptyList()
        }
    }

    fun getRandomQuestion(topic: String): Question? {
        val questions = getQuestionsByTopic(topic)
        if (questions.isEmpty()) return null

        val randomIndex = kotlin.random.Random.nextInt(questions.size)
        return questions[randomIndex]
    }

    fun checkAnswer(question: Question, selectedAnswerIndex: Int): Boolean {
        return selectedAnswerIndex == question.correctAnswerIndex
    }

    fun startPopupService(topic: String, activity: String, testMode: Boolean = false) {
        val intent = Intent(context, PopQuizService::class.java).apply {
            putExtra("topic", topic)
            putExtra("activity", activity)
            putExtra("interval", 60000L) // 60 seconds
            putExtra("test_mode", testMode)
            action = "START_SERVICE"
        }
        context.startService(intent)
    }

    fun stopPopupService() {
        val intent = Intent(context, PopQuizService::class.java).apply {
            action = "STOP_SERVICE"
        }
        context.startService(intent)
    }

    fun awardGears(amount: Int = 1): Boolean {
        val user = userController.getCurrentUser() ?: return false
        return userController.updateGears(user.gears + amount)
    }
}