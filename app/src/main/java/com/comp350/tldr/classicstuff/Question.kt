package com.comp350.tldr.classicstuff

// Data class to represent a quiz question
data class Question(
    val text: String,          // The question itself
    val options: List<String>, // List of possible answers (must be 4)
    val correctAnswerIndex: Int // Index (0-3) of the correct answer in 'options'
)
