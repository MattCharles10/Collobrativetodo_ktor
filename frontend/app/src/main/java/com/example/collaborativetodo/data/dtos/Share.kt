package com.example.collaborativetodo.data.dtos

import com.google.gson.annotations.SerializedName

data class Share(
    @SerializedName("id") val id: String,
    @SerializedName("taskId") val taskId: String,
    @SerializedName("sharedWith") val sharedWith: User,
    @SerializedName("permission") val permission: String, // "view" or "edit"
    @SerializedName("sharedBy") val sharedBy: User,
    @SerializedName("createdAt") val createdAt: String
)

data class ShareCreate(
    @SerializedName("taskId") val taskId: String,
    @SerializedName("sharedWithEmail") val sharedWithEmail: String,
    @SerializedName("permission") val permission: String = "view"
)