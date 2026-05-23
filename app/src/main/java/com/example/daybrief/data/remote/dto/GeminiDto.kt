package com.example.daybrief.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GeminiPartDto(
    @SerializedName("text") val text: String? = null,
    @SerializedName("functionCall") val functionCall: FunctionCallDto? = null,
    @SerializedName("functionResponse") val functionResponse: FunctionResponseDto? = null,
)

data class GeminiContentDto(
    @SerializedName("parts") val parts: List<GeminiPartDto>,
    @SerializedName("role") val role: String? = null,
)

data class FunctionCallDto(
    @SerializedName("name") val name: String,
    @SerializedName("args") val args: Map<String, Any>,
)

data class FunctionResponseDto(
    @SerializedName("name") val name: String,
    @SerializedName("response") val response: Map<String, Any>,
)

data class FunctionDeclarationDto(
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("parameters") val parameters: FunctionParametersDto,
)

data class FunctionParametersDto(
    @SerializedName("type") val type: String = "OBJECT",
    @SerializedName("properties") val properties: Map<String, ParameterPropertyDto>,
    @SerializedName("required") val required: List<String>,
)

data class ParameterPropertyDto(
    @SerializedName("type") val type: String,
    @SerializedName("description") val description: String,
)

data class GeminiToolDto(
    @SerializedName("function_declarations") val functionDeclarations: List<FunctionDeclarationDto>,
)

// Request
data class GeminiRequestDto(
    @SerializedName("contents") val contents: List<GeminiContentDto>,
    @SerializedName("tools") val tools: List<GeminiToolDto>? = null,
)

// Response
data class GeminiCandidateDto(
    @SerializedName("content") val content: GeminiContentDto?,
)

data class GeminiResponseDto(
    @SerializedName("candidates") val candidates: List<GeminiCandidateDto>?,
)
