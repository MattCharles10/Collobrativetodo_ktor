package com.example.collaborativetodo.data.repository

import com.example.collaborativetodo.data.api.ApiService
import com.example.collaborativetodo.data.dtos.LoginRequest
import com.example.collaborativetodo.data.dtos.RegisterRequest
import com.example.collaborativetodo.data.dtos.UserWithToken
import com.example.collaborativetodo.data.local.Preferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AuthRepository(
    private val preferences: Preferences
) {
    private val authService = ApiService.createAuthService()

    suspend fun register(email: String, username: String, password: String): Result<UserWithToken> {
        return try {
            val response = authService.register(RegisterRequest(email, username, password))
            if (response.isSuccessful && response.body() != null) {
                val userWithToken = response.body()!!
                preferences.saveToken(userWithToken.token)
                preferences.saveUser(userWithToken.user)
                Result.success(userWithToken)
            } else {
                Result.failure(Exception("Registration failed: ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<UserWithToken> {
        return try {
            val response = authService.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                val userWithToken = response.body()!!
                preferences.saveToken(userWithToken.token)
                preferences.saveUser(userWithToken.user)
                Result.success(userWithToken)
            } else {
                Result.failure(Exception("Login failed: ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun validateToken(): Flow<Boolean> = flow {
        try {
            val token = preferences.getToken()
            if (token == null) {
                emit(false)
                return@flow
            }

            val response = authService.validateToken()
            emit(response.isSuccessful)
        } catch (e: Exception) {
            emit(false)
        }
    }

    fun logout() {
        kotlinx.coroutines.runBlocking {
            preferences.clear()
        }
    }

    fun getCurrentUser() = preferences.getUser()
    fun getToken() = preferences.getToken()
    fun isLoggedIn() = preferences.getToken() != null
}