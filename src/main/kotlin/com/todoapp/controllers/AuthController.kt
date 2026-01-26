package com.todoapp.controllers

import com.todoapp.dtos.ErrorResponse
import com.todoapp.models.UserCreate
import com.todoapp.models.UserLogin
import com.todoapp.services.AuthService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class AuthController(
    private val authService: AuthService = AuthService()
) {

    fun configureRoutes(route: Route) {
        route.route("/api/auth") {
            post("/register") {
                try {
                    val request = call.receive<UserCreate>()

                    val result = authService.register(request)

                    when {
                        result.isSuccess -> {
                            val userWithToken = result.getOrThrow()
                            call.respond(HttpStatusCode.Created, userWithToken)
                        }
                        else -> {
                            val exception = result.exceptionOrNull()
                            call.respond(
                                HttpStatusCode.BadRequest,
                                ErrorResponse(
                                    error = "REGISTRATION_FAILED",
                                    message = exception?.message ?: "Registration failed"
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(
                            error = "INVALID_REQUEST",
                            message = "Invalid request format: ${e.message}"
                        )
                    )
                }
            }

            post("/login") {
                try {
                    val request = call.receive<UserLogin>()

                    val result = authService.login(request)

                    when {
                        result.isSuccess -> {
                            val userWithToken = result.getOrThrow()
                            call.respond(HttpStatusCode.OK, userWithToken)
                        }
                        else -> {
                            val exception = result.exceptionOrNull()
                            call.respond(
                                HttpStatusCode.Unauthorized,
                                ErrorResponse(
                                    error = "AUTHENTICATION_FAILED",
                                    message = exception?.message ?: "Authentication failed"
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(
                            error = "INVALID_REQUEST",
                            message = "Invalid request format: ${e.message}"
                        )
                    )
                }
            }

            post("/validate") {
                val authHeader = call.request.headers["Authorization"]
                val token = when {
                    authHeader?.startsWith("Bearer ") == true -> authHeader.removePrefix("Bearer ")
                    else -> ""
                }

                if (token.isBlank()) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorResponse(
                            error = "TOKEN_REQUIRED",
                            message = "Authorization token is required"
                        )
                    )
                    return@post
                }

                val decodedJWT = authService.verifyToken(token)
                if (decodedJWT != null) {
                    call.respond(
                        HttpStatusCode.OK,
                        mapOf(
                            "valid" to true,
                            "userId" to decodedJWT.getClaim("userId").asString(),
                            "email" to decodedJWT.getClaim("email").asString(),
                            "username" to decodedJWT.getClaim("username").asString()
                        )
                    )
                } else {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorResponse(
                            error = "INVALID_TOKEN",
                            message = "Invalid or expired token"
                        )
                    )
                }
            }

            // Health check endpoint
            get("/health") {
                call.respond(
                    HttpStatusCode.OK,
                    mapOf("status" to "healthy", "service" to "auth")
                )
            }
        }
    }
}