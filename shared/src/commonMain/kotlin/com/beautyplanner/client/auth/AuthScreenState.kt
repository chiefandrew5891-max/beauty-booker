package com.beautyplanner.client.auth

data class AuthScreenState(
    val email: String = "",
    val password: String = "",
    val mode: AuthMode = AuthMode.SIGN_IN,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val isRegisterMode: Boolean
        get() = mode == AuthMode.REGISTER
}