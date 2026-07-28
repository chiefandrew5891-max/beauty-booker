package com.beautyplanner.client.fake

import com.beautyplanner.client.domain.model.AvailableSlot
import com.beautyplanner.client.domain.model.BookingRequest
import com.beautyplanner.client.domain.model.BookingStatus
import com.beautyplanner.client.domain.model.ScheduleSnapshot
import com.beautyplanner.client.domain.repository.BookingRepository

/**
 * In-memory fake booking repository for development and demo.
 * TODO: Replace with real backend API calls.
 */
class FakeBookingRepository : BookingRepository {

    private val bookings = mutableListOf<BookingRequest>()

    override suspend fun getAvailableSlots(masterId: String, serviceId: String): List<AvailableSlot> {
        return listOf(
            AvailableSlot("slot-1", masterId, "2025-09-01T10:00:00", "2025-09-01T11:00:00"),
            AvailableSlot("slot-2", masterId, "2025-09-01T12:00:00", "2025-09-01T13:00:00"),
            AvailableSlot("slot-3", masterId, "2025-09-02T10:00:00", "2025-09-02T11:00:00"),
            AvailableSlot("slot-4", masterId, "2025-09-02T14:00:00", "2025-09-02T15:00:00"),
            AvailableSlot("slot-5", masterId, "2025-09-03T11:00:00", "2025-09-03T12:00:00")
        )
    }

    override suspend fun getScheduleSnapshot(masterId: String): ScheduleSnapshot {
        return ScheduleSnapshot(
            masterId = masterId,
            workDayStart = "09:00",
            workDayEnd = "18:00",
            autoPublishBusySlots = false
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
        return if (index >= 0) {
            bookings[index] = bookings[index].copy(status = BookingStatus.CANCELLED)
            Result.success(Unit)
        } else {
            Result.failure(IllegalArgumentException("Booking not found: $bookingId"))
        }
    }
}
