package com.beautyplanner.client.data

import com.beautyplanner.client.domain.model.ClientProfile
import kotlin.collections.get
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await

class BookerBackend(
    private val functions: FirebaseFunctions
) {

    suspend fun bootstrapBookerUser(
        email: String?,
        displayName: String?,
        authProvider: String
    ): Result<Unit> {
        return try {
            functions
                .getHttpsCallable("bootstrapBookerUser")
                .call(
                    mapOf(
                        "email" to (email ?: ""),
                        "displayName" to (displayName ?: ""),
                        "authProvider" to authProvider
                    )
                )
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncBookerIdentity(
        email: String?,
        displayName: String?,
        authProvider: String
    ): Result<Unit> {
        return try {
            functions
                .getHttpsCallable("syncBookerIdentity")
                .call(
                    mapOf(
                        "email" to (email ?: ""),
                        "displayName" to (displayName ?: ""),
                        "authProvider" to authProvider
                    )
                )
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun validateBookerSession(): Result<Unit> {
        return try {
            functions
                .getHttpsCallable("validateBookerSession")
                .call(emptyMap<String, Any>())
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBookerProfile(): Result<ClientProfile> {
        return try {
            val result = functions
                .getHttpsCallable("getBookerProfile")
                .call(emptyMap<String, Any>())
                .await()

            val data = result.getData() as? Map<*, *> ?: emptyMap<String, Any?>()

            val profile = ClientProfile(
                id = data["firebaseUid"]?.toString().orEmpty(),
                nickname = data["nickname"]?.toString().orEmpty(),
                email = data["email"]?.toString()?.ifBlank { null },
                isGuest = data["isGuest"]?.toString()?.toBooleanStrictOrNull() ?: false
            )

            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveBookerProfile(profile: ClientProfile): Result<ClientProfile> {
        return try {
            functions
                .getHttpsCallable("saveBookerProfile")
                .call(
                    mapOf(
                        "email" to (profile.email ?: ""),
                        "nickname" to profile.nickname,
                        "isGuest" to profile.isGuest
                    )
                )
                .await()

            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}