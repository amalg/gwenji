package com.dangerousthings.gwenji.data.emoji

import android.content.Context
import com.dangerousthings.gwenji.model.Category
import com.dangerousthings.gwenji.model.EmojiDictionary
import com.dangerousthings.gwenji.model.EmojiEntry
import kotlinx.serialization.json.Json

class EmojiRepository(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }
    private lateinit var dictionary: EmojiDictionary

    fun load() {
        val input = context.assets.open("emoji_dictionary.json").bufferedReader().readText()
        dictionary = json.decodeFromString<EmojiDictionary>(input)
    }

    fun getCategories(): List<Category> = dictionary.categories.sortedBy { it.displayOrder }

    fun getEmojisByCategory(categoryName: String): List<EmojiEntry> =
        dictionary.emojis.filter { it.category.equals(categoryName, ignoreCase = true) }

    fun getAllEmojis(): List<EmojiEntry> = dictionary.emojis

    fun searchEmojis(query: String): List<EmojiEntry> =
        dictionary.emojis.filter { entry ->
            entry.tags.any { tag -> tag.contains(query, ignoreCase = true) } ||
                entry.emoji.contains(query)
        }
}
