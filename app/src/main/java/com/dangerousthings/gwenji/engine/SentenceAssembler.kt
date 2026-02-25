package com.dangerousthings.gwenji.engine

import com.dangerousthings.gwenji.model.EmojiEntry

/**
 * Represents a segment of assembled text mapped back to its source emoji.
 *
 * @param text The resolved text for this emoji in context.
 * @param emojiIndex The index of the source emoji in the input list.
 * @param startOffset The character offset where this segment starts in the full assembled string.
 * @param endOffset The character offset where this segment ends (exclusive) in the full assembled string.
 */
data class WordSegment(
    val text: String,
    val emojiIndex: Int,
    val startOffset: Int,
    val endOffset: Int
)

/**
 * The result of assembling a sequence of emoji entries into natural text.
 *
 * @param text The fully assembled sentence string.
 * @param segments The list of word segments mapping text ranges back to source emojis.
 */
data class AssemblyResult(
    val text: String,
    val segments: List<WordSegment>
)

/**
 * Assembles a list of emoji entries into natural English text using context-aware chain rules.
 *
 * The assembly algorithm walks left to right through the emoji list. For each emoji after the
 * first, it checks whether the current emoji has a chain rule keyed to the first word of the
 * previous emoji's resolved text (e.g., "after_i" when the previous output started with "I").
 * If a matching context key is found, that value is used. Otherwise the assembler falls back
 * to the emoji's default chain value, and finally to its solo phrase.
 */
class SentenceAssembler {

    /**
     * Assembles the given emoji entries into a natural text result.
     *
     * @param entries The ordered list of emoji entries selected by the user.
     * @return An [AssemblyResult] containing the assembled text and segment mappings.
     */
    fun assemble(entries: List<EmojiEntry>): AssemblyResult {
        if (entries.isEmpty()) {
            return AssemblyResult("", emptyList())
        }

        if (entries.size == 1) {
            val solo = entries[0].solo
            return AssemblyResult(
                text = solo,
                segments = listOf(
                    WordSegment(
                        text = solo,
                        emojiIndex = 0,
                        startOffset = 0,
                        endOffset = solo.length
                    )
                )
            )
        }

        // Multiple emojis: walk left to right with context-aware chain resolution.
        val resolvedTexts = mutableListOf<String>()

        for (i in entries.indices) {
            val entry = entries[i]
            val text = if (i == 0) {
                // First emoji in a chain: use default chain value, fall back to solo.
                entry.chain["default"] ?: entry.solo
            } else {
                // Subsequent emojis: check for context key based on previous output.
                val previousText = resolvedTexts[i - 1]
                val firstWord = previousText.split(" ").first().lowercase()
                val contextKey = "after_$firstWord"
                entry.chain[contextKey] ?: entry.chain["default"] ?: entry.solo
            }
            resolvedTexts.add(text)
        }

        // Build the full text and compute segment offsets.
        val segments = mutableListOf<WordSegment>()
        var currentOffset = 0

        for (i in resolvedTexts.indices) {
            if (i > 0) {
                // Account for the space separator between segments.
                currentOffset += 1
            }
            val text = resolvedTexts[i]
            segments.add(
                WordSegment(
                    text = text,
                    emojiIndex = i,
                    startOffset = currentOffset,
                    endOffset = currentOffset + text.length
                )
            )
            currentOffset += text.length
        }

        val fullText = resolvedTexts.joinToString(" ")

        return AssemblyResult(text = fullText, segments = segments)
    }
}
