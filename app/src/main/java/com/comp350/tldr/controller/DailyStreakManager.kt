package com.comp350.tldr.controllers

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

class DailyStreakManager(private val context: Context) {
    private val auth = FirebaseAuth.getInstance()

    fun checkAndUpdateStreak(onComplete: (Int, Int) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        val userPrefs = context.getSharedPreferences("user_${userId}_prefs", Context.MODE_PRIVATE)

        val currentDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val lastLoginDate = userPrefs.getString("last_login_date", "")
        val currentStreak = userPrefs.getInt("current_streak", 0)

        val calendar = Calendar.getInstance()
        val yesterday = calendar.apply { add(Calendar.DAY_OF_YEAR, -1) }
        val yesterdayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(yesterday.time)

        val editor = userPrefs.edit()

        if (lastLoginDate.isNullOrEmpty() || lastLoginDate == currentDateStr) {
            editor.putString("last_login_date", currentDateStr)
            if (lastLoginDate.isNullOrEmpty()) {
                editor.putInt("current_streak", 1)
                onComplete(1, calculateReward(1))
            } else {
                // Already logged in today, no changes
                onComplete(currentStreak, 0)
            }
        } else if (lastLoginDate == yesterdayStr) {
            
            val newStreak = currentStreak + 1
            editor.putInt("current_streak", newStreak)
            editor.putString("last_login_date", currentDateStr)
            onComplete(newStreak, calculateReward(newStreak))
        } else {
          
            editor.putInt("current_streak", 1)
            editor.putString("last_login_date", currentDateStr)
            onComplete(1, calculateReward(1))
        }

        editor.apply()
    }

    fun getCurrentStreak(): Int {
        val userId = auth.currentUser?.uid ?: return 0
        val userPrefs = context.getSharedPreferences("user_${userId}_prefs", Context.MODE_PRIVATE)
        return userPrefs.getInt("current_streak", 0)
    }

    private fun calculateReward(streakCount: Int): Int {
        return 5 + (streakCount * 5)
    }

    fun awardDailyGearsIfNeeded() {
        val userId = auth.currentUser?.uid ?: return
        val userPrefs = context.getSharedPreferences("user_${userId}_prefs", Context.MODE_PRIVATE)

        val currentDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val lastRewardDate = userPrefs.getString("last_reward_date", "")

        if (lastRewardDate != currentDateStr) {
            checkAndUpdateStreak { streak, reward ->
                if (reward > 0) {
                    // Award gears
                    val currentGears = userPrefs.getInt("gears", 0)
                    userPrefs.edit()
                        .putInt("gears", currentGears + reward)
                        .putString("last_reward_date", currentDateStr)
                        .apply()
                }
            }
        }
    }
}
