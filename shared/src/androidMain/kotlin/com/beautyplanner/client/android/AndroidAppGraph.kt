package com.beautyplanner.client.android

import com.beautyplanner.client.app.AppGraph
import com.beautyplanner.client.data.BookerBackend
import com.beautyplanner.client.data.FirebaseBookingRepository
import com.beautyplanner.client.data.FirebaseMastersRepository
import com.beautyplanner.client.data.FirestoreClientProfileRepository
import com.beautyplanner.client.domain.model.ClientProfile
import com.beautyplanner.client.fake.AuthRepository
import com.beautyplanner.client.fake.FirebaseAuthRepositoryDelegate
import com.beautyplanner.client.fake.ReviewsRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.functions.ktx.functions
import kotlinx.coroutines.tasks.await

object AndroidAppGraph {
    fun create(): AppGraph {
        val firestore = FirebaseFirestore.getInstance()
        val firebaseAuth = Firebase.auth
        val functions = Firebase.functions

        val bookerBackend = BookerBackend(functions)

        val authRepository = AuthRepository(
            firebaseAuthRepository = object : FirebaseAuthRepositoryDelegate {
                override suspend fun signInWithEmail(email: String, password: String): Result<ClientProfile> {
                    return try {
                        val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
                        val user = authResult.user
                            ?: return Result.failure(IllegalStateException("Firebase user is null after sign-in"))

                        bookerBackend.bootstrapBookerUser(
                            email = user.email,
                            displayName = user.displayName,
                            authProvider = "email"
                        ).getOrThrow()

                        bookerBackend.getBookerProfile()
                    } catch (e: Exception) {
                        Result.failure(e)
                    }
                }

                override suspend fun registerWithEmail(email: String, password: String): Result<ClientProfile> {
                    return try {
                        val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                        val user = authResult.user
                            ?: return Result.failure(IllegalStateException("Firebase user is null after registration"))

                        bookerBackend.bootstrapBookerUser(
                            email = user.email,
                            displayName = user.displayName,
                            authProvider = "email"
                        ).getOrThrow()

                        bookerBackend.getBookerProfile()
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

        val clientProfileRepository = FirestoreClientProfileRepository(
            firestore = firestore
        )

        return AppGraph(
            authRepository = authRepository,
            mastersRepository = mastersRepository,
            bookingRepository = bookingRepository,
            reviewsRepository = reviewsRepository,
            clientProfileRepository = clientProfileRepository
        )
    }
}