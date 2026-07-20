package com.beautyplanner.client.domain.repository

import com.beautyplanner.client.domain.model.ClientProfile

/**
 * Manages the current client's profile (nickname, avatar, etc.).
 *
 * Fake implementation: [com.beautyplanner.client.fake.FakeClientProfileRepository]
 * TODO: Replace with real backend/Firebase profile storage.
 */
interface ClientProfileRepository {
    suspend fun getProfile(clientId: String): ClientProfile?
    /**
     * Save or update the client's profile.
     * Called after CompleteProfile step to persist the nickname.
     */
    suspend fun saveProfile(profile: ClientProfile): Result<ClientProfile>
}
