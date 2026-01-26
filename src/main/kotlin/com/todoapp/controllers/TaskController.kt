package com.todoapp.controllers

import com.todoapp.dtos.ErrorResponse
import com.todoapp.models.*
import com.todoapp.services.TaskService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class TaskController(
    private val taskService: TaskService = TaskService()
) {

    fun configureRoutes(route: Route) {
        route.route("/api") {
            // Task management
            route("/tasks") {
                authenticate("auth-jwt") {
                    // Get all user tasks
                    get {
                        try {
                            val userId = call.principal<UserIdPrincipal>()?.name
                                ?: return@get call.respond(
                                    HttpStatusCode.Unauthorized,
                                    ErrorResponse("UNAUTHORIZED", "Authentication required")
                                )

                            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                            val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 20

                            val result = taskService.getTasks(userId, page, pageSize)

                            if (result.isSuccess) {
                                call.respond(HttpStatusCode.OK, result.getOrThrow())
                            } else {
                                val exception = result.exceptionOrNull()
                                call.respond(
                                    HttpStatusCode.InternalServerError,
                                    ErrorResponse(
                                        error = "TASK_FETCH_FAILED",
                                        message = exception?.message ?: "Failed to fetch tasks"
                                    )
                                )
                            }
                        } catch (e: Exception) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                ErrorResponse("INVALID_REQUEST", "Invalid request: ${e.message}")
                            )
                        }
                    }

                    // Create new task
                    post {
                        try {
                            val userId = call.principal<UserIdPrincipal>()?.name
                                ?: return@post call.respond(
                                    HttpStatusCode.Unauthorized,
                                    ErrorResponse("UNAUTHORIZED", "Authentication required")
                                )

                            val request = call.receive<TaskCreate>()

                            val result = taskService.createTask(userId, request)

                            if (result.isSuccess) {
                                call.respond(HttpStatusCode.Created, result.getOrThrow())
                            } else {
                                val exception = result.exceptionOrNull()
                                call.respond(
                                    HttpStatusCode.BadRequest,
                                    ErrorResponse(
                                        error = "TASK_CREATION_FAILED",
                                        message = exception?.message ?: "Failed to create task"
                                    )
                                )
                            }
                        } catch (e: Exception) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                ErrorResponse("INVALID_REQUEST", "Invalid request format: ${e.message}")
                            )
                        }
                    }

                    // Share task
                    post("/share") {
                        try {
                            val userId = call.principal<UserIdPrincipal>()?.name
                                ?: return@post call.respond(
                                    HttpStatusCode.Unauthorized,
                                    ErrorResponse("UNAUTHORIZED", "Authentication required")
                                )

                            val request = call.receive<ShareCreate>()

                            val result = taskService.shareTask(userId, request)

                            if (result.isSuccess) {
                                call.respond(HttpStatusCode.Created, result.getOrThrow())
                            } else {
                                val exception = result.exceptionOrNull()
                                call.respond(
                                    HttpStatusCode.BadRequest,
                                    ErrorResponse(
                                        error = "SHARE_FAILED",
                                        message = exception?.message ?: "Failed to share task"
                                    )
                                )
                            }
                        } catch (e: Exception) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                ErrorResponse("INVALID_REQUEST", "Invalid request format: ${e.message}")
                            )
                        }
                    }

                    // Search users
                    get("/search-users") {
                        try {
                            val userId = call.principal<UserIdPrincipal>()?.name
                                ?: return@get call.respond(
                                    HttpStatusCode.Unauthorized,
                                    ErrorResponse("UNAUTHORIZED", "Authentication required")
                                )

                            val query = call.request.queryParameters["q"] ?: ""

                            val result = taskService.searchUsers(query, userId)

                            if (result.isSuccess) {
                                call.respond(HttpStatusCode.OK, result.getOrThrow())
                            } else {
                                val exception = result.exceptionOrNull()
                                call.respond(
                                    HttpStatusCode.InternalServerError,
                                    ErrorResponse(
                                        error = "SEARCH_FAILED",
                                        message = exception?.message ?: "Failed to search users"
                                    )
                                )
                            }
                        } catch (e: Exception) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                ErrorResponse("INVALID_REQUEST", "Invalid request: ${e.message}")
                            )
                        }
                    }

                    // Get shared tasks
                    get("/shared") {
                        try {
                            val userId = call.principal<UserIdPrincipal>()?.name
                                ?: return@get call.respond(
                                    HttpStatusCode.Unauthorized,
                                    ErrorResponse("UNAUTHORIZED", "Authentication required")
                                )

                            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                            val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 20

                            val result = taskService.getSharedTasks(userId, page, pageSize)

                            if (result.isSuccess) {
                                call.respond(HttpStatusCode.OK, result.getOrThrow())
                            } else {
                                val exception = result.exceptionOrNull()
                                call.respond(
                                    HttpStatusCode.InternalServerError,
                                    ErrorResponse(
                                        error = "SHARED_TASKS_FAILED",
                                        message = exception?.message ?: "Failed to fetch shared tasks"
                                    )
                                )
                            }
                        } catch (e: Exception) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                ErrorResponse("INVALID_REQUEST", "Invalid request: ${e.message}")
                            )
                        }
                    }

                    // Task operations by ID
                    route("/{taskId}") {
                        get {
                            try {
                                val userId = call.principal<UserIdPrincipal>()?.name
                                    ?: return@get call.respond(
                                        HttpStatusCode.Unauthorized,
                                        ErrorResponse("UNAUTHORIZED", "Authentication required")
                                    )

                                val taskId = call.parameters["taskId"]
                                    ?: return@get call.respond(
                                        HttpStatusCode.BadRequest,
                                        ErrorResponse("VALIDATION_ERROR", "Task ID is required")
                                    )

                                val result = taskService.getTask(userId, taskId)

                                if (result.isSuccess) {
                                    call.respond(HttpStatusCode.OK, result.getOrThrow())
                                } else {
                                    val exception = result.exceptionOrNull()
                                    val errorMessage = exception?.message ?: "Failed to fetch task"

                                    val statusCode = when {
                                        errorMessage.contains("Access denied", ignoreCase = true) ->
                                            HttpStatusCode.Forbidden
                                        errorMessage.contains("not found", ignoreCase = true) ->
                                            HttpStatusCode.NotFound
                                        else -> HttpStatusCode.BadRequest
                                    }

                                    call.respond(
                                        statusCode,
                                        ErrorResponse("TASK_FETCH_FAILED", errorMessage)
                                    )
                                }
                            } catch (e: Exception) {
                                call.respond(
                                    HttpStatusCode.BadRequest,
                                    ErrorResponse("INVALID_REQUEST", "Invalid request: ${e.message}")
                                )
                            }
                        }

                        put {
                            try {
                                val userId = call.principal<UserIdPrincipal>()?.name
                                    ?: return@put call.respond(
                                        HttpStatusCode.Unauthorized,
                                        ErrorResponse("UNAUTHORIZED", "Authentication required")
                                    )

                                val taskId = call.parameters["taskId"]
                                    ?: return@put call.respond(
                                        HttpStatusCode.BadRequest,
                                        ErrorResponse("VALIDATION_ERROR", "Task ID is required")
                                    )

                                val request = call.receive<TaskUpdate>()

                                val result = taskService.updateTask(userId, taskId, request)

                                if (result.isSuccess) {
                                    call.respond(HttpStatusCode.OK, result.getOrThrow())
                                } else {
                                    val exception = result.exceptionOrNull()
                                    val errorMessage = exception?.message ?: "Failed to update task"

                                    val statusCode = when {
                                        errorMessage.contains("permission denied", ignoreCase = true) ->
                                            HttpStatusCode.Forbidden
                                        errorMessage.contains("not found", ignoreCase = true) ->
                                            HttpStatusCode.NotFound
                                        else -> HttpStatusCode.BadRequest
                                    }

                                    call.respond(
                                        statusCode,
                                        ErrorResponse("TASK_UPDATE_FAILED", errorMessage)
                                    )
                                }
                            } catch (e: Exception) {
                                call.respond(
                                    HttpStatusCode.BadRequest,
                                    ErrorResponse("INVALID_REQUEST", "Invalid request format: ${e.message}")
                                )
                            }
                        }

                        delete {
                            try {
                                val userId = call.principal<UserIdPrincipal>()?.name
                                    ?: return@delete call.respond(
                                        HttpStatusCode.Unauthorized,
                                        ErrorResponse("UNAUTHORIZED", "Authentication required")
                                    )

                                val taskId = call.parameters["taskId"]
                                    ?: return@delete call.respond(
                                        HttpStatusCode.BadRequest,
                                        ErrorResponse("VALIDATION_ERROR", "Task ID is required")
                                    )

                                val result = taskService.deleteTask(userId, taskId)

                                if (result.isSuccess && result.getOrThrow()) {
                                    call.respond(
                                        HttpStatusCode.OK,
                                        mapOf("message" to "Task deleted successfully")
                                    )
                                } else {
                                    val exception = result.exceptionOrNull()
                                    val errorMessage = exception?.message ?: "Failed to delete task"

                                    val statusCode = when {
                                        errorMessage.contains("Only task owner", ignoreCase = true) ->
                                            HttpStatusCode.Forbidden
                                        errorMessage.contains("not found", ignoreCase = true) ->
                                            HttpStatusCode.NotFound
                                        else -> HttpStatusCode.BadRequest
                                    }

                                    call.respond(
                                        statusCode,
                                        ErrorResponse("TASK_DELETION_FAILED", errorMessage)
                                    )
                                }
                            } catch (e: Exception) {
                                call.respond(
                                    HttpStatusCode.BadRequest,
                                    ErrorResponse("INVALID_REQUEST", "Invalid request: ${e.message}")
                                )
                            }
                        }
                    }
                }
            }

            // Share management
            route("/shares") {
                authenticate("auth-jwt") {
                    delete("/{shareId}") {
                        try {
                            val userId = call.principal<UserIdPrincipal>()?.name
                                ?: return@delete call.respond(
                                    HttpStatusCode.Unauthorized,
                                    ErrorResponse("UNAUTHORIZED", "Authentication required")
                                )

                            val shareId = call.parameters["shareId"]
                                ?: return@delete call.respond(
                                    HttpStatusCode.BadRequest,
                                    ErrorResponse("VALIDATION_ERROR", "Share ID is required")
                                )

                            val result = taskService.removeShare(userId, shareId)

                            if (result.isSuccess && result.getOrThrow()) {
                                call.respond(
                                    HttpStatusCode.OK,
                                    mapOf("message" to "Share removed successfully")
                                )
                            } else {
                                val exception = result.exceptionOrNull()
                                val errorMessage = exception?.message ?: "Failed to remove share"

                                val statusCode = when {
                                    errorMessage.contains("Permission denied", ignoreCase = true) ->
                                        HttpStatusCode.Forbidden
                                    errorMessage.contains("not found", ignoreCase = true) ->
                                        HttpStatusCode.NotFound
                                    else -> HttpStatusCode.BadRequest
                                }

                                call.respond(
                                    statusCode,
                                    ErrorResponse("SHARE_REMOVAL_FAILED", errorMessage)
                                )
                            }
                        } catch (e: Exception) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                ErrorResponse("INVALID_REQUEST", "Invalid request: ${e.message}")
                            )
                        }
                    }
                }
            }
        }
    }
}