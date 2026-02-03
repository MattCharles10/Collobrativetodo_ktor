package com.example.collaborativetodo.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.collaborativetodo.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


data class AuthState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val token: String? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    fun register(email: String, username: String, password: String) {
        viewModelScope.launch {
            _state.value = AuthState(isLoading = true)

            try {
                val result = authRepository.register(email, username, password)
                result.fold(
                    onSuccess = { userWithToken ->
                        _state.value = AuthState(
                            isSuccess = true,
                            token = userWithToken.token
                        )
                    },
                    onFailure = { error ->
                        _state.value = AuthState(
                            error = error.message ?: "Registration failed"
                        )
                    }
                )
            } catch (e: Exception) {
                _state.value = AuthState(
                    error = e.message ?: "Registration failed"
                )
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.value = AuthState(isLoading = true)

            try {
                val result = authRepository.login(email, password)
                result.fold(
                    onSuccess = { userWithToken ->
                        _state.value = AuthState(
                            isSuccess = true,
                            token = userWithToken.token
                        )
                    },
                    onFailure = { error ->
                        _state.value = AuthState(
                            error = error.message ?: "Login failed"
                        )
                    }
                )
            } catch (e: Exception) {
                _state.value = AuthState(
                    error = e.message ?: "Login failed"
                )
            }
        }
    }

    fun logout() {
        authRepository.logout()
        _state.value = AuthState()
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun resetSuccess() {
        _state.value = _state.value.copy(isSuccess = false)
    }

    fun isLoggedIn(): Boolean {
        return authRepository.isLoggedIn()
    }

    fun getCurrentUser() = authRepository.getCurrentUser()
    fun getToken() = authRepository.getToken()
}