package com.beautyplanner.client.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beautyplanner.client.Locales

@Composable
fun AuthWelcomeScreen(
    state: AuthScreenState,
    errorMessage: String?,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onToggleMode: () -> Unit,
    onContinueWithGoogle: () -> Unit,
    onContinueWithApple: () -> Unit,
    onContinueWithEmail: () -> Unit,
    onContinueAnonymously: () -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val isTablet = maxWidth >= 700.dp

        val topSpacerHeight = if (isTablet) {
            maxHeight * 0.18f
        } else {
            maxHeight * 0.10f
        }

        CenteredNarrowContentContainer {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(topSpacerHeight))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = Locales.t("auth_title"),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = Locales.t("auth_subtitle"),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    BrandedAuthButton(
                        text = Locales.t("auth_sign_in_google"),
                        onClick = onContinueWithGoogle,
                        enabled = !state.isLoading,
                        backgroundColor = Color.White,
                        contentColor = Color(0xFF1F1F1F),
                        borderColor = Color(0xFFDADCE0),
                        leadingContent = {
                            GoogleIcon()
                        }
                    )

                    BrandedAuthButton(
                        text = Locales.t("auth_sign_in_apple"),
                        onClick = onContinueWithApple,
                        enabled = !state.isLoading,
                        backgroundColor = Color.White,
                        contentColor = Color(0xFF1F1F1F),
                        borderColor = Color(0xFFDADCE0),
                        leadingContent = {
                            AppleGlyphIcon(color = Color(0xFF1F1F1F))
                        }
                    )

                    OutlinedTextField(
                        value = state.email,
                        onValueChange = onEmailChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text(Locales.t("auth_email_label"))
                        },
                        singleLine = true,
                        enabled = !state.isLoading,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email
                        )
                    )

                    OutlinedTextField(
                        value = state.password,
                        onValueChange = onPasswordChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text(Locales.t("auth_password_label"))
                        },
                        singleLine = true,
                        enabled = !state.isLoading,
                        visualTransformation = PasswordVisualTransformation()
                    )

                    BrandedAuthButton(
                        text = if (state.isRegisterMode) {
                            Locales.t("auth_email_register")
                        } else {
                            Locales.t("auth_email_sign_in")
                        },
                        onClick = onContinueWithEmail,
                        enabled = !state.isLoading,
                        backgroundColor = Color.White,
                        contentColor = Color(0xFF1F1F1F),
                        borderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.14f),
                        leadingContent = {
                            MailIcon()
                        }
                    )

                    TextButton(
                        onClick = onToggleMode,
                        enabled = !state.isLoading
                    ) {
                        Text(
                            text = if (state.isRegisterMode) {
                                Locales.t("auth_switch_to_sign_in")
                            } else {
                                Locales.t("auth_switch_to_register")
                            }
                        )
                    }

                    BrandedAuthButton(
                        text = Locales.t("auth_guest"),
                        onClick = onContinueAnonymously,
                        enabled = !state.isLoading,
                        backgroundColor = Color.White,
                        contentColor = Color(0xFF1F1F1F),
                        borderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.14f),
                        leadingContent = {
                            Text(
                                text = "👤",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 18.sp
                            )
                        }
                    )

                    if (!errorMessage.isNullOrBlank()) {
                        Text(
                            text = errorMessage,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}