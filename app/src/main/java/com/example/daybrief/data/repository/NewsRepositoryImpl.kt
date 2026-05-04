package com.example.daybrief.data.repository

import com.example.daybrief.data.remote.NewsRemoteDataSource
import com.example.daybrief.domain.model.Article
import com.example.daybrief.domain.repository.NewsRepository

class NewsRepositoryImpl(
    private val dataSource: NewsRemoteDataSource,
) : NewsRepository {

    override suspend fun getHeadlines(topic: String, pageSize: Int): List<Article> =
        dataSource.fetchHeadlines(topic, pageSize).mapNotNull { dto ->
            val title = dto.title?.takeIf { it.isNotBlank() && it != "[Removed]" }
                ?: return@mapNotNull null
            Article(
                title = title,
                source = dto.source.name ?: "Unknown",
                topic = topic,
                url = dto.url.orEmpty(),
            )
        }
}
