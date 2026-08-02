package com.beautyplanner.client.android

import androidx.compose.runtime.Composable
import com.beautyplanner.client.android.navigation.AppNavigation
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
    AppNavigation(
        client = client,
        mastersRepository = mastersRepository,
        bookingRepository = bookingRepository,
        reviewsRepository = reviewsRepository,
        themeMode = themeMode,
        onThemeModeChange = onThemeModeChange
    )
}
