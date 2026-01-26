package com.todoapp

import com.todoapp.services.AuthService
import com.todoapp.services.WebSocketService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import java.time.Duration

fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    val webSocketService = WebSocketService()
    val authService = AuthService()

    routing {
        authenticate("auth-jwt") {
            webSocket("/ws") {
                val principal = call.principal<UserIdPrincipal>()
                val userId = principal?.name

                if (userId == null) {
                    close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Authentication required"))
                    return@webSocket
                }

                try {
                    webSocketService.connect(userId, this)
                } catch (e: ClosedReceiveChannelException) {
                    // Normal closure
                } catch (e: Exception) {
                    close(CloseReason(CloseReason.Codes.INTERNAL_ERROR, "Internal error"))
                }
            }
        }
    }
}