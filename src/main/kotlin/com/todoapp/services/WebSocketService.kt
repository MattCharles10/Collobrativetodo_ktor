package com.todoapp.services

import com.todoapp.models.WebSocketMessage
import com.todoapp.models.WebSocketSessionManager
import io.ktor.websocket.*
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class WebSocketService(
    private val sessionManager: WebSocketSessionManager = WebSocketSessionManager
) {

    private val heartbeats = ConcurrentHashMap<String, Long>()
    private val messageCounter = AtomicInteger(0)

    suspend fun connect(userId: String, session: DefaultWebSocketSession) {
        try {
            // Check if user already has a connection
            val existingSession = sessionManager.getSession(userId)
            if (existingSession != null) {
                // Close existing connection
                existingSession.session.close(
                    CloseReason(CloseReason.Codes.NORMAL, "New connection established")
                )
                sessionManager.removeSession(userId)
            }

            // Add new session
            sessionManager.addSession(userId, session)
            heartbeats[userId] = System.currentTimeMillis()

            // Send welcome message
            sendWelcomeMessage(userId)

            // Start message handling
            handleIncomingMessages(userId, session)

        } catch (e: Exception) {
            println("Error connecting WebSocket: ${e.message}")
            try {
                e.message?.let { session.close(CloseReason(CloseReason.Codes.INTERNAL_ERROR, it)) }
            } catch (_: Exception) {}
        } finally {
            disconnect(userId)
        }
    }

    fun disconnect(userId: String) {
        try {
            sessionManager.removeSession(userId)
            heartbeats.remove(userId)
            println("User $userId disconnected from WebSocket")
        } catch (e: Exception) {
            println("Error disconnecting WebSocket: ${e.message}")
        }
    }

    suspend fun sendMessage(userId: String, message: WebSocketMessage): Boolean {
        return try {
            val session = sessionManager.getSession(userId)
            if (session != null) {
                val jsonMessage = WebSocketMessage.toJson(message)
                session.session.send(Frame.Text(jsonMessage))
                true
            } else {
                false
            }
        } catch (e: Exception) {
            println("Error sending message to $userId: ${e.message}")
            false
        }
    }

    suspend fun broadcast(userIds: List<String>, message: WebSocketMessage): List<String> {
        val failedUsers = mutableListOf<String>()

        for (userId in userIds) {
            if (!sendMessage(userId, message)) {
                failedUsers.add(userId)
            }
        }

        return failedUsers
    }

    suspend fun sendToAll(message: WebSocketMessage) {
        val sessions = sessionManager.getAllSessions()

        for (session in sessions) {
            try {
                val jsonMessage = WebSocketMessage.toJson(message)
                session.session.send(Frame.Text(jsonMessage))
            } catch (e: Exception) {
                println("Error broadcasting to ${session.userId}: ${e.message}")
            }
        }
    }

    fun isUserConnected(userId: String): Boolean {
        return sessionManager.getSession(userId) != null
    }

    fun getConnectedUserIds(): List<String> {
        return sessionManager.getAllSessions().map { it.userId }
    }

    fun getConnectionCount(): Int {
        return sessionManager.getAllSessions().size
    }

    private suspend fun handleIncomingMessages(userId: String, session: DefaultWebSocketSession) {
        try {
            for (frame in session.incoming) {
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    processMessage(userId, text)
                    updateHeartbeat(userId)
                }
            }
        } catch (e: Exception) {
            println("Error handling messages for $userId: ${e.message}")
        }
    }

    private suspend fun processMessage(userId: String, messageText: String) {
        try {
            val message = WebSocketMessage.fromJson(messageText)
            if (message == null) {
                sendError(userId, "Invalid message format")
                return
            }

            when (message.type) {
                "PING" -> handlePing(userId)
                "HEARTBEAT" -> handleHeartbeat(userId)
                "TYPING_START" -> handleTypingStart(userId, message)
                "TYPING_END" -> handleTypingEnd(userId, message)
                "MESSAGE_READ" -> handleMessageRead(userId, message)
                else -> handleCustomMessage(userId, message)
            }
        } catch (e: Exception) {
            println("Error processing message from $userId: ${e.message}")
            sendError(userId, "Failed to process message")
        }
    }

    private suspend fun handlePing(userId: String) {
        sendMessage(userId, WebSocketMessage(
            type = "PONG",
            data = mapOf("timestamp" to System.currentTimeMillis().toString()),
            timestamp = System.currentTimeMillis()
        ))
    }

    private suspend fun handleHeartbeat(userId: String) {
        updateHeartbeat(userId)
        sendMessage(userId, WebSocketMessage(
            type = "HEARTBEAT_ACK",
            data = mapOf("timestamp" to System.currentTimeMillis().toString()),
            timestamp = System.currentTimeMillis()
        ))
    }

    private suspend fun handleTypingStart(userId: String, message: WebSocketMessage) {
        val taskId = message.data["taskId"]
        if (taskId != null) {
            // Broadcast typing indicator to other users
            val allUsers = getConnectedUserIds()
            val otherUsers = allUsers.filter { it != userId }

            broadcast(otherUsers, WebSocketMessage(
                type = "USER_TYPING",
                data = mapOf(
                    "userId" to userId,
                    "taskId" to taskId,
                    "timestamp" to System.currentTimeMillis().toString()
                ),
                timestamp = System.currentTimeMillis()
            ))
        }
    }

    private suspend fun handleTypingEnd(userId: String, message: WebSocketMessage) {
        val taskId = message.data["taskId"]
        if (taskId != null) {
            val allUsers = getConnectedUserIds()
            val otherUsers = allUsers.filter { it != userId }

            broadcast(otherUsers, WebSocketMessage(
                type = "USER_STOPPED_TYPING",
                data = mapOf(
                    "userId" to userId,
                    "taskId" to taskId,
                    "timestamp" to System.currentTimeMillis().toString()
                ),
                timestamp = System.currentTimeMillis()
            ))
        }
    }

    private suspend fun handleMessageRead(userId: String, message: WebSocketMessage) {
        val taskId = message.data["taskId"]
        if (taskId != null) {
            sendMessage(userId, WebSocketMessage(
                type = "MESSAGE_READ_ACK",
                data = mapOf(
                    "taskId" to taskId,
                    "readAt" to System.currentTimeMillis().toString()
                ),
                timestamp = System.currentTimeMillis()
            ))
        }
    }

    private suspend fun handleCustomMessage(userId: String, message: WebSocketMessage) {
        // Handle any custom message types
        sendMessage(userId, WebSocketMessage(
            type = "${message.type}_ACK",
            data = mapOf(
                "received" to "true",
                "originalType" to message.type
            ),
            timestamp = System.currentTimeMillis()
        ))
    }

    private fun updateHeartbeat(userId: String) {
        heartbeats[userId] = System.currentTimeMillis()
    }

    private suspend fun sendWelcomeMessage(userId: String) {
        val message = WebSocketMessage(
            type = "CONNECTED",
            data = mapOf(
                "userId" to userId,
                "timestamp" to System.currentTimeMillis().toString(),
                "message" to "WebSocket connection established"
            ),
            timestamp = System.currentTimeMillis()
        )

        sendMessage(userId, message)
    }

    private suspend fun sendError(userId: String, errorMessage: String) {
        val message = WebSocketMessage(
            type = "ERROR",
            data = mapOf(
                "message" to errorMessage,
                "timestamp" to System.currentTimeMillis().toString()
            ),
            timestamp = System.currentTimeMillis()
        )

        sendMessage(userId, message)
    }

    fun cleanupStaleConnections(timeoutMillis: Long = 120000) {
        val now = System.currentTimeMillis()
        val staleUsers = mutableListOf<String>()

        heartbeats.forEach { (userId, lastHeartbeat) ->
            if (now - lastHeartbeat > timeoutMillis) {
                staleUsers.add(userId)
            }
        }

        staleUsers.forEach { userId ->
            println("Cleaning up stale connection for user: $userId")
            disconnect(userId)
        }
    }

    fun getConnectionStats(): Map<String, Any> {
        return mapOf(
            "totalConnections" to getConnectionCount(),
            "connectedUsers" to getConnectedUserIds(),
            "heartbeatStatus" to heartbeats.entries.associate { (userId, timestamp) ->
                userId to mapOf(
                    "lastHeartbeat" to timestamp,
                    "ageSeconds" to (System.currentTimeMillis() - timestamp) / 1000
                )
            }
        )
    }
}