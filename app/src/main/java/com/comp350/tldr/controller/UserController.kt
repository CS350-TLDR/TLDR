package com.comp350.tldr.controllers

import com.comp350.tldr.models.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import android.content.Context

class UserController(private val context: Context) {
    private val auth = FirebaseAuth.getInstance()

    fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser ?: return null

        // Get user-specific preferences
        val userId = firebaseUser.uid
        val userPrefs = context.getSharedPreferences("user_${userId}_prefs", Context.MODE_PRIVATE)
        val gears = userPrefs.getInt("gears", 0)

        return User(
            id = userId,
            email = firebaseUser.email ?: "",
            gears = gears
        )
    }

    suspend fun signIn(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val user = getCurrentUser() ?: throw Exception("Failed to get user data")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUp(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val user = getCurrentUser() ?: throw Exception("Failed to get user data")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }

    fun updateGears(newGears: Int): Boolean {
        val user = getCurrentUser() ?: return false

        return try {
            val userPrefs = context.getSharedPreferences("user_${user.id}_prefs", Context.MODE_PRIVATE)
            userPrefs.edit().putInt("gears", newGears).apply()
            true
        } catch (e: Exception) {
            false
        }
    }



}
