package com.comp350.tldr

// Data class to represent a quiz question
data class Lesson(
    val text: String,          // The lesson info itself
    val otherTextRows: List<String>, // Easy formating
    val textIndex: Int // Future Formatting
)
