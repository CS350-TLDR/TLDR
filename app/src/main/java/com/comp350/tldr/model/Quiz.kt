package com.comp350.tldr.models

data class Question(
    val text: String,
    val options: List<String>,
    val correctAnswerIndex: Int
)

data class QuizResult(
    val score: Int,
    val totalQuestions: Int,
    val topic: String
)
