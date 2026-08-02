package com.beautyplanner.client.android.data

import com.beautyplanner.client.domain.model.MasterReview
import com.beautyplanner.client.domain.model.PendingReviewPrompt
import com.beautyplanner.client.domain.model.ReviewSubmission
import com.beautyplanner.client.domain.repository.ReviewsRepository
import kotlinx.datetime.Clock

class InMemoryReviewsRepository : ReviewsRepository {
    private val reviews = mutableListOf<MasterReview>()
    private val prompts = mutableListOf<PendingReviewPrompt>()

    override suspend fun getReviewsForMaster(masterId: String): List<MasterReview> {
        return reviews.filter { it.masterId == masterId && !it.isHiddenByMaster }
    }

    override suspend fun submitReview(submission: ReviewSubmission): Result<MasterReview> {
        val existing = reviews.firstOrNull { it.appointmentId == submission.appointmentId }
        if (existing != null) {
            return Result.failure(IllegalStateException("Review for this appointment already exists"))
        }

        val review = MasterReview(
            id = "review_${submission.appointmentId}",
            masterId = submission.masterId,
            appointmentId = submission.appointmentId,
            authorId = submission.clientId,
            authorNickname = "",
            rating = submission.rating.coerceIn(1, 5),
            comment = submission.comment,
            createdAt = Clock.System.now().toString()
        )

        reviews += review
        prompts.removeAll { it.appointmentId == submission.appointmentId }
        return Result.success(review)
    }

    override suspend fun getPendingPrompts(clientId: String): List<PendingReviewPrompt> {
        return prompts.filter { it.clientId == clientId && !it.isDismissed }
    }

    override suspend fun snoozePrompt(promptId: String, snoozedUntilIso: String): Result<Unit> {
        val index = prompts.indexOfFirst { it.id == promptId }
        if (index == -1) return Result.failure(IllegalArgumentException("Prompt not found"))

        prompts[index] = prompts[index].copy(
            snoozedUntil = snoozedUntilIso,
            snoozeCount = prompts[index].snoozeCount + 1
        )
        return Result.success(Unit)
    }

    override suspend fun dismissPrompt(promptId: String): Result<Unit> {
        val index = prompts.indexOfFirst { it.id == promptId }
        if (index == -1) return Result.failure(IllegalArgumentException("Prompt not found"))

        prompts[index] = prompts[index].copy(isDismissed = true)
        return Result.success(Unit)
    }
}
