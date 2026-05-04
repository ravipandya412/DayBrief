package com.example.daybrief.domain.repository

interface BriefingRepository {
    suspend fun generateMorningBriefing(): String
}
