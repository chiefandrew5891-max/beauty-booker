package com.beautyplanner.client.android.ui.review

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.beautyplanner.client.domain.model.ClientProfile
import com.beautyplanner.client.domain.model.ReviewSubmission
import com.beautyplanner.client.domain.repository.ReviewsRepository
import com.beautyplanner.client.strings.Strings
import kotlinx.coroutines.launch

/**
 * Screen for submitting a review after a completed appointment.
 * Only authenticated (non-guest) clients may reach this screen.
 * Enforces the one-review-per-appointment rule at the repository layer.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveReviewScreen(
    masterId: String,
    appointmentId: String,
    client: ClientProfile?,
    reviewsRepository: ReviewsRepository,
    onReviewSubmitted: () -> Unit,
    onBackClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var rating by remember { mutableIntStateOf(0) }
    var comment by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            ClientTopBar(
                title = Strings.LEAVE_REVIEW,
                showBack = true,
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Ваша оценка",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Star rating row
            Row {
                (1..5).forEach { star ->
                    IconButton(
                        onClick = { rating = star },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = if (star <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = "Звезда $star",
                            tint = if (star <= rating) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                label = { Text("Ваш отзыв (необязательно)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 6,
                minLines = 3
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (rating == 0) {
                        errorMessage = "Выберите оценку"
                        return@Button
                    }
                    val clientId = client?.id ?: return@Button
                    isLoading = true
                    errorMessage = null
                    scope.launch {
                        val submission = ReviewSubmission(
                            masterId = masterId,
                            appointmentId = appointmentId,
                            clientId = clientId,
                            rating = rating,
                            comment = comment.trim()
                        )
                        reviewsRepository.submitReview(submission)
                            .onSuccess { onReviewSubmitted() }
                            .onFailure { errorMessage = it.message ?: Strings.ERROR_GENERIC }
                        isLoading = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text(Strings.LEAVE_REVIEW)
            }
        }
    }
}
import com.beautyplanner.client.android.ui.common.ClientTopBar
