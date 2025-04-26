package com.comp350.tldr.controller.viewmodels

import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import androidx.lifecycle.ViewModel
import com.comp350.tldr.controllers.QuizController
import com.comp350.tldr.controllers.DailyStreakManager
import com.comp350.tldr.controllers.UserController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainMenuViewModel : ViewModel() {
    private val _topic = MutableStateFlow("Python")
    val topic: StateFlow<String> = _topic

    private val _activity = MutableStateFlow("Trivia")
    val activity: StateFlow<String> = _activity

    private val _interval = MutableStateFlow("1m")
    val interval: StateFlow<String> = _interval

    private val _timeRemaining = MutableStateFlow(60000L)
    val timeRemaining: StateFlow<Long> = _timeRemaining

    private val _streak = MutableStateFlow(0)
    val streak: StateFlow<Int> = _streak

    private val _isWearingSunglasses = MutableStateFlow(false)
    val isWearingSunglasses: StateFlow<Boolean> = _isWearingSunglasses

    private val _hasUnlockedSunglasses = MutableStateFlow(false)
    val hasUnlockedSunglasses: StateFlow<Boolean> = _hasUnlockedSunglasses

    private var countdownTimer: CountDownTimer? = null

    private val _popupEnabled = MutableStateFlow(false)
    val popupEnabled: StateFlow<Boolean> = _popupEnabled

    val topics = listOf("Python")
    val activities = listOf("Trivia", "Video", "Flashcards", "VocabMatch", "Random")
    val intervals = listOf("1m", "5m", "10m", "30m", "1h", "2h")

    private var streakManager: DailyStreakManager? = null

    private val PREFS_FILE = "com.comp350.tldr.preferences"
    private val PREFS_SUNGLASSES_UNLOCKED = "sunglasses_unlocked"
    private val PREFS_WEARING_SUNGLASSES = "wearing_sunglasses"

    fun loadUserData(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
        _hasUnlockedSunglasses.value = prefs.getBoolean(PREFS_SUNGLASSES_UNLOCKED, false)
        _isWearingSunglasses.value = prefs.getBoolean(PREFS_WEARING_SUNGLASSES, false)
        Log.d("MainMenuViewModel", "Loaded sunglasses data - unlocked: ${_hasUnlockedSunglasses.value}, wearing: ${_isWearingSunglasses.value}")
    }

    fun initStreakManager(context: Context) {
        streakManager = DailyStreakManager(context)
        _streak.value = streakManager?.getCurrentStreak() ?: 0
    }

    fun checkDailyStreak(context: Context, onReward: (Int) -> Unit) {
        val manager = streakManager ?: return
        manager.checkAndUpdateStreak { newStreak, reward ->
            _streak.value = newStreak
            if (reward > 0) {
                awardStreakReward(context, reward)
                onReward(reward)
            }
        }
    }

    private fun awardStreakReward(context: Context, reward: Int) {
        val userController = UserController(context)
        val currentUser = userController.getCurrentUser()
        if (currentUser != null) {
            val updatedGears = currentUser.gears + reward
            userController.updateGears(updatedGears)
            Log.d("MainMenuViewModel", "Updated gears: $updatedGears (+$reward)")
        }
    }

    fun setTopic(value: String) { _topic.value = value }

    fun setActivity(value: String) { _activity.value = value }

    fun setInterval(value: String, context: Context) {
        _interval.value = value
        if (_popupEnabled.value) {
            startCountdownTimer(getIntervalMillis(value))
            togglePopup(false, context)
            togglePopup(true, context)
        }
    }

    fun togglePopup(enabled: Boolean, context: Context) {
        if (streakManager == null) {
            initStreakManager(context)
        }
        val intervalMs = getIntervalMillis(_interval.value)

        when (_activity.value) {
            "VocabMatch" -> {
                val intent = Intent(context, com.comp350.tldr.model.services.VocabMatchService::class.java)
                context.stopService(intent)
            }
            "Random" -> {
                val intent = Intent(context, com.comp350.tldr.model.services.RandomService::class.java)
                context.stopService(intent)
            }
            else -> {
                val quiz = QuizController(context)
                quiz.stopPopupService()
            }
        }

        Handler().postDelayed({
            if (enabled) {
                when (_activity.value) {
                    "VocabMatch" -> {
                        val intent = Intent(context, com.comp350.tldr.model.services.VocabMatchService::class.java).apply {
                            action = "START_SERVICE"
                            putExtra("interval", intervalMs)
                        }
                        context.startService(intent)
                    }
                    "Random" -> {
                        val intent = Intent(context, com.comp350.tldr.model.services.RandomService::class.java).apply {
                            action = "START_SERVICE"
                            putExtra("interval", intervalMs)
                        }
                        context.startService(intent)
                    }
                    else -> {
                        val quiz = QuizController(context)
                        quiz.startPopupService(_topic.value, _activity.value, intervalMs)
                    }
                }
                startCountdownTimer(intervalMs)
            }
            _popupEnabled.value = enabled
        }, 100)
    }

    private fun getIntervalMillis(interval: String): Long {
        return when (interval) {
            "1m" -> 60000L
            "5m" -> 300000L
            "10m" -> 600000L
            "30m" -> 1800000L
            "1h" -> 3600000L
            "2h" -> 7200000L
            else -> 60000L
        }
    }

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
        stopCountdownTimer()
        _timeRemaining.value = intervalMs
        countdownTimer = object : CountDownTimer(intervalMs, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _timeRemaining.value = millisUntilFinished
            }
            override fun onFinish() {
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