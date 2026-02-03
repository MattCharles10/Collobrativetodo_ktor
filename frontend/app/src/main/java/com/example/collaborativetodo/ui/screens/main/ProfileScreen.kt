import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.collaborativetodo.ui.theme.TodoAppTheme
import com.example.collaborativetodo.ui.viewmodels.AuthViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    onLogout: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val currentUser by remember { derivedStateOf { authViewModel.getCurrentUser() } }
    val scrollState = rememberScrollState()

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    TodoAppTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Profile",
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
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile header
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 32.dp)
                        ) {
                            // Avatar
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (currentUser != null) {
                                    Text(
                                        text = currentUser!!.username.take(1).uppercase(),
                                        style = MaterialTheme.typography.displaySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = "User",
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // User info
                            currentUser?.let { user ->
                                Text(
                                    text = user.username,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = user.email,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(top = 4.dp)
                                )

                                Text(
                                    text = "Member since ${formatDate(user.createdAt)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            } ?: run {
                                Text(
                                    text = "Not logged in",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Menu items
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        ProfileMenuItem(
                            icon = Icons.Default.Settings,
                            title = "Settings",
                            subtitle = "App preferences and configurations",
                            onClick = { showSettingsDialog = true }
                        )

                        ProfileMenuItem(
                            icon = Icons.Default.Help,
                            title = "Help & Support",
                            subtitle = "Get help and contact support",
                            onClick = { showHelpDialog = true }
                        )

                        ProfileMenuItem(
                            icon = Icons.Default.Info,
                            title = "About",
                            subtitle = "App version and information",
                            onClick = { showAboutDialog = true }
                        )

                        ProfileMenuItem(
                            icon = Icons.Default.PrivacyTip,
                            title = "Privacy Policy",
                            subtitle = "Read our privacy policy",
                            onClick = {
                                // TODO: Open privacy policy
                            }
                        )

                        ProfileMenuItem(
                            icon = Icons.Default.Description,
                            title = "Terms of Service",
                            subtitle = "Read our terms and conditions",
                            onClick = {
                                // TODO: Open terms of service
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Logout button
                    OutlinedButton(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Icon(
                            Icons.Default.Logout,
                            contentDescription = "Logout",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Logout")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // App version
                    Text(
                        text = "Todo App v1.0.0",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                }

                // Logout confirmation dialog
                if (showLogoutDialog) {
                    AlertDialog(
                        onDismissRequest = { showLogoutDialog = false },
                        title = { Text("Logout") },
                        text = { Text("Are you sure you want to logout?") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showLogoutDialog = false
                                    authViewModel.logout()
                                    onLogout()
                                }
                            ) {
                                Text("Logout", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showLogoutDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                // About dialog
                if (showAboutDialog) {
                    AlertDialog(
                        onDismissRequest = { showAboutDialog = false },
                        title = { Text("About Todo App") },
                        text = {
                            Column {
                                Text(
                                    text = "Todo App v1.0.0",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "A collaborative task management application built with Ktor backend and Android frontend.",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                                Text(
                                    text = "Features:",
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(top = 12.dp)
                                )
                                Text("• Create and manage tasks")
                                Text("• Share tasks with others")
                                Text("• Real-time updates")
                                Text("• Priority levels and categories")
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showAboutDialog = false }) {
                                Text("Close")
                            }
                        }
                    )
                }

                // Help dialog
                if (showHelpDialog) {
                    AlertDialog(
                        onDismissRequest = { showHelpDialog = false },
                        title = { Text("Help & Support") },
                        text = {
                            Column {
                                Text(
                                    text = "Need help? Here's what you can do:",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                HelpItem(
                                    title = "FAQ",
                                    description = "Check our frequently asked questions",
                                    onClick = {
                                        // TODO: Open FAQ
                                    }
                                )
                                HelpItem(
                                    title = "Contact Support",
                                    description = "Email us at support@todoapp.com",
                                    onClick = {
                                        // TODO: Open email client
                                    }
                                )
                                HelpItem(
                                    title = "Report a Bug",
                                    description = "Found a bug? Let us know",
                                    onClick = {
                                        // TODO: Open bug report
                                    }
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showHelpDialog = false }) {
                                Text("Close")
                            }
                        }
                    )
                }

                // Settings dialog
                if (showSettingsDialog) {
                    AlertDialog(
                        onDismissRequest = { showSettingsDialog = false },
                        title = { Text("Settings") },
                        text = {
                            Column {
                                SettingsItem(
                                    title = "Notifications",
                                    description = "Manage notification preferences",
                                    enabled = true,
                                    checked = true,
                                    onCheckedChange = { /* TODO */ }
                                )
                                SettingsItem(
                                    title = "Dark Mode",
                                    description = "Toggle dark theme",
                                    enabled = true,
                                    checked = false,
                                    onCheckedChange = { /* TODO */ }
                                )
                                SettingsItem(
                                    title = "Sync Frequency",
                                    description = "Auto-sync every 15 minutes",
                                    enabled = true,
                                    checked = true,
                                    onCheckedChange = { /* TODO */ }
                                )
                                SettingsItem(
                                    title = "Data Backup",
                                    description = "Backup your data to cloud",
                                    enabled = false,
                                    checked = false,
                                    onCheckedChange = { /* TODO */ }
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showSettingsDialog = false }) {
                                Text("Save")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showSettingsDialog = false }) {
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
                        },
                        dismissButton = {
                            TextButton(onClick = { errorMessage = null }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpItem(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Icon(
                Icons.Default.OpenInNew,
                contentDescription = "Open",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun SettingsItem(
    title: String,
    description: String,
    enabled: Boolean,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (enabled) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = if (enabled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = if (enabled) onCheckedChange else null,
            enabled = enabled
        )
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}