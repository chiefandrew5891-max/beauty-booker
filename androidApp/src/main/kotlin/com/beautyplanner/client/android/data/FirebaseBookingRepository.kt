package com.beautyplanner.client.android.data

import com.beautyplanner.client.domain.model.AvailableSlot
import com.beautyplanner.client.domain.model.BookingRequest
import com.beautyplanner.client.domain.model.BookingStatus
import com.beautyplanner.client.domain.model.MasterService
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

        val busySlots = loadBusySlots(masterId)
        return buildAvailableSlots(
            masterId = masterId,
            service = service,
            busySlots = busySlots
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

    private suspend fun loadBusySlots(masterId: String): List<BusySlot> {
        val snap = firestore
            .collection("master_public_schedule")
            .document(masterId)
            .get()
            .await()

        if (!snap.exists()) return emptyList()

        val autoPublishBusySlots = snap.getBoolean("autoPublishBusySlots") ?: false
        if (!autoPublishBusySlots) return emptyList()

        val raw = snap.get("busySlots") as? List<Map<String, Any?>> ?: return emptyList()

        return raw.mapNotNull { item ->
            val date = item["date"]?.toString()?.trim().orEmpty()
            val startTime = item["startTime"]?.toString()?.trim().orEmpty()
            val endTime = item["endTime"]?.toString()?.trim().orEmpty()

            if (date.isBlank() || startTime.isBlank() || endTime.isBlank()) {
                null
            } else {
                BusySlot(
                    date = date,
                    startTime = startTime,
                    endTime = endTime
                )
            }
        }
    }

    private fun buildAvailableSlots(
        masterId: String,
        service: MasterService,
        busySlots: List<BusySlot>
    ): List<AvailableSlot> {
        val result = mutableListOf<AvailableSlot>()
        val today = LocalDate.now()
        val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
        val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

        for (dayOffset in 0 until 14) {
            val date = today.plusDays(dayOffset.toLong())
            val dateString = date.format(dateFormatter)

            val dayBusy = busySlots
                .filter { it.date == dateString }
                .sortedBy { parseTime(it.startTime) }

            var cursor = LocalTime.of(9, 0)
            val endOfDay = LocalTime.of(18, 0)
            val duration = service.durationMinutes.toLong()

            while (true) {
                val slotEnd = cursor.plusMinutes(duration)
                if (slotEnd > endOfDay) break

                val overlapsBusy = dayBusy.any { busy ->
                    val busyStart = parseTime(busy.startTime)
                    val busyEnd = parseTime(busy.endTime)
                    cursor < busyEnd && busyStart < slotEnd
                }

                if (!overlapsBusy) {
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

    private fun parseTime(value: String): LocalTime {
        return LocalTime.parse(value.trim())
    }

    private data class BusySlot(
        val date: String,
        val startTime: String,
        val endTime: String
    )
}