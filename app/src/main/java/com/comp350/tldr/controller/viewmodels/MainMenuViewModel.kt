package com.comp350.tldr.controller.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import com.comp350.tldr.controllers.QuizController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import android.content.Intent


class MainMenuViewModel : ViewModel() {
    private val _topic = MutableStateFlow("Python")
    val topic: StateFlow<String> = _topic

    private val _activity = MutableStateFlow("Trivia")
    val activity: StateFlow<String> = _activity

    private val _popupEnabled = MutableStateFlow(false)
    val popupEnabled: StateFlow<Boolean> = _popupEnabled

    val topics = listOf("Python")
    val activities = listOf("Trivia", "Video", "Flashcards", "VocabMatch")

    fun setTopic(value: String) { _topic.value = value }
    fun setActivity(value: String) { _activity.value = value }

    fun togglePopup(enabled: Boolean, context: Context) {
        val quiz = QuizController(context)
        if (enabled) {
            when (_activity.value) {
                "VocabMatch" -> {
                    val intent = Intent(context, com.comp350.tldr.model.services.VocabMatchService::class.java).apply {
                        action = "START_SERVICE"
                    }
                    context.startService(intent)
                }
                else -> quiz.startPopupService(_topic.value, _activity.value)
            }
        } else {
            when (_activity.value) {
                "VocabMatch" -> {
                    val intent = Intent(context, com.comp350.tldr.model.services.VocabMatchService::class.java)
                    context.stopService(intent)
                }
                else -> quiz.stopPopupService()
            }
        }

        _popupEnabled.value = enabled
    }
}
