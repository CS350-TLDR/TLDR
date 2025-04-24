package com.comp350.tldr.controller.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import com.comp350.tldr.controllers.UserController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LoginViewModel : ViewModel() {
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _savedEmails = MutableStateFlow<List<String>>(emptyList())
    val savedEmails: StateFlow<List<String>> = _savedEmails

    fun updateEmail(newEmail: String) {
        _email.value = newEmail
    }

    fun updatePassword(newPassword: String) {
        _password.value = newPassword
    }

    fun loadSavedEmails(context: Context) {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val emails = prefs.getStringSet("saved_emails", emptySet())?.toList() ?: emptyList()
        _savedEmails.value = emails
    }

    fun saveEmail(context: Context, email: String) {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val currentSet = prefs.getStringSet("saved_emails", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        currentSet.add(email)
        editor.putStringSet("saved_emails", currentSet)
        editor.apply()
    }

    suspend fun login(context: Context, onComplete: (Boolean) -> Unit) {
        _isLoading.value = true
        _errorMessage.value = null

        try {
            val userController = UserController(context)
            val result = userController.signIn(_email.value, _password.value)

            if (result.isSuccess) {
                saveEmail(context, _email.value) // Save email on successful login
                onComplete(true)
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Login failed"
                onComplete(false)
            }
        } catch (e: Exception) {
            _errorMessage.value = e.message ?: "Unknown error"
            onComplete(false)
        } finally {
            _isLoading.value = false
        }
    }
}
