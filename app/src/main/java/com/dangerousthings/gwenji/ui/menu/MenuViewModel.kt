package com.dangerousthings.gwenji.ui.menu

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.dangerousthings.gwenji.data.history.GwenjiDatabase
import com.dangerousthings.gwenji.data.history.HistoryEntry
import kotlinx.coroutines.flow.Flow

class MenuViewModel(application: Application) : AndroidViewModel(application) {
    private val database = GwenjiDatabase.getDatabase(application)
    val history: Flow<List<HistoryEntry>> = database.historyDao().getAll()
}
