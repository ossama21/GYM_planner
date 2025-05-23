package com.H_Oussama.gymplanner.ui.progress

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.H_Oussama.gymplanner.data.model.SetLog
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ExerciseHistoryScreen(
    viewModel: ExerciseHistoryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    @Composable
    fun CenteredMessage(text: String, isError: Boolean = false) {
        Box(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = if (isError) MaterialTheme.colorScheme.error else LocalContentColor.current,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }

    when {
        uiState.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        uiState.error != null -> {
            CenteredMessage("Error: ${uiState.error}", isError = true)
        }
        uiState.logs.isEmpty() -> {
            CenteredMessage("No history found for ${uiState.exerciseName ?: "this exercise"}.")
        }
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                 items(uiState.logs, key = { it.id }) { log ->
                    SetLogItem(log = log)
                }
            }
        }
    }
}

@Composable
private fun SetLogItem(log: SetLog) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                 Text(
                     text = "${log.reps} reps",
                     style = MaterialTheme.typography.bodyLarge
                 )
                 if (log.weight != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                     Text(
                         text = "@ ${log.weight} kg",
                         style = MaterialTheme.typography.bodyMedium,
                         color = MaterialTheme.colorScheme.secondary
                     )
                 }
            }
            Column(horizontalAlignment = Alignment.End) {
                 Text(
                    text = dateFormat.format(log.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                 )
            }
        }
    }
}

@Composable
private fun DateHeader(date: String) {
    // ... existing code ...
}