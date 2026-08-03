package com.beautyplanner.client.app

import com.beautyplanner.client.domain.model.ClientProfile

data class AppSessionState(
    val currentClient: ClientProfile? = null
) {
    val isAuthenticated: Boolean
        get() = currentClient != null

    val isGuest: Boolean
        get() = currentClient?.isGuest == true

    val needsProfileCompletion: Boolean
        get() = currentClient?.let { !it.isGuest && it.nickname.isBlank() } == true
}