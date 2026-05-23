package com.example.daybrief.domain.usecase

import com.example.daybrief.domain.model.AppSettings
import com.example.daybrief.domain.repository.BriefingRepository

class SaveSettingsUseCase(private val repository: BriefingRepository) {
    suspend operator fun invoke(settings: AppSettings) = repository.saveSettings(settings)
}
