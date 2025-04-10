package com.comp350.tldr.models

data class Question(
    val text: String,
    val options: List<String>,
    val correctAnswerIndex: Int
)

