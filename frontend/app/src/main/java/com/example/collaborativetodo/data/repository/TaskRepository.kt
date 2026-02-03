package com.example.collaborativetodo.data.repository

import com.example.collaborativetodo.data.api.ApiService
import com.example.collaborativetodo.data.dtos.Share
import com.example.collaborativetodo.data.dtos.ShareCreate
import com.example.collaborativetodo.data.dtos.Task
import com.example.collaborativetodo.data.dtos.TaskCreate
import com.example.collaborativetodo.data.dtos.TaskListResponse
import com.example.collaborativetodo.data.dtos.TaskUpdate
import com.example.collaborativetodo.data.dtos.User
import com.example.collaborativetodo.data.local.Preferences


class TaskRepository(
    private val preferences: Preferences
) {
    private fun getTaskService() = ApiService.createTaskService(preferences.getToken() ?: "")

    suspend fun createTask(
        title: String,
        description: String? = null,
        dueDate: String? = null,
        priority: Int = 0,
        category: String? = null
    ): Result<Task> {
        return try {
            val response = getTaskService().createTask(
                TaskCreate(title, description, dueDate, priority, category)
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to create task: ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTasks(page: Int = 1, pageSize: Int = 20): Result<TaskListResponse> {
        return try {
            val response = getTaskService().getTasks(page, pageSize)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get tasks: ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTask(id: String): Result<Task> {
        return try {
            val response = getTaskService().getTask(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get task: ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTask(
        id: String,
        title: String? = null,
        description: String? = null,
        isCompleted: Boolean? = null,
        dueDate: String? = null,
        priority: Int? = null,
        category: String? = null
    ): Result<Task> {
        return try {
            val response = getTaskService().updateTask(
                id,
                TaskUpdate(title, description, isCompleted, dueDate, priority, category)
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to update task: ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTask(id: String): Result<Boolean> {
        return try {
            val response = getTaskService().deleteTask(id)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to delete task: ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun shareTask(taskId: String, email: String, permission: String = "view"): Result<Share> {
        return try {
            val response = getTaskService().shareTask(ShareCreate(taskId, email, permission))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to share task: ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSharedTasks(page: Int = 1, pageSize: Int = 20): Result<TaskListResponse> {
        return try {
            val response = getTaskService().getSharedTasks(page, pageSize)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get shared tasks: ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeShare(shareId: String): Result<Boolean> {
        return try {
            val response = getTaskService().removeShare(shareId)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to remove share: ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchUsers(query: String): Result<List<User>> {
        return try {
            val response = getTaskService().searchUsers(query)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to search users: ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}