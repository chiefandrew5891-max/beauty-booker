package com.beautyplanner.client.auth

import com.beautyplanner.client.domain.model.ClientProfile
import com.beautyplanner.client.domain.repository.AuthRepository
import com.beautyplanner.client.strings.Strings

class AuthController(
    private val repository: AuthRepository
) {
    fun reduce(
        state: AuthScreenState,
        action: AuthScreenAction
    ): AuthScreenState {
        return AuthScreenReducer.reduce(state, action)
    }

    suspend fun submitGoogle(state: AuthScreenState): AuthResult {
        val reduced = AuthScreenReducer.reduce(state, AuthScreenAction.SubmitGoogle)
        val submission = AuthSubmissionFactory.fromState(reduced, AuthProvider.GOOGLE)
        return execute(reduced, submission)
    }

    suspend fun submitApple(state: AuthScreenState): AuthResult {
        val reduced = AuthScreenReducer.reduce(state, AuthScreenAction.SubmitApple)
        val submission = AuthSubmissionFactory.fromState(reduced, AuthProvider.APPLE)
        return execute(reduced, submission)
    }

    suspend fun submitEmail(state: AuthScreenState): AuthResult {
        val reduced = AuthScreenReducer.reduce(state, AuthScreenAction.SubmitEmail)
        if (!reduced.isLoading) {
            return AuthResult(
                state = reduced,
                profile = null
            )
        }

        val submission = AuthSubmissionFactory.fromState(reduced, AuthProvider.EMAIL)
        return execute(reduced, submission)
    }

    suspend fun submitGuest(state: AuthScreenState): AuthResult {
        val reduced = AuthScreenReducer.reduce(state, AuthScreenAction.SubmitGuest)
        val submission = AuthSubmissionFactory.fromState(reduced, AuthProvider.GUEST)
        return execute(reduced, submission)
    }

    private suspend fun execute(
        loadingState: AuthScreenState,
        submission: AuthSubmission
    ): AuthResult {
        return AuthExecutor.execute(repository, submission)
            .fold(
                onSuccess = {
                    AuthResult(
                        state = AuthScreenReducer.complete(loadingState),
                        profile = it
                    )
                },
                onFailure = {
                    AuthResult(
                        state = AuthScreenReducer.failure(
                            loadingState,
                            it.message ?: Strings.ERROR_GENERIC
                        ),
                        profile = null
                    )
                }
            )
    }
}

data class AuthResult(
    val state: AuthScreenState,
    val profile: ClientProfile?
)