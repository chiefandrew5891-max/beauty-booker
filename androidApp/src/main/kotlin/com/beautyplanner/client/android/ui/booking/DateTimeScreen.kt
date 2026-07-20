package com.beautyplanner.client.android.ui.booking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.beautyplanner.client.domain.model.AvailableSlot
import com.beautyplanner.client.domain.model.ClientProfile
import com.beautyplanner.client.domain.repository.BookingRepository
import com.beautyplanner.client.strings.Strings

/**
 * Shows available time slots for the selected master + service.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimeScreen(
    masterId: String,
    serviceId: String,
    client: ClientProfile?,
    bookingRepository: BookingRepository,
    onSlotSelected: (String) -> Unit,
    onBackClick: () -> Unit
) {
    var slots by remember { mutableStateOf<List<AvailableSlot>>(emptyList()) }

    LaunchedEffect(masterId, serviceId) {
        slots = bookingRepository.getAvailableSlots(masterId, serviceId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Strings.BOOKING_DATE_TIME_TITLE) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = Strings.BACK)
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(slots) { slot ->
                SlotItem(slot = slot, onSelect = { onSlotSelected(slot.id) })
            }
        }
    }
}

@Composable
private fun SlotItem(slot: AvailableSlot, onSelect: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = slot.startDateTime.replace("T", "  "),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onSelect,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(Strings.BOOK_NOW)
            }
        }
    }
}
