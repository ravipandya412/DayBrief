package com.example.daybrief.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.daybrief.domain.usecase.GenerateBriefingUseCase
import retrofit2.HttpException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BriefingViewModel(
    private val generateBriefing: GenerateBriefingUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<BriefingUiState>(BriefingUiState.Idle)
    val uiState: StateFlow<BriefingUiState> = _uiState.asStateFlow()

    fun fetchBriefing() {
        if (_uiState.value is BriefingUiState.Loading) return
        viewModelScope.launch {
            _uiState.value = BriefingUiState.Loading
            generateBriefing()
                .onSuccess { briefing ->
                    Log.i(TAG, "=== DayBrief Morning Briefing ===")
                    Log.i(TAG, briefing)
                    _uiState.value = BriefingUiState.Success(briefing)
                }
                .onFailure { error ->
                    Log.e(TAG, "Briefing failed: ${error.message}", error)
                    val message = when {
                        error is HttpException && error.code() == 429 ->
                            "Gemini rate limit reached. Wait ~60 seconds and try again."
                        else -> error.message ?: "Unknown error"
                    }
                    _uiState.value = BriefingUiState.Error(message)
                }
        }
    }

    companion object {
        private const val TAG = "DayBrief"

        fun factory(generateBriefing: GenerateBriefingUseCase): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    BriefingViewModel(generateBriefing) as T
            }
    }
}
