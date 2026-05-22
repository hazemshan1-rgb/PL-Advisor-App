# Shrimp PL Cycle Advisor

An Android app that gives intensive *L. vannamei* (whiteleg shrimp) farmers a full decision-support system in their pocket — from pre-stocking PL quality assessment to daily survival monitoring, cost tracking, AI-powered diagnostics, and harvest-window optimization.

---

## Features

| Tab | What it does |
|-----|-------------|
| **Overview** | Live dashboard summarising all five advisory modules for the active pond |
| **PL Gate** | Pre-stocking quality gate: stress-tolerance, gut-fullness, and supplier scores produce a STOCK / HOLD / REJECT verdict |
| **Stocking** | Calculates safe carrying capacity, optimal stocking density, and flags out-of-range water parameters |
| **Survival** | Plots survival trajectory against the industry reference curve; classifies deviations as Environmental or Pathogenic with directed interventions; logs historical daily readings |
| **FCR & Cost** | Real-time FCR index, cost-per-kg breakdown across feed, PL, aeration, probiotics, and labour |
| **Optimizer** | 30-day hold-vs-harvest simulation showing the net gain for every extra day before harvest |
| **AI Advisor** | Gemini-powered chat that answers natural-language questions with full pond context pre-loaded |
| **Report** | Consolidated text report with one-tap Copy or Share to any messaging app |

---

## Setup

### Requirements

- Android Studio Hedgehog (2023.1.1) or newer
- Android SDK 24+
- A [Google AI Studio](https://aistudio.google.com) API key

### 1. Clone

```bash
git clone https://github.com/<your-handle>/PL-Advisor-App.git
cd PL-Advisor-App
```

### 2. Configure the API key

```bash
cp .env.example .env
# Edit .env and set your key:
# GEMINI_API_KEY=your_key_here
```

The key is injected at build time via the [Secrets Gradle Plugin](https://github.com/google/secrets-gradle-plugin) and is never committed to source control.

### 3. Run

Open the project in Android Studio, wait for Gradle sync, and press **Run**.

---

## Architecture

```
app/
├── data/            # Room entities, DAOs, database, repository
│   ├── PondCycle        — master record for one grow cycle
│   └── DailyReading     — time-series log: water params + survival per day
└── ui/
    ├── AdvisorEngine    — pure Kotlin business logic (no Android deps, fully testable)
    ├── GeminiAdvisor    — Gemini REST API client (OkHttp + Moshi)
    ├── PondCycleViewModel
    ├── ShrimpAppScreens — tab navigation + all 8 tab composables
    ├── AiAdvisorScreen  — chat UI
    └── Components       — reusable charts (FCR gauge, survival curve, profit bar)
```

**State management**: Room → Repository → `StateFlow` in ViewModel → Compose `collectAsStateWithLifecycle`.

**Database**: Room v2 with a migration that adds `daily_readings` without destroying existing pond data.

---

## Running Tests

```bash
./gradlew test
```

Unit tests cover all five `AdvisorEngine` modules (STOCK/HOLD/REJECT verdicts, GREEN/YELLOW/RED survival, environmental vs pathogenic classification, FCR ranges, harvest optimisation) and the Gemini prompt builder.

---

## Localization

The app ships with translations for:

| Locale | Language |
|--------|----------|
| `en` (default) | English |
| `es` | Spanish |
| `vi` | Vietnamese |
| `ar` | Arabic (RTL) |

---

## Production Build

```bash
# Set signing credentials as env vars, then:
./gradlew assembleRelease
```

Release builds have R8 minification and resource shrinking enabled. ProGuard rules keep Room entities, Moshi reflection adapters, and OkHttp classes intact.

---

## License

MIT — see [LICENSE](LICENSE).
