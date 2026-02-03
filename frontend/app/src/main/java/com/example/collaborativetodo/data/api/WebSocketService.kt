package com.todoapp.data.api

import android.util.Log
import com.example.collaborativetodo.data.dtos.WebSocketMessage
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class WebSocketService(private val token: String) {
    private val _messages = MutableStateFlow<List<WebSocketMessage>>(emptyList())
    val messages: StateFlow<List<WebSocketMessage>> = _messages.asStateFlow()

    private val _connectionState = MutableStateFlow(false)
    val connectionState: StateFlow<Boolean> = _connectionState.asStateFlow()

    private var webSocketClient: WebSocketClient? = null
    private val gson = Gson()

    fun connect() {
        try {
            // Note: For Android emulator, use 10.0.2.2 for localhost
            // For real device, use your computer's IP address
            val uri = URI("ws://10.0.2.2:8080/ws")

            webSocketClient = object : WebSocketClient(uri) {
                override fun onOpen(handshakedata: ServerHandshake?) {
                    Log.d("WebSocket", "Connected to server")
                    _connectionState.value = true

                    // Send authentication message if needed
                    val authMessage = WebSocketMessage(
                        type = "AUTH",
                        data = mapOf("token" to token),
                        timestamp = System.currentTimeMillis()
                    )
                    send(gson.toJson(authMessage))
                }

                override fun onMessage(message: String?) {
                    Log.d("WebSocket", "Received: $message")
                    message?.let {
                        try {
                            val wsMessage = gson.fromJson(it, WebSocketMessage::class.java)
                            _messages.value = _messages.value + wsMessage

                            // Handle different message types
                            handleWebSocketMessage(wsMessage)

                        } catch (e: JsonSyntaxException) {
                            Log.e("WebSocket", "Failed to parse message: $e")
                        }
                    }
                }

                override fun onClose(code: Int, reason: String?, remote: Boolean) {
                    Log.d("WebSocket", "Disconnected: $reason (code: $code)")
                    _connectionState.value = false
                }

                override fun onError(ex: Exception?) {
                    Log.e("WebSocket", "Error: ${ex?.message}")
                    _connectionState.value = false
                }
            }

            // Connect with timeout
            webSocketClient?.connect()

        } catch (e: Exception) {
            Log.e("WebSocket", "Connection error: ${e.message}")
            _connectionState.value = false
        }
    }

    private fun handleWebSocketMessage(message: WebSocketMessage) {
        when (message.type) {
            "TASK_UPDATED" -> {
                Log.d("WebSocket", "Task updated: ${message.data["taskId"]}")
                // You can emit this to a separate flow for task updates
            }
            "TASK_DELETED" -> {
                Log.d("WebSocket", "Task deleted: ${message.data["taskId"]}")
            }
            "TASK_SHARED" -> {
                Log.d("WebSocket", "Task shared: ${message.data["taskId"]}")
            }
            "SHARE_REMOVED" -> {
                Log.d("WebSocket", "Share removed: ${message.data["taskId"]}")
            }
            "USER_TYPING" -> {
                Log.d("WebSocket", "User typing: ${message.data["userId"]}")
            }
            "USER_STOPPED_TYPING" -> {
                Log.d("WebSocket", "User stopped typing: ${message.data["userId"]}")
            }
            "PONG" -> {
                Log.d("WebSocket", "Received pong response")
            }
            "HEARTBEAT_ACK" -> {
                Log.d("WebSocket", "Heartbeat acknowledged")
            }
            "ERROR" -> {
                Log.e("WebSocket", "Server error: ${message.data["message"]}")
            }
            else -> {
                Log.d("WebSocket", "Unknown message type: ${message.type}")
            }
        }
    }

    fun disconnect() {
        webSocketClient?.close()
        webSocketClient = null
        _connectionState.value = false
    }

    fun sendMessage(type: String, data: Map<String, String> = emptyMap()) {
        val message = WebSocketMessage(
            type = type,
            data = data,
            timestamp = System.currentTimeMillis()
        )
        webSocketClient?.send(gson.toJson(message))
    }

    fun sendPing() {
        sendMessage("PING")
    }

    fun sendHeartbeat() {
        sendMessage("HEARTBEAT")
    }

    fun sendTypingStart(taskId: String) {
        sendMessage("TYPING_START", mapOf("taskId" to taskId))
    }

    fun sendTypingEnd(taskId: String) {
        sendMessage("TYPING_END", mapOf("taskId" to taskId))
    }

    fun sendMessageRead(taskId: String) {
        sendMessage("READ_STATUS", mapOf("taskId" to taskId, "read" to "true"))
    }

    fun clearMessages() {
        _messages.value = emptyList()
    }
}