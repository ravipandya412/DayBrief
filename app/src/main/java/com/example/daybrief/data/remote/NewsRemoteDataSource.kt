package com.example.daybrief.data.remote

import com.example.daybrief.data.remote.api.NewsApiService
import com.example.daybrief.data.remote.dto.ArticleDto

class NewsRemoteDataSource(private val api: NewsApiService) {

    suspend fun fetchHeadlines(topic: String, pageSize: Int): List<ArticleDto> =
        api.getEverything(query = topic, pageSize = pageSize).articles
}
