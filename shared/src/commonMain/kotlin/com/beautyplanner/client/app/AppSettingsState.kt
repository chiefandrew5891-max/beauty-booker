package com.beautyplanner.client.app

import com.beautyplanner.client.strings.LanguageOption
import com.beautyplanner.client.strings.Strings
import com.beautyplanner.client.theme.AppThemeMode

data class AppSettingsState(
    val themeMode: AppThemeMode = AppThemeMode.LIGHT,
    val selectedLanguageCode: String = "ru"
) {
    val selectedThemeLabel: String
        get() = when (themeMode) {
            AppThemeMode.SYSTEM -> Strings.SETTINGS_THEME_SYSTEM
            AppThemeMode.LIGHT -> Strings.SETTINGS_THEME_LIGHT
            AppThemeMode.DARK -> Strings.SETTINGS_THEME_DARK
        }

    val selectedLanguageLabel: String
        get() = Strings.LANGUAGE_OPTIONS
            .firstOrNull { it.code == selectedLanguageCode }
            ?.label
            ?: Strings.LANGUAGE_OPTIONS.first().label

    val languageOptions: List<LanguageOption>
        get() = Strings.LANGUAGE_OPTIONS
}