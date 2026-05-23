package com.example.daybrief.data.remote

import android.util.Log
import com.example.daybrief.data.remote.api.GeminiApiService
import com.example.daybrief.data.remote.dto.FunctionCallDto
import com.example.daybrief.data.remote.dto.FunctionDeclarationDto
import com.example.daybrief.data.remote.dto.FunctionParametersDto
import com.example.daybrief.data.remote.dto.FunctionResponseDto
import com.example.daybrief.data.remote.dto.GeminiContentDto
import com.example.daybrief.data.remote.dto.GeminiPartDto
import com.example.daybrief.data.remote.dto.GeminiRequestDto
import com.example.daybrief.data.remote.dto.GeminiToolDto
import com.example.daybrief.data.remote.dto.ParameterPropertyDto
import com.example.daybrief.domain.model.Article
import kotlinx.coroutines.delay
import retrofit2.HttpException
import java.io.IOException

typealias NewsFetcher = suspend (topic: String, limit: Int) -> List<Article>

class GeminiRemoteDataSource(private val api: GeminiApiService) {

    // Agentic loop: Gemini decides which news topics to fetch, calls tools, synthesizes briefing.
    suspend fun runAgent(topics: List<String>, newsFetcher: NewsFetcher): String = withRetry {
        val tools = listOf(buildFetchNewsTool())
        val topicList = topics.joinToString(", ")
        val systemPrompt = """
            You are an agentic morning briefing assistant for a senior Android developer.
            Use the fetch_news tool to retrieve headlines for each of the following topics: $topicList.
            Call fetch_news separately for each topic, then synthesize a crisp, insightful briefing.
        """.trimIndent()

        val conversation = mutableListOf(
            GeminiContentDto(role = "user", parts = listOf(GeminiPartDto(text = systemPrompt)))
        )

        repeat(MAX_AGENT_TURNS) {
            val response = api.generateContent(
                GeminiRequestDto(contents = conversation, tools = tools)
            )
            val candidate = response.candidates?.firstOrNull()
                ?: error("Gemini returned empty candidates")
            val modelContent = candidate.content
                ?: error("Gemini candidate has no content")

            conversation.add(modelContent)

            val functionCalls = modelContent.parts.mapNotNull { it.functionCall }
            if (functionCalls.isEmpty()) {
                // No more tool calls — extract final text
                return@withRetry modelContent.parts
                    .mapNotNull { it.text }
                    .joinToString("\n")
                    .takeIf { it.isNotBlank() }
                    ?: error("Gemini returned no text in final turn")
            }

            // Execute each tool call and feed results back
            val responseParts = functionCalls.map { call ->
                val result = executeTool(call, newsFetcher)
                Log.d(TAG, "Tool call: ${call.name}(${call.args}) → ${result.size} articles")
                GeminiPartDto(
                    functionResponse = FunctionResponseDto(
                        name = call.name,
                        response = result,
                    )
                )
            }
            conversation.add(
                GeminiContentDto(role = "user", parts = responseParts)
            )
        }
        error("Agent exceeded $MAX_AGENT_TURNS turns without producing a final response")
    }

    private suspend fun executeTool(
        call: FunctionCallDto,
        newsFetcher: NewsFetcher,
    ): Map<String, Any> {
        return when (call.name) {
            TOOL_FETCH_NEWS -> {
                val topic = call.args["topic"] as? String ?: "technology"
                val limit = (call.args["limit"] as? Double)?.toInt() ?: 8
                val articles = newsFetcher(topic, limit)
                mapOf(
                    "articles" to articles.map { article ->
                        mapOf("title" to article.title, "source" to article.source, "url" to article.url)
                    }
                )
            }
            else -> mapOf("error" to "Unknown tool: ${call.name}")
        }
    }

    private fun buildFetchNewsTool() = GeminiToolDto(
        functionDeclarations = listOf(
            FunctionDeclarationDto(
                name = TOOL_FETCH_NEWS,
                description = "Fetches recent news headlines for a given topic from NewsAPI.",
                parameters = FunctionParametersDto(
                    properties = mapOf(
                        "topic" to ParameterPropertyDto(
                            type = "STRING",
                            description = "The topic to search for, e.g. 'android development'",
                        ),
                        "limit" to ParameterPropertyDto(
                            type = "INTEGER",
                            description = "Max number of articles to return (1-10)",
                        ),
                    ),
                    required = listOf("topic"),
                )
            )
        )
    )

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
                val waitMs = e.response()
                    ?.headers()
                    ?.get("Retry-After")
                    ?.toLongOrNull()
                    ?.times(1_000L)
                    ?: fallbackDelayMs
                Log.w(TAG, "Gemini rate-limited (429), waiting ${waitMs / 1_000}s [${attempt + 1}/$maxAttempts]")
                delay(waitMs)
            }
        }
        throw IOException("Gemini retry loop exited unexpectedly")
    }

    companion object {
        private const val TAG = "DayBrief"
        private const val MAX_AGENT_TURNS = 10
        private const val TOOL_FETCH_NEWS = "fetch_news"
    }
}
