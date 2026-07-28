package com.beautyplanner.client.android.data

import com.beautyplanner.client.domain.model.AvailableSlot
import com.beautyplanner.client.domain.model.BookingRequest
import com.beautyplanner.client.domain.model.BookingStatus
import com.beautyplanner.client.domain.model.BusySlotModel
import com.beautyplanner.client.domain.model.DateOverride
import com.beautyplanner.client.domain.model.MasterService
import com.beautyplanner.client.domain.model.ScheduleSnapshot
import com.beautyplanner.client.domain.model.WeeklyBlockedInterval
import com.beautyplanner.client.domain.repository.BookingRepository
import com.beautyplanner.client.domain.repository.MastersRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class FirebaseBookingRepository(
    private val firestore: FirebaseFirestore,
    private val mastersRepository: MastersRepository
) : BookingRepository {

    override suspend fun getAvailableSlots(
        masterId: String,
        serviceId: String
    ): List<AvailableSlot> {
        val service = mastersRepository
            .getServicesForMaster(masterId)
            .firstOrNull { it.id == serviceId }
            ?: return emptyList()

        val snapshot = getScheduleSnapshot(masterId)
        return buildAvailableSlots(masterId = masterId, service = service, snapshot = snapshot)
    }

    override suspend fun getScheduleSnapshot(masterId: String): ScheduleSnapshot {
        val snap = firestore
            .collection("master_public_schedule")
            .document(masterId)
            .get()
            .await()

        if (!snap.exists()) {
            return ScheduleSnapshot(masterId = masterId)
        }

        val autoPublish = snap.getBoolean("autoPublishBusySlots") ?: false

        val workDayStart = snap.getString("workDayStart")?.trim()?.takeIf { it.isNotBlank() } ?: "09:00"
        val workDayEnd = snap.getString("workDayEnd")?.trim()?.takeIf { it.isNotBlank() } ?: "18:00"

        val busySlots: List<BusySlotModel> = if (autoPublish) {
            (snap.get("busySlots") as? List<Map<String, Any?>> ?: emptyList()).mapNotNull { item ->
                val date = item["date"]?.toString()?.trim().orEmpty()
                val startTime = item["startTime"]?.toString()?.trim().orEmpty()
                val endTime = item["endTime"]?.toString()?.trim().orEmpty()
                if (date.isBlank() || startTime.isBlank() || endTime.isBlank()) null
                else BusySlotModel(date = date, startTime = startTime, endTime = endTime)
            }
        } else emptyList()

        val weeklyBlockedIntervals: List<WeeklyBlockedInterval> =
            (snap.get("weeklyBlockedIntervals") as? List<Map<String, Any?>> ?: emptyList())
                .mapNotNull { item ->
                    val dow = (item["dayOfWeek"] as? Number)?.toInt() ?: return@mapNotNull null
                    val startTime = item["startTime"]?.toString()?.trim().orEmpty()
                    val endTime = item["endTime"]?.toString()?.trim().orEmpty()
                    if (startTime.isBlank() || endTime.isBlank()) null
                    else WeeklyBlockedInterval(dayOfWeek = dow, startTime = startTime, endTime = endTime)
                }

        val dateOverrides: List<DateOverride> =
            (snap.get("dateOverrides") as? List<Map<String, Any?>> ?: emptyList())
                .mapNotNull { item ->
                    val date = item["date"]?.toString()?.trim().orEmpty()
                    val isWorking = item["isWorkingDay"] as? Boolean ?: return@mapNotNull null
                    if (date.isBlank()) null
                    else DateOverride(date = date, isWorkingDay = isWorking)
                }

        return ScheduleSnapshot(
            masterId = masterId,
            workDayStart = workDayStart,
            workDayEnd = workDayEnd,
            autoPublishBusySlots = autoPublish,
            busySlots = busySlots,
            weeklyBlockedIntervals = weeklyBlockedIntervals,
            dateOverrides = dateOverrides
        )
    }

    override suspend fun submitBooking(request: BookingRequest): Result<BookingRequest> {
        return Result.success(
            request.copy(status = BookingStatus.PENDING)
        )
    }

    override suspend fun getBookingsForClient(clientId: String): List<BookingRequest> {
        return emptyList()
    }

    override suspend fun cancelBooking(bookingId: String): Result<Unit> {
        return Result.success(Unit)
    }

    private fun buildAvailableSlots(
        masterId: String,
        service: MasterService,
        snapshot: ScheduleSnapshot
    ): List<AvailableSlot> {
        val result = mutableListOf<AvailableSlot>()
        val today = LocalDate.now()
        val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
        val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

        for (dayOffset in 0 until 14) {
            val date = today.plusDays(dayOffset.toLong())
            val dateString = date.format(dateFormatter)

            val dayBusy = snapshot.busySlots
                .filter { it.date == dateString }
                .sortedBy { parseTimeHm(it.startTime) ?: LocalTime.MIN }

            val dayOfWeek = date.dayOfWeek.value
            val weeklyBlocked = snapshot.weeklyBlockedIntervals.filter { it.dayOfWeek == dayOfWeek }

            var cursor = parseTimeHm(snapshot.workDayStart) ?: LocalTime.of(9, 0)
            val endOfDay = parseTimeHm(snapshot.workDayEnd) ?: LocalTime.of(18, 0)
            val duration = service.durationMinutes.toLong()

            while (true) {
                val slotEnd = cursor.plusMinutes(duration)
                if (slotEnd > endOfDay) break

                val overlapsBusy = dayBusy.any { busy ->
                    val busyStart = parseTimeHm(busy.startTime) ?: return@any false
                    val busyEnd = parseTimeHm(busy.endTime) ?: return@any false
                    cursor < busyEnd && busyStart < slotEnd
                }

                val overlapsWeeklyBlock = weeklyBlocked.any { interval ->
                    val blockStart = parseTimeHm(interval.startTime) ?: return@any false
                    val blockEnd = parseTimeHm(interval.endTime) ?: return@any false
                    cursor < blockEnd && blockStart < slotEnd
                }

                if (!overlapsBusy && !overlapsWeeklyBlock) {
                    val startDateTime = LocalDateTime.of(date, cursor)
                    val endDateTime = LocalDateTime.of(date, slotEnd)

                    result += AvailableSlot(
                        id = "${masterId}_${service.id}_${dateString}_${cursor}",
                        masterId = masterId,
                        startDateTime = startDateTime.format(dateTimeFormatter),
                        endDateTime = endDateTime.format(dateTimeFormatter),
                        isBooked = false
                    )
                }

                cursor = cursor.plusMinutes(30)
            }
        }

        return result
    }

    private fun parseTimeHm(value: String): LocalTime? {
        return runCatching { LocalTime.parse(value.trim()) }.getOrNull()
    }
}