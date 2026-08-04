package com.beautyplanner.client.android.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.beautyplanner.client.Locales
import com.beautyplanner.client.android.ui.common.ClientTopBar
import com.beautyplanner.client.android.ui.discover.DiscoverScreen
import com.beautyplanner.client.app.MainTab
import com.beautyplanner.client.domain.model.BookingRequest
import com.beautyplanner.client.domain.model.BookingStatus
import com.beautyplanner.client.domain.model.ClientProfile
import com.beautyplanner.client.domain.model.MasterProfile
import com.beautyplanner.client.domain.repository.BookingRepository
import com.beautyplanner.client.domain.repository.MastersRepository
import com.beautyplanner.client.domain.repository.ReviewsRepository
import com.beautyplanner.client.theme.AppThemeMode

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
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.HOME) }
    var showSettings by rememberSaveable { mutableStateOf(false) }

    val topBarTitle = when {
        showSettings -> Locales.t("settings_title")
        selectedTab == MainTab.FAVORITES -> Locales.t("favorites_title")
        selectedTab == MainTab.PROFILE -> Locales.t("profile_title")
        selectedTab == MainTab.APPOINTMENTS -> Locales.t("appointments_title")
        else -> Locales.t("discover_title")
    }

    Scaffold(
        topBar = {
            ClientTopBar(
                title = topBarTitle,
                showBack = showSettings,
                onBackClick = { showSettings = false },
                actionIcon = if (showSettings) null else Icons.Filled.Settings,
                actionDescription = Locales.t("settings_title"),
                onActionClick = if (showSettings) null else ({ showSettings = true })
            )
        },
        bottomBar = {
            if (!showSettings) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    MainTab.entries.forEach { tab ->
                        val title = when (tab) {
                            MainTab.HOME -> Locales.t("nav_home")
                            MainTab.APPOINTMENTS -> Locales.t("nav_appointments")
                            MainTab.FAVORITES -> Locales.t("nav_favorites")
                            MainTab.PROFILE -> Locales.t("nav_profile")
                        }

                        val icon: ImageVector = when (tab) {
                            MainTab.HOME -> Icons.Outlined.Home
                            MainTab.APPOINTMENTS -> Icons.Outlined.CalendarMonth
                            MainTab.FAVORITES -> Icons.Outlined.FavoriteBorder
                            MainTab.PROFILE -> Icons.Outlined.Person
                        }

                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            icon = { Icon(icon, contentDescription = title) },
                            label = { Text(title) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        when {
            showSettings -> SettingsContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                themeMode = themeMode,
                selectedLanguageCode = selectedLanguageCode,
                onThemeModeChange = onThemeModeChange,
                onLanguageSelected = onLanguageCodeChange
            )

            selectedTab == MainTab.HOME -> DiscoverScreen(
                client = client,
                mastersRepository = mastersRepository,
                reviewsRepository = reviewsRepository,
                onMasterClick = onMasterClick,
                contentPadding = innerPadding
            )

            selectedTab == MainTab.APPOINTMENTS -> AppointmentsContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                bookingRepository = bookingRepository,
                client = client
            )

            selectedTab == MainTab.FAVORITES -> FavoritesContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                mastersRepository = mastersRepository,
                onMasterClick = onMasterClick
            )

            else -> ProfileContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                client = client
            )
        }
    }
}

@Composable
private fun AppointmentsContent(
    modifier: Modifier,
    bookingRepository: BookingRepository,
    client: ClientProfile?
) {
    var appointments by remember { mutableStateOf<List<BookingRequest>>(emptyList()) }

    LaunchedEffect(client?.id) {
        appointments = if (client != null) {
            bookingRepository.getBookingsForClient(client.id)
        } else {
            emptyList()
        }
    }

    val demoAppointments = remember(client?.id) {
        listOf(
            BookingRequest(
                id = "demo-upcoming",
                clientId = client?.id ?: "guest",
                masterId = "master-1",
                serviceId = "service-1",
                slotId = "slot-1",
                appointmentDateTime = "2026-07-24T11:00:00",
                status = BookingStatus.CONFIRMED
            ),
            BookingRequest(
                id = "demo-completed",
                clientId = client?.id ?: "guest",
                masterId = "master-2",
                serviceId = "service-2",
                slotId = "slot-2",
                appointmentDateTime = "2026-07-12T15:30:00",
                status = BookingStatus.COMPLETED
            ),
            BookingRequest(
                id = "demo-cancelled",
                clientId = client?.id ?: "guest",
                masterId = "master-3",
                serviceId = "service-3",
                slotId = "slot-3",
                appointmentDateTime = "2026-07-09T09:00:00",
                status = BookingStatus.CANCELLED
            )
        )
    }

    val items = if (appointments.isNotEmpty()) appointments else demoAppointments

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (items.isEmpty()) {
            Text(Locales.t("appointments_empty"))
        } else {
            items.forEach { booking ->
                AppointmentCard(booking)
            }
        }
    }
}

