package com.beautyplanner.client.android.ui.booking

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.beautyplanner.client.android.ui.common.ClientTopBar
import com.beautyplanner.client.domain.model.BookingRequest
import com.beautyplanner.client.domain.model.ClientProfile
import com.beautyplanner.client.domain.model.MasterProfile
import com.beautyplanner.client.domain.model.MasterService
import com.beautyplanner.client.domain.repository.BookingRepository
import com.beautyplanner.client.domain.repository.MastersRepository
import com.beautyplanner.client.strings.Strings
import kotlinx.coroutines.launch

/**
 * Booking confirmation form.
 * Shows a summary of the master, service, slot, and a note field.
 * Only non-guest clients can reach this screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingFormScreen(
    masterId: String,
    serviceId: String,
    slotId: String,
    client: ClientProfile?,
    mastersRepository: MastersRepository,
    bookingRepository: BookingRepository,
    onBookingConfirmed: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var master by remember { mutableStateOf<MasterProfile?>(null) }
    var service by remember { mutableStateOf<MasterService?>(null) }
    var note by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(masterId, serviceId) {
        master = mastersRepository.getMasterById(masterId)
        service = mastersRepository.getServicesForMaster(masterId).find { it.id == serviceId }
    }

    Scaffold(
        topBar = {
            ClientTopBar(
                title = Strings.BOOKING_FORM_TITLE,
                showBack = true,
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            master?.let {
                Text("Мастер: ${it.displayName}", style = MaterialTheme.typography.bodyLarge)
            }
            service?.let {
                Text("Услуга: ${it.titleRu}", style = MaterialTheme.typography.bodyMedium)
                Text("Стоимость: ${it.price.toInt()} ${it.currency}", style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text(Strings.BOOKING_NOTE_HINT) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    val clientId = client?.id ?: return@Button
                    isLoading = true
                    scope.launch {
                        val request = BookingRequest(
                            id = "booking-${System.currentTimeMillis()}",
                            clientId = clientId,
                            masterId = masterId,
                            serviceId = serviceId,
                            slotId = slotId,
                            appointmentDateTime = slotId,
                            noteFromClient = note.trim()
                        )
                        bookingRepository.submitBooking(request)
                            .onSuccess { onBookingConfirmed(it.id) }
                            .onFailure { errorMessage = Strings.ERROR_GENERIC }
                        isLoading = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text(Strings.BOOKING_CONFIRM_BUTTON)
            }
        }
    }
}
