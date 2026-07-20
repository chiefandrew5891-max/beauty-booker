package com.beautyplanner.client.domain.model

/**
 * Data submitted by a client when leaving a review.
 *
 * [appointmentId] ensures the one-review-per-appointment rule.
 * Only non-guest clients with a completed booking may submit a review.
 */
data class ReviewSubmission(
    val masterId: String,
    val appointmentId: String,
    val clientId: String,
    /** Rating from 1 to 5. */
    val rating: Int,
    val comment: String = ""
)
