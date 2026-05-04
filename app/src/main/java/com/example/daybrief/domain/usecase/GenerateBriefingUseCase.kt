package com.example.daybrief.domain.usecase

import com.example.daybrief.domain.repository.BriefingRepository

class GenerateBriefingUseCase(private val repository: BriefingRepository) {

    suspend operator fun invoke(): Result<String> = runCatching {
        repository.generateMorningBriefing()
    }
}
