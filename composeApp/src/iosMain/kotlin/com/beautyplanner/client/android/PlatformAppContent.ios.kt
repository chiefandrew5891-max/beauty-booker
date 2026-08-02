package com.beautyplanner.client.android

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeUIViewController
import com.beautyplanner.client.android.ui.theme.BeautyPlannerTheme
import com.beautyplanner.client.android.ui.theme.AppThemeMode
import com.beautyplanner.client.domain.model.ClientProfile
import com.beautyplanner.client.domain.repository.BookingRepository
import com.beautyplanner.client.domain.repository.ClientProfileRepository
import com.beautyplanner.client.domain.repository.MastersRepository
import com.beautyplanner.client.domain.repository.ReviewsRepository

@Composable
actual fun PlatformAppContent(
    client: ClientProfile?,
    mastersRepository: MastersRepository,
    bookingRepository: BookingRepository,
    reviewsRepository: ReviewsRepository,
    clientProfileRepository: ClientProfileRepository,
    themeMode: AppThemeMode,
    onThemeModeChange: (AppThemeMode) -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "composeApp hosts the shared auth flow.\niOS post-auth screens are still being migrated.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

fun MainViewController() = ComposeUIViewController {
    BeautyPlannerTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "composeApp iOS host is ready for iosApp wiring.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
