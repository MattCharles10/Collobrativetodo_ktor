package com.todoapp.ui.screens.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.collaborativetodo.ui.theme.TodoAppTheme
import com.example.collaborativetodo.ui.viewmodels.TaskViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskScreen(
    taskViewModel: TaskViewModel,
    onTaskCreated: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(0) } // 0: low, 1: medium, 2: high
    var category by remember { mutableStateOf("") }

    var showCategoryDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Predefined categories
    val categories = listOf(
        "Work", "Personal", "Shopping", "Health", "Finance",
        "Education", "Home", "Social", "Travel", "Other"
    )

    TodoAppTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Create Task",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                if (title.isBlank()) {
                                    errorMessage = "Please enter a title"
                                    return@IconButton
                                }

                                isLoading = true
                                scope.launch {
                                    try {
                                        taskViewModel.createTask(
                                            title = title,
                                            description = description.takeIf { it.isNotBlank() },
                                            priority = priority,
                                            category = category.takeIf { it.isNotBlank() }
                                        )
                                        onTaskCreated()
                                    } catch (e: Exception) {
                                        errorMessage = e.message ?: "Failed to create task"
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            },
                            enabled = title.isNotBlank() && !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Check, contentDescription = "Create")
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
            ) {
                // Title field
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title *") },
                    placeholder = { Text("What needs to be done?") },
                    leadingIcon = {
                        Icon(Icons.Default.Title, contentDescription = "Title")
                    },
                    singleLine = true,
                    isError = title.isBlank(),
                    supportingText = {
                        if (title.isBlank()) {
                            Text("Title is required")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )

                // Description field
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("Add details about your task...") },
                    leadingIcon = {
                        Icon(Icons.Default.Description, contentDescription = "Description")
                    },
                    minLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .padding(horizontal = 16.dp)
                )

                // Priority selection
                Card(
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Priority",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Simple radio button style
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { priority = 0 },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = priority == 0,
                                    onClick = { priority = 0 }
                                )
                                Text(
                                    text = "Low Priority",
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { priority = 1 },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = priority == 1,
                                    onClick = { priority = 1 }
                                )
                                Text(
                                    text = "Medium Priority",
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { priority = 2 },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = priority == 2,
                                    onClick = { priority = 2 }
                                )
                                Text(
                                    text = "High Priority",
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
                // Category
                Card(
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Category",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showCategoryDialog = true }
                        ) {
                            OutlinedTextField(
                                value = category,
                                onValueChange = { }, // Empty since it's read-only
                                placeholder = { Text("Select category") },
                                readOnly = true,
                                enabled = false, // Disable the text field itself
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = "Categories"
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                // Category selection dialog
                if (showCategoryDialog) {
                    AlertDialog(
                        onDismissRequest = { showCategoryDialog = false },
                        title = { Text("Select Category") },
                        text = {
                            Column {
                                categories.forEach { cat ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                category = cat
                                                showCategoryDialog = false
                                            }
                                            .padding(vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = cat,
                                            style = MaterialTheme.typography.bodyMedium
                                        )

                                        if (category == cat) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showCategoryDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
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
                        }
                    )
                }
            }
        }
    }

    @Composable
    fun SimplePriorityOption(
        text: String,
        selected: Boolean,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        FilterChip(
            selected = selected,
            onClick = onClick,
            label = { Text(text) },
            modifier = Modifier.width(1.dp)
        )
    }
}