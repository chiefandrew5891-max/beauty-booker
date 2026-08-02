package com.beautyplanner.client.android.ui.booking

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
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
import java.text.SimpleDateFormat
import java.util.Calendar
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
    var monthState by remember { mutableStateOf(currentMonthState()) }

    val todayString = remember { formatDate(calendarNow()) }

    LaunchedEffect(masterId, serviceId) {
        service = mastersRepository
            .getServicesForMaster(masterId)
            .firstOrNull { it.id == serviceId }

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
            val cells = buildMonthCells(monthState.year, monthState.monthZeroBased)

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
                    IconButton(
                        onClick = { monthState = shiftMonth(monthState, -1) }
                    ) {
                        Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = null)
                    }

                    Text(
                        text = monthTitleRu(monthState.year, monthState.monthZeroBased),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    IconButton(
                        onClick = { monthState = shiftMonth(monthState, 1) }
                    ) {
                        Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null)
                    }
                }

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

                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    userScrollEnabled = false
                ) {
                    items(cells) { cell ->
                        if (cell == null) {
                            Box(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .aspectRatio(1f)
                            )
                        } else {
                            val dateString = formatDate(cell)
                            val isPast = dateString < todayString
                            val isToday = dateString == todayString
                            val isAvailable = !isPast && hasAvailability(
                                dateString = dateString,
                                durationMinutes = localService.durationMinutes,
                                snapshot = localSnapshot
                            )

                            val containerColor = when {
                                isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                isAvailable -> MaterialTheme.colorScheme.surfaceVariant
                                else -> MaterialTheme.colorScheme.surface
                            }

                            Card(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .aspectRatio(1f)
                                    .clickable(enabled = isAvailable) {
                                        onDateSelected(dateString)
                                    },
                                colors = CardDefaults.cardColors(containerColor = containerColor),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cell.get(Calendar.DAY_OF_MONTH).toString(),
                                        color = if (isAvailable) {
                                            MaterialTheme.colorScheme.onSurface
                                        } else {
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                                        },
                                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
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

private data class MonthState(
    val year: Int,
    val monthZeroBased: Int
)

private fun currentMonthState(): MonthState {
    val cal = calendarNow()
    return MonthState(
        year = cal.get(Calendar.YEAR),
        monthZeroBased = cal.get(Calendar.MONTH)
    )
}

private fun shiftMonth(current: MonthState, delta: Int): MonthState {
    val cal = Calendar.getInstance()
    cal.set(Calendar.YEAR, current.year)
    cal.set(Calendar.MONTH, current.monthZeroBased)
    cal.set(Calendar.DAY_OF_MONTH, 1)
    cal.add(Calendar.MONTH, delta)

    return MonthState(
        year = cal.get(Calendar.YEAR),
        monthZeroBased = cal.get(Calendar.MONTH)
    )
}

private fun buildMonthCells(year: Int, monthZeroBased: Int): List<Calendar?> {
    val firstDay = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, monthZeroBased)
        set(Calendar.DAY_OF_MONTH, 1)
        clearTime()
    }

    val daysInMonth = firstDay.getActualMaximum(Calendar.DAY_OF_MONTH)

    val dayOfWeek = firstDay.get(Calendar.DAY_OF_WEEK)
    val offset = when (dayOfWeek) {
        Calendar.MONDAY -> 0
        Calendar.TUESDAY -> 1
        Calendar.WEDNESDAY -> 2
        Calendar.THURSDAY -> 3
        Calendar.FRIDAY -> 4
        Calendar.SATURDAY -> 5
        Calendar.SUNDAY -> 6
        else -> 0
    }

    val result = mutableListOf<Calendar?>()
    repeat(offset) { result.add(null) }

    for (day in 1..daysInMonth) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, monthZeroBased)
            set(Calendar.DAY_OF_MONTH, day)
            clearTime()
        }
        result.add(cal)
    }

    return result
}

private fun hasAvailability(
    dateString: String,
    durationMinutes: Int,
    snapshot: MasterScheduleSnapshot
): Boolean {
    val workStart = parseHmToMinutes(snapshot.workStartTime) ?: return false
    val workEnd = parseHmToMinutes(snapshot.workEndTime) ?: return false

    val override = snapshot.dateOverrides.firstOrNull { it.date == dateString }
    val blockedIntervals = if (override?.unblockAll == true) {
        emptyList()
    } else {
        val dayOfWeek = isoDayNumberFromDate(dateString)
        snapshot.weeklyBlockedIntervals.filter {
            it.isActive && it.dayOfWeek == dayOfWeek
        }
    }

    val busy = snapshot.busySlots.filter { it.date == dateString }

    var cursor = workStart
    while (cursor + durationMinutes <= workEnd) {
        val slotEnd = cursor + durationMinutes

        val overlapsBusy = busy.any {
            val start = parseHmToMinutes(it.startTime) ?: return@any false
            val end = parseHmToMinutes(it.endTime) ?: return@any false
            cursor < end && start < slotEnd
        }

        val overlapsBlocked = blockedIntervals.any {
            val start = parseHmToMinutes(it.startTime) ?: return@any false
            val end = parseHmToMinutes(it.endTime) ?: return@any false
            cursor < end && start < slotEnd
        }

        if (!overlapsBusy && !overlapsBlocked) {
            return true
        }

        cursor += 30
    }

    return false
}

private fun parseHmToMinutes(value: String): Int? {
    val parts = value.split(":")
    if (parts.size != 2) return null

    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].toIntOrNull() ?: return null

    return hour * 60 + minute
}

private fun calendarNow(): Calendar {
    return Calendar.getInstance().apply { clearMillisecondsOnly() }
}

private fun formatDate(calendar: Calendar): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    return formatter.format(calendar.time)
}

private fun monthTitleRu(year: Int, monthZeroBased: Int): String {
    val month = when (monthZeroBased) {
        0 -> "Январь"
        1 -> "Февраль"
        2 -> "Март"
        3 -> "Апрель"
        4 -> "Май"
        5 -> "Июнь"
        6 -> "Июль"
        7 -> "Август"
        8 -> "Сентябрь"
        9 -> "Октябрь"
        10 -> "Ноябрь"
        11 -> "Декабрь"
        else -> ""
    }
    return "$month $year"
}

private fun isoDayNumberFromDate(date: String): Int {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val parsed = formatter.parse(date) ?: return 1

    val cal = Calendar.getInstance().apply {
        time = parsed
        clearMillisecondsOnly()
    }

    return when (cal.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> 1
        Calendar.TUESDAY -> 2
        Calendar.WEDNESDAY -> 3
        Calendar.THURSDAY -> 4
        Calendar.FRIDAY -> 5
        Calendar.SATURDAY -> 6
        Calendar.SUNDAY -> 7
        else -> 1
    }
}

private fun Calendar.clearTime() {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}

private fun Calendar.clearMillisecondsOnly() {
    set(Calendar.MILLISECOND, 0)
}