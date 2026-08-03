package com.beautyplanner.client.auth

object AuthSubmissionFactory {
    fun fromState(
        state: AuthScreenState,
        provider: AuthProvider
    ): AuthSubmission {
        return AuthSubmission(
            provider = provider,
            email = state.email.trim(),
            password = state.password,
            mode = state.mode
        )
    }
}