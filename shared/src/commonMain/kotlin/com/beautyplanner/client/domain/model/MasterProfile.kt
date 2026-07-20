package com.beautyplanner.client.domain.model

/**
 * A beauty master (specialist) profile visible to clients.
 *
 * [categoryId] links to [MasterCategory.id].
 * [specialtyTitle] is the human-readable specialty, e.g. "Мастер маникюра".
 * [averageRating] and [reviewCount] are pre-computed summary fields.
 */
data class MasterProfile(
    val id: String,
    val displayName: String,
    val avatarUrl: String?,
    val categoryId: String,
    /** User-facing specialty label, e.g. "Мастер маникюра", "Парикмахер". */
    val specialtyTitle: String,
    val averageRating: Float,
    val reviewCount: Int,
    val bio: String = ""
)
