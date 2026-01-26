package com.todoapp.services

import com.todoapp.dtos.*
import com.todoapp.models.*
import com.todoapp.repositories.*
import java.time.Instant
import java.time.format.DateTimeParseException
import java.util.*

class TaskService(
    private val taskRepository: TaskRepository = TaskRepository(),
    private val userRepository: UserRepository = UserRepository(),
    private val shareRepository: ShareRepository = ShareRepository(),
    private val webSocketService: WebSocketService = WebSocketService(),
    private val emailService: EmailService = EmailService()
) {

    suspend fun createTask(userId: String, request: TaskCreate): Result<TaskDTO> {
        return try {
            // Validate input
            if (request.title.isBlank()) {
                return Result.failure(IllegalArgumentException("Title is required"))
            }

            if (request.title.length > 255) {
                return Result.failure(IllegalArgumentException("Title is too long"))
            }

            val dueDate = try {
                request.dueDate?.let { Instant.parse(it) }
            } catch (e: DateTimeParseException) {
                return Result.failure(IllegalArgumentException("Invalid date format. Use ISO 8601 format"))
            }

            if (request.priority !in 0..2) {
                return Result.failure(IllegalArgumentException("Priority must be between 0 and 2"))
            }

            // Get user
            val user = userRepository.findById(userId)
                ?: return Result.failure(IllegalArgumentException("User not found"))

            // Create task
            val now = Instant.now()
            val task = Task(
                id = UUID.randomUUID().toString(),
                title = request.title.trim(),
                description = request.description?.trim(),
                createdBy = userId,
                dueDate = dueDate?.toString(),
                priority = request.priority,
                category = request.category?.trim(),
                createdAt = now.toString(),
                updatedAt = now.toString()
            )

            val savedTask = taskRepository.create(task)
            val userDTO = user.toDTO()

            Result.success(savedTask.toDTO(userDTO))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTask(userId: String, taskId: String): Result<TaskDTO> {
        return try {
            // Check permission
            if (!shareRepository.hasPermission(userId, taskId, "view")) {
                return Result.failure(IllegalArgumentException("Access denied"))
            }

            val task = taskRepository.findById(taskId)
                ?: return Result.failure(IllegalArgumentException("Task not found"))

            val owner = userRepository.findById(task.createdBy)
                ?: return Result.failure(IllegalArgumentException("Owner not found"))

            val shares = shareRepository.findByTask(taskId)
            val shareDTOs = shares.mapNotNull { share ->
                val sharedWith = userRepository.findById(share.sharedWith)
                val sharedBy = userRepository.findById(share.sharedBy)

                if (sharedWith != null && sharedBy != null) {
                    ShareDTO(
                        id = share.id,
                        taskId = share.taskId,
                        sharedWith = sharedWith.toDTO(),
                        permission = share.permission,
                        sharedBy = sharedBy.toDTO(),
                        createdAt = share.createdAt
                    )
                } else {
                    null
                }
            }

            Result.success(task.toDTO(owner.toDTO(), shareDTOs))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTasks(userId: String, page: Int = 1, pageSize: Int = 20): Result<TaskListResponse> {
        return try {
            val (tasks, total) = taskRepository.findByUser(userId, page, pageSize)
            val user = userRepository.findById(userId)
                ?: return Result.failure(IllegalArgumentException("User not found"))

            val taskDTOs = tasks.map { task ->
                val shares = shareRepository.findByTask(task.id)
                val shareDTOs = shares.mapNotNull { share ->
                    val sharedWith = userRepository.findById(share.sharedWith)
                    val sharedBy = userRepository.findById(share.sharedBy)

                    if (sharedWith != null && sharedBy != null) {
                        ShareDTO(
                            id = share.id,
                            taskId = share.taskId,
                            sharedWith = sharedWith.toDTO(),
                            permission = share.permission,
                            sharedBy = sharedBy.toDTO(),
                            createdAt = share.createdAt
                        )
                    } else {
                        null
                    }
                }
                task.toDTO(user.toDTO(), shareDTOs)
            }

            val hasMore = (page * pageSize) < total

            Result.success(TaskListResponse(
                tasks = taskDTOs,
                page = page,
                pageSize = pageSize,
                total = total,
                hasMore = hasMore
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTask(userId: String, taskId: String, request: TaskUpdate): Result<TaskDTO> {
        return try {
            // Check edit permission
            if (!shareRepository.hasPermission(userId, taskId, "edit")) {
                return Result.failure(IllegalArgumentException("Edit permission denied"))
            }

            // Validate input
            val updates = mutableMapOf<String, Any?>()

            request.title?.let {
                if (it.isBlank()) {
                    return Result.failure(IllegalArgumentException("Title cannot be empty"))
                }
                if (it.length > 255) {
                    return Result.failure(IllegalArgumentException("Title is too long"))
                }
                updates["title"] = it.trim()
            }

            request.description?.let {
                updates["description"] = it.trim()
            }

            request.isCompleted?.let {
                updates["isCompleted"] = it
            }

            request.dueDate?.let {
                val dueDate = try {
                    Instant.parse(it)
                } catch (e: DateTimeParseException) {
                    return Result.failure(IllegalArgumentException("Invalid date format. Use ISO 8601 format"))
                }
                updates["dueDate"] = dueDate.toString()
            }

            request.priority?.let {
                if (it !in 0..2) {
                    return Result.failure(IllegalArgumentException("Priority must be between 0 and 2"))
                }
                updates["priority"] = it
            }

            request.category?.let {
                updates["category"] = it.trim()
            }

            if (updates.isEmpty()) {
                return Result.failure(IllegalArgumentException("No updates provided"))
            }

            val updatedTask = taskRepository.update(taskId, updates)
                ?: return Result.failure(IllegalArgumentException("Task not found"))

            // Notify all users with access
            notifyTaskUpdate(taskId, userId)

            // Get updated task with details
            getTask(userId, taskId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTask(userId: String, taskId: String): Result<Boolean> {
        return try {
            val task = taskRepository.findById(taskId)
                ?: return Result.failure(IllegalArgumentException("Task not found"))

            // Only owner can delete
            if (task.createdBy != userId) {
                return Result.failure(IllegalArgumentException("Only task owner can delete"))
            }

            // Get all users with access before deleting
            val usersWithAccess = shareRepository.getUsersWithAccess(taskId)

            val success = taskRepository.delete(taskId)

            if (success) {
                // Notify all users with access
                notifyTaskDeleted(taskId, userId, usersWithAccess)
            }

            Result.success(success)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun shareTask(userId: String, request: ShareCreate): Result<ShareDTO> {
        return try {
            // Validate input
            if (request.taskId.isBlank()) {
                return Result.failure(IllegalArgumentException("Task ID is required"))
            }

            if (request.sharedWithEmail.isBlank()) {
                return Result.failure(IllegalArgumentException("Email is required"))
            }

            if (request.permission !in listOf("view", "edit")) {
                return Result.failure(IllegalArgumentException("Permission must be 'view' or 'edit'"))
            }

            val task = taskRepository.findById(request.taskId)
                ?: return Result.failure(IllegalArgumentException("Task not found"))

            // Only owner can share
            if (task.createdBy != userId) {
                return Result.failure(IllegalArgumentException("Only task owner can share"))
            }

            val sharedWithUser = userRepository.findByEmail(request.sharedWithEmail.trim())
                ?: return Result.failure(IllegalArgumentException("User not found"))

            // Don't share with self
            if (sharedWithUser.id == userId) {
                return Result.failure(IllegalArgumentException("Cannot share task with yourself"))
            }

            // Check if already shared
            if (shareRepository.exists(request.taskId, sharedWithUser.id)) {
                return Result.failure(IllegalArgumentException("Task already shared with this user"))
            }

            // Create share
            val now = Instant.now()
            val share = Share(
                id = UUID.randomUUID().toString(),
                taskId = request.taskId,
                sharedWith = sharedWithUser.id,
                permission = request.permission,
                sharedBy = userId,
                createdAt = now.toString()
            )

            val savedShare = shareRepository.create(share)

            // Get user DTOs for response
            val sharedWithUserDTO = sharedWithUser.toDTO()
            val sharedByUser = userRepository.findById(userId)
                ?: return Result.failure(IllegalArgumentException("User not found"))
            val sharedByUserDTO = sharedByUser.toDTO()

            // Notify the shared user
            notifyTaskShared(sharedWithUser.id, request.taskId, userId)

            // Send email notification
            emailService.sendShareNotification(
                toEmail = sharedWithUser.email,
                taskTitle = task.title,
                sharedBy = sharedByUser.username,
                shareLink = "todoapp://tasks/${request.taskId}"
            )

            Result.success(
                ShareDTO(
                    id = savedShare.id,
                    taskId = savedShare.taskId,
                    sharedWith = sharedWithUserDTO,
                    permission = savedShare.permission,
                    sharedBy = sharedByUserDTO,
                    createdAt = savedShare.createdAt
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSharedTasks(userId: String, page: Int = 1, pageSize: Int = 20): Result<TaskListResponse> {
        return try {
            val (shares, totalShares) = shareRepository.findBySharedWith(userId, page, pageSize)
            val taskIds = shares.map { it.taskId }.distinct()

            val tasks = taskIds.mapNotNull { taskRepository.findById(it) }

            val taskDTOs = tasks.mapNotNull { task ->
                val owner = userRepository.findById(task.createdBy)
                    ?: return@mapNotNull null

                val taskShares = shareRepository.findByTask(task.id)
                val shareDTOs = taskShares.mapNotNull { share ->
                    val sharedWith = userRepository.findById(share.sharedWith)
                    val sharedBy = userRepository.findById(share.sharedBy)

                    if (sharedWith != null && sharedBy != null) {
                        ShareDTO(
                            id = share.id,
                            taskId = share.taskId,
                            sharedWith = sharedWith.toDTO(),
                            permission = share.permission,
                            sharedBy = sharedBy.toDTO(),
                            createdAt = share.createdAt
                        )
                    } else {
                        null
                    }
                }

                task.toDTO(owner.toDTO(), shareDTOs)
            }

            val hasMore = (page * pageSize) < totalShares

            Result.success(TaskListResponse(
                tasks = taskDTOs,
                page = page,
                pageSize = pageSize,
                total = totalShares,
                hasMore = hasMore
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeShare(userId: String, shareId: String): Result<Boolean> {
        return try {
            val share = shareRepository.findById(shareId)
                ?: return Result.failure(IllegalArgumentException("Share not found"))

            val task = taskRepository.findById(share.taskId)
                ?: return Result.failure(IllegalArgumentException("Task not found"))

            // Only owner or the person who was shared with can remove
            if (task.createdBy != userId && share.sharedWith != userId) {
                return Result.failure(IllegalArgumentException("Permission denied"))
            }

            val success = shareRepository.delete(shareId)

            if (success) {
                notifyShareRemoved(share.sharedWith, share.taskId, userId)
            }

            Result.success(success)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchUsers(query: String, excludeUserId: String): Result<List<UserDTO>> {
        return try {
            if (query.length < 2) {
                return Result.success(emptyList())
            }

            val users = userRepository.searchByEmailOrUsername(query, excludeUserId)
            val userDTOs = users.map { it.toDTO() }

            Result.success(userDTOs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun notifyTaskUpdate(taskId: String, updatedBy: String) {
        val task = taskRepository.findById(taskId) ?: return
        val usersWithAccess = shareRepository.getUsersWithAccess(taskId)

        // Notify all users with access except the updater
        val usersToNotify = (usersWithAccess + task.createdBy)
            .filter { it != updatedBy }
            .distinct()

        webSocketService.broadcast(usersToNotify, WebSocketMessage(
            type = "TASK_UPDATED",
            data = mapOf(
                "taskId" to taskId,
                "updatedBy" to updatedBy,
                "timestamp" to System.currentTimeMillis().toString()
            )
        ))
    }

    private suspend fun notifyTaskDeleted(taskId: String, deletedBy: String, usersWithAccess: List<String>) {
        webSocketService.broadcast(usersWithAccess, WebSocketMessage(
            type = "TASK_DELETED",
            data = mapOf(
                "taskId" to taskId,
                "deletedBy" to deletedBy,
                "timestamp" to System.currentTimeMillis().toString()
            )
        ))
    }

    private suspend fun notifyTaskShared(userId: String, taskId: String, sharedBy: String) {
        webSocketService.sendMessage(userId, WebSocketMessage(
            type = "TASK_SHARED",
            data = mapOf(
                "taskId" to taskId,
                "sharedBy" to sharedBy,
                "timestamp" to System.currentTimeMillis().toString()
            )
        ))
    }

    private suspend fun notifyShareRemoved(userId: String, taskId: String, removedBy: String) {
        webSocketService.sendMessage(userId, WebSocketMessage(
            type = "SHARE_REMOVED",
            data = mapOf(
                "taskId" to taskId,
                "removedBy" to removedBy,
                "timestamp" to System.currentTimeMillis().toString()
            )
        ))
    }

    // Extension functions
    private fun User.toDTO(): UserDTO = UserDTO(
        id = this.id,
        email = this.email,
        username = this.username,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )

    private fun Task.toDTO(createdBy: UserDTO, shares: List<ShareDTO> = emptyList()): TaskDTO = TaskDTO(
        id = this.id,
        title = this.title,
        description = this.description,
        isCompleted = this.isCompleted,
        createdBy = createdBy,
        dueDate = this.dueDate,
        priority = this.priority,
        category = this.category,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        sharedWith = shares
    )
}