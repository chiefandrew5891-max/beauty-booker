package com.beautyplanner.client.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.beautyplanner.client.android.data.FirebaseBookerProfileRepository
import com.beautyplanner.client.android.data.FirebaseBookingRepository
import com.beautyplanner.client.android.data.FirebaseMastersRepository
import com.beautyplanner.client.android.navigation.AppNavigation
import com.beautyplanner.client.android.ui.theme.AppThemeMode
import com.beautyplanner.client.android.ui.theme.BeautyPlannerTheme
import com.beautyplanner.client.domain.model.ClientProfile
import com.beautyplanner.client.fake.AuthRepository
import com.beautyplanner.client.fake.ClientProfileRepository
import com.beautyplanner.client.fake.FirebaseAuthRepositoryDelegate
import com.beautyplanner.client.fake.ReviewsRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val firestore = FirebaseFirestore.getInstance()
        val firebaseAuth = FirebaseAuth.getInstance()
        val profileRepository = FirebaseBookerProfileRepository(firestore)

        val authRepository = AuthRepository(
            firebaseAuthRepository = object : FirebaseAuthRepositoryDelegate {
                override suspend fun signInWithEmail(email: String, password: String): Result<ClientProfile> {
                    return try {
                        val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
                        val user = authResult.user
                            ?: return Result.failure(IllegalStateException("Firebase user is null after sign-in"))

                        profileRepository.getOrCreateProfile(
                            uid = user.uid,
                            email = user.email
                        )
                    } catch (e: Exception) {
                        Result.failure(e)
                    }
                }

                override suspend fun registerWithEmail(email: String, password: String): Result<ClientProfile> {
                    return try {
                        val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                        val user = authResult.user
                            ?: return Result.failure(IllegalStateException("Firebase user is null after registration"))

                        profileRepository.getOrCreateProfile(
                            uid = user.uid,
                            email = user.email
                        )
                    } catch (e: Exception) {
                        Result.failure(e)
                    }
                }

                override fun signOut() {
                    firebaseAuth.signOut()
                }
            }
        )

        val mastersRepository = FirebaseMastersRepository(
            firestore = firestore
        )
        val bookingRepository = FirebaseBookingRepository(
            firestore = firestore,
            mastersRepository = mastersRepository
        )
        val reviewsRepository = ReviewsRepository()
        val clientProfileRepository = ClientProfileRepository()

        setContent {
            var themeMode by rememberSaveable { mutableStateOf(AppThemeMode.LIGHT) }

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