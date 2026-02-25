package com.dangerousthings.gwenji

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dangerousthings.gwenji.data.preferences.UserPreferences
import com.dangerousthings.gwenji.speech.TtsManager
import com.dangerousthings.gwenji.ui.main.MainScreen
import com.dangerousthings.gwenji.ui.menu.HistoryScreen
import com.dangerousthings.gwenji.ui.menu.MenuDestination
import com.dangerousthings.gwenji.ui.menu.MenuDrawer
import com.dangerousthings.gwenji.ui.menu.MenuViewModel
import com.dangerousthings.gwenji.ui.menu.SettingsScreen
import com.dangerousthings.gwenji.ui.theme.GwenjiTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var ttsManager: TtsManager
    private lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        ttsManager = TtsManager(this)
        userPreferences = UserPreferences(this)

        setContent {
            GwenjiTheme {
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                val menuViewModel: MenuViewModel = viewModel()

                // Settings state
                val speechRate by userPreferences.speechRate.collectAsState(initial = 0.85f)
                val selectedVoice by userPreferences.selectedVoice.collectAsState(initial = null)
                var availableVoices by remember { mutableStateOf(emptyList<String>()) }
                var selectedDestination by remember { mutableStateOf(MenuDestination.HISTORY) }

                // Refresh voice list when drawer opens
                if (drawerState.isOpen && availableVoices.isEmpty()) {
                    availableVoices = ttsManager.getAvailableVoices().map { it.name }
                }

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet {
                            MenuDrawer(
                                selectedDestination = selectedDestination,
                                onDestinationSelected = { selectedDestination = it }
                            ) { destination ->
                                when (destination) {
                                    MenuDestination.HISTORY -> {
                                        HistoryScreen(
                                            historyFlow = menuViewModel.history,
                                            onEntryClick = { entry ->
                                                scope.launch { drawerState.close() }
                                            }
                                        )
                                    }
                                    MenuDestination.SETTINGS -> {
                                        SettingsScreen(
                                            speechRate = speechRate,
                                            onSpeechRateChange = { rate ->
                                                scope.launch {
                                                    userPreferences.setSpeechRate(rate)
                                                }
                                                ttsManager.setSpeechRate(rate)
                                            },
                                            availableVoices = availableVoices,
                                            selectedVoice = selectedVoice,
                                            onVoiceSelected = { voiceName ->
                                                scope.launch {
                                                    userPreferences.setSelectedVoice(voiceName)
                                                }
                                                val voice = ttsManager.getAvailableVoices()
                                                    .firstOrNull { it.name == voiceName }
                                                if (voice != null) {
                                                    ttsManager.setVoice(voice)
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                ) {
                    MainScreen(
                        onMenuClick = {
                            scope.launch { drawerState.open() }
                        }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::ttsManager.isInitialized) {
            ttsManager.shutdown()
        }
    }
}
