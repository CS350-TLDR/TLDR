package com.comp350.tldr.controller.viewmodels

import android.content.Context
import android.os.CountDownTimer
import android.os.Handler
import androidx.lifecycle.ViewModel
import com.comp350.tldr.controllers.QuizController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import android.content.Intent
import android.widget.Toast

class MainMenuViewModel : ViewModel() {
    private val _topic = MutableStateFlow("Python")
    val topic: StateFlow<String> = _topic

    private val _activity = MutableStateFlow("Trivia")
    val activity: StateFlow<String> = _activity

    // Interval state
    private val _interval = MutableStateFlow("1m")
    val interval: StateFlow<String> = _interval

    // Timer countdown state
    private val _timeRemaining = MutableStateFlow(60000L) // Default 1 minute
    val timeRemaining: StateFlow<Long> = _timeRemaining

    private var countdownTimer: CountDownTimer? = null

    private val _popupEnabled = MutableStateFlow(false)
    val popupEnabled: StateFlow<Boolean> = _popupEnabled

    val topics = listOf("Python")
    val activities = listOf("Trivia", "Video", "Flashcards", "VocabMatch")
    val intervals = listOf("1m", "5m", "10m", "30m", "1h", "2h")

    fun setTopic(value: String) { _topic.value = value }
    fun setActivity(value: String) { _activity.value = value }

    fun setInterval(value: String, context: Context) {
        _interval.value = value
        // Reset timer when interval changes
        if (_popupEnabled.value) {
            startCountdownTimer(getIntervalMillis(value))
            // Restart service with new interval
            togglePopup(false, context)
            togglePopup(true, context)
        }
    }

    fun togglePopup(enabled: Boolean, context: Context) {
        val intervalMs = getIntervalMillis(_interval.value)

        // Stop any existing services first regardless of enabled state
        when (_activity.value) {
            "VocabMatch" -> {
                val intent = Intent(context, com.comp350.tldr.model.services.VocabMatchService::class.java)
                context.stopService(intent)
            }
            else -> {
                val quiz = QuizController(context)
                quiz.stopPopupService()
            }
        }

        // Small delay to ensure service is fully stopped
        Handler().postDelayed({
            if (enabled) {
                when (_activity.value) {
                    "VocabMatch" -> {
                        val intent = Intent(context, com.comp350.tldr.model.services.VocabMatchService::class.java).apply {
                            action = "START_SERVICE"
                            putExtra("interval", intervalMs)
                        }
                        context.startService(intent)
                        Toast.makeText(context, "VocabMatch service started with interval: ${formatIntervalForDisplay(intervalMs)}", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        val quiz = QuizController(context)
                        quiz.startPopupService(_topic.value, _activity.value, intervalMs)
                        Toast.makeText(context, "${_activity.value} service started with interval: ${formatIntervalForDisplay(intervalMs)}", Toast.LENGTH_SHORT).show()
                    }
                }

                // Start the countdown timer
                startCountdownTimer(intervalMs)
            }

            _popupEnabled.value = enabled
        }, 100) // Small delay of 100ms
    }

    // Convert interval string to milliseconds
    private fun getIntervalMillis(interval: String): Long {
        return when (interval) {
            "1m" -> 60000L
            "5m" -> 300000L
            "10m" -> 600000L
            "30m" -> 1800000L
            "1h" -> 3600000L
            "2h" -> 7200000L
            else -> 60000L // Default to 1 minute
        }
    }

    // Format interval for display
    private fun formatIntervalForDisplay(intervalMs: Long): String {
        return when (intervalMs) {
            60000L -> "1 minute"
            300000L -> "5 minutes"
            600000L -> "10 minutes"
            1800000L -> "30 minutes"
            3600000L -> "1 hour"
            7200000L -> "2 hours"
            else -> "${intervalMs / 60000} minutes"
        }
    }

    private fun startCountdownTimer(intervalMs: Long) {
        stopCountdownTimer() // Cancel any existing timer

        _timeRemaining.value = intervalMs

        countdownTimer = object : CountDownTimer(intervalMs, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _timeRemaining.value = millisUntilFinished
            }

            override fun onFinish() {
                // Just reset the timer after it finishes
                // We don't need to trigger the popup here as the service handles that
                startCountdownTimer(intervalMs)
            }
        }.start()
    }

    private fun stopCountdownTimer() {
        countdownTimer?.cancel()
        countdownTimer = null
    }

    override fun onCleared() {
        super.onCleared()
        stopCountdownTimer()
    }
}