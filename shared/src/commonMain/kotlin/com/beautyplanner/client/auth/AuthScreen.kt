package com.beautyplanner.client.auth

import androidx.compose.runtime.Composable

@Composable
fun AuthScreen(
    state: AuthScreenState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onToggleMode: () -> Unit,
    onContinueWithGoogle: () -> Unit,
    onContinueWithApple: () -> Unit,
    onContinueWithEmail: () -> Unit,
    onContinueAnonymously: () -> Unit
) {
    AuthWelcomeScreen(
        state = state,
        errorMessage = state.errorMessage,
        onEmailChanged = onEmailChanged,
        onPasswordChanged = onPasswordChanged,
        onToggleMode = onToggleMode,
        onContinueWithGoogle = onContinueWithGoogle,
        onContinueWithApple = onContinueWithApple,
        onContinueWithEmail = onContinueWithEmail,
        onContinueAnonymously = onContinueAnonymously
    )
}