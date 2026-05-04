package com.example.daybrief.data.remote.api

import com.example.daybrief.data.remote.dto.NewsResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {

    @GET("v2/everything")
    suspend fun getEverything(
        @Query("q") query: String,
        @Query("pageSize") pageSize: Int,
        @Query("sortBy") sortBy: String = "publishedAt",
    ): NewsResponseDto
}
