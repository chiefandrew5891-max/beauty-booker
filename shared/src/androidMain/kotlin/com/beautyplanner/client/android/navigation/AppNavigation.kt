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
import com.beautyplanner.client.android.ui.auth.AuthScreenRoute
import com.beautyplanner.client.android.ui.auth.CompleteProfileScreen
import com.beautyplanner.client.android.ui.booking.BookingCalendarScreen
import com.beautyplanner.client.android.ui.booking.BookingConfirmationScreen
import com.beautyplanner.client.android.ui.booking.BookingDayScreen
import com.beautyplanner.client.android.ui.booking.BookingFormScreen
import com.beautyplanner.client.android.ui.main.ClientMainScreen
import com.beautyplanner.client.android.ui.master.MasterProfileScreen
import com.beautyplanner.client.android.ui.master.ServicesScreen
import com.beautyplanner.client.android.ui.review.LeaveReviewScreen
import com.beautyplanner.client.android.ui.review.ReviewsScreen
import com.beautyplanner.client.domain.model.ClientProfile
import com.beautyplanner.client.domain.repository.AuthRepository
import com.beautyplanner.client.domain.repository.BookingRepository
import com.beautyplanner.client.domain.repository.ClientProfileRepository
import com.beautyplanner.client.domain.repository.MastersRepository
import com.beautyplanner.client.domain.repository.ReviewsRepository
import com.beautyplanner.client.navigation.Routes
import com.beautyplanner.client.theme.AppThemeMode
import com.beautyplanner.client.app.AppSessionState
import com.beautyplanner.client.app.AuthFlowDecider
import com.beautyplanner.client.app.AuthFlowDestination

@Composable
fun AppNavigation(
    authRepository: AuthRepository,
    mastersRepository: MastersRepository,
    bookingRepository: BookingRepository,
    reviewsRepository: ReviewsRepository,
    clientProfileRepository: ClientProfileRepository,
    themeMode: AppThemeMode,
    onThemeModeChange: (AppThemeMode) -> Unit,
    selectedLanguageCode: String,
    onLanguageCodeChange: (String) -> Unit
) {
    val navController = rememberNavController()
    var sessionState by remember { mutableStateOf(AppSessionState()) }

    NavHost(navController = navController, startDestination = Routes.AUTH) {

        composable(Routes.AUTH) {
            AuthScreenRoute(
                authRepository = authRepository,
                onSignedIn = { profile ->
                    sessionState = AppSessionState(currentClient = profile)

                    when (AuthFlowDecider.destinationAfterSignIn(profile)) {
                        AuthFlowDestination.COMPLETE_PROFILE -> {
                            navController.navigate(Routes.COMPLETE_PROFILE) {
                                popUpTo(Routes.AUTH) { inclusive = true }
                            }
                        }
                        AuthFlowDestination.DISCOVER -> {
                            navController.navigate(Routes.DISCOVER) {
                                popUpTo(Routes.AUTH) { inclusive = true }
                            }
                        }
                    }
                }
            )
        }

        composable(Routes.COMPLETE_PROFILE) {
            CompleteProfileScreen(
                client = sessionState.currentClient,
                clientProfileRepository = clientProfileRepository,
                onProfileComplete = { updatedProfile ->
                    sessionState = sessionState.copy(currentClient = updatedProfile)
                    navController.navigate(Routes.DISCOVER) {
                        popUpTo(Routes.COMPLETE_PROFILE) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.DISCOVER) {
            ClientMainScreen(
                client = sessionState.currentClient,
                mastersRepository = mastersRepository,
                bookingRepository = bookingRepository,
                reviewsRepository = reviewsRepository,
                themeMode = themeMode,
                onThemeModeChange = onThemeModeChange,
                selectedLanguageCode = selectedLanguageCode,
                onLanguageCodeChange = onLanguageCodeChange,
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
                client = sessionState.currentClient,
                mastersRepository = mastersRepository,
                onServiceSelected = { serviceId ->
                    navController.navigate(Routes.bookingCalendar(masterId, serviceId))
                },
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
                client = sessionState.currentClient,
                mastersRepository = mastersRepository,
                onServiceSelected = { serviceId ->
                    navController.navigate(Routes.bookingCalendar(masterId, serviceId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            Routes.BOOKING_CALENDAR,
            arguments = listOf(
                navArgument("masterId") { type = NavType.StringType },
                navArgument("serviceId") { type = NavType.StringType }
            )
        ) { backStack ->
            val masterId = backStack.arguments?.getString("masterId") ?: return@composable
            val serviceId = backStack.arguments?.getString("serviceId") ?: return@composable

            BookingCalendarScreen(
                masterId = masterId,
                serviceId = serviceId,
                mastersRepository = mastersRepository,
                bookingRepository = bookingRepository,
                onDateSelected = { date ->
                    navController.navigate(Routes.bookingDay(masterId, serviceId, date))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            Routes.BOOKING_DAY,
            arguments = listOf(
                navArgument("masterId") { type = NavType.StringType },
                navArgument("serviceId") { type = NavType.StringType },
                navArgument("date") { type = NavType.StringType }
            )
        ) { backStack ->
            val masterId = backStack.arguments?.getString("masterId") ?: return@composable
            val serviceId = backStack.arguments?.getString("serviceId") ?: return@composable
            val date = backStack.arguments?.getString("date") ?: return@composable

            BookingDayScreen(
                masterId = masterId,
                serviceId = serviceId,
                date = date,
                mastersRepository = mastersRepository,
                bookingRepository = bookingRepository,
                onTimeSelected = { dateTime ->
                    navController.navigate(Routes.bookingForm(masterId, serviceId, dateTime))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            Routes.BOOKING_FORM,
            arguments = listOf(
                navArgument("masterId") { type = NavType.StringType },
                navArgument("serviceId") { type = NavType.StringType },
                navArgument("dateTime") { type = NavType.StringType }
            )
        ) { backStack ->
            val masterId = backStack.arguments?.getString("masterId") ?: return@composable
            val serviceId = backStack.arguments?.getString("serviceId") ?: return@composable
            val encodedDateTime = backStack.arguments?.getString("dateTime") ?: return@composable
            val appointmentDateTime = Routes.decodeBookingDateTime(encodedDateTime)

            BookingFormScreen(
                masterId = masterId,
                serviceId = serviceId,
                appointmentDateTime = appointmentDateTime,
                client = sessionState.currentClient,
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
                client = sessionState.currentClient,
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
                client = sessionState.currentClient,
                reviewsRepository = reviewsRepository,
                onReviewSubmitted = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}