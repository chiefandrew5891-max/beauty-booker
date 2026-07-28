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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
        val loadedService = mastersRepository.getServicesForMaster(masterId).firstOrNull { it.id == serviceId }
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
                    text = date,
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

                    item { Spacer(modifier = Modifier.height(8.dp)) }
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
                                val slotId = buildSlotId(masterId, serviceId, date, startTime)
                                onTimeSelected(slotId)
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
    val localDate = LocalDate.parse(date)
    val workStart = java.time.LocalTime.parse(snapshot.workStartTime)
    val workEnd = java.time.LocalTime.parse(snapshot.workEndTime)
    val duration = durationMinutes.toLong()

    val override = snapshot.dateOverrides.firstOrNull { it.date == date }
    val blockedIntervals = if (override?.unblockAll == true) {
        emptyList()
    } else {
        snapshot.weeklyBlockedIntervals.filter {
            it.isActive && it.dayOfWeek == localDate.dayOfWeek.value
        }
    }

    val busy = snapshot.busySlots.filter { it.date == date }

    var cursor = workStart
    while (true) {
        val slotEnd = cursor.plusMinutes(duration)
        if (slotEnd > workEnd) break

        val overlapsBusy = busy.any {
            val start = java.time.LocalTime.parse(it.startTime)
            val end = java.time.LocalTime.parse(it.endTime)
            cursor < end && start < slotEnd
        }

        val overlapsBlocked = blockedIntervals.any {
            val start = java.time.LocalTime.parse(it.startTime)
            val end = java.time.LocalTime.parse(it.endTime)
            cursor < end && start < slotEnd
        }

        if (!overlapsBusy && !overlapsBlocked) {
            result += cursor.toString()
        }

        cursor = cursor.plusMinutes(30)
    }

    return result
}

private fun buildSlotId(
    masterId: String,
    serviceId: String,
    date: String,
    startTime: String
): String {
    val dateTime = LocalDateTime.of(
        LocalDate.parse(date),
        java.time.LocalTime.parse(startTime)
    )

    return "${masterId}_${serviceId}_${dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}"
}