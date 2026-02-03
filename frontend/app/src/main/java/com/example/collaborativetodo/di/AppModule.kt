package com.example.collaborativetodo.di


import com.example.collaborativetodo.data.repository.AuthRepository
import com.example.collaborativetodo.data.repository.TaskRepository
import com.example.collaborativetodo.ui.viewmodels.AuthViewModel
import com.example.collaborativetodo.ui.viewmodels.TaskViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Local storage - use fully qualified name to avoid conflict
    single { com.example.collaborativetodo.data.local.Preferences(androidContext()) }

    // Repositories - AuthRepository needs Preferences
    single { AuthRepository(get()) }
    single { TaskRepository(get()) }

    // ViewModels - AuthViewModel only needs AuthRepository (not Preferences)
    viewModel { AuthViewModel(get()) }
    viewModel { TaskViewModel(get()) }
}