package com.beautyplanner.client.android

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.beautyplanner.client.android.data.FirebaseBookingRepository
import com.beautyplanner.client.android.data.FirebaseMastersRepository
import com.beautyplanner.client.android.navigation.AppNavigation
import com.beautyplanner.client.android.ui.theme.AppThemeMode
import com.beautyplanner.client.android.ui.theme.BeautyPlannerTheme
import com.beautyplanner.client.fake.FakeAuthRepository
import com.beautyplanner.client.fake.FakeClientProfileRepository
import com.beautyplanner.client.fake.FakeReviewsRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val firebaseApp = FirebaseApp.getInstance()
        Log.d("FirebaseConfigCheck", "applicationId=$packageName")
        Log.d("FirebaseConfigCheck", "projectId=${firebaseApp.options.projectId}")
        Log.d("FirebaseConfigCheck", "applicationIdFromFirebase=${firebaseApp.options.applicationId}")
        Log.d("FirebaseConfigCheck", "gcmSenderId=${firebaseApp.options.gcmSenderId}")

        val firestore = FirebaseFirestore.getInstance()

        val authRepository = FakeAuthRepository()
        val mastersRepository = FirebaseMastersRepository(
            firestore = firestore
        )
        val bookingRepository = FirebaseBookingRepository(
            firestore = firestore,
            mastersRepository = mastersRepository
        )
        val reviewsRepository = FakeReviewsRepository()
        val clientProfileRepository = FakeClientProfileRepository()

        setContent {
            var themeMode by rememberSaveable { mutableStateOf(AppThemeMode.SYSTEM) }

            BeautyPlannerTheme(themeMode = themeMode) {
                AppNavigation(
                    authRepository = authRepository,
                    mastersRepository = mastersRepository,
                    bookingRepository = bookingRepository,
                    reviewsRepository = reviewsRepository,
                    clientProfileRepository = clientProfileRepository,
                    themeMode = themeMode,
                    onThemeModeChange = { themeMode = it }
                )
            }
        }
    }
}