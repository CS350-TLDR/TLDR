package com.comp350.tldr.controller.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import com.comp350.tldr.controllers.QuizController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainMenuViewModel : ViewModel() {
    private val _selectedTopic = MutableStateFlow("Python")
    val selectedTopic: StateFlow<String> = _selectedTopic

    private val _selectedActivity = MutableStateFlow("Trivia")
    val selectedActivity: StateFlow<String> = _selectedActivity

    private val _isPopupEnabled = MutableStateFlow(false)
    val isPopupEnabled: StateFlow<Boolean> = _isPopupEnabled

    // Available options
    val availableTopics = listOf("Python", "Java", "JavaScript", "C++")
    val availableActivities = listOf("Trivia", "Video", "FlashCard")

    fun updateTopic(topic: String) {
        _selectedTopic.value = topic
    }

    fun updateActivity(activity: String) {
        _selectedActivity.value = activity
    }

    fun togglePopupService(enabled: Boolean, context: Context) {
        val quizController = QuizController(context)

        if (enabled) {
            quizController.startPopupService(_selectedTopic.value, _selectedActivity.value)
            _isPopupEnabled.value = true
        } else {
            quizController.stopPopupService()
            _isPopupEnabled.value = false
        }
    }

    fun testPopup(context: Context) {
        val quizController = QuizController(context)
        quizController.startPopupService(_selectedTopic.value, _selectedActivity.value, testMode = true)
    }

    private val _isTopicExpanded = MutableStateFlow(false)
    val isTopicExpanded: StateFlow<Boolean> = _isTopicExpanded

    private val _isActivityExpanded = MutableStateFlow(false)
    val isActivityExpanded: StateFlow<Boolean> = _isActivityExpanded

    fun toggleTopicDropdown() {
        _isTopicExpanded.value = !_isTopicExpanded.value
    }

    fun toggleActivityDropdown() {
        _isActivityExpanded.value = !_isActivityExpanded.value
    }
}