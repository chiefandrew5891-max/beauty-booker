package com.beautyplanner.client.domain.repository

import com.beautyplanner.client.domain.model.ClientProfile

/**
 * Authentication state for the current session.
 */
sealed class AuthState {
    /** No user is signed in. */
    data object SignedOut : AuthState()
    /** A guest session is active. Guests may browse but cannot book or review. */
    data object Guest : AuthState()
    /** A real user is signed in. [profile] may have an empty nickname before CompleteProfile step. */
    data class SignedIn(val profile: ClientProfile) : AuthState()
}

/**
 * Abstraction over the authentication provider (Firebase Auth, etc.).
 *
 * Current implementation: [com.beautyplanner.client.fake.AuthRepository]
 * TODO: Replace with Firebase/Auth provider integration.
 */
interface AuthRepository {
    fun getAuthState(): AuthState
    suspend fun signInWithGoogle(): Result<ClientProfile>
    suspend fun signInWithApple(): Result<ClientProfile>
    suspend fun signInWithEmail(email: String, password: String): Result<ClientProfile>
    suspend fun registerWithEmail(email: String, password: String): Result<ClientProfile>
    fun continueAsGuest(): ClientProfile
    fun signOut()
}
