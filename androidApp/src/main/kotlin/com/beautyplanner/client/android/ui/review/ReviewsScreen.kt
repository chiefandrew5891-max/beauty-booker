package com.beautyplanner.client.android.ui.review

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.beautyplanner.client.domain.model.ClientProfile
import com.beautyplanner.client.domain.model.MasterReview
import com.beautyplanner.client.domain.repository.ReviewsRepository
import com.beautyplanner.client.strings.Strings

/**
 * Shows all public reviews for a master.
 * Authenticated (non-guest) clients can navigate to the leave-review screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewsScreen(
    masterId: String,
    client: ClientProfile?,
    reviewsRepository: ReviewsRepository,
    onLeaveReviewClick: (appointmentId: String) -> Unit,
    onBackClick: () -> Unit
) {
    var reviews by remember { mutableStateOf<List<MasterReview>>(emptyList()) }

    LaunchedEffect(masterId) {
        reviews = reviewsRepository.getReviewsForMaster(masterId)
    }

    Scaffold(
        topBar = {
            ClientTopBar(
                title = Strings.REVIEWS_TITLE,
                showBack = true,
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp)
        ) {
            // Leave review button for authenticated clients
            item {
                if (client?.isGuest == true) {
                    Text(
                        text = Strings.GUEST_REVIEW_BLOCKED,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                } else if (client != null) {
                    Button(
                        onClick = { onLeaveReviewClick("appt-demo") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(Strings.LEAVE_REVIEW)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            items(reviews) { review ->
                ReviewCard(review = review)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun ReviewCard(review: MasterReview) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row {
                Text(
                    text = review.authorNickname,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "★ ${review.rating}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (review.comment.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = review.comment,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = review.createdAt.take(10),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
import com.beautyplanner.client.android.ui.common.ClientTopBar
