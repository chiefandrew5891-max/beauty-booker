package com.beautyplanner.client.auth

sealed interface AuthScreenAction {
    data class EmailChanged(val value: String) : AuthScreenAction
    data class PasswordChanged(val value: String) : AuthScreenAction
    data object ToggleMode : AuthScreenAction
    data object ClearError : AuthScreenAction
    data object SubmitGoogle : AuthScreenAction
    data object SubmitApple : AuthScreenAction
    data object SubmitEmail : AuthScreenAction
    data object SubmitGuest : AuthScreenAction
}