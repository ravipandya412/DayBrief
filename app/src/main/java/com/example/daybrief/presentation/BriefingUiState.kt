package com.example.daybrief.presentation

import com.example.daybrief.domain.model.AppSettings
import com.example.daybrief.domain.model.BriefingEntry

data class BriefingUiState(
    val briefingState: BriefingState = BriefingState.Idle,
    val history: List<BriefingEntry> = emptyList(),
    val settings: AppSettings = AppSettings(),
)

sealed interface BriefingState {
    data object Idle : BriefingState
    data object Loading : BriefingState
    data class Success(val briefing: String) : BriefingState
    data class Error(val message: String) : BriefingState
}
