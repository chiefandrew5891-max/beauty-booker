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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.beautyplanner.client.domain.model.ClientProfile
import com.beautyplanner.client.domain.repository.AuthRepository
import com.beautyplanner.client.strings.Strings
import kotlinx.coroutines.launch

/**
 * First screen shown on launch.
 * Allows sign-in with Google, Apple, Email, or continuing as a guest.
 *
 * Guest users are redirected to the main flow but will be blocked
 * from booking and leaving reviews.
 */
@Composable
fun AuthScreen(
    authRepository: AuthRepository,
    onSignedIn: (ClientProfile) -> Unit
) {
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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

            Spacer(modifier = Modifier.height(48.dp))

            AuthButton(
                text = Strings.AUTH_SIGN_IN_GOOGLE,
                onClick = {
                    isLoading = true
                    errorMessage = null
                    scope.launch {
                        authRepository.signInWithGoogle()
                            .onSuccess { onSignedIn(it) }
                            .onFailure { errorMessage = Strings.ERROR_GENERIC }
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
                            .onFailure { errorMessage = Strings.ERROR_GENERIC }
                        isLoading = false
                    }
                },
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(12.dp))

            AuthButton(
                text = Strings.AUTH_SIGN_IN_EMAIL,
                onClick = {
                    isLoading = true
                    errorMessage = null
                    scope.launch {
                        authRepository.signInWithEmail("demo@example.com", "password")
                            .onSuccess { onSignedIn(it) }
                            .onFailure { errorMessage = it.message ?: Strings.ERROR_GENERIC }
                        isLoading = false
                    }
                },
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

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
private fun AuthButton(text: String, onClick: () -> Unit, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled
    ) {
        Text(text = text)
    }
}
