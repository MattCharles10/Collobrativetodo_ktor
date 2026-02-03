package com.example.collaborativetodo.data.api


import com.example.collaborativetodo.data.dtos.LoginRequest
import com.example.collaborativetodo.data.dtos.RegisterRequest
import com.example.collaborativetodo.data.dtos.UserWithToken
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("api/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<UserWithToken>

    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<UserWithToken>

    @POST("api/auth/validate")
    suspend fun validateToken(

    ): Response<Map<String, Any>>
}