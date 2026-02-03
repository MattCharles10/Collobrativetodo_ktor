package com.example.collaborativetodo.data.api

import com.example.collaborativetodo.data.dtos.Share
import com.example.collaborativetodo.data.dtos.ShareCreate
import com.example.collaborativetodo.data.dtos.Task
import com.example.collaborativetodo.data.dtos.TaskCreate
import com.example.collaborativetodo.data.dtos.TaskListResponse
import com.example.collaborativetodo.data.dtos.TaskUpdate
import com.example.collaborativetodo.data.dtos.User
import retrofit2.Response
import retrofit2.http.*

interface TaskApi {
    // Tasks
    @POST("api/tasks")
    suspend fun createTask(@Body request: TaskCreate): Response<Task>

    @GET("api/tasks")
    suspend fun getTasks(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): Response<TaskListResponse>

    @GET("api/tasks/{id}")
    suspend fun getTask(@Path("id") id: String): Response<Task>

    @PUT("api/tasks/{id}")
    suspend fun updateTask(
        @Path("id") id: String,
        @Body request: TaskUpdate
    ): Response<Task>

    @DELETE("api/tasks/{id}")
    suspend fun deleteTask(
        @Path("id") id: String): Response<Void>

    // Sharing
    @POST("api/tasks/share")
    suspend fun shareTask(
        @Body request: ShareCreate): Response<Share>

    @GET("api/tasks/shared")
    suspend fun getSharedTasks(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): Response<TaskListResponse>

    @DELETE("api/shares/{id}")
    suspend fun removeShare(@Path("id") id: String): Response<Void>

    // User search
    @GET("api/tasks/search-users")
    suspend fun searchUsers(@Query("q") query: String): Response<List<User>>
}