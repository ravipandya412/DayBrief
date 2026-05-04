package com.example.daybrief.di

import com.example.daybrief.data.remote.GeminiRemoteDataSource
import com.example.daybrief.data.remote.api.GeminiApiService
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object GeminiModule {

    fun provideGeminiRemoteDataSource(apiKey: String): GeminiRemoteDataSource {
        val service = buildRetrofit(apiKey).create(GeminiApiService::class.java)
        return GeminiRemoteDataSource(service)
    }

    private fun buildRetrofit(apiKey: String): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(GeminiKeyInterceptor(apiKey))
                    .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
                    .build()
            )
            .addConverterFactory(GsonConverterFactory.create())
            .build()
}

private class GeminiKeyInterceptor(private val apiKey: String) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val url = chain.request().url.newBuilder()
            .addQueryParameter("key", apiKey)
            .build()
        return chain.proceed(chain.request().newBuilder().url(url).build())
    }
}
