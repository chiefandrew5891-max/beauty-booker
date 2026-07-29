package com.beautyplanner.client.android.data

import com.beautyplanner.client.domain.model.ClientProfile
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseBookerProfileRepository(
    private val firestore: FirebaseFirestore
) {

    private val collection = firestore.collection("users_beautybooker")

    suspend fun getOrCreateProfile(
        uid: String,
        email: String?
    ): Result<ClientProfile> {
        return try {
            val snapshot = collection.document(uid).get().await()

            if (snapshot.exists()) {
                val profile = ClientProfile(
                    id = uid,
                    nickname = snapshot.getString("nickname").orEmpty(),
                    email = snapshot.getString("email") ?: email,
                    isGuest = false
                )
                Result.success(profile)
            } else {
                val profileData = mapOf(
                    "uid" to uid,
                    "nickname" to "",
                    "email" to email,
                    "isGuest" to false
                )

                collection.document(uid).set(profileData).await()

                Result.success(
                    ClientProfile(
                        id = uid,
                        nickname = "",
                        email = email,
                        isGuest = false
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveProfile(profile: ClientProfile): Result<ClientProfile> {
        return try {
            val profileData = mapOf(
                "uid" to profile.id,
                "nickname" to profile.nickname,
                "email" to profile.email,
                "isGuest" to profile.isGuest
            )

            collection.document(profile.id).set(profileData).await()
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}