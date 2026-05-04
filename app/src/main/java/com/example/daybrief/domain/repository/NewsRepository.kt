package com.example.daybrief.domain.repository

import com.example.daybrief.domain.model.Article

interface NewsRepository {
    suspend fun getHeadlines(topic: String, pageSize: Int): List<Article>
}
