package com.beautyplanner.client.domain.repository

import com.beautyplanner.client.domain.model.MasterCategory
import com.beautyplanner.client.domain.model.MasterProfile
import com.beautyplanner.client.domain.model.MasterService

/**
 * Data contract for reading master profiles, services, and categories.
 *
 * Fake implementation: [com.beautyplanner.client.fake.FakeMastersRepository]
 * TODO: Replace with real backend API calls.
 */
interface MastersRepository {
    suspend fun getCategories(): List<MasterCategory>
    suspend fun getMasters(categoryId: String? = null, query: String? = null): List<MasterProfile>
    suspend fun getFeaturedMasters(): List<MasterProfile>
    suspend fun getMasterById(masterId: String): MasterProfile?
    suspend fun getServicesForMaster(masterId: String): List<MasterService>
}
