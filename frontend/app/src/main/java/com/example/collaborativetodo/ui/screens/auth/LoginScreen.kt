package com.example.collaborativetodo.ui.screens.auth

import com.example.collaborativetodo.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.collaborativetodo.ui.components.ErrorDialog
import com.example.collaborativetodo.ui.components.LoadingDialog
import com.example.collaborativetodo.ui.theme.TodoAppTheme
import com.example.collaborativetodo.ui.viewmodels.AuthViewModel

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val state by authViewModel.state.collectAsState()

    // Handle state changes
    LaunchedEffect(state) {
        when {
            state.isLoading -> isLoading = true
            state.isSuccess -> {
                isLoading = false
                onLoginSuccess()
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
                    // Logo/Header
                    Image(
                        painter = painterResource(id = R.drawable.test_logo),
                        contentDescription = "Todo App Logo",
                        modifier = Modifier.size(120.dp)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Title
                    Text(
                        text = "Welcome Back",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Sign in to continue",
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

                    // Login button
                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                errorMessage = "Please fill in all fields"
                                return@Button
                            }
                            authViewModel.login(email, password)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    ) {
                        Text(
                            text = if (isLoading) "Signing in..." else "Sign In",
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Register link - FIXED POSITIONING
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Don't have an account?",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        TextButton(
                            onClick = onNavigateToRegister,
                            modifier = Modifier.wrapContentWidth(),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Sign Up")
                        }
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