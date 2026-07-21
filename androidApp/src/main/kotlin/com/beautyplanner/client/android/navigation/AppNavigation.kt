package com.beautyplanner.client.android.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.beautyplanner.client.android.ui.auth.AuthScreen
import com.beautyplanner.client.android.ui.auth.CompleteProfileScreen
import com.beautyplanner.client.android.ui.booking.BookingConfirmationScreen
import com.beautyplanner.client.android.ui.booking.BookingFormScreen
import com.beautyplanner.client.android.ui.booking.DateTimeScreen
import com.beautyplanner.client.android.ui.main.ClientMainScreen
import com.beautyplanner.client.android.ui.master.MasterProfileScreen
import com.beautyplanner.client.android.ui.master.ServicesScreen
import com.beautyplanner.client.android.ui.review.LeaveReviewScreen
import com.beautyplanner.client.android.ui.review.ReviewsScreen
import com.beautyplanner.client.android.ui.theme.AppThemeMode
import com.beautyplanner.client.domain.model.ClientProfile
import com.beautyplanner.client.domain.repository.AuthRepository
import com.beautyplanner.client.domain.repository.BookingRepository
import com.beautyplanner.client.domain.repository.ClientProfileRepository
import com.beautyplanner.client.domain.repository.MastersRepository
import com.beautyplanner.client.domain.repository.ReviewsRepository

/** Named route constants for the app. */
object Routes {
    const val AUTH = "auth"
    const val COMPLETE_PROFILE = "complete_profile"
    const val DISCOVER = "discover"
    const val MASTER_PROFILE = "master_profile/{masterId}"
    const val SERVICES = "services/{masterId}"
    const val DATE_TIME = "date_time/{masterId}/{serviceId}"
    const val BOOKING_FORM = "booking_form/{masterId}/{serviceId}/{slotId}"
    const val BOOKING_CONFIRMATION = "booking_confirmation/{bookingId}"
    const val REVIEWS = "reviews/{masterId}"
    const val LEAVE_REVIEW = "leave_review/{masterId}/{appointmentId}"

    fun masterProfile(masterId: String) = "master_profile/$masterId"
    fun services(masterId: String) = "services/$masterId"
    fun dateTime(masterId: String, serviceId: String) = "date_time/$masterId/$serviceId"
    fun bookingForm(masterId: String, serviceId: String, slotId: String) =
        "booking_form/$masterId/$serviceId/$slotId"
    fun bookingConfirmation(bookingId: String) = "booking_confirmation/$bookingId"
    fun reviews(masterId: String) = "reviews/$masterId"
    fun leaveReview(masterId: String, appointmentId: String) =
        "leave_review/$masterId/$appointmentId"
}

/**
 * Root composable that owns the NavController and wires all screens together.
 * All repository dependencies are passed in from [MainActivity].
 */
