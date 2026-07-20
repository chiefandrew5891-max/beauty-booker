package com.beautyplanner.client.fake

import com.beautyplanner.client.domain.model.MasterReview
import com.beautyplanner.client.domain.model.PendingReviewPrompt
import com.beautyplanner.client.domain.model.ReviewSubmission
import com.beautyplanner.client.domain.repository.ReviewsRepository

/**
 * In-memory fake reviews repository for development and demo.
 * TODO: Replace with real backend API calls.
 */
class FakeReviewsRepository : ReviewsRepository {

    private val reviews = mutableListOf(
        MasterReview(
            id = "rev-1",
            masterId = "master-1",
            appointmentId = "appt-100",
            authorId = "user-x",
            authorNickname = "Наталія К.",
            rating = 5,
            comment = "Дуже задоволена! Маникюр тримається вже 3 тижні.",
            createdAt = "2025-08-15T10:00:00"
        ),
        MasterReview(
            id = "rev-2",
            masterId = "master-1",
            appointmentId = "appt-101",
            authorId = "user-y",
            authorNickname = "Оля М.",
            rating = 5,
            comment = "Рекомендую! Анна дуже акуратна і уважна.",
            createdAt = "2025-08-10T14:00:00"
        ),
        MasterReview(
            id = "rev-3",
            masterId = "master-2",
            appointmentId = "appt-200",
            authorId = "user-z",
            authorNickname = "Тетяна В.",
            rating = 4,
            comment = "Красивая стрижка, всё как хотела.",
            createdAt = "2025-08-05T12:00:00"
        )
    )

    private val pendingPrompts = mutableListOf(
        PendingReviewPrompt(
            id = "prompt-1",
            clientId = "fake-google-user",
            masterId = "master-1",
            masterName = "Анна Коваль",
            appointmentId = "appt-999",
            appointmentDateTime = "2025-08-20T11:00:00"
        )
    )

    override suspend fun getReviewsForMaster(masterId: String): List<MasterReview> =
        reviews.filter { it.masterId == masterId && !it.isHiddenByMaster }

    override suspend fun submitReview(submission: ReviewSubmission): Result<MasterReview> {
        if (reviews.any { it.appointmentId == submission.appointmentId }) {
            return Result.failure(IllegalStateException("Review already submitted for this appointment"))
        }
        val review = MasterReview(
            id = "rev-${reviews.size + 1}",
            masterId = submission.masterId,
            appointmentId = submission.appointmentId,
            authorId = submission.clientId,
            authorNickname = "Ви",
            rating = submission.rating,
            comment = submission.comment,
            createdAt = "2025-08-21T10:00:00"
        )
        reviews.add(review)
        pendingPrompts.removeAll { it.appointmentId == submission.appointmentId }
        return Result.success(review)
    }

    override suspend fun getPendingPrompts(clientId: String): List<PendingReviewPrompt> =
        pendingPrompts.filter { it.clientId == clientId && !it.isDismissed }

    override suspend fun snoozePrompt(promptId: String, snoozedUntilIso: String): Result<Unit> {
        val index = pendingPrompts.indexOfFirst { it.id == promptId }
        return if (index >= 0) {
            val prompt = pendingPrompts[index]
            pendingPrompts[index] = prompt.copy(
                snoozedUntil = snoozedUntilIso,
                snoozeCount = prompt.snoozeCount + 1
            )
            Result.success(Unit)
        } else {
            Result.failure(IllegalArgumentException("Prompt not found: $promptId"))
        }
    }

    override suspend fun dismissPrompt(promptId: String): Result<Unit> {
        val index = pendingPrompts.indexOfFirst { it.id == promptId }
        return if (index >= 0) {
            pendingPrompts[index] = pendingPrompts[index].copy(isDismissed = true)
            Result.success(Unit)
        } else {
            Result.failure(IllegalArgumentException("Prompt not found: $promptId"))
        }
    }
}
