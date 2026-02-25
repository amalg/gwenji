package com.dangerousthings.gwenji.engine

import com.dangerousthings.gwenji.model.EmojiEntry
import com.dangerousthings.gwenji.model.GrammarRole
import org.junit.Assert.assertEquals
import org.junit.Test

class SentenceAssemblerTest {

    private val assembler = SentenceAssembler()

    private val selfEmoji = EmojiEntry(
        emoji = "\uD83D\uDC49",
        category = "people",
        solo = "I need something",
        chain = mapOf("default" to "I"),
        tags = listOf("self"),
        grammarRole = GrammarRole.SUBJECT
    )

    private val sadEmoji = EmojiEntry(
        emoji = "\uD83D\uDE22",
        category = "feelings",
        solo = "I feel sad",
        chain = mapOf("default" to "sad", "after_i" to "feel sad"),
        tags = listOf("emotion"),
        grammarRole = GrammarRole.DESCRIPTOR
    )

    private val hamburgerEmoji = EmojiEntry(
        emoji = "\uD83C\uDF54",
        category = "food",
        solo = "I want a hamburger",
        chain = mapOf(
            "default" to "a hamburger",
            "after_i" to "want a hamburger",
            "after_want" to "a hamburger"
        ),
        tags = listOf("food"),
        grammarRole = GrammarRole.OBJECT
    )

    private val heartEmoji = EmojiEntry(
        emoji = "\u2764\uFE0F",
        category = "feelings",
        solo = "I love you",
        chain = mapOf("default" to "love", "after_i" to "love"),
        tags = listOf("love"),
        grammarRole = GrammarRole.VERB
    )

    private val momEmoji = EmojiEntry(
        emoji = "\uD83D\uDC69",
        category = "people",
        solo = "Mom",
        chain = mapOf("default" to "mom"),
        tags = listOf("family"),
        grammarRole = GrammarRole.OBJECT
    )

    @Test
    fun `single emoji returns solo phrase`() {
        val result = assembler.assemble(listOf(sadEmoji))
        assertEquals("I feel sad", result.text)
    }

    @Test
    fun `single self emoji returns solo phrase`() {
        val result = assembler.assemble(listOf(selfEmoji))
        assertEquals("I need something", result.text)
    }

    @Test
    fun `two emojis with context rule`() {
        val result = assembler.assemble(listOf(selfEmoji, sadEmoji))
        assertEquals("I feel sad", result.text)
    }

    @Test
    fun `two emojis food after self`() {
        val result = assembler.assemble(listOf(selfEmoji, hamburgerEmoji))
        assertEquals("I want a hamburger", result.text)
    }

    @Test
    fun `three emojis chain`() {
        val result = assembler.assemble(listOf(selfEmoji, heartEmoji, momEmoji))
        assertEquals("I love mom", result.text)
    }

    @Test
    fun `two emojis no context match falls to default`() {
        val result = assembler.assemble(listOf(heartEmoji, momEmoji))
        assertEquals("love mom", result.text)
    }

    @Test
    fun `empty list returns empty result`() {
        val result = assembler.assemble(emptyList())
        assertEquals("", result.text)
        assertEquals(0, result.segments.size)
    }

    @Test
    fun `result contains word segments with emoji indices`() {
        val result = assembler.assemble(listOf(selfEmoji, sadEmoji))
        assertEquals(2, result.segments.size)
        assertEquals("I", result.segments[0].text)
        assertEquals(0, result.segments[0].emojiIndex)
        assertEquals("feel sad", result.segments[1].text)
        assertEquals(1, result.segments[1].emojiIndex)
    }

    @Test
    fun `result word segments have correct character offsets`() {
        val result = assembler.assemble(listOf(selfEmoji, sadEmoji))
        assertEquals("I feel sad", result.text)

        // "I" -> start=0, end=1
        assertEquals(0, result.segments[0].startOffset)
        assertEquals(1, result.segments[0].endOffset)

        // "feel sad" -> start=2, end=10
        assertEquals(2, result.segments[1].startOffset)
        assertEquals(10, result.segments[1].endOffset)
    }
}
