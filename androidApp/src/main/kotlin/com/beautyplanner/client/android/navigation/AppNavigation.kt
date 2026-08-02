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
import java.net.URLDecoder
import java.net.URLEncoder
import com.beautyplanner.client.android.ui.booking.BookingCalendarScreen
import com.beautyplanner.client.android.ui.booking.BookingConfirmationScreen
import com.beautyplanner.client.android.ui.booking.BookingDayScreen
import com.beautyplanner.client.android.ui.booking.BookingFormScreen
import com.beautyplanner.client.android.ui.main.ClientMainScreen
import com.beautyplanner.client.android.ui.master.MasterProfileScreen
import com.beautyplanner.client.android.ui.master.ServicesScreen
import com.beautyplanner.client.android.ui.review.LeaveReviewScreen
import com.beautyplanner.client.android.ui.review.ReviewsScreen
import com.beautyplanner.client.android.ui.theme.AppThemeMode
import com.beautyplanner.client.domain.model.ClientProfile
import com.beautyplanner.client.domain.repository.BookingRepository
import com.beautyplanner.client.domain.repository.MastersRepository
import com.beautyplanner.client.domain.repository.ReviewsRepository

object Routes {
    const val DISCOVER = "discover"
    const val MASTER_PROFILE = "master_profile/{masterId}"
    const val SERVICES = "services/{masterId}"
    const val BOOKING_CALENDAR = "booking_calendar/{masterId}/{serviceId}"
    const val BOOKING_DAY = "booking_day/{masterId}/{serviceId}/{date}"
    const val BOOKING_FORM = "booking_form/{masterId}/{serviceId}/{dateTime}"
    const val BOOKING_CONFIRMATION = "booking_confirmation/{bookingId}"
    const val REVIEWS = "reviews/{masterId}"
    const val LEAVE_REVIEW = "leave_review/{masterId}/{appointmentId}"

    fun masterProfile(masterId: String) = "master_profile/$masterId"

    fun services(masterId: String) = "services/$masterId"

    fun bookingCalendar(masterId: String, serviceId: String) =
        "booking_calendar/$masterId/$serviceId"

    fun bookingDay(masterId: String, serviceId: String, date: String) =
        "booking_day/$masterId/$serviceId/$date"

    fun bookingForm(
        masterId: String,
        serviceId: String,
        dateTime: String
    ): String {
        val encoded = URLEncoder.encode(dateTime, "UTF-8")
        return "booking_form/$masterId/$serviceId/$encoded"
    }

    fun bookingConfirmation(bookingId: String) = "booking_confirmation/$bookingId"

    fun reviews(masterId: String) = "reviews/$masterId"

    fun leaveReview(masterId: String, appointmentId: String) =
        "leave_review/$masterId/$appointmentId"
}

@Composable
fun AppNavigation(
    client: ClientProfile?,
    mastersRepository: MastersRepository,
    bookingRepository: BookingRepository,
    reviewsRepository: ReviewsRepository,
    themeMode: AppThemeMode,
    onThemeModeChange: (AppThemeMode) -> Unit
) {
    val navController = rememberNavController()
    var currentClient by remember(client) { mutableStateOf(client) }

    NavHost(navController = navController, startDestination = Routes.DISCOVER) {
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
                client = currentClient,
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
            val appointmentDateTime = URLDecoder.decode(encodedDateTime, "UTF-8")

            BookingFormScreen(
                masterId = masterId,
                serviceId = serviceId,
                appointmentDateTime = appointmentDateTime,
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