package com.example.daybrief.data.remote.api

import com.example.daybrief.data.remote.dto.GeminiRequestDto
import com.example.daybrief.data.remote.dto.GeminiResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface GeminiApiService {

    @POST("v1beta/models/gemini-2.5-flash:generateContent")
    suspend fun generateContent(@Body request: GeminiRequestDto): GeminiResponseDto
}
