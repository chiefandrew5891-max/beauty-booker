package com.beautyplanner.client.android.ui.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.beautyplanner.client.android.ui.common.ClientTopBar
import com.beautyplanner.client.domain.model.ClientProfile
import com.beautyplanner.client.domain.model.MasterService
import com.beautyplanner.client.domain.model.ScheduleSnapshot
import com.beautyplanner.client.domain.repository.BookingRepository
import com.beautyplanner.client.domain.repository.MastersRepository
import com.beautyplanner.client.strings.Strings
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingCalendarScreen(
    masterId: String,
    serviceId: String,
    client: ClientProfile?,
    mastersRepository: MastersRepository,
    bookingRepository: BookingRepository,
    onDateSelected: (String) -> Unit,
    onBackClick: () -> Unit
) {
    var service by remember { mutableStateOf<MasterService?>(null) }
    var snapshot by remember { mutableStateOf<ScheduleSnapshot?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    LaunchedEffect(masterId, serviceId) {
        isLoading = true
        runCatching {
            service = mastersRepository.getServicesForMaster(masterId).find { it.id == serviceId }
            snapshot = bookingRepository.getScheduleSnapshot(masterId)
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            ClientTopBar(
                title = Strings.BOOKING_DATE_TIME_TITLE,
                showBack = true,
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                item {
                    val svc = service
                    if (svc != null) {
                        Text(
                            text = "${svc.titleRu} · ${svc.durationMinutes} мин",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }
                }

                item {
                    MonthNavigationRow(
                        currentMonth = currentMonth,
                        onPrev = { currentMonth = currentMonth.minusMonths(1) },
                        onNext = { currentMonth = currentMonth.plusMonths(1) }
                    )
                }

                item {
                    val snap = snapshot
                    val svc = service
                    if (snap != null && svc != null) {
                        CalendarGrid(
                            month = currentMonth,
                            snapshot = snap,
                            durationMinutes = svc.durationMinutes,
                            onDateClick = { date ->
                                onDateSelected(date.toString())
                            }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LegendDot(color = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "Доступно",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        LegendDot(color = MaterialTheme.colorScheme.surfaceVariant)
                        Text(
                            text = "Недоступно",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthNavigationRow(
    currentMonth: YearMonth,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    val today = YearMonth.now()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = onPrev,
            enabled = currentMonth > today
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Previous month"
            )
        }

        val monthName = currentMonth.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale("ru"))
            .replaceFirstChar { it.uppercase() }
        Text(
            text = "$monthName ${currentMonth.year}",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
        )

        IconButton(onClick = onNext) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next month"
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    month: YearMonth,
    snapshot: ScheduleSnapshot,
    durationMinutes: Int,
    onDateClick: (LocalDate) -> Unit
) {
    val today = LocalDate.now()
    val daysInMonth = month.lengthOfMonth()
    val firstDay = LocalDate.of(month.year, month.month, 1)
    // dayOfWeek: 1=Mon..7=Sun → offset for first column (0-based)
    val offset = firstDay.dayOfWeek.value - 1

    val weekdays = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        val cellSize = (maxWidth / 7)

        Column {
            // Weekday header row
            Row(modifier = Modifier.fillMaxWidth()) {
                weekdays.forEach { day ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            val totalCells = offset + daysInMonth
            val rows = (totalCells + 6) / 7

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(cellSize * rows),
                userScrollEnabled = false
            ) {
                // Empty cells before the first day
                items(offset) {
                    Box(modifier = Modifier.size(cellSize))
                }

                items((1..daysInMonth).toList()) { day ->
                    val date = LocalDate.of(month.year, month.month, day)
                    val isPast = date < today
                    val isToday = date == today
                    val available = !isPast && hasAvailableSlot(date, durationMinutes, snapshot)
                    val interactionSource = remember { MutableInteractionSource() }

                    Box(
                        modifier = Modifier
                            .size(cellSize)
                            .padding(3.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                when {
                                    available -> MaterialTheme.colorScheme.primaryContainer
                                    isToday && !available -> MaterialTheme.colorScheme.surfaceVariant
                                    else -> Color.Transparent
                                }
                            )
                            .then(
                                if (available) {
                                    Modifier.clickable(
                                        interactionSource = interactionSource,
                                        indication = null,
                                        onClick = { onDateClick(date) }
                                    )
                                } else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = when {
                                isPast -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                                available -> MaterialTheme.colorScheme.onPrimaryContainer
                                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                            },
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color) {
    Box(
        modifier = Modifier
            .size(12.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(color)
    )
}

/** Returns true if there is at least one available time slot of [durationMinutes] on [date]. */
internal fun hasAvailableSlot(
    date: LocalDate,
    durationMinutes: Int,
    snapshot: ScheduleSnapshot
): Boolean {
    val today = LocalDate.now()
    if (date < today) return false

    // Check date overrides
    val override = snapshot.dateOverrides.find { it.date == date.toString() }
    if (override != null && !override.isWorkingDay) return false

    val workStart = parseTimeHm(snapshot.workDayStart) ?: LocalTime.of(9, 0)
    val workEnd = parseTimeHm(snapshot.workDayEnd) ?: LocalTime.of(18, 0)

    val dateStr = date.toString()
    val dayBusy = snapshot.busySlots.filter { it.date == dateStr }
    val dayOfWeek = date.dayOfWeek.value
    val weeklyBlocked = snapshot.weeklyBlockedIntervals.filter { it.dayOfWeek == dayOfWeek }
    val duration = durationMinutes.toLong()

    var cursor = workStart
    while (true) {
        val slotEnd = cursor.plusMinutes(duration)
        if (slotEnd > workEnd) break

        val isBusy = dayBusy.any { busy ->
            val bStart = parseTimeHm(busy.startTime) ?: return@any false
            val bEnd = parseTimeHm(busy.endTime) ?: return@any false
            cursor < bEnd && bStart < slotEnd
        }
        val isWeeklyBlocked = weeklyBlocked.any { interval ->
            val bStart = parseTimeHm(interval.startTime) ?: return@any false
            val bEnd = parseTimeHm(interval.endTime) ?: return@any false
            cursor < bEnd && bStart < slotEnd
        }

        if (!isBusy && !isWeeklyBlocked) return true
        cursor = cursor.plusMinutes(30)
    }
    return false
}

internal fun parseTimeHm(value: String): LocalTime? {
    return runCatching { LocalTime.parse(value.trim()) }.getOrNull()
}
