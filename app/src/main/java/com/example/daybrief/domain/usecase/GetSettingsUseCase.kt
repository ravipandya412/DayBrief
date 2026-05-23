package com.example.daybrief.domain.usecase

import com.example.daybrief.domain.model.AppSettings
import com.example.daybrief.domain.repository.BriefingRepository
import kotlinx.coroutines.flow.Flow

class GetSettingsUseCase(private val repository: BriefingRepository) {
    operator fun invoke(): Flow<AppSettings> = repository.getSettings()
}
