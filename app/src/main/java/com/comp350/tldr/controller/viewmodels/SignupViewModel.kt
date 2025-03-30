package com.comp350.tldr.controller.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import com.comp350.tldr.controllers.UserController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SignupViewModel : ViewModel() {
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun updateEmail(newEmail: String) {
        _email.value = newEmail
    }

    fun updatePassword(newPassword: String) {
        _password.value = newPassword
    }

    fun updateConfirmPassword(newConfirmPassword: String) {
        _confirmPassword.value = newConfirmPassword
    }

    suspend fun signup(context: Context, onComplete: (Boolean) -> Unit) {
        // Validate inputs
        when {
            _email.value.isBlank() || _password.value.isBlank() || _confirmPassword.value.isBlank() -> {
                _errorMessage.value = "All fields are required"
                onComplete(false)
                return
            }
            _password.value != _confirmPassword.value -> {
                _errorMessage.value = "Passwords do not match"
                onComplete(false)
                return
            }
            _password.value.length < 6 -> {
                _errorMessage.value = "Password must be at least 6 characters"
                onComplete(false)
                return
            }
        }

        _isLoading.value = true
        _errorMessage.value = null

        try {
            val userController = UserController(context)
            val result = userController.signUp(_email.value, _password.value)

            if (result.isSuccess) {
                onComplete(true)
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Signup failed"
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