package com.beautyplanner.client.android.auth

import com.beautyplanner.client.data.BookerBackend
import com.beautyplanner.client.domain.model.ClientProfile
import com.beautyplanner.client.fake.FirebaseAuthRepositoryDelegate
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class AndroidFirebaseAuthDelegate(
    private val firebaseAuth: FirebaseAuth,
    private val bookerBackend: BookerBackend
) : FirebaseAuthRepositoryDelegate {

    override suspend fun signInWithEmail(email: String, password: String): Result<ClientProfile> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = authResult.user
                ?: return Result.failure(IllegalStateException("Firebase user is null after sign-in"))

            bookerBackend.bootstrapBookerUser(
                email = user.email,
                displayName = user.displayName,
                authProvider = "email"
            ).getOrThrow()

            bookerBackend.getBookerProfile()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun registerWithEmail(email: String, password: String): Result<ClientProfile> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user
                ?: return Result.failure(IllegalStateException("Firebase user is null after registration"))

            bookerBackend.bootstrapBookerUser(
                email = user.email,
                displayName = user.displayName,
                authProvider = "email"
            ).getOrThrow()

            bookerBackend.getBookerProfile()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun signOut() {
        firebaseAuth.signOut()
    }
}