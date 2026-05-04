package com.example.daybrief

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.daybrief.di.AppModule
import com.example.daybrief.presentation.BriefingUiState
import com.example.daybrief.presentation.BriefingViewModel
import com.example.daybrief.ui.theme.DayBriefTheme

class MainActivity : ComponentActivity() {

    private val viewModel: BriefingViewModel by viewModels {
        AppModule.provideBriefingViewModelFactory(
            newsApiKey = BuildConfig.NEWS_API_KEY,
            geminiApiKey = BuildConfig.GEMINI_API_KEY,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DayBriefTheme {
                val uiState by viewModel.uiState.collectAsState()
                BriefingScreen(
                    uiState = uiState,
                    onGetBriefing = viewModel::fetchBriefing,
                )
            }
        }
    }
}

@Composable
fun BriefingScreen(
    uiState: BriefingUiState,
    onGetBriefing: () -> Unit,
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "DayBrief",
                style = MaterialTheme.typography.headlineLarge,
            )
            Text(
                text = "Your AI-powered morning briefing",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                onClick = onGetBriefing,
                enabled = uiState !is BriefingUiState.Loading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = if (uiState is BriefingUiState.Loading) "Fetching briefing..." else "Get Morning Briefing"
                )
            }
            when (uiState) {
                is BriefingUiState.Idle -> Unit
                is BriefingUiState.Loading -> CircularProgressIndicator()
                is BriefingUiState.Success -> Text(
                    text = uiState.briefing,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                )
                is BriefingUiState.Error -> Text(
                    text = "Error: ${uiState.message}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
