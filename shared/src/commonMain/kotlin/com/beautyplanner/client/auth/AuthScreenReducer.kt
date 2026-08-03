package com.beautyplanner.client.auth

import com.beautyplanner.client.strings.Strings

object AuthScreenReducer {
    fun reduce(
        state: AuthScreenState,
        action: AuthScreenAction
    ): AuthScreenState {
        return when (action) {
            is AuthScreenAction.EmailChanged -> {
                state.copy(email = action.value, errorMessage = null)
            }

            is AuthScreenAction.PasswordChanged -> {
                state.copy(password = action.value, errorMessage = null)
            }

            AuthScreenAction.ToggleMode -> {
                state.copy(
                    mode = if (state.mode == AuthMode.SIGN_IN) {
                        AuthMode.REGISTER
                    } else {
                        AuthMode.SIGN_IN
                    },
                    errorMessage = null
                )
            }

            AuthScreenAction.ClearError -> {
                state.copy(errorMessage = null)
            }

            AuthScreenAction.SubmitGoogle,
            AuthScreenAction.SubmitApple,
            AuthScreenAction.SubmitGuest -> {
                state.copy(isLoading = true, errorMessage = null)
            }

            AuthScreenAction.SubmitEmail -> {
                val trimmedEmail = state.email.trim()

                when {
                    trimmedEmail.isBlank() -> {
                        state.copy(errorMessage = Strings.ERROR_EMPTY_EMAIL)
                    }

                    state.password.isBlank() -> {
                        state.copy(errorMessage = Strings.ERROR_EMPTY_PASSWORD)
                    }

                    else -> {
                        state.copy(
                            email = trimmedEmail,
                            isLoading = true,
                            errorMessage = null
                        )
                    }
                }
            }
        }
    }

    fun complete(state: AuthScreenState): AuthScreenState {
        return state.copy(isLoading = false)
    }

    fun failure(state: AuthScreenState, message: String): AuthScreenState {
        return state.copy(
            isLoading = false,
            errorMessage = message
        )
    }
}