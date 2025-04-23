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

    fun loadUserData(context: Context) {
        _isLoading.value = true

        try {
            val currentUser = auth.currentUser

            if (currentUser != null) {
                // Get user ID
                val userId = currentUser.uid

                // Load from SharedPreferences
                val userPrefs = context.getSharedPreferences("user_${userId}_prefs", Context.MODE_PRIVATE)

                // Load user data
                _nickname.value = userPrefs.getString("nickname", "Academia Lord") ?: "Academia Lord"
                _questionsAnswered.value = userPrefs.getInt("questions_answered", 0)
                _gears.value = userPrefs.getInt("gears", 0)
                _hasUnlockedSunglasses.value = userPrefs.getBoolean("has_unlocked_sunglasses", false)
                _isWearingSunglasses.value = userPrefs.getBoolean("is_wearing_sunglasses", false)
            } else {
                // Anonymous user or not logged in
                val sharedPrefs = context.getSharedPreferences("tldr_prefs", Context.MODE_PRIVATE)
                _nickname.value = sharedPrefs.getString("nickname", "TLDR Player") ?: "TLDR Player"
                _questionsAnswered.value = sharedPrefs.getInt("questions_answered", 0)
                _gears.value = sharedPrefs.getInt("gears", 0)
                _hasUnlockedSunglasses.value = sharedPrefs.getBoolean("has_unlocked_sunglasses", false)
                _isWearingSunglasses.value = sharedPrefs.getBoolean("is_wearing_sunglasses", false)
            }
        } catch (e: Exception) {
            // Handle errors
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
                // Logged in user
                val userId = currentUser.uid
                val userPrefs = context.getSharedPreferences("user_${userId}_prefs", Context.MODE_PRIVATE)
                userPrefs.edit().putString("nickname", newNickname).apply()
            } else {
                // Anonymous user
                val sharedPrefs = context.getSharedPreferences("tldr_prefs", Context.MODE_PRIVATE)
                sharedPrefs.edit().putString("nickname", newNickname).apply()
            }

            // Update state
            _nickname.value = newNickname
            _isEditing.value = false
        } catch (e: Exception) {
            // Handle errors
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
                userPrefs.edit()
                    .putInt("gears", updatedGears)
                    .putBoolean("has_unlocked_sunglasses", true)
                    .putBoolean("is_wearing_sunglasses", true)
                    .apply()
            } else {
                val sharedPrefs = context.getSharedPreferences("tldr_prefs", Context.MODE_PRIVATE)
                sharedPrefs.edit()
                    .putInt("gears", updatedGears)
                    .putBoolean("has_unlocked_sunglasses", true)
                    .putBoolean("is_wearing_sunglasses", true)
                    .apply()
            }

            _gears.value = updatedGears
            _hasUnlockedSunglasses.value = true
            _isWearingSunglasses.value = true

        } catch (e: Exception) {
            // Handle errors
        } finally {
            _isLoading.value = false
        }
    }

    fun toggleWearingSunglasses(context: Context) {
        if (!_hasUnlockedSunglasses.value) return
        _isLoading.value = true

        try {
            val newWearingState = !_isWearingSunglasses.value
            val currentUser = auth.currentUser

            if (currentUser != null) {
                val userId = currentUser.uid
                val userPrefs = context.getSharedPreferences("user_${userId}_prefs", Context.MODE_PRIVATE)
                userPrefs.edit()
                    .putBoolean("is_wearing_sunglasses", newWearingState)
                    .apply()
            } else {
                val sharedPrefs = context.getSharedPreferences("tldr_prefs", Context.MODE_PRIVATE)
                sharedPrefs.edit()
                    .putBoolean("is_wearing_sunglasses", newWearingState)
                    .apply()
            }

            _isWearingSunglasses.value = newWearingState

        } catch (e: Exception) {
            // Handle errors
        } finally {
            _isLoading.value = false
        }
    }
}