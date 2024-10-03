package com.acetylsalicylsaeure.platten.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.acetylsalicylsaeure.platten.data.Exercise
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.math.abs

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ExerciseItem(
    exercise: Exercise,
    lastTrainedDate: Date?,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = formatLastTrainedDays(lastTrainedDate),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun formatLastTrainedDays(date: Date?): String {
    return if (date != null) {
        val lastTrainedLocalDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        val currentLocalDate = LocalDate.now()
        val daysBetween = ChronoUnit.DAYS.between(lastTrainedLocalDate, currentLocalDate)

        when {
            daysBetween == 0L -> "Trained today"
            daysBetween == 1L -> "Trained yesterday"
            daysBetween > 1L -> "Trained $daysBetween days ago"
            else -> "Date error" // This shouldn't happen unless there's a future date
        }
    } else {
        "Never trained"
    }
}