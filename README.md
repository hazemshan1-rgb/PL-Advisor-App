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
| **Optimizer** | 30-day hold-vs-harvest simulation with cumulative cost tracking; configurable daily mortality rate and weekly acceleration; includes a side-by-side disease scenario at ×2.5 baseline mortality |
| **AI Advisor** | Gemini 2.0 Flash chat with full pond context, last 14 daily readings, and 7-day trend analysis (DO, TAN, pH, survival, ABW) pre-loaded in every prompt |
| **Report** | Consolidated text report with one-tap Copy or Share to any messaging app |
| **Regions** | Built-in regional price profiles (Vietnam, Indonesia, Saudi Arabia, Generic) with 5-bracket size-based pricing (20 g → 40 g) and linear interpolation; add unlimited custom profiles |

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
│   ├── PondCycle        — master record for one grow cycle; includes configurable
│   │                      mortality rate, weekly acceleration, and region profile link
│   ├── DailyReading     — time-series log: water params + survival per day
│   └── RegionProfile    — regional price profiles with 5 size-bracket prices
└── ui/
    ├── AdvisorEngine    — pure Kotlin business logic (no Android deps, fully testable)
    │                      Module 5 uses a running cost accumulator and runs both a
    │                      normal and a disease scenario (×2.5 mortality) in one call
    ├── GeminiAdvisor    — Gemini REST API client (OkHttp + Moshi); buildPrompt injects
    │                      last 14 readings + 7-day half-period trend analysis
    ├── PondCycleViewModel
    ├── ShrimpAppScreens — tab navigation + all composables including RegionSelectorCard,
    │                      MortalitySettingsCard, and disease scenario comparison panel
    ├── AiAdvisorScreen  — chat UI
    └── Components       — reusable charts (FCR gauge, survival curve, profit bar)
```

**State management**: Room → Repository → `StateFlow` in ViewModel → Compose `collectAsStateWithLifecycle`.

**Database**: Room v3 with three cumulative migrations. Migration 2→3 creates the `region_profiles` table, seeds 4 built-in profiles, and adds `regionProfileId` to `pond_cycles`.

---

## Running Tests

```bash
./gradlew test
```

Unit tests cover all five `AdvisorEngine` modules including the new features: regional price interpolation across 5 brackets, extrapolation above 40 g, configurable mortality rate and weekly acceleration, disease scenario verification (×2.5 rate, lower biomass, no nested recursion), cumulative cost monotonicity, and `buildPrompt` with historical readings and 7-day trend output.

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
