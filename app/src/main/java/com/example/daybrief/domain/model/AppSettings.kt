package com.example.daybrief.domain.model

data class AppSettings(
    val topics: List<String> = listOf("android development", "artificial intelligence", "technology"),
    val notificationHour: Int = 7,
    val notificationMinute: Int = 0,
)
