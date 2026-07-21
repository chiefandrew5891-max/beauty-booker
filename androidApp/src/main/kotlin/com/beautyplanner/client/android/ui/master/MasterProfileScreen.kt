package com.beautyplanner.client.android.ui.master

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.beautyplanner.client.domain.model.ClientProfile
import com.beautyplanner.client.domain.model.MasterProfile
import com.beautyplanner.client.domain.repository.MastersRepository
import com.beautyplanner.client.strings.Strings

/**
 * Master profile screen.
 * Displays avatar, name, specialty, rating, bio, and CTAs to book or view reviews.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasterProfileScreen(
    masterId: String,
    client: ClientProfile?,
    mastersRepository: MastersRepository,
    onServicesClick: () -> Unit,
    onReviewsClick: () -> Unit,
    onBackClick: () -> Unit
) {
    var master by remember { mutableStateOf<MasterProfile?>(null) }

    LaunchedEffect(masterId) {
        master = mastersRepository.getMasterById(masterId)
    }

    Scaffold(
        topBar = {
            ClientTopBar(
                title = master?.displayName ?: "",
                showBack = true,
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        master?.let { m ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = m.avatarUrl,
                    contentDescription = m.displayName,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = m.displayName,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = m.specialtyTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "★ ${m.averageRating}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${m.reviewCount} отзывов",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (m.bio.isNotBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = m.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Book now — disabled for guests
                if (client?.isGuest == true) {
                    Text(
                        text = Strings.GUEST_BOOKING_BLOCKED,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Button(
                    onClick = onServicesClick,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = client?.isGuest != true
                ) {
                    Text(Strings.BOOK_NOW)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onReviewsClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(Strings.REVIEWS_TITLE)
                }
            }
        }
    }
}
import com.beautyplanner.client.android.ui.common.ClientTopBar
