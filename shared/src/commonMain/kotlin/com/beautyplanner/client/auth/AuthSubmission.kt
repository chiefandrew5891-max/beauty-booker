package com.beautyplanner.client.auth

data class AuthSubmission(
    val provider: AuthProvider,
    val email: String = "",
    val password: String = "",
    val mode: AuthMode = AuthMode.SIGN_IN
)