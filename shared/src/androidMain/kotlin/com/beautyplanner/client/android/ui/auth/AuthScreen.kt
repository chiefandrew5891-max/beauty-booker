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
import com.beautyplanner.client.auth.AuthExecutor
import com.beautyplanner.client.auth.AuthProvider
import com.beautyplanner.client.auth.AuthScreenAction
import com.beautyplanner.client.auth.AuthScreenReducer
import com.beautyplanner.client.auth.AuthScreenState
import com.beautyplanner.client.auth.AuthSubmissionFactory
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
    var state by remember { mutableStateOf(AuthScreenState()) }

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
                    state = AuthScreenReducer.reduce(state, AuthScreenAction.SubmitGoogle)
                    val submission = AuthSubmissionFactory.fromState(state, AuthProvider.GOOGLE)

                    scope.launch {
                        AuthExecutor.execute(authRepository, submission)
                            .onSuccess {
                                state = AuthScreenReducer.complete(state)
                                onSignedIn(it)
                            }
                            .onFailure {
                                state = AuthScreenReducer.failure(
                                    state,
                                    it.message ?: Strings.ERROR_GENERIC
                                )
                            }
                    }
                },
                enabled = !state.isLoading
            )

            Spacer(modifier = Modifier.height(12.dp))

            AuthButton(
                text = Strings.AUTH_SIGN_IN_APPLE,
                onClick = {
                    state = AuthScreenReducer.reduce(state, AuthScreenAction.SubmitApple)
                    val submission = AuthSubmissionFactory.fromState(state, AuthProvider.APPLE)

                    scope.launch {
                        AuthExecutor.execute(authRepository, submission)
                            .onSuccess {
                                state = AuthScreenReducer.complete(state)
                                onSignedIn(it)
                            }
                            .onFailure {
                                state = AuthScreenReducer.failure(
                                    state,
                                    it.message ?: Strings.ERROR_GENERIC
                                )
                            }
                    }
                },
                enabled = !state.isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = state.email,
                onValueChange = {
                    state = AuthScreenReducer.reduce(
                        state,
                        AuthScreenAction.EmailChanged(it)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(Strings.AUTH_EMAIL_LABEL) },
                singleLine = true,
                enabled = !state.isLoading
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = state.password,
                onValueChange = {
                    state = AuthScreenReducer.reduce(
                        state,
                        AuthScreenAction.PasswordChanged(it)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(Strings.AUTH_PASSWORD_LABEL) },
                singleLine = true,
                enabled = !state.isLoading,
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(12.dp))

            AuthButton(
                text = if (state.isRegisterMode) {
                    Strings.AUTH_EMAIL_REGISTER
                } else {
                    Strings.AUTH_EMAIL_SIGN_IN
                },
                onClick = {
                    state = AuthScreenReducer.reduce(state, AuthScreenAction.SubmitEmail)

                    if (!state.isLoading) return@AuthButton

                    val submission = AuthSubmissionFactory.fromState(state, AuthProvider.EMAIL)

                    scope.launch {
                        AuthExecutor.execute(authRepository, submission)
                            .onSuccess {
                                state = AuthScreenReducer.complete(state)
                                onSignedIn(it)
                            }
                            .onFailure {
                                state = AuthScreenReducer.failure(
                                    state,
                                    it.message ?: Strings.ERROR_GENERIC
                                )
                            }
                    }
                },
                enabled = !state.isLoading
            )

            TextButton(
                onClick = {
                    if (!state.isLoading) {
                        state = AuthScreenReducer.reduce(state, AuthScreenAction.ToggleMode)
                    }
                },
                enabled = !state.isLoading
            ) {
                Text(
                    text = if (state.isRegisterMode) {
                        Strings.AUTH_SWITCH_TO_SIGN_IN
                    } else {
                        Strings.AUTH_SWITCH_TO_REGISTER
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = {
                    state = AuthScreenReducer.reduce(state, AuthScreenAction.SubmitGuest)
                    val submission = AuthSubmissionFactory.fromState(state, AuthProvider.GUEST)

                    scope.launch {
                        AuthExecutor.execute(authRepository, submission)
                            .onSuccess {
                                state = AuthScreenReducer.complete(state)
                                onSignedIn(it)
                            }
                            .onFailure {
                                state = AuthScreenReducer.failure(
                                    state,
                                    it.message ?: Strings.ERROR_GENERIC
                                )
                            }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            ) {
                Text(text = Strings.AUTH_GUEST)
            }

            if (state.errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = state.errorMessage!!,
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