package com.dangerousthings.gwenji.ui.menu

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

/**
 * Settings screen with speech rate slider and voice selection.
 * All data is passed in as parameters -- no ViewModel dependency.
 */
@Composable
fun SettingsScreen(
    speechRate: Float,
    onSpeechRateChange: (Float) -> Unit,
    availableVoices: List<String>,
    selectedVoice: String?,
    onVoiceSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Speech rate section
        Text(
            text = "Speech Rate",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "%.2f".format(speechRate),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Slider(
            value = speechRate,
            onValueChange = onSpeechRateChange,
            valueRange = 0.5f..1.5f,
            steps = 9,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Voice selection section
        Text(
            text = "Voice",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (availableVoices.isEmpty()) {
            Text(
                text = "No voices available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Column(modifier = Modifier.selectableGroup()) {
                availableVoices.forEach { voiceName ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = voiceName == selectedVoice,
                                onClick = { onVoiceSelected(voiceName) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = voiceName == selectedVoice,
                            onClick = null
                        )
                        Text(
                            text = cleanVoiceName(voiceName),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Guided Mode placeholder
        Text(
            text = "Guided Mode (coming soon)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

/**
 * Cleans up TTS voice names for display by removing common prefixes and
 * making them more human-readable.
 */
private fun cleanVoiceName(name: String): String {
    return name
        .removePrefix("en-us-x-")
        .removePrefix("en-gb-x-")
        .removePrefix("en-au-x-")
        .removePrefix("en-in-x-")
        .replaceFirstChar { it.uppercase() }
        .replace("-", " ")
}
