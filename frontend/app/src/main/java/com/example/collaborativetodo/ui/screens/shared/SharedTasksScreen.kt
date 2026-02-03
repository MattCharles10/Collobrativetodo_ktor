package com.todoapp.ui.screens.shared

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.collaborativetodo.data.dtos.User
import com.example.collaborativetodo.ui.theme.TodoAppTheme
import com.example.collaborativetodo.ui.viewmodels.TaskViewModel

import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareTaskScreen(
    taskId: String,
    taskViewModel: TaskViewModel,
    onShareSuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val userSearchState by taskViewModel.userSearchState.collectAsState()
    val taskState by taskViewModel.taskDetailState.collectAsState()
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedUsers by remember { mutableStateOf<Set<User>>(emptySet()) }
    var permission by remember { mutableStateOf("view") } // "view" or "edit"
    var shareMessage by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var isSharing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Load task details
    LaunchedEffect(taskId) {
        taskViewModel.loadTask(taskId)
    }

    // Search users when query changes
    LaunchedEffect(searchQuery) {
        if (searchQuery.length >= 2) {
            taskViewModel.searchUsers(searchQuery)
        }
    }

    val currentTask by remember { derivedStateOf { taskState.task } }

    TodoAppTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Share Task",
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
                        if (selectedUsers.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    if (selectedUsers.isEmpty()) {
                                        errorMessage = "Please select at least one user"
                                        return@IconButton
                                    }

                                    isSharing = true
                                    scope.launch {
                                        selectedUsers.forEach { user ->
                                            try {
                                                taskViewModel.shareTask(
                                                    taskId = taskId,
                                                    email = user.email,
                                                    permission = permission
                                                )
                                            } catch (e: Exception) {
                                                errorMessage = "Failed to share with ${user.username}: ${e.message}"
                                                isSharing = false
                                                return@launch
                                            }
                                        }

                                        isSharing = false
                                        showSuccessDialog = true
                                    }
                                },
                                enabled = !isSharing
                            ) {
                                if (isSharing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(Icons.Default.Send, contentDescription = "Share")
                                }
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
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Task info header
                    currentTask?.let { task ->
                        Card(
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Sharing task:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        text = task.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 2,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }

                                // Already shared count
                                if (task.sharedWith.isNotEmpty()) {
                                    Surface(
                                        shape = MaterialTheme.shapes.small,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    ) {
                                        Text(
                                            text = "${task.sharedWith.size} shared",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Search bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search users by email or username") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    // Selected users
                    if (selectedUsers.isNotEmpty()) {
                        Card(
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Selected users (${selectedUsers.size})",
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(selectedUsers.toList(), key = { it.id }) { user ->
                                        SelectedUserItem(
                                            user = user,
                                            onRemove = {
                                                selectedUsers = selectedUsers - user
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Permission selection
                    Card(
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Permissions",
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                PermissionOption(
                                    permission = "view",
                                    title = "Can View",
                                    description = "Can see task but not edit",
                                    selected = permission == "view",
                                    onClick = { permission = "view" }
                                )

                                PermissionOption(
                                    permission = "edit",
                                    title = "Can Edit",
                                    description = "Can view and edit task",
                                    selected = permission == "edit",
                                    onClick = { permission = "edit" }
                                )
                            }
                        }
                    }

                    // Optional message
                    OutlinedTextField(
                        value = shareMessage,
                        onValueChange = { shareMessage = it },
                        placeholder = { Text("Optional message (will be included in notification)") },
                        leadingIcon = {
                            Icon(Icons.Default.Message, contentDescription = "Message")
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    // Search results or empty state
                    if (searchQuery.length >= 2) {
                        if (userSearchState.isLoading) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        } else if (userSearchState.users.isNotEmpty()) {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(1.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                items(userSearchState.users, key = { it.id }) { user ->
                                    UserSearchResultItem(
                                        user = user,
                                        isSelected = selectedUsers.contains(user),
                                        onSelect = {
                                            selectedUsers = selectedUsers + user
                                            searchQuery = ""
                                        }
                                    )
                                }
                            }
                        } else if (searchQuery.isNotBlank()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "No users found",
                                    modifier = Modifier.size(80.dp),
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No users found",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = "Try searching by email or username",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    } else if (searchQuery.isBlank() && selectedUsers.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                modifier = Modifier.size(120.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Share with others",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "Search for users by email or username to share this task",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                modifier = Modifier.padding(top = 4.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                // Error dialog
                if (errorMessage != null) {
                    AlertDialog(
                        onDismissRequest = { errorMessage = null },
                        title = { Text("Error") },
                        text = {
                            Text(errorMessage ?: "An error occurred")
                        },
                        confirmButton = {
                            TextButton(onClick = { errorMessage = null }) {
                                Text("OK")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { errorMessage = null }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                // Success dialog
                if (showSuccessDialog) {
                    AlertDialog(
                        onDismissRequest = { showSuccessDialog = false },
                        title = { Text("Success") },
                        text = {
                            Text("Task shared successfully with ${selectedUsers.size} user${if (selectedUsers.size != 1) "s" else ""}")
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showSuccessDialog = false
                                    onShareSuccess()
                                }
                            ) {
                                Text("OK")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SelectedUserItem(
    user: User,
    onRemove: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.username.take(1).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // User info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.username,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // Remove button
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionOption(
    permission: String,
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val color = when (permission) {
        "edit" -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.primary
    }

    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = if (selected) color.copy(alpha = 0.1f)
        else MaterialTheme.colorScheme.surface,
        border = if (selected) androidx.compose.foundation.BorderStroke(
            1.dp,
            color
        ) else androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        ),
        modifier = Modifier
            .width(1.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = when (permission) {
                    "edit" -> Icons.Default.Edit
                    else -> Icons.Default.Visibility
                },
                contentDescription = title,
                tint = if (selected) color else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (selected) color else MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) color.copy(alpha = 0.8f)
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            if (selected) {
                Spacer(modifier = Modifier.height(8.dp))
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun UserSearchResultItem(
    user: User,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Surface(
        onClick = onSelect,
        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
        else MaterialTheme.colorScheme.surface
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
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.username.take(1).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // User info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.username,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // Selection indicator
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Icon(
                    Icons.Default.AddCircle,
                    contentDescription = "Add",
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}