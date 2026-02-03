package com.example.collaborativetodo.ui.screens.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.collaborativetodo.data.dtos.Task
import com.example.collaborativetodo.ui.theme.TodoAppTheme
import com.example.collaborativetodo.ui.viewmodels.TaskViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedTasksScreen(
    taskViewModel: TaskViewModel,
    onNavigateToTaskDetail: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val sharedTaskState by taskViewModel.sharedTaskListState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedTask by remember { mutableStateOf<Task?>(null) }
    var showTaskInfo by remember { mutableStateOf(false) }

    // Load shared tasks on initial composition
    LaunchedEffect(Unit) {
        taskViewModel.loadSharedTasks()
    }

    // Handle shared task state changes
    LaunchedEffect(sharedTaskState) {
        isLoading = sharedTaskState.isLoading
        if (sharedTaskState.error != null) {
            errorMessage = sharedTaskState.error
        }
    }

    TodoAppTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Shared Tasks",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header
                    Card(
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = "Shared",
                                modifier = Modifier.size(24.dp)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "Tasks shared with you",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "You can view these tasks but may not be able to edit them",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }

                    // Task count
                    Text(
                        text = "${sharedTaskState.tasks.size} shared tasks",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    // Shared tasks list
                    if (sharedTaskState.tasks.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "No shared tasks",
                                modifier = Modifier.size(120.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No tasks shared with you yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "When someone shares a task with you, it will appear here",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                modifier = Modifier.padding(top = 4.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            items(sharedTaskState.tasks, key = { it.id }) { task ->
                                SharedTaskItem(
                                    task = task,
                                    onTaskClick = { onNavigateToTaskDetail(task.id) },
                                    onShowInfo = {
                                        selectedTask = task
                                        showTaskInfo = true
                                    }
                                )
                            }
                        }
                    }
                }

                // Error dialog
                if (errorMessage != null) {
                    AlertDialog(
                        onDismissRequest = {
                            errorMessage = null
                            taskViewModel.clearError()
                        },
                        title = { Text("Error") },
                        text = {
                            Text(errorMessage ?: "An error occurred")
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    errorMessage = null
                                    taskViewModel.clearError()
                                    taskViewModel.loadSharedTasks()
                                }
                            ) {
                                Text("Retry")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                errorMessage = null
                                taskViewModel.clearError()
                            }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                // Task info dialog
                selectedTask?.let { task ->
                    if (showTaskInfo) {
                        AlertDialog(
                            onDismissRequest = {
                                showTaskInfo = false
                                selectedTask = null
                            },
                            title = { Text("Task Information") },
                            text = {
                                Column {
                                    // Owner info
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = task.createdBy.username.take(1).uppercase(),
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column {
                                            Text(
                                                text = "Shared by",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                            Text(
                                                text = task.createdBy.username,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Shared with count
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.People,
                                            contentDescription = "Shared with",
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(20.dp)
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = "Shared with ${task.sharedWith.size} other${if (task.sharedWith.size != 1) "s" else ""}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Your permissions - FIXED VERSION
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = "Permissions",
                                            tint = MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier.size(20.dp)
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Column {
                                            Text(
                                                text = "Your permissions",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                            Text(
                                                // FIX: Changed to show permission distribution
                                                text = if (task.sharedWith.isNotEmpty()) {
                                                    val editCount = task.sharedWith.count { it.permission == "edit" }
                                                    val viewCount = task.sharedWith.count { it.permission == "view" }
                                                    when {
                                                        editCount > 0 && viewCount > 0 -> "$editCount can edit, $viewCount can view"
                                                        editCount > 0 -> "All can edit"
                                                        else -> "View only"
                                                    }
                                                } else {
                                                    "No permissions set"
                                                },
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        showTaskInfo = false
                                        selectedTask = null
                                    }
                                ) {
                                    Text("Close")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SharedTaskItem(
    task: Task,
    onTaskClick: () -> Unit,
    onShowInfo: () -> Unit
) {
    Card(
        onClick = onTaskClick,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with owner info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = task.createdBy.username.take(1).uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Shared by ${task.createdBy.username}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                IconButton(
                    onClick = onShowInfo,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "Info",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Task details
            if (!task.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = task.description!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Footer with priority and category
            Row(
                modifier = Modifier.padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Priority badge
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = when (task.priority) {
                        0 -> Color(0xFF4CAF50).copy(alpha = 0.1f) // Green
                        1 -> Color(0xFFFF9800).copy(alpha = 0.1f) // Orange
                        2 -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(
                        text = when (task.priority) {
                            0 -> "Low"
                            1 -> "Medium"
                            2 -> "High"
                            else -> "Unknown"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = when (task.priority) {
                            0 -> Color(0xFF4CAF50) // Green
                            1 -> Color(0xFFFF9800) // Orange
                            2 -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Category
                task.category?.let { category ->
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Due date
                task.formattedDueDate?.let { dueDate ->
                    Text(
                        text = "Due: $dueDate",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            // Shared users
            if (task.sharedWith.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.People,
                        contentDescription = "Shared with",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "Shared with ${task.sharedWith.size} person${if (task.sharedWith.size != 1) "s" else ""}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}