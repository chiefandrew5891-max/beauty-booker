package com.beautyplanner.client.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.beautyplanner.client.android.navigation.AppNavigation
import com.beautyplanner.client.android.ui.theme.AppThemeMode
import com.beautyplanner.client.android.ui.theme.BeautyPlannerTheme
import com.beautyplanner.client.fake.FakeAuthRepository
import com.beautyplanner.client.fake.FakeBookingRepository
import com.beautyplanner.client.fake.FakeClientProfileRepository
import com.beautyplanner.client.fake.FakeMastersRepository
import com.beautyplanner.client.fake.FakeReviewsRepository

/**
 * Single activity entry point.
 * All dependencies are wired here using fake implementations.
 * TODO: Replace fakes with real repositories when backend/Firebase is ready.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ── Dependency wiring (replace fakes with real implementations here) ──
        val authRepository = FakeAuthRepository()
        val mastersRepository = FakeMastersRepository()
        val bookingRepository = FakeBookingRepository()
        val reviewsRepository = FakeReviewsRepository()
        val clientProfileRepository = FakeClientProfileRepository()

        setContent {
            var themeMode by rememberSaveable { mutableStateOf(AppThemeMode.SYSTEM) }

            BeautyPlannerTheme(themeMode = themeMode) {
                AppNavigation(
                    authRepository = authRepository,
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
