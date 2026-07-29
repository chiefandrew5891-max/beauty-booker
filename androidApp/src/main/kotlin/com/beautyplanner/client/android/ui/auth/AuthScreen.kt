package com.beautyplanner.client.android.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.beautyplanner.client.domain.model.ClientProfile
import com.beautyplanner.client.domain.repository.AuthRepository
import com.beautyplanner.client.strings.Strings
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    authRepository: AuthRepository,
    onSignedIn: (ClientProfile) -> Unit
) {
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegisterMode by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = Strings.AUTH_TITLE,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = Strings.AUTH_SUBTITLE,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            AuthButton(
                text = Strings.AUTH_SIGN_IN_GOOGLE,
                onClick = {
                    isLoading = true
                    errorMessage = null
                    scope.launch {
                        authRepository.signInWithGoogle()
                            .onSuccess { onSignedIn(it) }
                            .onFailure { errorMessage = it.message ?: Strings.ERROR_GENERIC }
                        isLoading = false
                    }
                },
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(12.dp))

            AuthButton(
                text = Strings.AUTH_SIGN_IN_APPLE,
                onClick = {
                    isLoading = true
                    errorMessage = null
                    scope.launch {
                        authRepository.signInWithApple()
                            .onSuccess { onSignedIn(it) }
                            .onFailure { errorMessage = it.message ?: Strings.ERROR_GENERIC }
                        isLoading = false
                    }
                },
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(Strings.AUTH_EMAIL_LABEL) },
                singleLine = true,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(Strings.AUTH_PASSWORD_LABEL) },
                singleLine = true,
                enabled = !isLoading,
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(12.dp))

            AuthButton(
                text = if (isRegisterMode) {
                    Strings.AUTH_EMAIL_REGISTER
                } else {
                    Strings.AUTH_EMAIL_SIGN_IN
                },
                onClick = {
                    val trimmedEmail = email.trim()

                    when {
                        trimmedEmail.isBlank() -> {
                            errorMessage = Strings.ERROR_EMPTY_EMAIL
                        }

                        password.isBlank() -> {
                            errorMessage = Strings.ERROR_EMPTY_PASSWORD
                        }

                        else -> {
                            isLoading = true
                            errorMessage = null

                            scope.launch {
                                val result = if (isRegisterMode) {
                                    authRepository.registerWithEmail(trimmedEmail, password)
                                } else {
                                    authRepository.signInWithEmail(trimmedEmail, password)
                                }

                                result
                                    .onSuccess { onSignedIn(it) }
                                    .onFailure { errorMessage = it.message ?: Strings.ERROR_GENERIC }

                                isLoading = false
                            }
                        }
                    }
                },
                enabled = !isLoading
            )

            TextButton(
                onClick = {
                    if (!isLoading) {
                        isRegisterMode = !isRegisterMode
                        errorMessage = null
                    }
                },
                enabled = !isLoading
            ) {
                Text(
                    text = if (isRegisterMode) {
                        Strings.AUTH_SWITCH_TO_SIGN_IN
                    } else {
                        Strings.AUTH_SWITCH_TO_REGISTER
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = {
                    val guest = authRepository.continueAsGuest()
                    onSignedIn(guest)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text(text = Strings.AUTH_GUEST)
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun AuthButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled
    ) {
        Text(text = text)
    }
}