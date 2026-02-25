package com.dangerousthings.gwenji

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.dangerousthings.gwenji.ui.main.MainScreen
import com.dangerousthings.gwenji.ui.theme.GwenjiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GwenjiTheme {
                MainScreen(onMenuClick = { /* drawer will be wired in Task 13 */ })
            }
        }
    }
}
