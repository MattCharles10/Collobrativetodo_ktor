package com.todoapp.dtos

import kotlinx.serialization.Serializable

@Serializable
data class TaskDTO(
    val id: String,
    val title: String,
    val description: String? = null,
    val isCompleted: Boolean = false,
    val createdBy: UserDTO,
    val dueDate: String? = null,
    val priority: Int = 0,
    val category: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val sharedWith: List<ShareDTO> = emptyList()
)

@Serializable
data class TaskListResponse(
    val tasks: List<TaskDTO>,
    val page: Int,
    val pageSize: Int,
    val total: Int,
    val hasMore: Boolean
)

@Serializable
data class ShareDTO(
    val id: String,
    val taskId: String,
    val sharedWith: UserDTO,
    val permission: String,
    val sharedBy: UserDTO,
    val createdAt: String
)

@Serializable
data class ErrorResponse(
    val error: String,
    val message: String,
    val timestamp: String? = null
)

@Serializable
data class SuccessResponse(
    val message: String,
    val data: Map<String, String>? = null
)