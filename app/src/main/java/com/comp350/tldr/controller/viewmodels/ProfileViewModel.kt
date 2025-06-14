package com.comp350.tldr.controller.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    private val _nickname = MutableStateFlow("")
    val nickname: StateFlow<String> = _nickname.asStateFlow()

    private val _questionsAnswered = MutableStateFlow(0)
    val questionsAnswered: StateFlow<Int> = _questionsAnswered.asStateFlow()

    private val _gears = MutableStateFlow(0)
    val gears: StateFlow<Int> = _gears.asStateFlow()

    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _hasUnlockedSunglasses = MutableStateFlow(false)
    val hasUnlockedSunglasses: StateFlow<Boolean> = _hasUnlockedSunglasses.asStateFlow()

    private val _isWearingSunglasses = MutableStateFlow(false)
    val isWearingSunglasses: StateFlow<Boolean> = _isWearingSunglasses.asStateFlow()

    private val SUNGLASSES_COST = 5

    private val PREFS_FILE = "com.comp350.tldr.preferences"
    private val PREFS_SUNGLASSES_UNLOCKED = "sunglasses_unlocked"
    private val PREFS_WEARING_SUNGLASSES = "wearing_sunglasses"

    fun loadUserData(context: Context) {
        _isLoading.value = true

        try {
            val currentUser = auth.currentUser

            if (currentUser != null) {
                val userId = currentUser.uid
                val userPrefs = context.getSharedPreferences("user_${userId}_prefs", Context.MODE_PRIVATE)
                _nickname.value = userPrefs.getString("nickname", "Academia Lord") ?: "Academia Lord"
                _questionsAnswered.value = userPrefs.getInt("questions_answered", 0)
                _gears.value = userPrefs.getInt("gears", 0)

                val appPrefs = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
                _hasUnlockedSunglasses.value = appPrefs.getBoolean(PREFS_SUNGLASSES_UNLOCKED, false)
                _isWearingSunglasses.value = appPrefs.getBoolean(PREFS_WEARING_SUNGLASSES, false)
            } else {
                val sharedPrefs = context.getSharedPreferences("tldr_prefs", Context.MODE_PRIVATE)
                _nickname.value = sharedPrefs.getString("nickname", "TLDR Player") ?: "TLDR Player"
                _questionsAnswered.value = sharedPrefs.getInt("questions_answered", 0)
                _gears.value = sharedPrefs.getInt("gears", 0)

                val appPrefs = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
                _hasUnlockedSunglasses.value = appPrefs.getBoolean(PREFS_SUNGLASSES_UNLOCKED, false)
                _isWearingSunglasses.value = appPrefs.getBoolean(PREFS_WEARING_SUNGLASSES, false)
            }
        } catch (e: Exception) {
        } finally {
            _isLoading.value = false
        }
    }

    fun toggleEditing() {
        _isEditing.value = !_isEditing.value
    }

    fun updateNickname(context: Context, newNickname: String) {
        if (newNickname.isBlank()) return

        try {
            val currentUser = auth.currentUser

            if (currentUser != null) {
                val userId = currentUser.uid
                val userPrefs = context.getSharedPreferences("user_${userId}_prefs", Context.MODE_PRIVATE)
                userPrefs.edit().putString("nickname", newNickname).apply()
            } else {
                val sharedPrefs = context.getSharedPreferences("tldr_prefs", Context.MODE_PRIVATE)
                sharedPrefs.edit().putString("nickname", newNickname).apply()
            }

            _nickname.value = newNickname
            _isEditing.value = false
        } catch (e: Exception) {
        }
    }

    fun purchaseSunglasses(context: Context) {
        if (_gears.value < SUNGLASSES_COST) return
        _isLoading.value = true

        try {
            val currentUser = auth.currentUser
            val updatedGears = _gears.value - SUNGLASSES_COST

            if (currentUser != null) {
                val userId = currentUser.uid
                val userPrefs = context.getSharedPreferences("user_${userId}_prefs", Context.MODE_PRIVATE)
                userPrefs.edit().putInt("gears", updatedGears).apply()
            } else {
                val sharedPrefs = context.getSharedPreferences("tldr_prefs", Context.MODE_PRIVATE)
                sharedPrefs.edit().putInt("gears", updatedGears).apply()
            }

            val appPrefs = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
            appPrefs.edit()
                .putBoolean(PREFS_SUNGLASSES_UNLOCKED, true)
                .putBoolean(PREFS_WEARING_SUNGLASSES, true)
                .apply()

            _gears.value = updatedGears
            _hasUnlockedSunglasses.value = true
            _isWearingSunglasses.value = true

        } catch (e: Exception) {
        } finally {
            _isLoading.value = false
        }
    }

    fun toggleWearingSunglasses(context: Context) {
        if (!_hasUnlockedSunglasses.value) return
        _isLoading.value = true

        try {
            val newWearingState = !_isWearingSunglasses.value

            val appPrefs = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
            appPrefs.edit()
                .putBoolean(PREFS_WEARING_SUNGLASSES, newWearingState)
                .apply()

            _isWearingSunglasses.value = newWearingState

        } catch (e: Exception) {
        } finally {
            _isLoading.value = false
        }
    }
}