package com.example.collaborativetodo.ui.screens.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.collaborativetodo.R
import com.example.collaborativetodo.ui.components.PriorityChip
import com.example.collaborativetodo.ui.theme.TodoAppTheme
import com.example.collaborativetodo.ui.viewmodels.TaskViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: String,
    taskViewModel: TaskViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToShareTask: (String) -> Unit
) {
    val taskState by taskViewModel.taskDetailState.collectAsState()
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditMode by remember { mutableStateOf(false) }
    var editedTitle by remember { mutableStateOf("") }
    var editedDescription by remember { mutableStateOf("") }
    var editedPriority by remember { mutableStateOf(0) }

    // Load task on initial composition
    LaunchedEffect(taskId) {
        taskViewModel.loadTask(taskId)
    }

    // Handle task state changes
    LaunchedEffect(taskState) {
        isLoading = taskState.isLoading
        if (taskState.error != null) {
            errorMessage = taskState.error
        }

        // Initialize edit values when task loads
        taskState.task?.let { task ->
            editedTitle = task.title
            editedDescription = task.description ?: ""
            editedPriority = task.priority
        }
    }

    TodoAppTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = if (showEditMode) "Edit Task" else "Task Details",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        if (!showEditMode) {
                            taskState.task?.let { task ->
                                IconButton(onClick = {
                                    onNavigateToShareTask(task.id)
                                }) {
                                    Icon(Icons.Default.Share, contentDescription = "Share")
                                }

                                IconButton(onClick = { showEditMode = true }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                                }

                                IconButton(onClick = { showDeleteDialog = true }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                                }
                            }
                        } else {
                            IconButton(onClick = {
                                showEditMode = false
                                // Reset edit values
                                taskState.task?.let { task ->
                                    editedTitle = task.title
                                    editedDescription = task.description ?: ""
                                    editedPriority = task.priority
                                }
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Cancel")
                            }

                            IconButton(
                                onClick = {
                                    scope.launch {
                                        taskViewModel.updateTask(
                                            id = taskId,
                                            title = editedTitle,
                                            description = editedDescription,
                                            priority = editedPriority
                                        )
                                        showEditMode = false
                                    }
                                },
                                enabled = editedTitle.isNotBlank()
                            ) {
                                Icon(Icons.Default.Check, contentDescription = "Save")
                            }
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
                if (taskState.task != null) {
                    val task = taskState.task!!

                    if (showEditMode) {
                        // Edit mode
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            // Title field
                            OutlinedTextField(
                                value = editedTitle,
                                onValueChange = { editedTitle = it },
                                label = { Text("Title") },
                                singleLine = true,
                                isError = editedTitle.isBlank(),
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (editedTitle.isBlank()) {
                                Text(
                                    text = "Title is required",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Description field
                            OutlinedTextField(
                                value = editedDescription,
                                onValueChange = { editedDescription = it },
                                label = { Text("Description") },
                                minLines = 4,
                                maxLines = 8,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Priority selection
                            Text(
                                text = "Priority",
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                PriorityOption(
                                    priority = 0,
                                    text = "Low",
                                    selected = editedPriority == 0,
                                    onClick = { editedPriority = 0 }
                                )

                                PriorityOption(
                                    priority = 1,
                                    text = "Medium",
                                    selected = editedPriority == 1,
                                    onClick = { editedPriority = 1 }
                                )

                                PriorityOption(
                                    priority = 2,
                                    text = "High",
                                    selected = editedPriority == 2,
                                    onClick = { editedPriority = 2 }
                                )
                            }
                        }
                    } else {
                        // View mode
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            // Title with priority
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(bottom = 16.dp)
                            ) {
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.headlineMedium,
                                    modifier = Modifier.weight(1f),
                                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough
                                    else TextDecoration.None,
                                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    else MaterialTheme.colorScheme.onSurface
                                )

                                PriorityChip(priority = task.priority)
                            }

                            // Completion toggle
                            Surface(
                                onClick = {
                                    scope.launch {
                                        taskViewModel.updateTask(
                                            id = task.id,
                                            isCompleted = !task.isCompleted
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = if (task.isCompleted) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                else MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (task.isCompleted) Icons.Default.CheckCircle
                                        else Icons.Default.Circle,
                                        contentDescription = if (task.isCompleted) "Completed"
                                        else "Not completed",
                                        tint = if (task.isCompleted) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outline
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Text(
                                        text = if (task.isCompleted) "Mark as incomplete"
                                        else "Mark as complete",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Task info cards
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Description
                                if (!task.description.isNullOrBlank()) {
                                    InfoCard(
                                        icon = Icons.Default.Description,
                                        title = "Description",
                                        content = task.description!!
                                    )
                                }

                                // Due date
                                task.formattedDueDate?.let { dueDate ->
                                    InfoCard(
                                        icon = Icons.Default.DateRange,
                                        title = "Due Date",
                                        content = dueDate
                                    )
                                }

                                // Category
                                task.category?.let { category ->
                                    InfoCard(
                                        icon = Icons.Default.Category,
                                        title = "Category",
                                        content = category
                                    )
                                }

                                // Created by
                                InfoCard(
                                    icon = Icons.Default.Person,
                                    title = "Created by",
                                    content = task.createdBy.username
                                )

                                // Created at
                                InfoCard(
                                    icon = Icons.Default.CalendarToday,
                                    title = "Created at",
                                    content = task.createdAt
                                )

                                // Updated at
                                InfoCard(
                                    icon = Icons.Default.Update,
                                    title = "Updated at",
                                    content = task.updatedAt
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Shared with section
                            if (task.sharedWith.isNotEmpty()) {
                                Text(
                                    text = "Shared With",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    task.sharedWith.forEach { share ->
                                        SharedUserCard(share = share)
                                    }
                                }
                            }
                        }
                    }
                } else if (isLoading) {
                    // Loading state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    // Error/empty state
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Using a placeholder image - you can replace R.drawable.ic_error with your actual drawable
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Error",
                            modifier = Modifier.size(120.dp),
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Task not found",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "The task you're looking for doesn't exist or has been deleted",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            modifier = Modifier.padding(top = 4.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onNavigateBack) {
                            Text("Go Back")
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
                                    taskViewModel.loadTask(taskId)
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

                // Delete confirmation dialog
                if (showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        title = { Text("Delete Task") },
                        text = {
                            Text("Are you sure you want to delete this task? This action cannot be undone.")
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showDeleteDialog = false
                                    scope.launch {
                                        taskViewModel.deleteTask(taskId)
                                        onNavigateBack()
                                    }
                                }
                            ) {
                                Text("Delete", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun InfoCard(
    icon: ImageVector,
    title: String,
    content: String
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedUserCard(share: com.example.collaborativetodo.data.dtos.Share) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        onClick = {}
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = share.sharedWith.username.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // User info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = share.sharedWith.username,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = share.sharedWith.email,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // Permission badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = when (share.permission) {
                    "edit" -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                    else -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                }
            ) {
                Text(
                    text = share.permission.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = when (share.permission) {
                        "edit" -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.tertiary
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriorityOption(
    priority: Int,
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    // Define custom colors for priority levels since Material3 doesn't have success/warning by default
    val successColor = Color(0xFF4CAF50) // Green
    val warningColor = Color(0xFFFF9800) // Orange

    val color = when (priority) {
        0 -> successColor
        1 -> warningColor
        2 -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.outline
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) color.copy(alpha = 0.1f)
        else MaterialTheme.colorScheme.surface,
        border = if (selected) BorderStroke(1.dp, color) else null,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) color else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}