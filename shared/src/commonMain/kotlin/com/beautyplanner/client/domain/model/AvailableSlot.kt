package com.beautyplanner.client.domain.model

/**
 * An available booking slot for a master.
 */
data class AvailableSlot(
    val id: String,
    val masterId: String,
    /** ISO-8601 date-time string, e.g. "2025-09-01T14:00:00". */
    val startDateTime: String,
    /** ISO-8601 date-time string. */
    val endDateTime: String,
    val isBooked: Boolean = false
)
