package com.beautyplanner.client.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.beautyplanner.client.android.navigation.AppNavigation
import com.beautyplanner.client.android.ui.theme.BeautyPlannerTheme
import com.beautyplanner.client.app.AppPreferences
import com.beautyplanner.client.app.AppPreferencesReducer

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appGraph = AndroidAppGraph.create()

        setContent {
            var appPreferences by rememberSaveable { mutableStateOf(AppPreferences()) }

            BeautyPlannerTheme(themeMode = appPreferences.themeMode) {
                AppNavigation(
                    authRepository = appGraph.authRepository,
                    mastersRepository = appGraph.mastersRepository,
                    bookingRepository = appGraph.bookingRepository,
                    reviewsRepository = appGraph.reviewsRepository,
                    clientProfileRepository = appGraph.clientProfileRepository,
                    themeMode = appPreferences.themeMode,
                    onThemeModeChange = {
                        appPreferences = AppPreferencesReducer.updateTheme(appPreferences, it)
                    },
                    selectedLanguageCode = appPreferences.languageCode,
                    onLanguageCodeChange = {
                        appPreferences = AppPreferencesReducer.updateLanguage(appPreferences, it)
                    }
                )
            }
        }
    }
}