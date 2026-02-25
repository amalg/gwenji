package com.dangerousthings.gwenji.ui.menu

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class MenuDestination {
    HISTORY, SETTINGS
}

/**
 * Navigation drawer content showing History and Settings destinations.
 * Includes a title header, nav items, and the selected content below.
 */
@Composable
fun MenuDrawer(
    selectedDestination: MenuDestination,
    onDestinationSelected: (MenuDestination) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (MenuDestination) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 24.dp)
    ) {
        // Title
        Text(
            text = "Gwenji",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        // Navigation items
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.History, contentDescription = null) },
            label = { Text("History") },
            selected = selectedDestination == MenuDestination.HISTORY,
            onClick = { onDestinationSelected(MenuDestination.HISTORY) },
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            label = { Text("Settings") },
            selected = selectedDestination == MenuDestination.SETTINGS,
            onClick = { onDestinationSelected(MenuDestination.SETTINGS) },
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Content area
        content(selectedDestination)
    }
}
