package com.beautyplanner.client.android.ui.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.beautyplanner.client.domain.model.PendingReviewPrompt
import com.beautyplanner.client.strings.Strings

/**
 * Reminder dialog shown after a completed appointment.
 * Encourages the client to leave a review, with options to act now or snooze.
 *
 * Multiple title/body text variants are selected based on [prompt.snoozeCount]
 * to keep the reminder fresh and avoid repetition.
 */
@Composable
fun ReviewReminderDialog(
    prompt: PendingReviewPrompt,
    onLeaveReview: () -> Unit,
    onSnooze: () -> Unit,
    onDismiss: () -> Unit
) {
    val variantIndex = prompt.snoozeCount % Strings.REMINDER_TITLE_VARIANTS.size
    val titleText = Strings.REMINDER_TITLE_VARIANTS[variantIndex]
    val bodyText = Strings.REMINDER_BODY_VARIANTS[variantIndex]

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = titleText,
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column {
                Text(
                    text = "Визит к ${prompt.masterName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = bodyText,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp, start = 8.dp, end = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onSnooze,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(Strings.REVIEW_SNOOZE)
                }
                Button(
                    onClick = onLeaveReview,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(Strings.LEAVE_REVIEW)
                }
            }
        }
    )
}
