package com.example.daybrief.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.daybrief.BuildConfig
import com.example.daybrief.di.AppModule
import com.example.daybrief.domain.model.BriefingEntry
import com.example.daybrief.notification.NotificationHelper
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BriefingWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.i(TAG, "BriefingWorker started")
        return try {
            val repository = AppModule.provideBriefingRepository(
                context = applicationContext,
                newsApiKey = BuildConfig.NEWS_API_KEY,
                geminiApiKey = BuildConfig.GEMINI_API_KEY,
            )

            val topics = repository.getSettings().first().topics
            val briefing = repository.generateMorningBriefing(topics)
            val entry = BriefingEntry(
                briefing = briefing,
                generatedAt = System.currentTimeMillis(),
                dateLabel = SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date()),
            )
            repository.saveBriefing(entry)

            NotificationHelper.sendBriefingNotification(applicationContext, briefing)
            Log.i(TAG, "BriefingWorker completed successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "BriefingWorker failed: ${e.message}", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "DayBrief"
        const val WORK_NAME = "daily_briefing"
    }
}
