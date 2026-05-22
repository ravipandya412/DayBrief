package com.example.daybrief.di

import com.example.daybrief.data.remote.NewsRemoteDataSource
import com.example.daybrief.data.remote.api.NewsApiService
import com.example.daybrief.data.repository.NewsRepositoryImpl
import com.example.daybrief.domain.repository.NewsRepository
import com.example.daybrief.domain.usecase.FetchHeadlinesUseCase
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NewsModule {

    fun provideNewsRepository(apiKey: String): NewsRepository {
        val service = buildRetrofit(apiKey).create(NewsApiService::class.java)
        return NewsRepositoryImpl(NewsRemoteDataSource(service))
    }

    fun provideFetchHeadlinesUseCase(apiKey: String): FetchHeadlinesUseCase =
        FetchHeadlinesUseCase(provideNewsRepository(apiKey))

    private fun buildRetrofit(apiKey: String): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://newsapi.org/")
            .client(
                OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(ApiKeyInterceptor(apiKey))
                    .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
                    .build()
            )
            .addConverterFactory(GsonConverterFactory.create())
            .build()
}

private class ApiKeyInterceptor(private val apiKey: String) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val url = chain.request().url.newBuilder()
            .addQueryParameter("apiKey", apiKey)
            .build()
        return chain.proceed(chain.request().newBuilder().url(url).build())
    }
}
