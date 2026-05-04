package com.example.daybrief.presentation

sealed interface BriefingUiState {
    data object Idle : BriefingUiState
    data object Loading : BriefingUiState
    data class Success(val briefing: String) : BriefingUiState
    data class Error(val message: String) : BriefingUiState
}
