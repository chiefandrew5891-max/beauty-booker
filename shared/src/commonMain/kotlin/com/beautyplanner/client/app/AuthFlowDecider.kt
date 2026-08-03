package com.beautyplanner.client.app

import com.beautyplanner.client.domain.model.ClientProfile

object AuthFlowDecider {
    fun destinationAfterSignIn(profile: ClientProfile): AuthFlowDestination {
        return if (profile.nickname.isBlank() && !profile.isGuest) {
            AuthFlowDestination.COMPLETE_PROFILE
        } else {
            AuthFlowDestination.DISCOVER
        }
    }
}