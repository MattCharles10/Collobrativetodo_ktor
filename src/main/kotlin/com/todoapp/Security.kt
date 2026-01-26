package com.todoapp

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.github.cdimascio.dotenv.dotenv
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.http.*
import io.ktor.server.response.respond

fun Application.configureSecurity() {
    val env = dotenv()
    val secret = env["JWT_SECRET"] ?: "your-super-secret-jwt-key-change-in-production"
    val issuer = env["JWT_ISSUER"] ?: "todo-app"
    val audience = env["JWT_AUDIENCE"] ?: "todo-app-users"

    authentication {
        jwt("auth-jwt") {
            realm = issuer

            // Create the verifier
            verifier(
                JWT.require(Algorithm.HMAC256(secret))
                    .withAudience(audience)
                    .withIssuer(issuer)
                    .build()
            )

            // Validate the JWT
            validate { credential ->
                // In Ktor, the credential.payload contains the decoded JWT
                // You can extract the user ID from claims
                val userId = credential.payload.getClaim("userId").asString()

                if (userId != null) {
                    // Create a UserIdPrincipal with the userId
                    UserIdPrincipal(userId)
                } else {
                    null
                }
            }

            // Handle authentication failure
            challenge { _, _ ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf(
                        "error" to "INVALID_TOKEN",
                        "message" to "Invalid or expired token"
                    )
                )
            }
        }
    }
}