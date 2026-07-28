package com.beautyplanner.client.domain.repository

import com.beautyplanner.client.domain.model.AvailableSlot
import com.beautyplanner.client.domain.model.BookingRequest
import com.beautyplanner.client.domain.model.MasterScheduleSnapshot

interface BookingRepository {
    suspend fun getAvailableSlots(masterId: String, serviceId: String): List<AvailableSlot>
    suspend fun getScheduleSnapshot(masterId: String): MasterScheduleSnapshot
    suspend fun submitBooking(request: BookingRequest): Result<BookingRequest>
    suspend fun getBookingsForClient(clientId: String): List<BookingRequest>
    suspend fun cancelBooking(bookingId: String): Result<Unit>
}