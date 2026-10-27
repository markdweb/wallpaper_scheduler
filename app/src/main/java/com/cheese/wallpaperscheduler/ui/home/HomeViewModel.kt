package com.cheese.wallpaperscheduler.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cheese.wallpaperscheduler.data.ScheduleSettings
import com.cheese.wallpaperscheduler.data.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: SettingsRepository,
) : ViewModel() {

    val settings: StateFlow<ScheduleSettings?> = repository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /**
     * The Home screen's own on/off switch controls the same underlying flag the Settings
     * screen does. AlarmScheduler is passed in per-call (rather than injected) since Home is
     * the one screen that doesn't otherwise need scheduler wiring.
     */
    fun setSchedulerEnabled(
        enabled: Boolean,
        alarmScheduler: com.cheese.wallpaperscheduler.scheduler.AlarmScheduler,
    ) {
        viewModelScope.launch {
            repository.setSchedulerEnabled(enabled)
            val updated = repository.settingsFlow.first()
            alarmScheduler.rescheduleAll(updated)
        }
    }
}
