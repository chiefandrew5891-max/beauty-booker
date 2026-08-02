package com.beautyplanner.client.android

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.beautyplanner.client.android.ui.auth.AuthScreen
import com.beautyplanner.client.android.ui.auth.CompleteProfileScreen
import com.beautyplanner.client.android.ui.theme.AppThemeMode
import com.beautyplanner.client.android.ui.theme.BeautyPlannerTheme
import com.beautyplanner.client.domain.model.ClientProfile
import com.beautyplanner.client.domain.repository.AuthRepository
import com.beautyplanner.client.domain.repository.AuthState
import com.beautyplanner.client.domain.repository.BookingRepository
import com.beautyplanner.client.domain.repository.ClientProfileRepository
import com.beautyplanner.client.domain.repository.MastersRepository
import com.beautyplanner.client.domain.repository.ReviewsRepository

@Composable
fun BeautyBookerApp(
    authRepository: AuthRepository,
    mastersRepository: MastersRepository,
    bookingRepository: BookingRepository,
    reviewsRepository: ReviewsRepository,
    clientProfileRepository: ClientProfileRepository
) {
    var themeMode by rememberSaveable { mutableStateOf(AppThemeMode.LIGHT) }
    var currentClient by remember { mutableStateOf<ClientProfile?>(null) }
    var isRestoringSession by remember { mutableStateOf(true) }

    LaunchedEffect(authRepository, clientProfileRepository) {
        currentClient = when (val authState = authRepository.getAuthState()) {
            AuthState.SignedOut -> null
            AuthState.Guest -> authRepository.continueAsGuest()
            is AuthState.SignedIn -> clientProfileRepository.getProfile(authState.profile.id) ?: authState.profile
        }
        isRestoringSession = false
    }

    BeautyPlannerTheme(themeMode = themeMode) {
        when {
            isRestoringSession -> {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            currentClient == null -> {
                AuthScreen(
                    authRepository = authRepository,
                    onSignedIn = { currentClient = it }
                )
            }

            currentClient?.nickname.isNullOrBlank() && currentClient?.isGuest == false -> {
                CompleteProfileScreen(
                    client = currentClient,
                    clientProfileRepository = clientProfileRepository,
                    onProfileComplete = { currentClient = it }
                )
            }

            else -> {
                PlatformAppContent(
                    client = currentClient,
                    mastersRepository = mastersRepository,
                    bookingRepository = bookingRepository,
                    reviewsRepository = reviewsRepository,
                    clientProfileRepository = clientProfileRepository,
                    themeMode = themeMode,
                    onThemeModeChange = { themeMode = it }
                )
            }
        }
    }
}

@Composable
expect fun PlatformAppContent(
    client: ClientProfile?,
    mastersRepository: MastersRepository,
    bookingRepository: BookingRepository,
    reviewsRepository: ReviewsRepository,
    clientProfileRepository: ClientProfileRepository,
    themeMode: AppThemeMode,
    onThemeModeChange: (AppThemeMode) -> Unit
)
