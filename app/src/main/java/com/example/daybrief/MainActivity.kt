package com.example.daybrief

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.daybrief.di.AppModule
import com.example.daybrief.notification.NotificationHelper
import com.example.daybrief.presentation.BriefingViewModel
import com.example.daybrief.ui.BriefingDetailScreen
import com.example.daybrief.ui.HomeScreen
import com.example.daybrief.ui.SettingsScreen
import com.example.daybrief.ui.theme.DayBriefTheme
import com.example.daybrief.worker.BriefingWorker
import com.example.daybrief.worker.WorkScheduler
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op */ }

    private val viewModel: BriefingViewModel by viewModels {
        AppModule.provideBriefingViewModelFactory(
            context = applicationContext,
            newsApiKey = BuildConfig.NEWS_API_KEY,
            geminiApiKey = BuildConfig.GEMINI_API_KEY,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.createChannel(this)
        requestNotificationPermissionIfNeeded()
        scheduleOnFirstLaunchOnly()

        // If launched directly from notification, open briefing detail
        if (intent?.action == NotificationHelper.ACTION_OPEN_BRIEFING) {
            viewModel.openLatestBriefing()
        }

        enableEdgeToEdge()
        setContent {
            DayBriefTheme {
                val uiState by viewModel.uiState.collectAsState()
                val navController = rememberNavController()

                // Collect one-shot navigation events (e.g. from notification tap)
                LaunchedEffect(Unit) {
                    viewModel.navigationEvent.collect { destination ->
                        navController.navigate(destination)
                    }
                }

                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(
                            briefingState = uiState.briefingState,
                            history = uiState.history,
                            onGetBriefing = viewModel::fetchBriefing,
                            onNavigateToSettings = { navController.navigate("settings") },
                        )
                    }
                    composable("settings") {
                        SettingsScreen(
                            settings = uiState.settings,
                            onSettingsChange = { newSettings ->
                                viewModel.updateSettings(newSettings)
                                WorkScheduler.scheduleDailyBriefing(
                                    this@MainActivity,
                                    newSettings.notificationHour,
                                    newSettings.notificationMinute,
                                )
                            },
                            onBack = { navController.popBackStack() },
                        )
                    }
                    composable("briefing_detail") {
                        BriefingDetailScreen(
                            entry = uiState.history.firstOrNull(),
                            onBack = { navController.popBackStack() },
                        )
                    }
                }
            }
        }
    }

    // Called when the app is already running and the user taps the notification
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action == NotificationHelper.ACTION_OPEN_BRIEFING) {
            viewModel.openLatestBriefing()
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun scheduleOnFirstLaunchOnly() {
        lifecycleScope.launch {
            val workInfos = WorkManager.getInstance(applicationContext)
                .getWorkInfosForUniqueWorkFlow(BriefingWorker.WORK_NAME)
                .first()
            val hasActiveWork = workInfos.any {
                it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING
            }
            if (!hasActiveWork) {
                WorkScheduler.scheduleDailyBriefing(applicationContext)
            }
        }
    }
}
