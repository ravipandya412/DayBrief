package com.example.daybrief.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.daybrief.domain.model.AppSettings
import com.example.daybrief.domain.usecase.GenerateBriefingUseCase
import com.example.daybrief.domain.usecase.GetBriefingHistoryUseCase
import com.example.daybrief.domain.usecase.GetSettingsUseCase
import com.example.daybrief.domain.usecase.SaveSettingsUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

class BriefingViewModel(
    private val generateBriefing: GenerateBriefingUseCase,
    private val getHistory: GetBriefingHistoryUseCase,
    private val getSettings: GetSettingsUseCase,
    private val saveSettings: SaveSettingsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BriefingUiState())
    val uiState: StateFlow<BriefingUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val navigationEvent: SharedFlow<String> = _navigationEvent.asSharedFlow()

    fun openLatestBriefing() {
        _navigationEvent.tryEmit("briefing_detail")
    }

    init {
        viewModelScope.launch {
            getHistory().collect { history ->
                _uiState.update { it.copy(history = history) }
            }
        }
        viewModelScope.launch {
            getSettings().collect { settings ->
                _uiState.update { it.copy(settings = settings) }
            }
        }
    }

    fun fetchBriefing() {
        if (_uiState.value.briefingState is BriefingState.Loading) return
        viewModelScope.launch {
            _uiState.update { it.copy(briefingState = BriefingState.Loading) }
            val topics = _uiState.value.settings.topics
            generateBriefing(topics)
                .onSuccess { briefing ->
                    Log.i(TAG, briefing)
                    _uiState.update { it.copy(briefingState = BriefingState.Success(briefing)) }
                }
                .onFailure { error ->
                    Log.e(TAG, "Briefing failed: ${error.message}", error)
                    val message = when {
                        error is HttpException && error.code() == 429 ->
                            "Gemini rate limit reached. Wait ~60 seconds and try again."
                        else -> error.message ?: "Unknown error"
                    }
                    _uiState.update { it.copy(briefingState = BriefingState.Error(message)) }
                }
        }
    }

    fun updateSettings(settings: AppSettings) {
        viewModelScope.launch {
            saveSettings(settings)
        }
    }

    companion object {
        private const val TAG = "DayBrief"

        fun factory(
            generateBriefing: GenerateBriefingUseCase,
            getHistory: GetBriefingHistoryUseCase,
            getSettings: GetSettingsUseCase,
            saveSettings: SaveSettingsUseCase,
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    BriefingViewModel(generateBriefing, getHistory, getSettings, saveSettings) as T
            }
    }
}
