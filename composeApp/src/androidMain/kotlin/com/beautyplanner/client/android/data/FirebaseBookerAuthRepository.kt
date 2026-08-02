package com.beautyplanner.client.android.data

import com.beautyplanner.client.domain.model.ClientProfile
import com.beautyplanner.client.domain.repository.AuthRepository
import com.beautyplanner.client.domain.repository.AuthState
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class FirebaseBookerAuthRepository(
    private val firebaseAuth: FirebaseAuth,
    private val backend: BookerBackend
) : AuthRepository {

    override fun getAuthState(): AuthState {
        val user = firebaseAuth.currentUser ?: return AuthState.SignedOut
        return AuthState.SignedIn(
            ClientProfile(
                id = user.uid,
                nickname = user.displayName.orEmpty(),
                email = user.email,
                isGuest = false
            )
        )
    }

    override suspend fun signInWithGoogle(): Result<ClientProfile> {
        return Result.failure(UnsupportedOperationException("Google sign-in is not wired in composeApp yet"))
    }

    override suspend fun signInWithApple(): Result<ClientProfile> {
        return Result.failure(UnsupportedOperationException("Apple sign-in is not wired in composeApp yet"))
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<ClientProfile> {
        return authenticate(
            authProvider = "email"
        ) {
            firebaseAuth.signInWithEmailAndPassword(email, password).await().user
        }
    }

    override suspend fun registerWithEmail(email: String, password: String): Result<ClientProfile> {
        return authenticate(
            authProvider = "email"
        ) {
            firebaseAuth.createUserWithEmailAndPassword(email, password).await().user
        }
    }

    override fun continueAsGuest(): ClientProfile {
        return ClientProfile(
            id = "guest",
            nickname = "",
            isGuest = true
        )
    }

    override fun signOut() {
        firebaseAuth.signOut()
    }

    private suspend fun authenticate(
        authProvider: String,
        action: suspend () -> com.google.firebase.auth.FirebaseUser?
    ): Result<ClientProfile> {
        return try {
            val user = action()
                ?: return Result.failure(IllegalStateException("Firebase user is null"))

            backend.bootstrapBookerUser(
                email = user.email,
                displayName = user.displayName,
                authProvider = authProvider
            ).getOrThrow()

            backend.getBookerProfile()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
