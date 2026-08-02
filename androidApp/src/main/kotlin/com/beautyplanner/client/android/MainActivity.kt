package com.beautyplanner.client.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.beautyplanner.client.android.data.BookerBackend
import com.beautyplanner.client.android.data.BookerClientProfileRepository
import com.beautyplanner.client.android.data.FirebaseBookerAuthRepository
import com.beautyplanner.client.android.data.FirebaseBookingRepository
import com.beautyplanner.client.android.data.FirebaseMastersRepository
import com.beautyplanner.client.android.data.InMemoryReviewsRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.functions.ktx.functions

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val firestore = FirebaseFirestore.getInstance()
        val firebaseAuth = FirebaseAuth.getInstance()
        val functions = Firebase.functions

        val bookerBackend = BookerBackend(functions)
        val authRepository = FirebaseBookerAuthRepository(firebaseAuth, bookerBackend)

        val mastersRepository = FirebaseMastersRepository(
            firestore = firestore
        )
        val bookingRepository = FirebaseBookingRepository(
            firestore = firestore,
            mastersRepository = mastersRepository
        )
        val reviewsRepository = InMemoryReviewsRepository()
        val clientProfileRepository = BookerClientProfileRepository(bookerBackend)

        setContent {
            BeautyBookerApp(
                authRepository = authRepository,
                mastersRepository = mastersRepository,
                bookingRepository = bookingRepository,
                reviewsRepository = reviewsRepository,
                clientProfileRepository = clientProfileRepository
            )
        }
    }
}