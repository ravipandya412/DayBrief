# DayBrief — Agentic AI Morning Briefing

> An Android app that autonomously delivers a personalised tech news briefing every morning, powered by a Gemini AI agent that uses real tool-calling to fetch and synthesise news.

---

## What It Does

DayBrief runs a Gemini AI agent every morning at your chosen time. The agent autonomously decides which news topics to fetch, calls a live news tool for each one, analyses the results, and writes a structured briefing — all without any user input. The briefing is delivered as a push notification and is readable in full inside the app.

**Key features:**
- Daily AI-generated morning briefing delivered as a push notification at an exact time
- Tap the notification to read the full briefing instantly on the home screen
- Manual "Run Agent Now" button to generate a briefing on demand
- Last 7 days of briefings stored and browsable in-app
- Customisable notification time (5 AM – 10 AM)
- Customisable news topics (Android Dev, AI, Technology, Kotlin, ML, Startups)
- Light / dark theme following the system setting

---

## Agentic AI — How It Works

This is the core differentiator of the app. Unlike a standard AI integration where the app fetches data and asks the model to summarise it, DayBrief uses **Gemini function calling** to make the AI an autonomous agent that drives the entire pipeline.

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

Gemini calls this tool autonomously for each topic it wants to cover. The app intercepts each call, hits the NewsAPI, and feeds results back into the conversation. Gemini continues this loop (up to 10 turns) until it has enough information to write the final briefing.

### Autonomous Scheduling

The agent runs in the background with no user interaction required:
1. `AlarmManager.setExactAndAllowWhileIdle()` fires at the exact chosen time, even during Doze mode
2. `AlarmReceiver` wakes up, enqueues a one-time `BriefingWorker`
3. `BriefingWorker` runs the full agent pipeline — fetch → synthesise → save → notify
4. `AlarmReceiver` then schedules tomorrow's alarm automatically
5. `BootReceiver` reschedules the alarm after device reboot

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation Compose |
| Architecture | Clean Architecture — Data / Domain / Presentation |
| DI | Manual DI (no Hilt — explicit object graphs) |
| AI | Google Gemini 2.5 Flash — function calling API |
| News data | NewsAPI (`/v2/everything`) |
| Networking | Retrofit 2 + OkHttp + Gson |
| Scheduling | AlarmManager (`setExactAndAllowWhileIdle`) |
| Background work | WorkManager (`CoroutineWorker`) |
| Persistence | DataStore Preferences |
| Concurrency | Kotlin Coroutines + Flow |
| Notifications | NotificationManager + NotificationCompat |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 35 (Android 15) |

---

## Architecture

The app is built on **MVVM (Model-View-ViewModel)** combined with **Clean Architecture**, organised into three strict layers.

### MVVM Pattern

```
  View (Compose screens)
      │  observes StateFlow
      ▼
  ViewModel  ──calls──▶  Use Cases  ──calls──▶  Repositories  ──calls──▶  Data Sources
      ▲                                                                    (API / DataStore)
      │  emits UiState
      └──────────────────────────────────────────────────────────────────────────────────
```

| MVVM role | This app |
|---|---|
| **Model** | Domain layer — Use Cases, Repository interfaces, plain Kotlin models |
| **View** | Compose screens — stateless, just render state and forward events |
| **ViewModel** | `BriefingViewModel` — holds `StateFlow<BriefingUiState>`, calls use cases, survives configuration changes |

The UI follows **Unidirectional Data Flow (UDF)**:
- State flows **down** from ViewModel → Compose screen
- Events flow **up** from screen → ViewModel (e.g. `onGetBriefing`, `onSettingsChange`)
- The screen never modifies state directly

### Clean Architecture — Three Layers

Each layer has a single responsibility. Dependencies only point inward — outer layers depend on inner layers, never the reverse.

```
┌─────────────────────────────────────────────┐
│             Presentation Layer              │  ← ViewModel, UI State, Compose screens
├─────────────────────────────────────────────┤
│               Domain Layer                  │  ← Business logic, Use Cases, Interfaces
├─────────────────────────────────────────────┤
│                Data Layer                   │  ← APIs, DataStore, Repository impls
└─────────────────────────────────────────────┘
         Dependencies flow inward only →
```

### Domain Layer — the core

The innermost layer. Has **zero Android dependencies** — pure Kotlin. Everything else depends on it; it depends on nothing.

- **Models** — `Article`, `BriefingEntry`, `AppSettings` — plain Kotlin data classes, no annotations
- **Repository interfaces** — `BriefingRepository`, `NewsRepository` — contracts defined here, implemented in the data layer
- **Use cases** — each encapsulates exactly one business action:

| Use Case | Responsibility |
|---|---|
| `GenerateBriefingUseCase` | Runs the agent, saves the result to history |
| `FetchHeadlinesUseCase` | Fetches headlines for a list of topics concurrently |
| `GetBriefingHistoryUseCase` | Returns a `Flow<List<BriefingEntry>>` from DataStore |
| `GetSettingsUseCase` | Returns a `Flow<AppSettings>` from DataStore |
| `SaveSettingsUseCase` | Persists updated settings to DataStore |

### Data Layer — implementations

Implements the domain interfaces. Divided into remote (network) and local (persistence).

