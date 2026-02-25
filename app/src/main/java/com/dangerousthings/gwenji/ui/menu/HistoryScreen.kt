package com.dangerousthings.gwenji.ui.menu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dangerousthings.gwenji.data.history.HistoryEntry
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Displays a scrollable list of previously spoken sentences from history.
 */
@Composable
fun HistoryScreen(
    historyFlow: Flow<List<HistoryEntry>>,
    onEntryClick: (HistoryEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    val history by historyFlow.collectAsState(initial = emptyList())

    if (history.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No history yet",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(modifier = modifier.fillMaxSize()) {
            items(history, key = { it.id }) { entry ->
                HistoryItem(entry = entry, onClick = { onEntryClick(entry) })
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun HistoryItem(
    entry: HistoryEntry,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = entry.emojiSequence,
            fontSize = 24.sp
        )
        Text(
            text = entry.spokenText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            text = formatTimestamp(entry.timestamp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val formatter = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    return formatter.format(Date(timestamp))
}
