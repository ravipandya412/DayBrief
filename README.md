# DayBrief — Agentic AI Morning Briefing

> An Android app that autonomously delivers a personalised tech news briefing every morning, powered by a Gemini AI agent that uses real tool-calling to fetch and synthesise news.

---

## What It Does

DayBrief runs a Gemini AI agent every morning at your chosen time. The agent autonomously decides which news topics to fetch, calls a live news tool for each one, analyses the results, and writes a structured briefing — all without any user input. The briefing is delivered as a push notification and is readable in full inside the app.

**Key user-facing features:**
- Daily AI-generated morning briefing delivered as a push notification
- Tap the notification to read the full briefing instantly
- Manual "Run Agent Now" button to generate a briefing on demand
- Last 7 days of briefings stored and browsable in-app
- Customisable notification time (5 AM – 10 AM)
- Customisable news topics (Android Dev, AI, Technology, Kotlin, ML, Startups)
- Light / dark theme following system setting

---

## Agentic AI — How It Works

This is the core differentiator of the app. Unlike a standard AI integration where the app fetches data and then asks the model to summarise it, DayBrief uses **Gemini function calling** to make the AI an autonomous agent that drives the entire pipeline.

### The Agent Loop

```
┌─────────────────────────────────────────────────────────┐
│                     Gemini Agent                        │
│                                                         │
│  1. Receives system prompt + available tools            │
│  2. Decides: "I need to fetch Android Dev news"         │
│  3. Calls  → fetch_news("android development", 8)       │
│                         ↓                               │
│             App executes NewsAPI call                   │
│             Returns articles to Gemini                  │
│                         ↓                               │
│  4. Decides: "Now I need AI news"                       │
│  5. Calls  → fetch_news("artificial intelligence", 8)   │
│                         ↓  (repeats per topic)          │
│  6. All topics collected — Gemini synthesises           │
│     a structured, insightful morning briefing           │
└─────────────────────────────────────────────────────────┘
```

### Why This Is Agentic

| Standard AI integration | DayBrief Agentic approach |
|---|---|
| App fetches all data upfront | **Gemini decides what data to fetch** |
| Single prompt → single response | **Multi-turn conversation with tool calls** |
| Static, hardcoded pipeline | **Agent adapts — picks topics, decides order** |
| App is in control | **AI is in control of the workflow** |

### Tool Defined

```
fetch_news(topic: String, limit: Int) → List<Article>
```

Gemini calls this tool autonomously for each topic it wants to cover. The app intercepts the call, hits the NewsAPI, and feeds the results back into the conversation. Gemini continues until it has enough information to write the briefing.

### Autonomous Scheduling

The agent runs entirely in the background via **WorkManager** — no user interaction required. After the first launch, the app:
1. Calculates the delay to your next chosen wake-up time
2. Schedules a `PeriodicWorkRequest` that fires daily
3. Re-schedules automatically after device reboot via `BootReceiver`
4. Sends a rich push notification with the briefing snippet when done

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation Compose |
| Architecture | Clean Architecture (Data / Domain / Presentation) |
| DI | Manual DI (AppModule / NewsModule / GeminiModule) |
| AI | Google Gemini 2.5 Flash — function calling API |
| News data | NewsAPI (`/v2/everything`) |
| Networking | Retrofit 2 + OkHttp + Gson |
| Background work | WorkManager (`CoroutineWorker`) |
| Persistence | DataStore Preferences |
| Concurrency | Kotlin Coroutines + Flow |
| Notifications | NotificationManager + NotificationCompat |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 35 (Android 15) |

---

## Architecture

```
app/
├── data/
│   ├── local/          # DataStore — briefing history + settings
│   ├── remote/
│   │   ├── api/        # Retrofit interfaces (Gemini, NewsAPI)
│   │   ├── dto/        # DTOs incl. function-calling types
│   │   ├── GeminiRemoteDataSource.kt   ← agentic loop lives here
│   │   └── NewsRemoteDataSource.kt
│   └── repository/     # Repository implementations
├── domain/
│   ├── model/          # Article, BriefingEntry, AppSettings
│   ├── repository/     # Repository interfaces
│   └── usecase/        # GenerateBriefing, GetHistory, GetSettings, SaveSettings
├── presentation/       # BriefingViewModel, BriefingUiState
├── ui/
│   ├── HomeScreen.kt           # Agent status card + history
│   ├── BriefingDetailScreen.kt # Full briefing view (opened from notification)
│   ├── SettingsScreen.kt       # Time picker + topic chips
│   └── theme/                  # Color, Type, Theme (light + dark)
├── worker/
│   ├── BriefingWorker.kt   # CoroutineWorker — runs agent, saves, notifies
│   └── WorkScheduler.kt    # Schedules/reschedules daily PeriodicWork
├── receiver/
│   └── BootReceiver.kt     # Reschedules after device reboot
├── notification/
│   └── NotificationHelper.kt
└── di/
    ├── AppModule.kt
    ├── GeminiModule.kt
    ├── NewsModule.kt
    └── DataStoreModule.kt
```

---

## Setup

### Prerequisites
- Android Studio Hedgehog or later
- Android device / emulator (API 24+)
- A [NewsAPI](https://newsapi.org/) free API key
- A [Google AI Studio](https://aistudio.google.com/) Gemini API key

### Steps

1. Clone the repository
   ```bash
   git clone https://github.com/ravipandya/DayBrief.git
   cd DayBrief
   ```

2. Add your API keys to `local.properties` (create if it doesn't exist):
   ```properties
   NEWS_API_KEY=your_newsapi_key_here
   GEMINI_API_KEY=your_gemini_api_key_here
   ```

3. Build and run
   ```bash
   ./gradlew assembleDebug
   ```
   Or open in Android Studio and press Run.

4. Grant the notification permission when prompted on first launch.

> **Note:** The `local.properties` file is git-ignored. Never commit API keys.

---

## Notification Reliability

WorkManager is fully on-device — no FCM or server required. Delivery is reliable on stock Android. On OEM-customised Android (Samsung, Xiaomi, OnePlus), battery optimisation may delay background tasks. For best results, go to:

**Settings → Battery → App battery usage → DayBrief → Unrestricted**

---

## Portfolio Notes

This app was built to demonstrate real-world Agentic AI implementation on Android:

- **Function calling / tool use** — the AI agent calls tools rather than just consuming pre-fetched data
- **Multi-turn agent loop** — Gemini drives a conversation of up to 10 turns before producing output
- **Autonomous background execution** — the agent runs daily without any user interaction
- **Rate-limit resilience** — automatic retry with `Retry-After` header support on Gemini 429 responses
- **Clean Architecture** — strict layer separation, no framework DI, easy to test
- **Full delivery pipeline** — from background agent execution to DataStore persistence to push notification to in-app deep link
