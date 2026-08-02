package com.beautyplanner.client.data

import com.beautyplanner.client.domain.model.AvailableSlot
import com.beautyplanner.client.domain.model.BookingRequest
import com.beautyplanner.client.domain.model.BookingStatus
import com.beautyplanner.client.domain.model.BusySlot
import com.beautyplanner.client.domain.model.MasterScheduleSnapshot
import com.beautyplanner.client.domain.model.MasterService
import com.beautyplanner.client.domain.model.ScheduleDateOverride
import com.beautyplanner.client.domain.model.WeeklyBlockedInterval
import com.beautyplanner.client.domain.repository.BookingRepository
import com.beautyplanner.client.domain.repository.MastersRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.max

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

        return buildAvailableSlots(
            masterId = masterId,
            service = service,
            snapshot = snapshot
        )
    }

    override suspend fun getScheduleSnapshot(masterId: String): MasterScheduleSnapshot {
        val snap = firestore
            .collection("master_public_schedule")
            .document(masterId)
            .get()
            .await()

        if (!snap.exists()) {
            return MasterScheduleSnapshot()
        }

        val autoPublishBusySlots = snap.getBoolean("autoPublishBusySlots") ?: false
        val workStartTime = snap.getString("workStartTime")?.trim().orEmpty().ifBlank { "08:00" }
        val workEndTime = snap.getString("workEndTime")?.trim().orEmpty().ifBlank { "20:00" }

        val busySlots = if (autoPublishBusySlots) {
            val rawBusy = snap.get("busySlots") as? List<Map<String, Any?>> ?: emptyList()
            rawBusy.mapNotNull { item ->
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
        } else {
            emptyList()
        }

        val rawWeeklyBlocked =
            snap.get("weeklyBlockedIntervals") as? List<Map<String, Any?>> ?: emptyList()

        val weeklyBlockedIntervals = rawWeeklyBlocked.mapNotNull { item ->
            val dayOfWeek = when (val raw = item["dayOfWeek"]) {
                is Number -> raw.toInt()
                is String -> raw.toIntOrNull()
                else -> null
            } ?: return@mapNotNull null

            val startTime = item["startTime"]?.toString()?.trim().orEmpty()
            val endTime = item["endTime"]?.toString()?.trim().orEmpty()

            val isActive = when (val raw = item["isActive"]) {
                is Boolean -> raw
                is String -> raw.equals("true", ignoreCase = true)
                is Number -> raw.toInt() != 0
                null -> true
                else -> true
            }

            if (startTime.isBlank() || endTime.isBlank()) {
                null
            } else {
                WeeklyBlockedInterval(
                    dayOfWeek = dayOfWeek,
                    startTime = startTime,
                    endTime = endTime,
                    isActive = isActive
                )
            }
        }

        val rawOverrides = snap.get("dateOverrides") as? List<Map<String, Any?>> ?: emptyList()

        val dateOverrides = rawOverrides.mapNotNull { item ->
            val date = item["date"]?.toString()?.trim().orEmpty()
            if (date.isBlank()) return@mapNotNull null

            val unblockAll = when (val raw = item["unblockAll"]) {
                is Boolean -> raw
                is String -> raw.equals("true", ignoreCase = true)
                is Number -> raw.toInt() != 0
                else -> false
            }

            ScheduleDateOverride(
                date = date,
                unblockAll = unblockAll
            )
        }

        return MasterScheduleSnapshot(
            workStartTime = workStartTime,
            workEndTime = workEndTime,
            busySlots = busySlots,
            weeklyBlockedIntervals = weeklyBlockedIntervals,
            dateOverrides = dateOverrides
        )
    }

    override suspend fun submitBooking(request: BookingRequest): Result<BookingRequest> {
        return runCatching {
            val appointmentId = request.id.ifBlank {
                "appt_${System.currentTimeMillis()}"
            }

            val appointmentDateTime = request.appointmentDateTime.trim()
            val dateString = appointmentDateTime.substringBefore("T")
            val time = appointmentDateTime.substringAfter("T").substringBeforeLast(":")

            val nowMillis = System.currentTimeMillis()

            val service = mastersRepository
                .getServicesForMaster(request.masterId)
                .firstOrNull { it.id == request.serviceId }

            val serviceName = service?.titleRu.orEmpty()
            val price = service?.price?.let { value ->
                if (value % 1.0 == 0.0) {
                    value.toInt().toString()
                } else {
                    value.toString()
                }
            }.orEmpty()

            val durationMinutes = service?.durationMinutes ?: 0
            val durationHours = if (durationMinutes > 0) {
                max(1, durationMinutes / 60)
            } else {
                1
            }

            val currency = service?.currency?.ifBlank { "EUR" } ?: "EUR"

            // Пока используем clientId как fallback.
            // Позже лучше заменить на настоящее имя клиента из профиля.
            val clientName = request.clientNickname.ifBlank { request.clientId }
            val phone = ""

            val payloadJson = buildAppointmentPayloadJson(
                id = appointmentId,
                dateString = dateString,
                time = time,
                clientName = clientName,
                phone = phone,
                serviceName = serviceName,
                price = price,
                durationMinutes = durationMinutes,
                durationHours = durationHours,
                notes = request.noteFromClient,
                paymentDeferred = false,
                paymentStatus = "",
                updatedAtMillis = nowMillis,
                isDeleted = false,
                currency = currency,
                bookingSource = "online"
            )

            val docData = mapOf(
                "id" to appointmentId,
                "dateString" to dateString,
                "time" to time,
                "updatedAtMillis" to nowMillis,
                "isDeleted" to false,
                "paymentStatus" to "",
                "bookingSource" to "online",
                "payload" to payloadJson
            )

            firestore
                .collection("users")
                .document(request.masterId)
                .collection("appointments")
                .document(appointmentId)
                .set(docData)
                .await()

            request.copy(
                id = appointmentId,
                status = BookingStatus.PENDING
            )
        }
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
        snapshot: MasterScheduleSnapshot
    ): List<AvailableSlot> {
        val result = mutableListOf<AvailableSlot>()

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val workStart = parseHmToMinutes(snapshot.workStartTime) ?: return emptyList()
        val workEnd = parseHmToMinutes(snapshot.workEndTime) ?: return emptyList()
        val duration = service.durationMinutes

        for (dayOffset in 0 until 31) {
            val dateCal = (today.clone() as Calendar).apply {
                add(Calendar.DAY_OF_MONTH, dayOffset)
            }

            val dateString = formatDate(dateCal)

            val override = snapshot.dateOverrides.firstOrNull { it.date == dateString }
            val dayBusy = snapshot.busySlots.filter { it.date == dateString }

            val blockedIntervals = if (override?.unblockAll == true) {
                emptyList()
            } else {
                val isoDay = isoDayOfWeek(dateCal)
                snapshot.weeklyBlockedIntervals.filter {
                    it.isActive && it.dayOfWeek == isoDay
                }
            }

            var cursor = workStart
            while (cursor + duration <= workEnd) {
                val slotEnd = cursor + duration

                val overlapsBusy = dayBusy.any { busy ->
                    val busyStart = parseHmToMinutes(busy.startTime) ?: return@any false
                    val busyEnd = parseHmToMinutes(busy.endTime) ?: return@any false
                    cursor < busyEnd && busyStart < slotEnd
                }

                val overlapsBlocked = blockedIntervals.any { blocked ->
                    val blockedStart = parseHmToMinutes(blocked.startTime) ?: return@any false
                    val blockedEnd = parseHmToMinutes(blocked.endTime) ?: return@any false
                    cursor < blockedEnd && blockedStart < slotEnd
                }

                if (!overlapsBusy && !overlapsBlocked) {
                    val startDateTime = "${dateString}T${minutesToHm(cursor)}:00"
                    val endDateTime = "${dateString}T${minutesToHm(slotEnd)}:00"

                    result += AvailableSlot(
                        id = "${masterId}_${service.id}_${dateString}_${minutesToHm(cursor)}",
                        masterId = masterId,
                        startDateTime = startDateTime,
                        endDateTime = endDateTime,
                        isBooked = false
                    )
                }

                cursor += 30
            }
        }

        return result
    }

    private fun parseHmToMinutes(value: String): Int? {
        val parts = value.split(":")
        if (parts.size != 2) return null

        val hour = parts[0].toIntOrNull() ?: return null
        val minute = parts[1].toIntOrNull() ?: return null

        if (hour !in 0..23 || minute !in 0..59) return null

        return hour * 60 + minute
    }

    private fun minutesToHm(totalMinutes: Int): String {
        val normalized = totalMinutes.coerceAtLeast(0)
        val hour = normalized / 60
        val minute = normalized % 60
        return "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
    }

    private fun formatDate(calendar: Calendar): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.time)
    }

    private fun isoDayOfWeek(calendar: Calendar): Int {
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
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

    private fun buildAppointmentPayloadJson(
        id: String,
        dateString: String,
        time: String,
        clientName: String,
        phone: String,
        serviceName: String,
        price: String,
        durationMinutes: Int,
        durationHours: Int,
        notes: String,
        paymentDeferred: Boolean,
        paymentStatus: String,
        updatedAtMillis: Long,
        isDeleted: Boolean,
        currency: String,
        bookingSource: String
    ): String {
        return """
        {
          "id": ${jsonString(id)},
          "dateString": ${jsonString(dateString)},
          "time": ${jsonString(time)},
          "clientName": ${jsonString(clientName)},
          "phone": ${jsonString(phone)},
          "serviceName": ${jsonString(serviceName)},
          "price": ${jsonString(price)},
          "durationMinutes": $durationMinutes,
          "durationHours": $durationHours,
          "notes": ${jsonString(notes)},
          "paymentDeferred": $paymentDeferred,
          "paymentStatus": ${jsonString(paymentStatus)},
          "updatedAtMillis": $updatedAtMillis,
          "isDeleted": $isDeleted,
          "currency": ${jsonString(currency)},
          "bookingSource": ${jsonString(bookingSource)}
        }
        """.trimIndent()
    }

    private fun jsonString(value: String): String {
        val escaped = value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
        return "\"$escaped\""
    }
}