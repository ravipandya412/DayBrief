package com.example.daybrief.domain.usecase

import com.example.daybrief.domain.model.Article
import com.example.daybrief.domain.repository.NewsRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class FetchHeadlinesUseCase(private val repository: NewsRepository) {

    suspend operator fun invoke(
        topics: List<String>,
        pageSize: Int = 10,
    ): Result<List<Article>> = runCatching {
        coroutineScope {
            topics
                .map { topic -> async { repository.getHeadlines(topic, pageSize) } }
                .awaitAll()
                .flatten()
        }
    }
}
