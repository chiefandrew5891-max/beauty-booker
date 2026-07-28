package com.beautyplanner.client.android.ui.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.beautyplanner.client.android.ui.common.ClientTopBar
import com.beautyplanner.client.domain.model.MasterScheduleSnapshot
import com.beautyplanner.client.domain.model.MasterService
import com.beautyplanner.client.domain.repository.BookingRepository
import com.beautyplanner.client.domain.repository.MastersRepository
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingCalendarScreen(
    masterId: String,
    serviceId: String,
    mastersRepository: MastersRepository,
    bookingRepository: BookingRepository,
    onDateSelected: (String) -> Unit,
    onBackClick: () -> Unit
) {
    var service by remember { mutableStateOf<MasterService?>(null) }
    var snapshot by remember { mutableStateOf<MasterScheduleSnapshot?>(null) }
    var month by remember { mutableStateOf(YearMonth.now()) }
    val today = LocalDate.now()

    LaunchedEffect(masterId, serviceId) {
        service = mastersRepository.getServicesForMaster(masterId).firstOrNull { it.id == serviceId }
        snapshot = bookingRepository.getScheduleSnapshot(masterId)
    }

    Scaffold(
        topBar = {
            ClientTopBar(
                title = "Выбор даты",
                showBack = true,
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        val localService = service
        val localSnapshot = snapshot

        if (localService == null || localSnapshot == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text("Загрузка расписания...")
            }
        } else {
            val dates = buildMonthCells(month)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { month = month.minusMonths(1) }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null)
                    }

                    Text(
                        text = "${month.month.getDisplayName(TextStyle.FULL, Locale("ru"))} ${month.year}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    IconButton(onClick = { month = month.plusMonths(1) }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                    }
                }

                Spacer(modifier = Modifier.padding(top = 8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс").forEach { day ->
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.padding(top = 8.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    userScrollEnabled = false
                ) {
                    items(dates) { date ->
                        if (date == null) {
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .padding(4.dp)
                            )
                        } else {
                            val isPast = date < today
                            val isAvailable = !isPast && hasAvailability(
                                date = date,
                                durationMinutes = localService.durationMinutes,
                                snapshot = localSnapshot
                            )

                            val bg = when {
                                date == today -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                isAvailable -> MaterialTheme.colorScheme.surfaceVariant
                                else -> MaterialTheme.colorScheme.surface
                            }

                            Card(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .aspectRatio(1f)
                                    .clickable(enabled = isAvailable) {
                                        onDateSelected(date.toString())
                                    },
                                colors = CardDefaults.cardColors(containerColor = bg),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = date.dayOfMonth.toString(),
                                        color = if (isAvailable) {
                                            MaterialTheme.colorScheme.onSurface
                                        } else {
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                                        },
                                        fontWeight = if (date == today) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun buildMonthCells(month: YearMonth): List<LocalDate?> {
    val firstDay = month.atDay(1)
    val daysInMonth = month.lengthOfMonth()
    val offset = firstDay.dayOfWeek.value - 1

    val result = mutableListOf<LocalDate?>()
    repeat(offset) { result += null }
    for (day in 1..daysInMonth) {
        result += month.atDay(day)
    }
    return result
}

private fun hasAvailability(
    date: LocalDate,
    durationMinutes: Int,
    snapshot: MasterScheduleSnapshot
): Boolean {
    val workStart = parseTime(snapshot.workStartTime)
    val workEnd = parseTime(snapshot.workEndTime)
    val duration = durationMinutes.toLong()

    val dateString = date.toString()
    val override = snapshot.dateOverrides.firstOrNull { it.date == dateString }

    val blockedIntervals = if (override?.unblockAll == true) {
        emptyList()
    } else {
        snapshot.weeklyBlockedIntervals.filter {
            it.isActive && it.dayOfWeek == date.dayOfWeek.value
        }
    }

    val busy = snapshot.busySlots.filter { it.date == dateString }

    var cursor = workStart
    while (true) {
        val slotEnd = cursor.plusMinutes(duration)
        if (slotEnd > workEnd) break

        val overlapsBusy = busy.any {
            val start = parseTime(it.startTime)
            val end = parseTime(it.endTime)
            cursor < end && start < slotEnd
        }

        val overlapsBlocked = blockedIntervals.any {
            val start = parseTime(it.startTime)
            val end = parseTime(it.endTime)
            cursor < end && start < slotEnd
        }

        if (!overlapsBusy && !overlapsBlocked) {
            return true
        }

        cursor = cursor.plusMinutes(30)
    }

    return false
}

private fun parseTime(value: String) = java.time.LocalTime.parse(value)