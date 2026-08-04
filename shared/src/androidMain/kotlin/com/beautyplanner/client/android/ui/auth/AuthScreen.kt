package com.beautyplanner.client.android.ui.auth

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.beautyplanner.client.auth.AuthController
import com.beautyplanner.client.auth.AuthScreenAction
import com.beautyplanner.client.auth.AuthScreenState
import com.beautyplanner.client.auth.AuthWelcomeScreen
import com.beautyplanner.client.domain.model.ClientProfile
import com.beautyplanner.client.domain.repository.AuthRepository
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    authRepository: AuthRepository,
    onSignedIn: (ClientProfile) -> Unit
) {
    val scope = rememberCoroutineScope()
    val controller = remember(authRepository) { AuthController(authRepository) }
    var state by remember { mutableStateOf(AuthScreenState()) }

    Surface(modifier = Modifier.fillMaxSize()) {
        AuthWelcomeScreen(
            state = state,
            errorMessage = state.errorMessage,
            onEmailChanged = {
                state = controller.reduce(state, AuthScreenAction.EmailChanged(it))
            },
            onPasswordChanged = {
                state = controller.reduce(state, AuthScreenAction.PasswordChanged(it))
            },
            onToggleMode = {
                if (!state.isLoading) {
                    state = controller.reduce(state, AuthScreenAction.ToggleMode)
                }
            },
            onContinueWithGoogle = {
                scope.launch {
                    val result = controller.submitGoogle(state)
                    state = result.state
                    result.profile?.let(onSignedIn)
                }
            },
            onContinueWithApple = {
                scope.launch {
                    val result = controller.submitApple(state)
                    state = result.state
                    result.profile?.let(onSignedIn)
                }
            },
            onContinueWithEmail = {
                scope.launch {
                    val result = controller.submitEmail(state)
                    state = result.state
                    result.profile?.let(onSignedIn)
                }
            },
            onContinueAnonymously = {
                scope.launch {
                    val result = controller.submitGuest(state)
                    state = result.state
                    result.profile?.let(onSignedIn)
                }
            }
        )
    }
}