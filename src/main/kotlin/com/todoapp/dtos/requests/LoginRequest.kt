package com.todoapp.dtos.requests

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class TokenValidationRequest(
    val token: String
)

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)