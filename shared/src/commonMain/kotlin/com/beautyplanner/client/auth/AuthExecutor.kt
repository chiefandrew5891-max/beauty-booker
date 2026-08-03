package com.beautyplanner.client.auth

import com.beautyplanner.client.domain.model.ClientProfile
import com.beautyplanner.client.domain.repository.AuthRepository

object AuthExecutor {
    suspend fun execute(
        repository: AuthRepository,
        submission: AuthSubmission
    ): Result<ClientProfile> {
        return when (submission.provider) {
            AuthProvider.GOOGLE -> repository.signInWithGoogle()
            AuthProvider.APPLE -> repository.signInWithApple()
            AuthProvider.GUEST -> Result.success(repository.continueAsGuest())
            AuthProvider.EMAIL -> {
                if (submission.mode == AuthMode.REGISTER) {
                    repository.registerWithEmail(submission.email, submission.password)
                } else {
                    repository.signInWithEmail(submission.email, submission.password)
                }
            }
        }
    }
}