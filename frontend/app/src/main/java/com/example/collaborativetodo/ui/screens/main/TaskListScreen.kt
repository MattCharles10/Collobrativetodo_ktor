package com.example.collaborativetodo.ui.screens.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.collaborativetodo.R
import com.example.collaborativetodo.data.dtos.Task
import com.example.collaborativetodo.ui.theme.TodoAppTheme
import com.example.collaborativetodo.ui.viewmodels.TaskViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    taskViewModel: TaskViewModel,
    onNavigateToTaskDetail: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val taskState by taskViewModel.taskListState.collectAsState()
    val lazyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showFilterMenu by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedTaskForAction by remember { mutableStateOf<Task?>(null) }
    var showTaskActions by remember { mutableStateOf(false) }

    val filteredTasks = remember(taskState.tasks, searchQuery) {
        if (searchQuery.isBlank()) {
            taskState.tasks
        } else {
            taskState.tasks.filter { task ->
                task.title.contains(searchQuery, ignoreCase = true) ||
                        task.description?.contains(searchQuery, ignoreCase = true) == true ||
                        task.category?.contains(searchQuery, ignoreCase = true) == true
            }
        }
    }

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

    // Handle scroll to load more
    LaunchedEffect(lazyListState.canScrollForward) {
        if (!lazyListState.canScrollForward &&
            taskState.hasMore &&
            !taskState.isLoading &&
            filteredTasks.isNotEmpty()) {
            taskViewModel.loadTasks(taskState.page + 1)
        }
    }

    TodoAppTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "My Tasks",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.Default.Sort, contentDescription = "Sort")
                        }
                        IconButton(onClick = { showFilterMenu = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { /* TODO: Navigate to create task */ },
                    icon = {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    },
                    text = {
                        Text("New Task")
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
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
                    // Search bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search tasks...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = MaterialTheme.shapes.large
                    )

                    // Task count
                    Text(
                        text = "${filteredTasks.size} tasks",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    // Tasks list
                    if (filteredTasks.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (searchQuery.isNotBlank()) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "No results",
                                    modifier = Modifier.size(120.dp),
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No tasks found",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = "Try a different search term",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "No tasks",
                                    modifier = Modifier.size(120.dp),
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
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
                            }
                        }
                    } else {
                        LazyColumn(
                            state = lazyListState,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            items(filteredTasks, key = { it.id }) { task ->
                                // Using a simple TaskItem placeholder - you need to implement or import your actual TaskItem
                                SimpleTaskItem(
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

                            // Loading more indicator
                            if (taskState.isLoading && taskState.page > 1) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(32.dp),
                                            strokeWidth = 3.dp
                                        )
                                    }
                                }
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
                                    taskViewModel.loadTasks()
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

                // Sort menu
                if (showSortMenu) {
                    AlertDialog(
                        onDismissRequest = { showSortMenu = false },
                        title = { Text("Sort Tasks") },
                        text = {
                            Column {
                                SortOption("Date (Newest first)", selected = true) { showSortMenu = false }
                                SortOption("Date (Oldest first)") { showSortMenu = false }
                                SortOption("Priority (High to Low)") { showSortMenu = false }
                                SortOption("Priority (Low to High)") { showSortMenu = false }
                                SortOption("Title (A-Z)") { showSortMenu = false }
                                SortOption("Title (Z-A)") { showSortMenu = false }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showSortMenu = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                // Filter menu
                if (showFilterMenu) {
                    AlertDialog(
                        onDismissRequest = { showFilterMenu = false },
                        title = { Text("Filter Tasks") },
                        text = {
                            Column {
                                FilterOption("All Tasks", selected = true) { showFilterMenu = false }
                                FilterOption("Active") { showFilterMenu = false }
                                FilterOption("Completed") { showFilterMenu = false }
                                FilterOption("High Priority") { showFilterMenu = false }
                                FilterOption("Medium Priority") { showFilterMenu = false }
                                FilterOption("Low Priority") { showFilterMenu = false }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showFilterMenu = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                // Task actions menu
                selectedTaskForAction?.let { task ->
                    if (showTaskActions) {
                        DropdownMenu(
                            expanded = showTaskActions,
                            onDismissRequest = {
                                showTaskActions = false
                                selectedTaskForAction = null
                            }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit") },
                                onClick = {
                                    showTaskActions = false
                                    selectedTaskForAction = null
                                    onNavigateToTaskDetail(task.id)
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete") },
                                onClick = {
                                    showTaskActions = false
                                    scope.launch {
                                        taskViewModel.deleteTask(task.id)
                                    }
                                    selectedTaskForAction = null
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Simple placeholder for TaskItem - you should replace with your actual TaskItem component
@Composable
fun SimpleTaskItem(
    task: Task,
    onTaskClick: () -> Unit,
    onToggleComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onTaskClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggleComplete() }
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Task details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (task.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough
                    else androidx.compose.ui.text.style.TextDecoration.None,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                task.description?.let { description ->
                    if (description.isNotBlank()) {
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Priority indicator
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(
                        when (task.priority) {
                            0 -> androidx.compose.ui.graphics.Color(0xFF4CAF50) // Green
                            1 -> androidx.compose.ui.graphics.Color(0xFFFF9800) // Orange
                            2 -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.outline
                        }
                    )
            )
        }
    }
}

@Composable
fun SortOption(
    text: String,
    selected: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )

        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun FilterOption(
    text: String,
    selected: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(
                    if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
                .border(
                    width = if (selected) 0.dp else 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}