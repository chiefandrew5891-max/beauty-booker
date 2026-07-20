package com.beautyplanner.client.domain.model

/**
 * The signed-in client's profile.
 *
 * [nickname] is the public display name shown in reviews and ratings.
 * A client is considered a guest when [isGuest] is true.
 * Guest clients may browse but must not book or leave reviews.
 */
data class ClientProfile(
    val id: String,
    /** Public name shown in reviews. Required for non-guest users. */
    val nickname: String,
    val email: String? = null,
    val avatarUrl: String? = null,
    val isGuest: Boolean = false
)
