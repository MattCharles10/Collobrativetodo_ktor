package com.example.collaborativetodo.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.collaborativetodo.ui.components.ErrorDialog
import com.example.collaborativetodo.ui.components.LoadingDialog
import com.example.collaborativetodo.ui.theme.SuccessColor
import com.example.collaborativetodo.ui.theme.TodoAppTheme
import com.example.collaborativetodo.ui.viewmodels.AuthViewModel


private val ColorScheme.success: Color
    get() = SuccessColor

@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val state by authViewModel.state.collectAsState()

    // Handle state changes
    LaunchedEffect(state) {
        when {
            state.isLoading -> isLoading = true
            state.isSuccess -> {
                isLoading = false
                onRegisterSuccess()
            }

            state.error != null -> {
                isLoading = false
                errorMessage = state.error
            }
        }
    }

    // Clear error when screen recomposes
    LaunchedEffect(Unit) {
        authViewModel.clearError()
    }

    TodoAppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Title
                    Text(
                        text = "Create Account",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Sign up to get started",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Email field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = "Email")
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email
                        ),
                        singleLine = true,
                        isError = errorMessage != null,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Username field
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = "Username")
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text
                        ),
                        singleLine = true,
                        isError = errorMessage != null,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = "Password")
                        },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.Visibility
                                    else Icons.Default.VisibilityOff,
                                    contentDescription = if (showPassword) "Hide password"
                                    else "Show password"
                                )
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password
                        ),
                        singleLine = true,
                        isError = errorMessage != null,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Confirm Password field
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = "Confirm Password")
                        },
                        trailingIcon = {
                            IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                                Icon(
                                    imageVector = if (showConfirmPassword) Icons.Default.Visibility
                                    else Icons.Default.VisibilityOff,
                                    contentDescription = if (showConfirmPassword) "Hide password"
                                    else "Show password"
                                )
                            }
                        },
                        visualTransformation = if (showConfirmPassword) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password
                        ),
                        singleLine = true,
                        isError = errorMessage != null,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Password requirements
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Text(
                            text = "Password must contain:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "• At least 8 characters",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (password.length >= 8) MaterialTheme.colorScheme.success
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                        Text(
                            text = "• One uppercase letter",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (password.any { it.isUpperCase() }) MaterialTheme.colorScheme.success
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                        Text(
                            text = "• One lowercase letter",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (password.any { it.isLowerCase() }) MaterialTheme.colorScheme.success
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                        Text(
                            text = "• One digit",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (password.any { it.isDigit() }) MaterialTheme.colorScheme.success
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }

                    // Error message
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Register button
                    Button(
                        onClick = {
                            if (email.isBlank() || username.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                                errorMessage = "Please fill in all fields"
                                return@Button
                            }

                            if (password != confirmPassword) {
                                errorMessage = "Passwords do not match"
                                return@Button
                            }

                            if (password.length < 8) {
                                errorMessage = "Password must be at least 8 characters"
                                return@Button
                            }

                            if (!password.any { it.isUpperCase() } ||
                                !password.any { it.isLowerCase() } ||
                                !password.any { it.isDigit() }) {
                                errorMessage =
                                    "Password must contain uppercase, lowercase, and digit"
                                return@Button
                            }

                            authViewModel.register(email, username, password)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    ) {
                        Text(
                            text = if (isLoading) "Creating Account..." else "Sign Up",
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Login link
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Already have an account?",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        TextButton(
                            onClick = onNavigateToLogin,
                            modifier = Modifier.wrapContentWidth(),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Sign In")
                        }
                    }
                    // Loading dialog
                    if (isLoading) {
                        LoadingDialog()
                    }

                    // Error dialog
                    if (errorMessage != null && !isLoading) {
                        ErrorDialog(
                            message = errorMessage ?: "An error occurred",
                            onDismiss = {
                                errorMessage = null
                                authViewModel.clearError()
                            }
                        )
                    }
                }
            }
        }
    }
}