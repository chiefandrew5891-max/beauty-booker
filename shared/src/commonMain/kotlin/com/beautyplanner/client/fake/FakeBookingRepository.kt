package com.beautyplanner.client.fake

import com.beautyplanner.client.domain.model.AvailableSlot
import com.beautyplanner.client.domain.model.BookingRequest
import com.beautyplanner.client.domain.model.BookingStatus
import com.beautyplanner.client.domain.model.BusySlot
import com.beautyplanner.client.domain.model.MasterScheduleSnapshot
import com.beautyplanner.client.domain.model.ScheduleDateOverride
import com.beautyplanner.client.domain.model.WeeklyBlockedInterval
import com.beautyplanner.client.domain.repository.BookingRepository

class FakeBookingRepository : BookingRepository {

    private val bookings = mutableListOf<BookingRequest>()

    override suspend fun getAvailableSlots(masterId: String, serviceId: String): List<AvailableSlot> {
        return listOf(
            AvailableSlot("slot-1", masterId, "2026-08-01T10:00:00", "2026-08-01T11:00:00"),
            AvailableSlot("slot-2", masterId, "2026-08-01T12:00:00", "2026-08-01T13:00:00"),
            AvailableSlot("slot-3", masterId, "2026-08-02T10:00:00", "2026-08-02T11:00:00")
        )
    }

    override suspend fun getScheduleSnapshot(masterId: String): MasterScheduleSnapshot {
        return MasterScheduleSnapshot(
            workStartTime = "08:00",
            workEndTime = "20:00",
            busySlots = listOf(
                BusySlot(date = "2026-08-01", startTime = "10:00", endTime = "11:00"),
                BusySlot(date = "2026-08-01", startTime = "14:00", endTime = "15:30"),
                BusySlot(date = "2026-08-02", startTime = "09:00", endTime = "10:00")
            ),
            weeklyBlockedIntervals = listOf(
                WeeklyBlockedInterval(dayOfWeek = 7, startTime = "08:00", endTime = "20:00")
            ),
            dateOverrides = listOf(
                ScheduleDateOverride(date = "2026-08-03", unblockAll = true)
            )
        )
    }

    override suspend fun submitBooking(request: BookingRequest): Result<BookingRequest> {
        val confirmed = request.copy(status = BookingStatus.CONFIRMED)
        bookings.add(confirmed)
        return Result.success(confirmed)
    }

    override suspend fun getBookingsForClient(clientId: String): List<BookingRequest> =
        bookings.filter { it.clientId == clientId }

    override suspend fun cancelBooking(bookingId: String): Result<Unit> {
        val index = bookings.indexOfFirst { it.id == bookingId }
        if (index >= 0) {
            bookings[index] = bookings[index].copy(status = BookingStatus.CANCELLED)
        }
        return Result.success(Unit)
    }
}