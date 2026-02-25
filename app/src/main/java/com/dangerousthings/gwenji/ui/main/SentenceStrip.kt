package com.dangerousthings.gwenji.ui.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dangerousthings.gwenji.engine.AssemblyResult
import com.dangerousthings.gwenji.model.EmojiEntry
import com.dangerousthings.gwenji.speech.SpeechProgress
import com.dangerousthings.gwenji.ui.theme.HighlightGold

/**
 * Determines which segment index is currently active based on TTS speech progress.
 *
 * Returns the index of the segment whose character range contains the current speech
 * position, or -1 if no segment is active.
 */
private fun activeSegmentIndex(
    speechProgress: SpeechProgress,
    assemblyResult: AssemblyResult
): Int {
    if (!speechProgress.isSpeaking || speechProgress.currentCharStart < 0) return -1
    return assemblyResult.segments.indexOfFirst { segment ->
        speechProgress.currentCharStart >= segment.startOffset &&
            speechProgress.currentCharStart < segment.endOffset
    }
}

/**
 * The sentence strip displayed at the top of the main screen.
 *
 * Shows the selected emojis in a horizontal row with a clear button, and a live text
 * preview below. During TTS playback, the currently spoken word is highlighted in gold
 * in both the emoji row and the text preview.
 */
@Composable
fun SentenceStrip(
    sentenceEmojis: List<EmojiEntry>,
    assemblyResult: AssemblyResult,
    speechProgress: SpeechProgress,
    onRemoveEmoji: (Int) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeIndex = activeSegmentIndex(speechProgress, assemblyResult)

    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Emoji row with clear button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LazyRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    itemsIndexed(sentenceEmojis) { index, emoji ->
                        val isActive = index == activeIndex
                        val backgroundColor by animateColorAsState(
                            targetValue = if (isActive) HighlightGold else Color.Transparent,
                            label = "emojiBg"
                        )
                        Box(
                            modifier = Modifier
                                .background(backgroundColor, RoundedCornerShape(8.dp))
                                .clickable { onRemoveEmoji(index) }
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = emoji.emoji,
                                fontSize = 36.sp
                            )
                        }
                    }
                }
                if (sentenceEmojis.isNotEmpty()) {
                    IconButton(
                        onClick = onClear,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear sentence"
                        )
                    }
                }
            }

            // Live text preview with word highlighting
            if (assemblyResult.text.isNotEmpty()) {
                val annotatedText = buildAnnotatedString {
                    if (activeIndex >= 0 && assemblyResult.segments.isNotEmpty()) {
                        var lastEnd = 0
                        for (segment in assemblyResult.segments) {
                            // Append any text between the last segment end and this segment start
                            if (segment.startOffset > lastEnd) {
                                append(
                                    assemblyResult.text.substring(lastEnd, segment.startOffset)
                                )
                            }
                            if (segment.emojiIndex == activeIndex) {
                                withStyle(SpanStyle(background = HighlightGold)) {
                                    append(
                                        assemblyResult.text.substring(
                                            segment.startOffset,
                                            segment.endOffset
                                        )
                                    )
                                }
                            } else {
                                append(
                                    assemblyResult.text.substring(
                                        segment.startOffset,
                                        segment.endOffset
                                    )
                                )
                            }
                            lastEnd = segment.endOffset
                        }
                        // Append any trailing text after the last segment
                        if (lastEnd < assemblyResult.text.length) {
                            append(assemblyResult.text.substring(lastEnd))
                        }
                    } else {
                        append(assemblyResult.text)
                    }
                }
                Text(
                    text = annotatedText,
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
