package com.example.collaborativetodo.data.dtos


import com.google.gson.annotations.SerializedName
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class Task(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String?,
    @SerializedName("isCompleted") val isCompleted: Boolean,
    @SerializedName("createdBy") val createdBy: User,
    @SerializedName("dueDate") val dueDate: String?,
    @SerializedName("priority") val priority: Int, // 0: low, 1: medium, 2: high
    @SerializedName("category") val category: String?,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String,
    @SerializedName("sharedWith") val sharedWith: List<Share>
) {
    val formattedDueDate: String?
        get() = dueDate?.let {
            try {
                val instant = Instant.parse(it)
                val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
                "${localDateTime.date} ${localDateTime.time}"
            } catch (e: Exception) {
                null
            }
        }

    val priorityText: String
        get() = when (priority) {
            0 -> "Low"
            1 -> "Medium"
            2 -> "High"
            else -> "Unknown"
        }

    val priorityColor: Long
        get() = when (priority) {
            0 -> 0xFF4CAF50  // Green
            1 -> 0xFFFF9800  // Orange
            2 -> 0xFFF44336  // Red
            else -> 0xFF9E9E9E // Gray
        }
}

data class TaskCreate(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("dueDate") val dueDate: String? = null,
    @SerializedName("priority") val priority: Int = 0,
    @SerializedName("category") val category: String? = null
)

data class TaskUpdate(
    @SerializedName("title") val title: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("isCompleted") val isCompleted: Boolean? = null,
    @SerializedName("dueDate") val dueDate: String? = null,
    @SerializedName("priority") val priority: Int? = null,
    @SerializedName("category") val category: String? = null
)

data class TaskListResponse(
    @SerializedName("tasks") val tasks: List<Task>,
    @SerializedName("page") val page: Int,
    @SerializedName("pageSize") val pageSize: Int,
    @SerializedName("total") val total: Int,
    @SerializedName("hasMore") val hasMore: Boolean
)
