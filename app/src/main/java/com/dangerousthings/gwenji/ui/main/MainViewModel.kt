package com.dangerousthings.gwenji.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dangerousthings.gwenji.data.emoji.EmojiRepository
import com.dangerousthings.gwenji.data.history.GwenjiDatabase
import com.dangerousthings.gwenji.data.history.HistoryEntry
import com.dangerousthings.gwenji.engine.AssemblyResult
import com.dangerousthings.gwenji.engine.SentenceAssembler
import com.dangerousthings.gwenji.model.Category
import com.dangerousthings.gwenji.model.EmojiEntry
import com.dangerousthings.gwenji.speech.SpeechProgress
import com.dangerousthings.gwenji.speech.TtsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class MainUiState(
    val categories: List<Category> = emptyList(),
    val currentCategoryEmojis: List<EmojiEntry> = emptyList(),
    val selectedCategory: String = "",
    val sentenceEmojis: List<EmojiEntry> = emptyList(),
    val assemblyResult: AssemblyResult = AssemblyResult("", emptyList()),
    val speechProgress: SpeechProgress = SpeechProgress()
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val emojiRepository = EmojiRepository(application)
    private val assembler = SentenceAssembler()
    private val ttsManager = TtsManager.getInstance(application)
    private val database = GwenjiDatabase.getDatabase(application)
    private val historyDao = database.historyDao()

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState
    val speechProgress: StateFlow<SpeechProgress> = ttsManager.progress

    init {
        emojiRepository.load()
        val categories = emojiRepository.getCategories()
        val firstCategory = categories.firstOrNull()
        _uiState.value = MainUiState(
            categories = categories,
            selectedCategory = firstCategory?.name ?: "",
            currentCategoryEmojis = firstCategory?.let {
                emojiRepository.getEmojisByCategory(it.name)
            } ?: emptyList()
        )
    }

    fun selectCategory(categoryName: String) {
        _uiState.value = _uiState.value.copy(
            selectedCategory = categoryName,
            currentCategoryEmojis = emojiRepository.getEmojisByCategory(categoryName)
        )
    }

    fun addEmoji(emoji: EmojiEntry) {
        val updated = _uiState.value.sentenceEmojis + emoji
        _uiState.value = _uiState.value.copy(
            sentenceEmojis = updated,
            assemblyResult = assembler.assemble(updated)
        )
    }

    fun removeEmoji(index: Int) {
        val updated = _uiState.value.sentenceEmojis.toMutableList().apply { removeAt(index) }
        _uiState.value = _uiState.value.copy(
            sentenceEmojis = updated,
            assemblyResult = assembler.assemble(updated)
        )
    }

    fun clearSentence() {
        _uiState.value = _uiState.value.copy(
            sentenceEmojis = emptyList(),
            assemblyResult = AssemblyResult("", emptyList())
        )
    }

    fun speak() {
        val state = _uiState.value
        if (state.assemblyResult.text.isBlank()) return
        viewModelScope.launch {
            historyDao.insert(
                HistoryEntry(
                    emojiSequence = state.sentenceEmojis.joinToString("") { it.emoji },
                    spokenText = state.assemblyResult.text
                )
            )
        }
        ttsManager.speak(state.assemblyResult.text)
    }

    fun stopSpeaking() {
        ttsManager.stop()
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.stop()
    }
}