**Remote:**
- `GeminiRemoteDataSource` — owns the agentic function-calling loop. Sends the initial prompt + tool definitions to Gemini, intercepts `functionCall` responses, executes the matching tool (NewsAPI), feeds results back, and repeats until Gemini returns a final text response
- `NewsRemoteDataSource` — thin wrapper around the `NewsApiService` Retrofit interface
- `GeminiApiService` / `NewsApiService` — Retrofit interfaces. DTOs include full Gemini function-calling types (`FunctionDeclarationDto`, `FunctionCallDto`, `FunctionResponseDto`)

**Local:**
- `LocalDataSource` — wraps DataStore Preferences. Stores briefing history (last 7 entries as JSON) and app settings (notification time, topics list)
- `BriefingRepositoryImpl` / `NewsRepositoryImpl` — bridge domain interfaces to the data sources

### Presentation Layer — UI

Follows a **unidirectional data flow** pattern:

```
User action → ViewModel → Use Case → Repository → DataSource
                 ↓
            UiState (StateFlow)
                 ↓
           Compose Screen (collects + renders)
```

- `BriefingViewModel` — single source of truth. Exposes `BriefingUiState` as a `StateFlow` collected by the UI. Navigation events are emitted as a `SharedFlow` to keep one-shot actions out of state
- `BriefingUiState` — a single data class holding `briefingState` (Idle / Loading / Success / Error), `history`, and `settings`
- Screens are stateless composables — they receive state and emit events upward, containing no logic of their own

### Dependency Injection — Manual Object Graph

No framework DI (no Hilt/Dagger). Dependencies are wired explicitly through `object` modules:

```
AppModule
  └── provideBriefingRepository(context, newsApiKey, geminiApiKey)
        ├── NewsModule.provideNewsRepository(newsApiKey)
        │     └── NewsApiService (Retrofit)
        ├── GeminiModule.provideGeminiRemoteDataSource(geminiApiKey)
        │     └── GeminiApiService (Retrofit)
        └── DataStoreModule.provideLocalDataSource(context)
              └── DataStore<Preferences>
```

The `ViewModelProvider.Factory` is constructed in `AppModule` and passed to `viewModels {}` in `MainActivity`, keeping the Activity free of wiring logic.

### Background Pipeline

```
AlarmManager (exact time)
    → AlarmReceiver
        ├── WorkManager.enqueue(OneTimeWorkRequest<BriefingWorker>)
        └── AlarmScheduler.scheduleDailyBriefing()  ← reschedule tomorrow

BriefingWorker (CoroutineWorker)
    ├── repository.getSettings().first()             ← read saved topics
    ├── repository.generateMorningBriefing(topics)   ← run agent
    ├── repository.saveBriefing(entry)               ← persist to DataStore
    └── NotificationHelper.sendBriefingNotification() ← push to user
```

### Project Structure

```
app/
├── data/
│   ├── local/
│   │   └── LocalDataSource.kt          # DataStore — history + settings
│   ├── remote/
│   │   ├── api/                        # Retrofit service interfaces
│   │   ├── dto/                        # Network DTOs incl. function-calling types
│   │   ├── GeminiRemoteDataSource.kt   # Agentic loop lives here
│   │   └── NewsRemoteDataSource.kt
│   └── repository/
│       ├── BriefingRepositoryImpl.kt
│       └── NewsRepositoryImpl.kt
├── domain/
│   ├── model/                          # Article, BriefingEntry, AppSettings
│   ├── repository/                     # Repository interfaces (contracts)
│   └── usecase/                        # One class per business action
├── presentation/
│   ├── BriefingViewModel.kt
│   └── BriefingUiState.kt
├── ui/
│   ├── HomeScreen.kt                   # Agent status card + history list
│   ├── BriefingDetailScreen.kt         # Full briefing (opened from notification)
│   ├── BriefingRenderer.kt             # Shared markdown-aware briefing renderer
│   ├── SettingsScreen.kt               # Time picker + topic chips
│   └── theme/                          # Color, Type, Theme (light + dark)
├── worker/
│   ├── BriefingWorker.kt               # CoroutineWorker — full agent pipeline
│   ├── AlarmScheduler.kt               # Schedules exact AlarmManager alarms
│   └── WorkScheduler.kt                # WorkManager fallback scheduling
├── receiver/
│   ├── AlarmReceiver.kt                # Fires at alarm time, triggers worker
│   └── BootReceiver.kt                 # Reschedules alarm after device reboot
├── notification/
│   └── NotificationHelper.kt
└── di/
    ├── AppModule.kt
    ├── GeminiModule.kt
    ├── NewsModule.kt
    └── DataStoreModule.kt
```

---

## Portfolio Highlights

- **Gemini function calling** — the AI agent calls tools rather than consuming pre-fetched data; the app executes tool calls and feeds results back in a multi-turn loop
- **Exact-time scheduling** — `AlarmManager.setExactAndAllowWhileIdle()` fires through Doze mode; WorkManager handles the long-running network work
- **Clean Architecture** — strict inward-only dependency rule across all three layers; domain layer has zero Android imports
- **Unidirectional data flow** — single `StateFlow` drives the entire UI; navigation events via `SharedFlow` stay out of persistent state
- **Rate-limit resilience** — automatic retry with `Retry-After` header support on Gemini 429 responses
- **Inline markdown rendering** — custom `AnnotatedString` parser renders `**bold**` section headers and bullet points from raw Gemini output without any external library
