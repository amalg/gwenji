package com.dangerousthings.gwenji.model

import kotlinx.serialization.Serializable

@Serializable
data class EmojiDictionary(
    val categories: List<Category>,
    val emojis: List<EmojiEntry>
)
