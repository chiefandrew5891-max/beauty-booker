package com.beautyplanner.client.fake

import com.beautyplanner.client.domain.model.ClientProfile
import com.beautyplanner.client.domain.repository.AuthRepository
import com.beautyplanner.client.domain.repository.AuthState

/**
 * In-memory fake auth repository for development and demo.
 * TODO: Replace with Firebase Auth integration.
 */
class FakeAuthRepository : AuthRepository {

    private var currentState: AuthState = AuthState.SignedOut

    override fun getAuthState(): AuthState = currentState

    override suspend fun signInWithGoogle(): Result<ClientProfile> {
        val profile = ClientProfile(
            id = "fake-google-user",
            nickname = "",
            email = "user@gmail.com",
            isGuest = false
        )
        currentState = AuthState.SignedIn(profile)
        return Result.success(profile)
    }

    override suspend fun signInWithApple(): Result<ClientProfile> {
        val profile = ClientProfile(
            id = "fake-apple-user",
            nickname = "",
            email = "user@icloud.com",
            isGuest = false
        )
        currentState = AuthState.SignedIn(profile)
        return Result.success(profile)
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<ClientProfile> {
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Email and password are required"))
        }
        val profile = ClientProfile(
            id = "fake-email-user",
            nickname = "",
            email = email,
            isGuest = false
        )
        currentState = AuthState.SignedIn(profile)
        return Result.success(profile)
    }

    override fun continueAsGuest(): ClientProfile {
        val profile = ClientProfile(
            id = "guest",
            nickname = "Гость",
            isGuest = true
        )
        currentState = AuthState.Guest
        return profile
    }

    override fun signOut() {
        currentState = AuthState.SignedOut
    }
}