@Composable
private fun AppointmentCard(booking: BookingRequest) {
    val statusTitle: String
    val chipColor: Color

    when (booking.status) {
        BookingStatus.CONFIRMED,
        BookingStatus.PENDING -> {
            statusTitle = Locales.t("appointment_upcoming")
            chipColor = Color(0xFFD81B60)
        }

        BookingStatus.COMPLETED -> {
            statusTitle = Locales.t("appointment_completed")
            chipColor = Color(0xFF2E7D32)
        }

        BookingStatus.CANCELLED -> {
            statusTitle = Locales.t("appointment_cancelled")
            chipColor = Color(0xFF8E24AA)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = booking.appointmentDateTime.replace("T", "  "),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                StatusChip(
                    title = statusTitle,
                    color = chipColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${Locales.t("label_master")}: ${booking.masterId}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "${Locales.t("label_service")}: ${booking.serviceId}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = Locales.t("appointments_demo_price"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun StatusChip(
    title: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .background(
                color = color.copy(alpha = 0.12f),
                shape = RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = title,
            color = color,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun FavoritesContent(
    modifier: Modifier,
    mastersRepository: MastersRepository,
    onMasterClick: (String) -> Unit
) {
    var masters by remember { mutableStateOf<List<MasterProfile>>(emptyList()) }

    LaunchedEffect(Unit) {
        masters = mastersRepository.getFeaturedMasters()
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (masters.isEmpty()) {
            Text(Locales.t("favorites_empty"))
        } else {
            masters.forEach { master ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onMasterClick(master.id) },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.FavoriteBorder,
                                contentDescription = null,
                                modifier = Modifier.padding(10.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = master.displayName,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = master.specialtyTitle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileContent(
    modifier: Modifier,
    client: ClientProfile?
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = client?.nickname?.ifBlank { Locales.t("profile_guest_fallback_name") }
                        ?: Locales.t("profile_guest_fallback_name"),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (client?.isGuest == true) {
                        Locales.t("profile_guest_mode")
                    } else {
                        Locales.t("profile_client_mode")
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text(
            text = Locales.t("profile_settings_hint"),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun SettingsContent(
    modifier: Modifier,
    themeMode: AppThemeMode,
    selectedLanguageCode: String,
    onThemeModeChange: (AppThemeMode) -> Unit,
    onLanguageSelected: (String) -> Unit
) {
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    val themeOptions = remember {
        listOf(
            AppThemeMode.SYSTEM.name to Locales.t("settings_theme_system"),
            AppThemeMode.LIGHT.name to Locales.t("settings_theme_light"),
            AppThemeMode.DARK.name to Locales.t("settings_theme_dark")
        )
    }

    val languageCodes = remember {
        listOf(
            "system", "ru", "en", "uk", "pl", "de", "fr", "es", "it", "pt",
            "tr", "ar", "hi", "zh-Hans", "zh-Hant", "ja", "ko", "vi", "th",
            "id", "ms", "nl", "sv", "no", "da", "fi", "cs", "sk", "hu", "ro",
            "bg", "el"
        )
    }

    val languageOptions = remember(Locales.currentLanguage) {
        languageCodes.map { code -> code to localizedLanguageLabel(code) }
    }

    val selectedThemeLabel = when (themeMode) {
        AppThemeMode.SYSTEM -> Locales.t("settings_theme_system")
        AppThemeMode.LIGHT -> Locales.t("settings_theme_light")
        AppThemeMode.DARK -> Locales.t("settings_theme_dark")
    }

    val selectedLanguageLabel = languageOptions
        .firstOrNull { it.first == selectedLanguageCode }
        ?.second
        ?: languageOptions.first().second

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SelectorCard(
            title = Locales.t("settings_theme"),
            value = selectedThemeLabel,
            onClick = { showThemeDialog = true }
        )

        SelectorCard(
            title = Locales.t("settings_language"),
            value = selectedLanguageLabel,
            onClick = { showLanguageDialog = true }
        )
    }

    if (showThemeDialog) {
        OptionsDialog(
            title = Locales.t("settings_theme"),
            options = themeOptions,
            selectedCode = themeMode.name,
            onSelected = { code ->
                onThemeModeChange(AppThemeMode.valueOf(code))
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }

    if (showLanguageDialog) {
        OptionsDialog(
            title = Locales.t("settings_language"),
            options = languageOptions,
            selectedCode = selectedLanguageCode,
            onSelected = { code ->
                onLanguageSelected(code)
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false }
        )
    }
}

private fun localizedLanguageLabel(code: String): String = when (code) {
    "system" -> Locales.t("language_name_system")
    "ru" -> Locales.t("language_name_ru")
    "en" -> Locales.t("language_name_en")
    "uk" -> "Українська"
    "pl" -> "Polski"
    "de" -> "Deutsch"
    "fr" -> "Français"
    "es" -> "Español"
    "it" -> "Italiano"
    "pt" -> "Português"
    "tr" -> "Türkçe"
    "ar" -> "العربية"
    "hi" -> "हिन्दी"
    "zh-Hans" -> "简体中文"
    "zh-Hant" -> "繁體中文"
    "ja" -> "日本語"
    "ko" -> "한국어"
    "vi" -> "Tiếng Việt"
    "th" -> "ไทย"
    "id" -> "Bahasa Indonesia"
    "ms" -> "Bahasa Melayu"
    "nl" -> "Nederlands"
    "sv" -> "Svenska"
    "no" -> "Norsk"
    "da" -> "Dansk"
    "fi" -> "Suomi"
    "cs" -> "Čeština"
    "sk" -> "Slovenčina"
    "hu" -> "Magyar"
    "ro" -> "Română"
    "bg" -> "Български"
    "el" -> "Ελληνικά"
    else -> code
}

@Composable
private fun SelectorCard(
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun OptionsDialog(
    title: String,
    options: List<Pair<String, String>>,
    selectedCode: String,
    onSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelected(option.first) }
                            .padding(horizontal = 10.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = option.second,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (option.first == selectedCode) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(Locales.t("settings_close"))
            }
        }
    )
}