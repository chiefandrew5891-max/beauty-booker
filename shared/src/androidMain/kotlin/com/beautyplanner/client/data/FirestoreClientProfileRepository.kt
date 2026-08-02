package com.beautyplanner.client.data

import com.beautyplanner.client.domain.model.ClientProfile
import com.beautyplanner.client.domain.repository.ClientProfileRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreClientProfileRepository(
    private val firestore: FirebaseFirestore
) : ClientProfileRepository {

    private val collection = firestore.collection("users_beautybooker")

    override suspend fun getProfile(clientId: String): ClientProfile? {
        return try {
            val snapshot = collection.document(clientId).get().await()
            if (!snapshot.exists()) return null

            ClientProfile(
                id = clientId,
                nickname = snapshot.getString("nickname").orEmpty(),
                email = snapshot.getString("email"),
                isGuest = snapshot.getBoolean("isGuest") ?: false
            )
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun saveProfile(profile: ClientProfile): Result<ClientProfile> {
        return try {
            val data = mapOf(
                "uid" to profile.id,
                "nickname" to profile.nickname,
                "email" to profile.email,
                "isGuest" to profile.isGuest
            )

            collection.document(profile.id).set(data).await()
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}