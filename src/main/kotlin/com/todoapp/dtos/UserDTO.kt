package com.todoapp.dtos

import kotlinx.serialization.Serializable

@Serializable
data class UserDTO(
    val id: String,
    val email: String,
    val username: String,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class UserWithTokenDTO(
    val user: UserDTO,
    val token: String
)

@Serializable
data class UserSearchResponse(
    val users: List<UserDTO>,
    val total: Int,
    val hasMore: Boolean
)