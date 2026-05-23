package com.example.daybrief.data.repository

import com.example.daybrief.data.local.LocalDataSource
import com.example.daybrief.data.remote.GeminiRemoteDataSource
import com.example.daybrief.domain.model.AppSettings
import com.example.daybrief.domain.model.BriefingEntry
import com.example.daybrief.domain.repository.BriefingRepository
import com.example.daybrief.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow

class BriefingRepositoryImpl(
    private val newsRepository: NewsRepository,
    private val geminiDataSource: GeminiRemoteDataSource,
    private val localDataSource: LocalDataSource,
) : BriefingRepository {

    override suspend fun generateMorningBriefing(topics: List<String>): String =
        geminiDataSource.runAgent(topics) { topic, limit ->
            newsRepository.getHeadlines(topic, limit)
        }

    override fun getBriefingHistory(): Flow<List<BriefingEntry>> =
        localDataSource.briefingHistory

    override suspend fun saveBriefing(entry: BriefingEntry) =
        localDataSource.saveBriefing(entry)

    override fun getSettings(): Flow<AppSettings> =
        localDataSource.settings

    override suspend fun saveSettings(settings: AppSettings) =
        localDataSource.saveSettings(settings)
}
