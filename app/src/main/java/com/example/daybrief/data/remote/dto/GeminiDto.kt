package com.example.daybrief.data.remote.dto

import com.google.gson.annotations.SerializedName

// Shared by request and response — Gemini uses identical shape for both
data class GeminiPartDto(
    @SerializedName("text") val text: String,
)

data class GeminiContentDto(
    @SerializedName("parts") val parts: List<GeminiPartDto>,
    @SerializedName("role") val role: String? = null,
)

// Request
data class GeminiRequestDto(
    @SerializedName("contents") val contents: List<GeminiContentDto>,
)

// Response
data class GeminiCandidateDto(
    @SerializedName("content") val content: GeminiContentDto?,
)

data class GeminiResponseDto(
    @SerializedName("candidates") val candidates: List<GeminiCandidateDto>?,
)
