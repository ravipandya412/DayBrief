package com.example.daybrief.di

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.example.daybrief.data.repository.BriefingRepositoryImpl
import com.example.daybrief.domain.repository.BriefingRepository
import com.example.daybrief.domain.usecase.GenerateBriefingUseCase
import com.example.daybrief.domain.usecase.GetBriefingHistoryUseCase
import com.example.daybrief.domain.usecase.GetSettingsUseCase
import com.example.daybrief.domain.usecase.SaveSettingsUseCase
import com.example.daybrief.presentation.BriefingViewModel

object AppModule {

    fun provideBriefingRepository(
        context: Context,
        newsApiKey: String,
        geminiApiKey: String,
    ): BriefingRepository {
        val newsRepository = NewsModule.provideNewsRepository(newsApiKey)
        val geminiDataSource = GeminiModule.provideGeminiRemoteDataSource(geminiApiKey)
        val localDataSource = DataStoreModule.provideLocalDataSource(context)
        return BriefingRepositoryImpl(newsRepository, geminiDataSource, localDataSource)
    }

    fun provideBriefingViewModelFactory(
        context: Context,
        newsApiKey: String,
        geminiApiKey: String,
    ): ViewModelProvider.Factory {
        val repository = provideBriefingRepository(context, newsApiKey, geminiApiKey)
        return BriefingViewModel.factory(
            generateBriefing = GenerateBriefingUseCase(repository),
            getHistory = GetBriefingHistoryUseCase(repository),
            getSettings = GetSettingsUseCase(repository),
            saveSettings = SaveSettingsUseCase(repository),
        )
    }
}
