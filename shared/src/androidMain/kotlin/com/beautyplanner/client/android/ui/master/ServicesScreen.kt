package com.beautyplanner.client.android.ui.master

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.beautyplanner.client.android.ui.common.ClientTopBar
import com.beautyplanner.client.domain.model.ClientProfile
import com.beautyplanner.client.domain.model.MasterService
import com.beautyplanner.client.domain.repository.MastersRepository
import com.beautyplanner.client.strings.Strings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesScreen(
    masterId: String,
    client: ClientProfile?,
    mastersRepository: MastersRepository,
    onServiceSelected: (String) -> Unit,
    onBackClick: () -> Unit
) {
    var services by remember { mutableStateOf<List<MasterService>>(emptyList()) }
    var debugStatus by remember { mutableStateOf("loading...") }
    var debugFirstService by remember { mutableStateOf("none") }

    LaunchedEffect(masterId) {
        runCatching {
            val loaded = mastersRepository.getServicesForMaster(masterId)
            services = loaded
            debugStatus = "masterId=$masterId, services=${loaded.size}"
            debugFirstService = loaded.firstOrNull()?.let {
                "id=${it.id}, title=${it.titleRu}, price=${it.price}, duration=${it.durationMinutes}, currency=${it.currency}"
            } ?: "no services"
        }.onFailure {
            debugStatus = "error: ${it.message}"
            debugFirstService = "failed"
        }
    }

    Scaffold(
        topBar = {
            ClientTopBar(
                title = Strings.BOOKING_SERVICES_TITLE,
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
            item {
                Text(
                    text = "DEBUG: $debugStatus",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "DEBUG FIRST: $debugFirstService",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (services.isEmpty()) {
                item {
                    Text(
                        text = "У этого мастера пока нет доступных услуг",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                items(services) { service ->
                    ServiceItem(
                        service = service,
                        onSelect = { onServiceSelected(service.id) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun ServiceItem(service: MasterService, onSelect: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = service.titleRu,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
                if (service.descriptionRu.isNotBlank()) {
                    Text(
                        text = service.descriptionRu,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${service.durationMinutes} мин  ·  ${service.price.toInt()} ${service.currency}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = onSelect) {
                Text(Strings.BOOK_NOW)
            }
        }
    }
}