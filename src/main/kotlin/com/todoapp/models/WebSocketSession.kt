package com.todoapp.models

import io.ktor.websocket.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap
import kotlinx.serialization.encodeToString

@Serializable
data class WebSocketMessage(
    val type: String,
    val data: Map<String, String> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun fromJson(jsonString: String): WebSocketMessage? {
            return try {
                json.decodeFromString(jsonString)
            } catch (e: Exception) {
                null
            }
        }

        fun toJson(message: WebSocketMessage): String {
            return json.encodeToString(message)
        }
    }
}

data class UserSession(
    val userId: String,
    val session: DefaultWebSocketSession,
    val connectedAt: Long = System.currentTimeMillis()
)

object WebSocketSessionManager {
    private val userSessions = ConcurrentHashMap<String, UserSession>()

    fun addSession(userId: String, session: DefaultWebSocketSession) {
        userSessions[userId] = UserSession(userId, session)
    }

    fun removeSession(userId: String) {
        userSessions.remove(userId)
    }

    fun getSession(userId: String): UserSession? {
        return userSessions[userId]
    }

    fun getAllSessions(): List<UserSession> {
        return userSessions.values.toList()
    }

    fun isUserConnected(userId: String): Boolean {
        return userSessions.containsKey(userId)
    }

    suspend fun broadcast(userIds: List<String>, message: WebSocketMessage) {
        val jsonMessage = WebSocketMessage.toJson(message)
        userIds.forEach { userId ->
            userSessions[userId]?.session?.send(Frame.Text(jsonMessage))
        }
    }
}