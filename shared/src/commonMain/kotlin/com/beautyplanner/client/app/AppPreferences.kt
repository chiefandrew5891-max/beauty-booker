package com.beautyplanner.client.app

import com.beautyplanner.client.theme.AppThemeMode

data class AppPreferences(
    val themeMode: AppThemeMode = AppThemeMode.LIGHT,
    val languageCode: String = "ru"
)