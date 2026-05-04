package com.example.daybrief.di

import androidx.lifecycle.ViewModelProvider
import com.example.daybrief.data.repository.BriefingRepositoryImpl
import com.example.daybrief.domain.repository.BriefingRepository
import com.example.daybrief.domain.usecase.GenerateBriefingUseCase
import com.example.daybrief.presentation.BriefingViewModel

object AppModule {

    fun provideBriefingViewModelFactory(
        newsApiKey: String,
        geminiApiKey: String,
    ): ViewModelProvider.Factory {
        val newsRepository = NewsModule.provideNewsRepository(newsApiKey)
        val geminiDataSource = GeminiModule.provideGeminiRemoteDataSource(geminiApiKey)
        val briefingRepository: BriefingRepository = BriefingRepositoryImpl(newsRepository, geminiDataSource)
        val useCase = GenerateBriefingUseCase(briefingRepository)
        return BriefingViewModel.factory(useCase)
    }
}
