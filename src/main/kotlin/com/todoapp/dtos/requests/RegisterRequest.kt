package com.todoapp.dtos.requests

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val username: String,
    val password: String
)

@Serializable
data class EmailVerificationRequest(
    val email: String,
    val code: String
)

@Serializable
data class PasswordResetRequest(
    val email: String
)

@Serializable
data class PasswordResetConfirmRequest(
    val token: String,
    val newPassword: String
)

@Serializable
data class ProfileUpdateRequest(
    val username: String? = null,
    val currentPassword: String? = null,
    val newPassword: String? = null
)