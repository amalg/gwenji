package com.dangerousthings.gwenji.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "gwenji_prefs")

class UserPreferences(private val context: Context) {
    private object Keys {
        val SELECTED_VOICE = stringPreferencesKey("selected_voice")
        val SPEECH_RATE = floatPreferencesKey("speech_rate")
        val APP_MODE = stringPreferencesKey("app_mode")
    }

    val selectedVoice: Flow<String?> = context.dataStore.data.map { it[Keys.SELECTED_VOICE] }
    val speechRate: Flow<Float> = context.dataStore.data.map { it[Keys.SPEECH_RATE] ?: 0.85f }
    val appMode: Flow<String> = context.dataStore.data.map { it[Keys.APP_MODE] ?: "free" }

    suspend fun setSelectedVoice(voiceName: String) {
        context.dataStore.edit { it[Keys.SELECTED_VOICE] = voiceName }
    }
    suspend fun setSpeechRate(rate: Float) {
        context.dataStore.edit { it[Keys.SPEECH_RATE] = rate }
    }
    suspend fun setAppMode(mode: String) {
        context.dataStore.edit { it[Keys.APP_MODE] = mode }
    }
}
