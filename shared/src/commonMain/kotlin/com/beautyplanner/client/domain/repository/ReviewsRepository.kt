package com.beautyplanner.client.domain.repository

import com.beautyplanner.client.domain.model.MasterReview
import com.beautyplanner.client.domain.model.PendingReviewPrompt
import com.beautyplanner.client.domain.model.ReviewSubmission

/**
 * Data contract for reading/submitting reviews and managing review reminders.
 *
 * Review rules:
 * - Reviews are only allowed after a COMPLETED appointment.
 * - One review per [appointmentId] — enforced here.
 * - Guest clients must NOT submit reviews.
 * - Masters may hide reviews but not delete or alter ratings.
 *
 * Current implementation: [com.beautyplanner.client.fake.ReviewsRepository]
 * TODO: Replace with real backend API calls.
 */
interface ReviewsRepository {
    /** Public reviews for a master (excluding hidden ones for clients). */
    suspend fun getReviewsForMaster(masterId: String): List<MasterReview>
    /**
     * Submit a review. Throws if a review for this appointmentId already exists.
     * Guest clients must be rejected before calling this.
     */
    suspend fun submitReview(submission: ReviewSubmission): Result<MasterReview>
    /** Pending review reminder prompts for a client. */
    suspend fun getPendingPrompts(clientId: String): List<PendingReviewPrompt>
    /** Snooze a reminder until [snoozedUntilIso] (ISO-8601 timestamp). */
    suspend fun snoozePrompt(promptId: String, snoozedUntilIso: String): Result<Unit>
    /** Dismiss a prompt permanently (after review submitted or user declines). */
    suspend fun dismissPrompt(promptId: String): Result<Unit>
}
