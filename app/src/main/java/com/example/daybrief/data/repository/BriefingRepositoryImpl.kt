package com.example.daybrief.data.repository

import com.example.daybrief.data.remote.GeminiRemoteDataSource
import com.example.daybrief.domain.repository.BriefingRepository
import com.example.daybrief.domain.repository.NewsRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class BriefingRepositoryImpl(
    private val newsRepository: NewsRepository,
    private val geminiDataSource: GeminiRemoteDataSource,
) : BriefingRepository {

    private val topics = listOf("android development", "artificial intelligence", "technology")

    override suspend fun generateMorningBriefing(): String {
        val articles = coroutineScope {
            topics
                .map { topic -> async { newsRepository.getHeadlines(topic, pageSize = 10) } }
                .awaitAll()
                .flatten()
        }

        val headlinesList = articles.joinToString("\n") { article ->
            "- [${article.topic}] ${article.title} (${article.source})"
        }

        val prompt = """
            You are a personal tech briefing assistant for a senior Android developer.
            Summarize these headlines into a crisp, insightful morning briefing covering Android dev, AI and Tech news.

            Headlines:
            $headlinesList
        """.trimIndent()

        return geminiDataSource.generateContent(prompt)
    }
}
