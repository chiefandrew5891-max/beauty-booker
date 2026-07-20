package com.beautyplanner.client.fake

import com.beautyplanner.client.domain.model.ClientProfile
import com.beautyplanner.client.domain.repository.ClientProfileRepository

/**
 * In-memory fake client profile repository for development and demo.
 * TODO: Replace with Firebase/backend profile storage.
 */
class FakeClientProfileRepository : ClientProfileRepository {

    private val profiles = mutableMapOf<String, ClientProfile>()

    override suspend fun getProfile(clientId: String): ClientProfile? = profiles[clientId]

    override suspend fun saveProfile(profile: ClientProfile): Result<ClientProfile> {
        profiles[profile.id] = profile
        return Result.success(profile)
    }
}
