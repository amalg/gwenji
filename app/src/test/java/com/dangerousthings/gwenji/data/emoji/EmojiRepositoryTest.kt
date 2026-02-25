package com.dangerousthings.gwenji.data.emoji

import com.dangerousthings.gwenji.model.EmojiDictionary
import com.dangerousthings.gwenji.model.GrammarRole
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EmojiRepositoryTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `parse test dictionary from resource`() {
        val input = javaClass.classLoader!!.getResourceAsStream("test_dictionary.json")!!
            .bufferedReader().readText()
        val dictionary = json.decodeFromString<EmojiDictionary>(input)
        assertTrue(dictionary.emojis.isNotEmpty())
        assertTrue(dictionary.categories.isNotEmpty())
    }

    @Test
    fun `emojis are grouped by category`() {
        val input = javaClass.classLoader!!.getResourceAsStream("test_dictionary.json")!!
            .bufferedReader().readText()
        val dictionary = json.decodeFromString<EmojiDictionary>(input)
        val byCategory = dictionary.emojis.groupBy { it.category }
        assertTrue(byCategory.containsKey("feelings"))
        assertTrue(byCategory.containsKey("people"))
    }

    @Test
    fun `categories are sorted by display order`() {
        val input = javaClass.classLoader!!.getResourceAsStream("test_dictionary.json")!!
            .bufferedReader().readText()
        val dictionary = json.decodeFromString<EmojiDictionary>(input)
        val sorted = dictionary.categories.sortedBy { it.displayOrder }
        assertEquals(sorted, dictionary.categories)
    }
}
