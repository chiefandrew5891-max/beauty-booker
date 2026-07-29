package com.beautyplanner.client.android.ui.master

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.beautyplanner.client.android.ui.common.ClientTopBar
import com.beautyplanner.client.domain.model.ClientProfile
import com.beautyplanner.client.domain.model.MasterProfile
import com.beautyplanner.client.domain.model.MasterService
import com.beautyplanner.client.domain.repository.MastersRepository
import com.beautyplanner.client.strings.Strings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasterProfileScreen(
    masterId: String,
    client: ClientProfile?,
    mastersRepository: MastersRepository,
    onServiceSelected: (String) -> Unit,
    onReviewsClick: () -> Unit,
    onBackClick: () -> Unit
) {
    var master by remember { mutableStateOf<MasterProfile?>(null) }
    var services by remember { mutableStateOf<List<MasterService>>(emptyList()) }
    var showServicesDialog by remember { mutableStateOf(false) }

    LaunchedEffect(masterId) {
        master = mastersRepository.getMasterById(masterId)
        services = mastersRepository.getServicesForMaster(masterId)
    }

    if (showServicesDialog) {
        ServicesDialog(
            services = services,
            onDismiss = { showServicesDialog = false },
            onServiceClick = { serviceId ->
                showServicesDialog = false
                onServiceSelected(serviceId)
            }
        )
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

                if (services.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(12.dp))

                    ServicesDialogField(
                        onClick = { showServicesDialog = true }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

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
                    onClick = {
                        services.firstOrNull()?.let { onServiceSelected(it.id) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = client?.isGuest != true && services.isNotEmpty()
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

@Composable
private fun ServicesDialogField(
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val onSurface = MaterialTheme.colorScheme.onSurface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                onClick()
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            onSurface.copy(alpha = 0.25f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Услуги",
                style = MaterialTheme.typography.bodyLarge,
                color = onSurface,
                maxLines = 1
            )
            Text(
                text = "▼",
                style = LocalTextStyle.current,
                color = onSurface.copy(alpha = 0.65f)
            )
        }
    }
}

@Composable
private fun ServicesDialog(
    services: List<MasterService>,
    onDismiss: () -> Unit,
    onServiceClick: (String) -> Unit
) {
    val onSurface = MaterialTheme.colorScheme.onSurface

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Услуги",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = onSurface
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp)
                ) {
                    items(services) { service ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onServiceClick(service.id) }
                                .padding(vertical = 10.dp)
                        ) {
                            Text(
                                text = service.titleRu,
                                style = MaterialTheme.typography.bodyLarge,
                                color = onSurface
                            )

                            if (service.descriptionRu.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = service.descriptionRu,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${service.durationMinutes} мин · ${service.price.toInt()} ${service.currency}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        HorizontalDivider()
                    }
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Закрыть")
                }
            }
        }
    }
}