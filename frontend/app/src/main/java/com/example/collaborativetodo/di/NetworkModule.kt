package com.collaborativetodo.di

import com.example.collaborativetodo.data.api.TaskApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// for practice

val networkModule = module {
    single<OkHttpClient> {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    single<Retrofit> {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/")
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Only TaskApi
    factory { (token: String) ->
        val authenticatedClient = get<OkHttpClient>().newBuilder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                chain.proceed(request)
            }
            .build()

        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/")
            .client(authenticatedClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TaskApi::class.java)
    }
}