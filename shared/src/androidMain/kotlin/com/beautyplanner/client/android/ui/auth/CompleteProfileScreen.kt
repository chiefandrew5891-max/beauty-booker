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
import androidx.compose.material3.OutlinedTextField
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
import com.beautyplanner.client.Locales
import com.beautyplanner.client.domain.model.ClientProfile
import com.beautyplanner.client.domain.repository.ClientProfileRepository
import kotlinx.coroutines.launch

@Composable
fun CompleteProfileScreen(
    client: ClientProfile?,
    clientProfileRepository: ClientProfileRepository,
    onProfileComplete: (ClientProfile) -> Unit
) {
    val scope = rememberCoroutineScope()
    var nickname by remember { mutableStateOf("") }
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
                text = Locales.t("complete_profile_title"),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = Locales.t("complete_profile_subtitle"),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it },
                label = { Text(Locales.t("complete_profile_hint")) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = errorMessage != null
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val trimmed = nickname.trim()
                    if (trimmed.isBlank()) {
                        errorMessage = Locales.t("complete_profile_error_empty_nickname")
                        return@Button
                    }
                    if (client == null) return@Button
                    isLoading = true
                    errorMessage = null
                    val updatedProfile = client.copy(nickname = trimmed)
                    scope.launch {
                        clientProfileRepository.saveProfile(updatedProfile)
                            .onSuccess { onProfileComplete(it) }
                            .onFailure { errorMessage = Locales.t("error_generic") }
                        isLoading = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text(text = Locales.t("complete_profile_button"))
            }
        }
    }
}