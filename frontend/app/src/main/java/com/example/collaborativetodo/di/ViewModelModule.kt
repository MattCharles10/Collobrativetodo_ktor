package com.example.collaborativetodo.di

import com.example.collaborativetodo.ui.viewmodels.AuthViewModel
import com.example.collaborativetodo.ui.viewmodels.TaskViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


// for practice

val viewModelModule = module {
    viewModel { AuthViewModel(get()) }
    viewModel { TaskViewModel(get()) }
}