package com.comp350.tldr.models

data class Topic(
    val id: String,
    val name: String,
    val description: String,
    val activities: List<Activity>
)

data class Activity(
    val id: String,
    val name: String,
    val type: ActivityType
)

enum class ActivityType {
    TRIVIA,
    VIDEO
}