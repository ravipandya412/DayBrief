package com.example.daybrief.data.remote

import android.util.Log
import com.example.daybrief.data.remote.api.GeminiApiService
import com.example.daybrief.data.remote.dto.GeminiContentDto
import com.example.daybrief.data.remote.dto.GeminiPartDto
import com.example.daybrief.data.remote.dto.GeminiRequestDto
import kotlinx.coroutines.delay
import retrofit2.HttpException
import java.io.IOException

class GeminiRemoteDataSource(private val api: GeminiApiService) {

    suspend fun generateContent(prompt: String): String = withRetry {
        val request = GeminiRequestDto(
            contents = listOf(GeminiContentDto(parts = listOf(GeminiPartDto(text = prompt))))
        )
        api.generateContent(request)
            .candidates
            ?.firstOrNull()
            ?.content
            ?.parts
            ?.firstOrNull()
            ?.text
            ?: error("Gemini returned an empty response")
    }

    private suspend fun <T> withRetry(
        maxAttempts: Int = 3,
        fallbackDelayMs: Long = 60_000L,
        block: suspend () -> T,
    ): T {
        repeat(maxAttempts) { attempt ->
            try {
                return block()
            } catch (e: HttpException) {
                if (e.code() != 429 || attempt == maxAttempts - 1) throw e

                // Honour the Retry-After header when present; fall back to 60 s.
                val waitMs = e.response()
                    ?.headers()
                    ?.get("Retry-After")
                    ?.toLongOrNull()
                    ?.times(1_000L)
                    ?: fallbackDelayMs

                Log.w(TAG, "Gemini rate-limited (429), waiting ${waitMs / 1_000}s then retrying [${attempt + 1}/$maxAttempts]")
                delay(waitMs)
            }
        }
        // Unreachable — loop always throws or returns, but required for type-safety.
        throw IOException("Gemini retry loop exited unexpectedly")
    }

    companion object {
        private const val TAG = "DayBrief"
    }
}
