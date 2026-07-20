package com.beautyplanner.client.domain.model

/**
 * Category for grouping masters (e.g. nails, hair, makeup).
 * [id] is a machine-readable key; [titleRu] is the user-facing Russian label.
 */
data class MasterCategory(
    val id: String,
    val titleRu: String,
    val iconUrl: String? = null
)
