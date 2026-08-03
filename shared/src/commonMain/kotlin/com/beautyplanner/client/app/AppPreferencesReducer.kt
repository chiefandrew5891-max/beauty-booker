package com.beautyplanner.client.app

import com.beautyplanner.client.theme.AppThemeMode

object AppPreferencesReducer {
    fun updateTheme(
        preferences: AppPreferences,
        themeMode: AppThemeMode
    ): AppPreferences {
        return preferences.copy(themeMode = themeMode)
    }

    fun updateLanguage(
        preferences: AppPreferences,
        languageCode: String
    ): AppPreferences {
        return preferences.copy(languageCode = languageCode)
    }
}