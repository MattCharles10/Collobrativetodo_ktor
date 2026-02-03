package com.example.collaborativetodo.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiService {
    private const val BASE_URL = "http://10.0.2.2:8080/"
    // For real device: "http://YOUR_LOCAL_IP:8080/"

    private fun getOkHttpClient(token: String? = null): OkHttpClient {
        val interceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder().apply {
                    token?.let {
                        addHeader("Authorization", "Bearer $it")
                    }
                    addHeader("Content-Type", "application/json")
                    addHeader("Accept", "application/json")
                }.build()
                chain.proceed(request)
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

  fun createAuthService(): AuthApi {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApi::class.java)
    }

      fun createTaskService(token: String): TaskApi {
          return Retrofit.Builder()
              .baseUrl(BASE_URL)
              .client(getOkHttpClient(token))
              .addConverterFactory(GsonConverterFactory.create())
              .build()
              .create(TaskApi::class.java)
      }
}