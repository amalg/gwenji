package com.dangerousthings.gwenji.ui.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * The main screen of the Gwenji app.
 *
 * Assembles the SentenceStrip (top), EmojiPicker (middle), and "Say it!" button (bottom)
 * in a vertical layout inside a Scaffold with a top app bar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val speechProgress by viewModel.speechProgress.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Gwenji") },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Sentence strip at top
            SentenceStrip(
                sentenceEmojis = uiState.sentenceEmojis,
                assemblyResult = uiState.assemblyResult,
                speechProgress = speechProgress,
                onRemoveEmoji = { viewModel.removeEmoji(it) },
                onClear = { viewModel.clearSentence() }
            )

            // Emoji picker in middle, filling available space
            EmojiPicker(
                categories = uiState.categories,
                selectedCategory = uiState.selectedCategory,
                emojis = uiState.currentCategoryEmojis,
                onCategorySelected = { viewModel.selectCategory(it) },
                onEmojiClick = { viewModel.addEmoji(it) },
                modifier = Modifier.weight(1f)
            )

            // "Say it!" button at bottom -- always speaks (or re-speaks) the current sentence
            Button(
                onClick = { viewModel.speak() },
                enabled = uiState.assemblyResult.text.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Text(
                    text = "Say it!",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}
