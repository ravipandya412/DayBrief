package com.example.daybrief.domain.repository

import com.example.daybrief.domain.model.AppSettings
import com.example.daybrief.domain.model.BriefingEntry
import kotlinx.coroutines.flow.Flow

interface BriefingRepository {
    suspend fun generateMorningBriefing(topics: List<String>): String
    fun getBriefingHistory(): Flow<List<BriefingEntry>>
    suspend fun saveBriefing(entry: BriefingEntry)
    fun getSettings(): Flow<AppSettings>
    suspend fun saveSettings(settings: AppSettings)
}
