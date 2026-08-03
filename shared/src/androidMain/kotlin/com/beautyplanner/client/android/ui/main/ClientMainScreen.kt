package com.beautyplanner.client.android.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.beautyplanner.client.domain.model.ClientProfile
import com.beautyplanner.client.domain.repository.BookingRepository
import com.beautyplanner.client.domain.repository.MastersRepository
import com.beautyplanner.client.domain.repository.ReviewsRepository
import com.beautyplanner.client.strings.Strings
import com.beautyplanner.client.theme.AppThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientMainScreen(
    client: ClientProfile?,
    mastersRepository: MastersRepository,
    bookingRepository: BookingRepository,
    reviewsRepository: ReviewsRepository,
    themeMode: AppThemeMode,
    onThemeModeChange: (AppThemeMode) -> Unit,
    selectedLanguageCode: String,
    onLanguageCodeChange: (String) -> Unit,
    onMasterClick: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(Strings.DISCOVER_TITLE)
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Temporary client screen",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Client: ${client?.nickname ?: "guest"}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Language: $selectedLanguageCode",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Theme: ${themeMode.name}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}