package com.beautyplanner.client.domain.model

/**
 * Status of a booking appointment.
 */
enum class BookingStatus {
    PENDING,
    CONFIRMED,
    COMPLETED,
    CANCELLED
}

/**
 * A booking request submitted by a client.
 *
 * Important: Only authenticated (non-guest) clients may create bookings.
 */
data class BookingRequest(
    val id: String,
    val clientId: String,
    val masterId: String,
    val serviceId: String,
    val slotId: String,
    /** ISO-8601 date-time of the appointment. */
    val appointmentDateTime: String,
    val status: BookingStatus = BookingStatus.PENDING,
    val noteFromClient: String = ""
)
