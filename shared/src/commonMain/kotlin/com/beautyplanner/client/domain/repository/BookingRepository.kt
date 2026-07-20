package com.beautyplanner.client.domain.repository

import com.beautyplanner.client.domain.model.AvailableSlot
import com.beautyplanner.client.domain.model.BookingRequest

/**
 * Data contract for booking slots and submitting/reading booking requests.
 *
 * Fake implementation: [com.beautyplanner.client.fake.FakeBookingRepository]
 * TODO: Replace with real backend API calls.
 */
interface BookingRepository {
    suspend fun getAvailableSlots(masterId: String, serviceId: String): List<AvailableSlot>
    /**
     * Submit a booking request.
     * Only authenticated (non-guest) clients may call this.
     */
    suspend fun submitBooking(request: BookingRequest): Result<BookingRequest>
    suspend fun getBookingsForClient(clientId: String): List<BookingRequest>
    suspend fun cancelBooking(bookingId: String): Result<Unit>
}
