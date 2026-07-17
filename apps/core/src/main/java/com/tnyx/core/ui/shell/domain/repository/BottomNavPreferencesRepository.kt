package com.tnyx.core.ui.shell.domain.repository

import com.tnyx.core.ui.shell.domain.model.ShellTab
import kotlinx.coroutines.flow.Flow

interface BottomNavPreferencesRepository {
    val tabs: Flow<List<ShellTab>>

    suspend fun saveTabs(tabs: List<ShellTab>)

    suspend fun resetToDefault()
}
