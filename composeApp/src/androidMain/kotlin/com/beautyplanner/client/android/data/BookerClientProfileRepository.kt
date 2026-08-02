package com.beautyplanner.client.android.data

import com.beautyplanner.client.domain.model.ClientProfile
import com.beautyplanner.client.domain.repository.ClientProfileRepository

class BookerClientProfileRepository(
    private val backend: BookerBackend
) : ClientProfileRepository {

    override suspend fun getProfile(clientId: String): ClientProfile? {
        return backend.getBookerProfile().getOrNull()
            ?.takeIf { it.id == clientId }
    }

    override suspend fun saveProfile(profile: ClientProfile): Result<ClientProfile> {
        return backend.saveBookerProfile(profile)
    }
}
