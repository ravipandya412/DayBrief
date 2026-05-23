package com.example.daybrief.domain.usecase

import com.example.daybrief.domain.model.BriefingEntry
import com.example.daybrief.domain.repository.BriefingRepository
import kotlinx.coroutines.flow.Flow

class GetBriefingHistoryUseCase(private val repository: BriefingRepository) {
    operator fun invoke(): Flow<List<BriefingEntry>> = repository.getBriefingHistory()
}
