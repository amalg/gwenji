package com.dangerousthings.gwenji.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EmojiEntry(
    val emoji: String,
    val category: String,
    val solo: String,
    val chain: Map<String, String>,
    val tags: List<String>,
    @SerialName("grammar_role") val grammarRole: GrammarRole
)
