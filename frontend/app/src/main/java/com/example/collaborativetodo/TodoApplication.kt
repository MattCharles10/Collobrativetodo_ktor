package com.example.collaborativetodo

import android.app.Application
import com.example.collaborativetodo.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class TodoApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@TodoApplication)
            modules(appModule)
        }
    }
}