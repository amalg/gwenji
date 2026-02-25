package com.dangerousthings.gwenji.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class GrammarRole {
    @SerialName("subject") SUBJECT,
    @SerialName("verb") VERB,
    @SerialName("descriptor") DESCRIPTOR,
    @SerialName("object") OBJECT,
    @SerialName("modifier") MODIFIER,
    @SerialName("greeting") GREETING
}
