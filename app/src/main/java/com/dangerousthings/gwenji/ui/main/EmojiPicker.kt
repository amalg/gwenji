package com.dangerousthings.gwenji.ui.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dangerousthings.gwenji.model.Category
import com.dangerousthings.gwenji.model.EmojiEntry
import kotlinx.coroutines.launch

/**
 * Emoji picker with category tabs at the top and a scrollable emoji grid below.
 *
 * Each emoji cell responds to a tap (adding it to the sentence) and a long-press
 * (showing a tooltip with the word the emoji contributes).
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun EmojiPicker(
    categories: List<Category>,
    selectedCategory: String,
    emojis: List<EmojiEntry>,
    onCategorySelected: (String) -> Unit,
    onEmojiClick: (EmojiEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedIndex = categories.indexOfFirst { it.name == selectedCategory }.coerceAtLeast(0)

    Column(modifier = modifier.fillMaxWidth()) {
        // Category tabs
        if (categories.isNotEmpty()) {
            ScrollableTabRow(
                selectedTabIndex = selectedIndex,
                edgePadding = 8.dp
            ) {
                categories.forEachIndexed { index, category ->
                    Tab(
                        selected = index == selectedIndex,
                        onClick = { onCategorySelected(category.name) },
                        text = {
                            Text(text = "${category.icon} ${category.name}")
                        }
                    )
                }
            }
        }

        // Emoji grid
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 56.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 4.dp)
        ) {
            items(emojis, key = { it.emoji }) { emoji ->
                val tooltipState = rememberTooltipState()
                val scope = rememberCoroutineScope()
                val tooltipText = emoji.chain["default"] ?: emoji.solo

                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = {
                        PlainTooltip {
                            Text(text = tooltipText)
                        }
                    },
                    state = tooltipState
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .combinedClickable(
                                onClick = { onEmojiClick(emoji) },
                                onLongClick = {
                                    scope.launch { tooltipState.show() }
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = emoji.emoji,
                            fontSize = 32.sp
                        )
                    }
                }
            }
        }
    }
}
