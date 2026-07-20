package com.beautyplanner.client.domain.model

/**
 * A service offered by a master.
 */
data class MasterService(
    val id: String,
    val masterId: String,
    val titleRu: String,
    val descriptionRu: String = "",
    /** Duration in minutes. */
    val durationMinutes: Int,
    /** Price in local currency. */
    val price: Double,
    val currency: String = "UAH"
)
