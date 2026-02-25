package com.dangerousthings.gwenji.model

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class EmojiEntryTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `deserialize emoji entry with all fields`() {
        val jsonString = """
        {
            "emoji": "\uD83D\uDE22",
            "category": "feelings",
            "solo": "I feel sad",
            "chain": {
                "default": "sad",
                "after_i": "feel sad"
            },
            "tags": ["emotion", "negative"],
            "grammar_role": "descriptor"
        }
        """.trimIndent()

        val entry = json.decodeFromString<EmojiEntry>(jsonString)
        assertEquals("\uD83D\uDE22", entry.emoji)
        assertEquals("feelings", entry.category)
        assertEquals("I feel sad", entry.solo)
        assertEquals("sad", entry.chain["default"])
        assertEquals("feel sad", entry.chain["after_i"])
        assertEquals(listOf("emotion", "negative"), entry.tags)
        assertEquals(GrammarRole.DESCRIPTOR, entry.grammarRole)
    }

    @Test
    fun `deserialize emoji entry with minimal chain`() {
        val jsonString = """
        {
            "emoji": "\uD83D\uDC49",
            "category": "people",
            "solo": "I need something",
            "chain": {
                "default": "I"
            },
            "tags": ["self", "me"],
            "grammar_role": "subject"
        }
        """.trimIndent()

        val entry = json.decodeFromString<EmojiEntry>(jsonString)
        assertEquals("I", entry.chain["default"])
        assertEquals(1, entry.chain.size)
    }

    @Test
    fun `deserialize full dictionary`() {
        val jsonString = """
        {
            "categories": [
                {
                    "name": "Feelings",
                    "icon": "heart",
                    "display_order": 1
                }
            ],
            "emojis": [
                {
                    "emoji": "\uD83D\uDE22",
                    "category": "feelings",
                    "solo": "I feel sad",
                    "chain": { "default": "sad" },
                    "tags": ["emotion"],
                    "grammar_role": "descriptor"
                }
            ]
        }
        """.trimIndent()

        val dict = json.decodeFromString<EmojiDictionary>(jsonString)
        assertEquals(1, dict.categories.size)
        assertEquals("Feelings", dict.categories[0].name)
        assertEquals(1, dict.emojis.size)
    }
}
