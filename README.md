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



https://github.com/user-attachments/assets/8a7c477a-a73c-446a-869a-3acc91df578c