@Composable
fun AppNavigation(
    authRepository: AuthRepository,
    mastersRepository: MastersRepository,
    bookingRepository: BookingRepository,
    reviewsRepository: ReviewsRepository,
    clientProfileRepository: ClientProfileRepository,
    themeMode: AppThemeMode,
    onThemeModeChange: (AppThemeMode) -> Unit
) {
    val navController = rememberNavController()
    var currentClient by remember { mutableStateOf<ClientProfile?>(null) }

    NavHost(navController = navController, startDestination = Routes.AUTH) {

        composable(Routes.AUTH) {
            AuthScreen(
                authRepository = authRepository,
                onSignedIn = { profile ->
                    currentClient = profile
                    if (profile.nickname.isBlank() && !profile.isGuest) {
                        navController.navigate(Routes.COMPLETE_PROFILE) {
                            popUpTo(Routes.AUTH) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Routes.DISCOVER) {
                            popUpTo(Routes.AUTH) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Routes.COMPLETE_PROFILE) {
            CompleteProfileScreen(
                client = currentClient,
                clientProfileRepository = clientProfileRepository,
                onProfileComplete = { updatedProfile ->
                    currentClient = updatedProfile
                    navController.navigate(Routes.DISCOVER) {
                        popUpTo(Routes.COMPLETE_PROFILE) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.DISCOVER) {
            ClientMainScreen(
                client = currentClient,
                mastersRepository = mastersRepository,
                bookingRepository = bookingRepository,
                reviewsRepository = reviewsRepository,
                themeMode = themeMode,
                onThemeModeChange = onThemeModeChange,
                onMasterClick = { masterId ->
                    navController.navigate(Routes.masterProfile(masterId))
                }
            )
        }

        composable(
            Routes.MASTER_PROFILE,
            arguments = listOf(navArgument("masterId") { type = NavType.StringType })
        ) { backStack ->
            val masterId = backStack.arguments?.getString("masterId") ?: return@composable
            MasterProfileScreen(
                masterId = masterId,
                client = currentClient,
                mastersRepository = mastersRepository,
                onServicesClick = { navController.navigate(Routes.services(masterId)) },
                onReviewsClick = { navController.navigate(Routes.reviews(masterId)) },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            Routes.SERVICES,
            arguments = listOf(navArgument("masterId") { type = NavType.StringType })
        ) { backStack ->
            val masterId = backStack.arguments?.getString("masterId") ?: return@composable
            ServicesScreen(
                masterId = masterId,
                client = currentClient,
                mastersRepository = mastersRepository,
                onServiceSelected = { serviceId ->
                    navController.navigate(Routes.dateTime(masterId, serviceId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            Routes.DATE_TIME,
            arguments = listOf(
                navArgument("masterId") { type = NavType.StringType },
                navArgument("serviceId") { type = NavType.StringType }
            )
        ) { backStack ->
            val masterId = backStack.arguments?.getString("masterId") ?: return@composable
            val serviceId = backStack.arguments?.getString("serviceId") ?: return@composable
            DateTimeScreen(
                masterId = masterId,
                serviceId = serviceId,
                client = currentClient,
                bookingRepository = bookingRepository,
                onSlotSelected = { slotId ->
                    navController.navigate(Routes.bookingForm(masterId, serviceId, slotId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            Routes.BOOKING_FORM,
            arguments = listOf(
                navArgument("masterId") { type = NavType.StringType },
                navArgument("serviceId") { type = NavType.StringType },
                navArgument("slotId") { type = NavType.StringType }
            )
        ) { backStack ->
            val masterId = backStack.arguments?.getString("masterId") ?: return@composable
            val serviceId = backStack.arguments?.getString("serviceId") ?: return@composable
            val slotId = backStack.arguments?.getString("slotId") ?: return@composable
            BookingFormScreen(
                masterId = masterId,
                serviceId = serviceId,
                slotId = slotId,
                client = currentClient,
                mastersRepository = mastersRepository,
                bookingRepository = bookingRepository,
                onBookingConfirmed = { bookingId ->
                    navController.navigate(Routes.bookingConfirmation(bookingId)) {
                        popUpTo(Routes.DISCOVER)
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            Routes.BOOKING_CONFIRMATION,
            arguments = listOf(navArgument("bookingId") { type = NavType.StringType })
        ) { backStack ->
            val bookingId = backStack.arguments?.getString("bookingId") ?: return@composable
            BookingConfirmationScreen(
                bookingId = bookingId,
                onGoToDiscover = {
                    navController.navigate(Routes.DISCOVER) {
                        popUpTo(Routes.DISCOVER) { inclusive = true }
                    }
                }
            )
        }

        composable(
            Routes.REVIEWS,
            arguments = listOf(navArgument("masterId") { type = NavType.StringType })
        ) { backStack ->
            val masterId = backStack.arguments?.getString("masterId") ?: return@composable
            ReviewsScreen(
                masterId = masterId,
                client = currentClient,
                reviewsRepository = reviewsRepository,
                onLeaveReviewClick = { appointmentId ->
                    navController.navigate(Routes.leaveReview(masterId, appointmentId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            Routes.LEAVE_REVIEW,
            arguments = listOf(
                navArgument("masterId") { type = NavType.StringType },
                navArgument("appointmentId") { type = NavType.StringType }
            )
        ) { backStack ->
            val masterId = backStack.arguments?.getString("masterId") ?: return@composable
            val appointmentId = backStack.arguments?.getString("appointmentId") ?: return@composable
            LeaveReviewScreen(
                masterId = masterId,
                appointmentId = appointmentId,
                client = currentClient,
                reviewsRepository = reviewsRepository,
                onReviewSubmitted = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
