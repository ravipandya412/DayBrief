package com.example.daybrief.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ArticleDto(
    @SerializedName("source") val source: SourceDto,
    @SerializedName("title") val title: String?,
    @SerializedName("url") val url: String?,
)
