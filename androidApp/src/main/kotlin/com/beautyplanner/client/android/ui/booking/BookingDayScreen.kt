package com.beautyplanner.client.android.ui.booking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
fun BookingDayScreen(
    masterId: String,
    serviceId: String,
    date: String,
    mastersRepository: MastersRepository,
    bookingRepository: BookingRepository,
    onTimeSelected: (String) -> Unit,
    onBackClick: () -> Unit
) {
    var service by remember { mutableStateOf<MasterService?>(null) }
    var snapshot by remember { mutableStateOf<MasterScheduleSnapshot?>(null) }
    var availableTimes by remember { mutableStateOf<List<String>>(emptyList()) }
    var busyTimes by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(masterId, serviceId, date) {
        val loadedService = mastersRepository
            .getServicesForMaster(masterId)
            .firstOrNull { it.id == serviceId }

        val loadedSnapshot = bookingRepository.getScheduleSnapshot(masterId)

        service = loadedService
        snapshot = loadedSnapshot

        if (loadedService != null) {
            availableTimes = buildAvailableTimesForDay(
                date = date,
                durationMinutes = loadedService.durationMinutes,
                snapshot = loadedSnapshot
            )

            busyTimes = loadedSnapshot.busySlots
                .filter { it.date == date }
                .map { "${it.startTime} - ${it.endTime}" }
        }
    }

    Scaffold(
        topBar = {
            ClientTopBar(
                title = "Выбранный день",
                showBack = true,
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = formatDateRu(date),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                service?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${it.titleRu} · ${it.durationMinutes} мин",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider()

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (busyTimes.isNotEmpty()) {
                    item {
                        Text(
                            text = "Занято",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    items(busyTimes) { busy ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                text = busy,
                                modifier = Modifier.padding(14.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                item {
                    Text(
                        text = "Свободное время",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                }

                if (availableTimes.isEmpty()) {
                    item {
                        Text(
                            text = "На этот день нет доступного времени",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    items(availableTimes) { startTime ->
                        TimeItem(
                            startTime = startTime,
                            onClick = {
                                onTimeSelected("${date}T${startTime}:00")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeItem(
    startTime: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = startTime,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )
            Button(onClick = onClick) {
                Text("Выбрать")
            }
        }
    }
}

private fun buildAvailableTimesForDay(
    date: String,
    durationMinutes: Int,
    snapshot: MasterScheduleSnapshot
): List<String> {
    val result = mutableListOf<String>()
    val workStart = parseHmToMinutes(snapshot.workStartTime) ?: return emptyList()
    val workEnd = parseHmToMinutes(snapshot.workEndTime) ?: return emptyList()

    val override = snapshot.dateOverrides.firstOrNull { it.date == date }
    val blockedIntervals = if (override?.unblockAll == true) {
        emptyList()
    } else {
        val dayOfWeek = isoDayNumberFromDate(date)
        snapshot.weeklyBlockedIntervals.filter {
            it.isActive && it.dayOfWeek == dayOfWeek
        }
    }

    val busy = snapshot.busySlots.filter { it.date == date }

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
            result += minutesToHm(cursor)
        }

        cursor += 30
    }

    return result
}

private fun parseHmToMinutes(value: String): Int? {
    val parts = value.split(":")
    if (parts.size != 2) return null

    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].toIntOrNull() ?: return null

    return hour * 60 + minute
}

private fun minutesToHm(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return "${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}"
}

private fun isoDayNumberFromDate(date: String): Int {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val parsed = formatter.parse(date) ?: return 1

    val cal = Calendar.getInstance().apply {
        time = parsed
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
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

private fun formatDateRu(date: String): String {
    val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val parsed = parser.parse(date) ?: return date

    val cal = Calendar.getInstance().apply { time = parsed }

    val day = cal.get(Calendar.DAY_OF_MONTH)
    val year = cal.get(Calendar.YEAR)
    val month = when (cal.get(Calendar.MONTH)) {
        0 -> "января"
        1 -> "февраля"
        2 -> "марта"
        3 -> "апреля"
        4 -> "мая"
        5 -> "июня"
        6 -> "июля"
        7 -> "августа"
        8 -> "сентября"
        9 -> "октября"
        10 -> "ноября"
        11 -> "декабря"
        else -> ""
    }

    return "$day $month $year"
}