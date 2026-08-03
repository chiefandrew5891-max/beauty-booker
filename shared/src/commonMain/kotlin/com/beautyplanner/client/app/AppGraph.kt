package com.beautyplanner.client.app

import com.beautyplanner.client.domain.repository.AuthRepository
import com.beautyplanner.client.domain.repository.BookingRepository
import com.beautyplanner.client.domain.repository.ClientProfileRepository
import com.beautyplanner.client.domain.repository.MastersRepository
import com.beautyplanner.client.domain.repository.ReviewsRepository

data class AppGraph(
    val authRepository: AuthRepository,
    val mastersRepository: MastersRepository,
    val bookingRepository: BookingRepository,
    val reviewsRepository: ReviewsRepository,
    val clientProfileRepository: ClientProfileRepository
)