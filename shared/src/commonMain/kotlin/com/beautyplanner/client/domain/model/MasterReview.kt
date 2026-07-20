package com.beautyplanner.client.domain.model

/**
 * A review left by a client for a completed appointment.
 *
 * Rules:
 * - One review per [appointmentId] — enforced at repository level.
 * - Review author name is the client's [ClientProfile.nickname].
 * - A master may hide a review from public display ([isHiddenByMaster] = true)
 *   but must NOT delete it or alter its rating.
 * - Only available after a COMPLETED booking.
 */
data class MasterReview(
    val id: String,
    val masterId: String,
    val appointmentId: String,
    val authorId: String,
    /** Public display name from [ClientProfile.nickname]. */
    val authorNickname: String,
    /** Rating from 1 to 5. */
    val rating: Int,
    val comment: String = "",
    /** ISO-8601 timestamp when the review was submitted. */
    val createdAt: String,
    /** Master may hide review from public feed but cannot delete or modify rating. */
    val isHiddenByMaster: Boolean = false
)
