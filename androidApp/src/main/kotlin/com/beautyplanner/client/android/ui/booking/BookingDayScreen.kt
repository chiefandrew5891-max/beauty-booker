package com.beautyplanner.client.android.ui.booking

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.beautyplanner.client.domain.model.ClientProfile
import com.beautyplanner.client.domain.model.MasterService
import com.beautyplanner.client.domain.model.ScheduleSnapshot
import com.beautyplanner.client.domain.repository.BookingRepository
import com.beautyplanner.client.domain.repository.MastersRepository
import com.beautyplanner.client.strings.Strings
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDayScreen(
    masterId: String,
    serviceId: String,
    date: String,
    client: ClientProfile?,
    mastersRepository: MastersRepository,
    bookingRepository: BookingRepository,
    onTimeSelected: (String) -> Unit,
    onBackClick: () -> Unit
) {
    var service by remember { mutableStateOf<MasterService?>(null) }
    var snapshot by remember { mutableStateOf<ScheduleSnapshot?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val parsedDate = remember(date) {
        runCatching { LocalDate.parse(date) }.getOrNull() ?: LocalDate.now()
    }

    LaunchedEffect(masterId, serviceId) {
        isLoading = true
        runCatching {
            service = mastersRepository.getServicesForMaster(masterId).find { it.id == serviceId }
            snapshot = bookingRepository.getScheduleSnapshot(masterId)
        }
        isLoading = false
    }

    val dateTitle = remember(parsedDate) {
        val dayOfWeek = parsedDate.dayOfWeek.getDisplayName(TextStyle.FULL_STANDALONE, Locale("ru"))
            .replaceFirstChar { it.uppercase() }
        val monthName = parsedDate.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale("ru"))
        "${parsedDate.dayOfMonth} $monthName, $dayOfWeek"
    }

    Scaffold(
        topBar = {
            ClientTopBar(
                title = dateTitle,
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
            val snap = snapshot
            val svc = service
            if (snap != null && svc != null) {
                DaySlotList(
                    date = parsedDate,
                    snapshot = snap,
                    service = svc,
                    innerPadding = innerPadding,
                    onTimeSelected = onTimeSelected
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Нет доступных данных",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DaySlotList(
    date: LocalDate,
    snapshot: ScheduleSnapshot,
    service: MasterService,
    innerPadding: PaddingValues,
    onTimeSelected: (String) -> Unit
) {
    val today = LocalDate.now()
    val nowTime = LocalTime.now()

    val workStart = parseTimeHm(snapshot.workDayStart) ?: LocalTime.of(9, 0)
    val workEnd = parseTimeHm(snapshot.workDayEnd) ?: LocalTime.of(18, 0)
    val duration = service.durationMinutes.toLong()

    val dateStr = date.toString()
    val dayBusy = snapshot.busySlots.filter { it.date == dateStr }
    val dayOfWeek = date.dayOfWeek.value
    val weeklyBlocked = snapshot.weeklyBlockedIntervals.filter { it.dayOfWeek == dayOfWeek }

    // Check if date override marks this as non-working
    val override = snapshot.dateOverrides.find { it.date == dateStr }
    val isNonWorkingDay = override != null && !override.isWorkingDay

    data class TimeSlot(
        val startTime: LocalTime,
        val endTime: LocalTime,
        val isAvailable: Boolean
    )

    val slots = remember(date, snapshot, service) {
        if (isNonWorkingDay) return@remember emptyList<TimeSlot>()

        val result = mutableListOf<TimeSlot>()
        var cursor = workStart

        while (true) {
            val slotEnd = cursor.plusMinutes(duration)
            if (slotEnd > workEnd) break

            val isPastTime = date == today && cursor < nowTime

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

            val available = !isPastTime && !isBusy && !isWeeklyBlocked
            result.add(TimeSlot(startTime = cursor, endTime = slotEnd, isAvailable = available))
            cursor = cursor.plusMinutes(SLOT_STEP_MINUTES)
        }
        result
    }

    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    if (isNonWorkingDay) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Выходной день",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    if (slots.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Нет доступных слотов",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        Text(
            text = "${service.titleRu} · ${service.durationMinutes} мин",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items(slots.size) { index ->
                val slot = slots[index]
                val interactionSource = remember { MutableInteractionSource() }

                val startLabel = slot.startTime.format(timeFormatter)
                val endLabel = slot.endTime.format(timeFormatter)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .height(64.dp)
                        .then(
                            if (slot.isAvailable) {
                                Modifier.clickable(
                                    interactionSource = interactionSource,
                                    indication = null
                                ) {
                                    val slotId = "${date}T${startLabel}:00"
                                    onTimeSelected(slotId)
                                }
                            } else Modifier
                        ),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (slot.isAvailable)
                            MaterialTheme.colorScheme.surface
                        else
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (slot.isAvailable)
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        else
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = startLabel,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (slot.isAvailable)
                                MaterialTheme.colorScheme.onSurface
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "– $endLabel",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (slot.isAvailable)
                                MaterialTheme.colorScheme.onSurfaceVariant
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        if (slot.isAvailable) {
                            Text(
                                text = Strings.BOOK_NOW,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else {
                            Text(
                                text = "Занято",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                            )
                        }
                    }
                }
            }
        }
    }
}
