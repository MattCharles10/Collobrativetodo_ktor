package com.todoapp

import com.todoapp.controllers.AuthController
import com.todoapp.controllers.TaskController
import com.todoapp.dtos.ErrorResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.Instant

// Global variable to track startup time
private val applicationStartTime = Instant.now()

fun Application.configureRouting() {
    val authController = AuthController()
    val taskController = TaskController()

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            val timestamp = Instant.now().toString()
            val errorId = "ERR-${System.currentTimeMillis()}"

            call.application.environment.log.error("Error ID: $errorId", cause)

            call.respond(HttpStatusCode.InternalServerError,
                ErrorResponse("INTERNAL_SERVER_ERROR",
                    "An unexpected error occurred. Error ID: $errorId"))
        }

        status(HttpStatusCode.NotFound) { call, status ->
            call.respond(status, ErrorResponse("NOT_FOUND", "The requested resource was not found"))
        }

        status(HttpStatusCode.Unauthorized) { call, status ->
            call.respond(status, ErrorResponse("UNAUTHORIZED", "Authentication is required to access this resource"))
        }

        status(HttpStatusCode.Forbidden) { call, status ->
            call.respond(status, ErrorResponse("FORBIDDEN", "You don't have permission to access this resource"))
        }

        status(HttpStatusCode.BadRequest) { call, status ->
            call.respond(status, ErrorResponse("BAD_REQUEST", "The request could not be understood by the server"))
        }
    }

    routing {
        get("/") {
            call.respondText("Todo App API v1.0.0", ContentType.Text.Plain)
        }

        get("/health") {
            val uptimeSeconds = Instant.now().epochSecond - applicationStartTime.epochSecond

            call.respond(mapOf(
                "status" to "healthy",
                "timestamp" to Instant.now().toString(),
                "service" to "todo-app-backend",
                "version" to "1.0.0",
                "uptime" to formatUptime(uptimeSeconds)
            ))
        }

        authController.configureRoutes(this)
        taskController.configureRoutes(this)
    }
}

// Helper function to format uptime
private fun formatUptime(seconds: Long): String {
    val days = seconds / (24 * 3600)
    val hours = (seconds % (24 * 3600)) / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60

    return when {
        days > 0 -> "${days}d ${hours}h ${minutes}m ${secs}s"
        hours > 0 -> "${hours}h ${minutes}m ${secs}s"
        minutes > 0 -> "${minutes}m ${secs}s"
        else -> "${secs}s"
    }
}