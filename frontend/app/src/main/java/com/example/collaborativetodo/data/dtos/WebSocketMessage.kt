package com.example.collaborativetodo.data.dtos

import com.google.gson.annotations.SerializedName


data class WebSocketMessage(
    @SerializedName("type") val type: String,
    @SerializedName("data") val data: Map<String, String> = emptyMap(),
    @SerializedName("timestamp") val timestamp: Long = System.currentTimeMillis()
)