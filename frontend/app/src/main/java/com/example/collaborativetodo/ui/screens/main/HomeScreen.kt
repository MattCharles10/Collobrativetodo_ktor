package com.example.collaborativetodo.ui.screens.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Task
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.collaborativetodo.ui.components.ErrorDialog
import com.example.collaborativetodo.ui.components.LoadingDialog
import com.example.collaborativetodo.ui.components.TaskItem
import com.example.collaborativetodo.ui.theme.TodoAppTheme
import com.example.collaborativetodo.ui.viewmodels.AuthViewModel
import com.example.collaborativetodo.ui.viewmodels.TaskViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    taskViewModel: TaskViewModel,
    onNavigateToTaskList: () -> Unit,
    onNavigateToSharedTasks: () -> Unit,
    onNavigateToCreateTask: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToTaskDetail: (String) -> Unit
) {
    val currentUser by remember { derivedStateOf { authViewModel.getCurrentUser() } }
    val taskState by taskViewModel.taskListState.collectAsState()
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showMenu by remember { mutableStateOf(false) }

    // Load tasks on initial composition
    LaunchedEffect(Unit) {
        taskViewModel.loadTasks()
    }

    // Handle task state changes
    LaunchedEffect(taskState) {
        isLoading = taskState.isLoading
        if (taskState.error != null) {
            errorMessage = taskState.error
        }
    }

    TodoAppTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Welcome back,",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = currentUser?.username ?: "User",
                                style = MaterialTheme.typography.titleLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { showMenu = !showMenu }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* TODO: Notifications */ }) {
                            Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
                        }
                        IconButton(onClick = onNavigateToProfile) {
                            Icon(Icons.Filled.Person, contentDescription = "Profile")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onNavigateToCreateTask,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Task")
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    // Stats cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        StatsCard(
                            title = "Total Tasks",
                            value = taskState.total.toString(),
                            icon = Icons.Filled.Task,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )

                        StatsCard(
                            title = "Completed",
                            value = taskState.tasks.count { it.isCompleted }.toString(),
                            icon = Icons.Filled.Task,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Quick actions
                    Text(
                        text = "Quick Actions",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ActionCard(
                            title = "My Tasks",
                            icon = Icons.Filled.Task,
                            onClick = onNavigateToTaskList,
                            modifier = Modifier.weight(1f)
                        )

                        ActionCard(
                            title = "Shared",
                            icon = Icons.Filled.Share,
                            onClick = onNavigateToSharedTasks,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Recent tasks header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Tasks",
                            style = MaterialTheme.typography.titleMedium
                        )

                        TextButton(onClick = onNavigateToTaskList) {
                            Text("See All")
                        }
                    }

                    // Recent tasks list
                    if (taskState.tasks.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(id = android.R.drawable.ic_menu_report_image), // Change to your drawable
                                contentDescription = "No tasks",
                                modifier = Modifier.size(120.dp),
                                contentScale = ContentScale.Fit
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No tasks yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "Create your first task to get started",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = onNavigateToCreateTask) {
                                Text("Create Task")
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(top = 12.dp)
                        ) {
                            items(taskState.tasks.take(5)) { task ->
                                TaskItem(
                                    task = task,
                                    onTaskClick = { onNavigateToTaskDetail(task.id) },
                                    onToggleComplete = {
                                        scope.launch {
                                            taskViewModel.updateTask(
                                                id = task.id,
                                                isCompleted = !task.isCompleted
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // Loading dialog
                if (isLoading) {
                    LoadingDialog()
                }

                // Error dialog
                if (errorMessage != null) {
                    ErrorDialog(
                        message = errorMessage ?: "An error occurred",
                        onDismiss = {
                            errorMessage = null
                            taskViewModel.clearError()
                        },
                        onConfirm = {
                            errorMessage = null
                            taskViewModel.clearError()
                            taskViewModel.loadTasks()
                        }
                    )
                }

                // Dropdown menu
                if (showMenu) {
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("My Tasks") },
                            onClick = {
                                showMenu = false
                                onNavigateToTaskList()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Shared Tasks") },
                            onClick = {
                                showMenu = false
                                onNavigateToSharedTasks()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Profile") },
                            onClick = {
                                showMenu = false
                                onNavigateToProfile()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatsCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f),
            contentColor = color
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        modifier = Modifier.size(18.dp),
                        tint = color
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = color
            )

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = color.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun ActionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}