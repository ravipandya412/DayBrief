package com.example.daybrief.domain.usecase

import com.example.daybrief.domain.model.BriefingEntry
import com.example.daybrief.domain.repository.BriefingRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GenerateBriefingUseCase(private val repository: BriefingRepository) {

    suspend operator fun invoke(topics: List<String>): Result<String> = runCatching {
        val briefing = repository.generateMorningBriefing(topics)
        repository.saveBriefing(
            BriefingEntry(
                briefing = briefing,
                generatedAt = System.currentTimeMillis(),
                dateLabel = SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date()),
            )
        )
        briefing
    }
}
