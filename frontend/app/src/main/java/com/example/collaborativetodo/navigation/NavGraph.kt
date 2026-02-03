package com.example.collaborativetodo.navigation


import ProfileScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.collaborativetodo.ui.screens.auth.LoginScreen
import com.example.collaborativetodo.ui.screens.auth.RegisterScreen
import com.example.collaborativetodo.ui.screens.main.HomeScreen
import com.example.collaborativetodo.ui.screens.main.TaskDetailScreen
import com.example.collaborativetodo.ui.screens.main.TaskListScreen
import com.example.collaborativetodo.ui.screens.shared.SharedTasksScreen
import com.example.collaborativetodo.ui.viewmodels.AuthViewModel
import com.example.collaborativetodo.ui.viewmodels.TaskViewModel
import com.todoapp.ui.screens.main.CreateTaskScreen
import com.todoapp.ui.screens.shared.ShareTaskScreen
import org.koin.compose.rememberKoinInject

sealed class Screen(val route: String) {
    // Auth screens
    object Login : Screen("login")
    object Register : Screen("register")

    // Main screens
    object Home : Screen("home")
    object TaskList : Screen("task_list")
    object TaskDetail : Screen("task_detail/{taskId}") {
        fun createRoute(taskId: String) = "task_detail/$taskId"
    }
    object CreateTask : Screen("create_task")
    object Profile : Screen("profile")

    // Shared screens
    object SharedTasks : Screen("shared_tasks")
    object ShareTask : Screen("share_task/{taskId}") {
        fun createRoute(taskId: String) = "share_task/$taskId"
    }
}

@Composable
fun NavGraph(
    isLoggedIn: Boolean,
    authViewModel: AuthViewModel,
    navController: NavHostController = rememberNavController()
) {
    var startDestination by remember { mutableStateOf(Screen.Login.route) }

    LaunchedEffect(isLoggedIn) {
        startDestination = if (isLoggedIn) Screen.Home.route else Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth screens
        composable(Screen.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                authViewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // Main screens
        composable(Screen.Home.route) {
            val taskViewModel: TaskViewModel = rememberKoinInject()
            HomeScreen(
                authViewModel = authViewModel,
                taskViewModel = taskViewModel,
                onNavigateToTaskList = {
                    navController.navigate(Screen.TaskList.route)
                },
                onNavigateToSharedTasks = {
                    navController.navigate(Screen.SharedTasks.route)
                },
                onNavigateToCreateTask = {
                    navController.navigate(Screen.CreateTask.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToTaskDetail = { taskId ->
                    navController.navigate(Screen.TaskDetail.createRoute(taskId))
                }
            )
        }

        composable(Screen.TaskList.route) {
            val taskViewModel: TaskViewModel = rememberKoinInject()
            TaskListScreen(
                taskViewModel = taskViewModel,
                onNavigateToTaskDetail = { taskId ->
                    navController.navigate(Screen.TaskDetail.createRoute(taskId))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.TaskDetail.route) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
            val taskViewModel: TaskViewModel = rememberKoinInject()
            TaskDetailScreen(
                taskId = taskId,
                taskViewModel = taskViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToShareTask = { shareTaskId ->
                    navController.navigate(Screen.ShareTask.createRoute(shareTaskId))
                }
            )
        }

        composable(Screen.CreateTask.route) {
            val taskViewModel: TaskViewModel = rememberKoinInject()
            CreateTaskScreen(
                taskViewModel = taskViewModel,
                onTaskCreated = {
                    navController.popBackStack()
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                authViewModel = authViewModel,
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Shared screens
        composable(Screen.SharedTasks.route) {
            val taskViewModel: TaskViewModel = rememberKoinInject()
            SharedTasksScreen(
                taskViewModel = taskViewModel,
                onNavigateToTaskDetail = { taskId ->
                    navController.navigate(Screen.TaskDetail.createRoute(taskId))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.ShareTask.route) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
            val taskViewModel: TaskViewModel = rememberKoinInject()
            ShareTaskScreen(
                taskId = taskId,
                taskViewModel = taskViewModel,
                onShareSuccess = {
                    navController.popBackStack()
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}