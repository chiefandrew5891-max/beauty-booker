package com.beautyplanner.client.android.ui.discover

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.beautyplanner.client.android.ui.review.ReviewReminderDialog
import com.beautyplanner.client.domain.model.ClientProfile
import com.beautyplanner.client.domain.model.MasterCategory
import com.beautyplanner.client.domain.model.MasterProfile
import com.beautyplanner.client.domain.model.PendingReviewPrompt
import com.beautyplanner.client.domain.repository.MastersRepository
import com.beautyplanner.client.domain.repository.ReviewsRepository
import com.beautyplanner.client.strings.Strings
import kotlinx.coroutines.launch

/**
 * Discover / home screen.
 * Shows a search field, category filters, a featured masters carousel, and a full masters list.
 * Also triggers the review reminder popup when there are pending prompts.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    client: ClientProfile?,
    mastersRepository: MastersRepository,
    reviewsRepository: ReviewsRepository,
    onMasterClick: (String) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val scope = rememberCoroutineScope()

    var categories by remember { mutableStateOf<List<MasterCategory>>(emptyList()) }
    var featuredMasters by remember { mutableStateOf<List<MasterProfile>>(emptyList()) }
    var allMasters by remember { mutableStateOf<List<MasterProfile>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var pendingPrompt by remember { mutableStateOf<PendingReviewPrompt?>(null) }

    LaunchedEffect(Unit) {
        categories = mastersRepository.getCategories()
        featuredMasters = mastersRepository.getFeaturedMasters()
        allMasters = mastersRepository.getMasters()
        if (client != null && !client.isGuest) {
            pendingPrompt = reviewsRepository.getPendingPrompts(client.id).firstOrNull()
        }
    }

    LaunchedEffect(selectedCategory, searchQuery) {
        allMasters = mastersRepository.getMasters(
            categoryId = selectedCategory,
            query = searchQuery.ifBlank { null }
        )
    }

    // Review reminder popup
    pendingPrompt?.let { prompt ->
        ReviewReminderDialog(
            prompt = prompt,
            onLeaveReview = { /* navigate via callback is handled externally */ },
            onSnooze = {
                scope.launch {
                    reviewsRepository.snoozePrompt(prompt.id, "2025-09-01T00:00:00")
                    pendingPrompt = null
                }
            },
            onDismiss = {
                scope.launch {
                    reviewsRepository.dismissPrompt(prompt.id)
                    pendingPrompt = null
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Search field
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(Strings.DISCOVER_SEARCH_HINT) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true,
                shape = RoundedCornerShape(24.dp)
            )
        }

        // Category filters
        if (categories.isNotEmpty()) {
            item {
                Text(
                    text = Strings.DISCOVER_CATEGORIES,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text("Все") }
                    )
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat.id,
                            onClick = {
                                selectedCategory = if (selectedCategory == cat.id) null else cat.id
                            },
                            label = { Text(cat.titleRu) }
                        )
                    }
                }
            }
        }

        // Featured masters carousel
        if (featuredMasters.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = Strings.DISCOVER_FEATURED,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(featuredMasters) { master ->
                        FeaturedMasterCard(master = master, onClick = { onMasterClick(master.id) })
                    }
                }
            }
        }

        // All masters list
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = Strings.DISCOVER_ALL_MASTERS,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(allMasters) { master ->
            MasterListItem(master = master, onClick = { onMasterClick(master.id) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeaturedMasterCard(master: MasterProfile, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(140.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp)
        ) {
            AsyncImage(
                model = master.avatarUrl,
                contentDescription = master.displayName,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = master.displayName,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 1
            )
            Text(
                text = master.specialtyTitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Text(
                text = "★ ${master.averageRating}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MasterListItem(master: MasterProfile, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = master.avatarUrl,
                contentDescription = master.displayName,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = master.displayName,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = master.specialtyTitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "★ ${master.averageRating}  ·  ${master.reviewCount} отзывов",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
