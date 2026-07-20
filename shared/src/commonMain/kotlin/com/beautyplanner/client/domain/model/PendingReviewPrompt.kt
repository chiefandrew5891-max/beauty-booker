package com.beautyplanner.client.domain.model

/**
 * A reminder prompt shown to a client after a completed appointment
 * for which no review has been submitted yet.
 *
 * Snooze/postpone support: [snoozedUntil] holds an ISO-8601 timestamp
 * after which the prompt should re-appear. If null, the prompt is active.
 *
 * [titleVariantKey] and [bodyVariantKey] reference string variant keys
 * from [Strings] so that each reminder can show a different wording,
 * making the reminders feel fresh instead of repetitive.
 */
data class PendingReviewPrompt(
    val id: String,
    val clientId: String,
    val masterId: String,
    val masterName: String,
    val appointmentId: String,
    /** ISO-8601 timestamp of the completed appointment. */
    val appointmentDateTime: String,
    /** If non-null, do not show this prompt until this time has passed. */
    val snoozedUntil: String? = null,
    /** How many times the user has postponed. Used to select variant text. */
    val snoozeCount: Int = 0,
    val isDismissed: Boolean = false
)
