# PL-Advisor-App — Improvement Roadmap

> Generated: 2026-05-23 | Based on full codebase audit of ShrimpPLAdvisor v1.0

---

## Existing Bugs (Fix First)

| # | Location | Bug | Fix |
|---|----------|-----|-----|
| 1 | `AdvisorEngine.kt` → Harvest Optimizer | `projectedBiomass × 0.025` hardcodes feed cost rate — ignores actual `feedCostPerKg` from RegionProfile | Replace constant with `regionProfile.feedCostPerKg` |
| 2 | `ShrimpRepository.kt` → `getDailyReadings()` | All readings loaded into memory with no archival or pagination — bloats ViewModel at 100+ day cycles | Add `getRecentReadings(limit: Int)` DAO query; archive older rows |
| 3 | `ShrimpAppScreens.kt` → RegionProfile tab | RegionProfile selection does not auto-populate FCR cost fields in the Cost Tracker | Wire `onRegionSelected` callback to update `feedCostPerKg`, `labourCostPerDay`, `electricityCostPerDay` StateFlows |

---

## Tier 1 — Highest Operational Value

These directly affect daily farm decisions and have clear data inputs already in the model.

### T1.1 — Daily Feed Recommendation Engine
- **What:** Calculate recommended feed amount as 3–5% of current estimated biomass, adjusted for temperature (reduce 10% per °C below 26°C, cap at 2% below 22°C)
- **Where:** New method in `AdvisorEngine.kt`; expose via new `recommendedFeedKg: StateFlow<Float>` in ViewModel
- **Display:** Card in DashboardOverviewTab above the FCR card
- **Inputs available:** `currentBiomassKg` (already computed), `lastReading.waterTemp`

### T1.2 — Predictive Mortality Alert
- **What:** Linear regression over last 5–7 `DailyReading.survivalRate` values; project 3 days forward; fire alert if projected survival drops below threshold (configurable, default 85%)
- **Where:** New `predictSurvival(readings: List<DailyReading>): SurvivalForecast` in `AdvisorEngine.kt`
- **Display:** Warning banner in DashboardOverviewTab when forecast < threshold
- **Inputs available:** `dailyReadings` already flows into ViewModel

### T1.3 — Water Quality Push Notifications
- **What:** Background check (WorkManager periodic, 6-hour interval) that fires a notification when:
  - TAN ≥ 0.8 mg/L
  - DO < 5.3 mg/L
  - pH outside 7.3–8.7
- **Where:** New `WaterQualityAlertWorker.kt` (mirrors `FeedingReminderWorker.kt` pattern)
- **Trigger:** Reads latest `DailyReading` from Room; compares against thresholds
- **Channel:** Separate notification channel `wq_alerts` (IMPORTANCE_HIGH)

### T1.4 — Multi-Pond Comparison Dashboard
- **What:** Side-by-side table or bar chart comparing active cycles on: current survival %, FCR, projected profit, DOC (days of culture)
- **Where:** New screen/tab `ComparisonTab`; query all non-archived `PondCycle` rows
- **Display:** Sortable table with conditional row colouring (green/amber/red) by FCR threshold

### T1.5 — Temperature-Adjusted FCR
- **What:** Apply published correction factor to raw FCR: FCR_adj = FCR_raw × (1 + 0.02 × (26 − temp)) for temp < 26°C
- **Where:** `AdvisorEngine.kt` → FCR/Cost Tracker module
- **Expose:** `adjustedFcr: StateFlow<Float>` alongside existing `currentFcr`

---

## Tier 2 — Significant Refinements

These improve data richness and user workflow without requiring new sensors or backends.

### T2.1 — Daily Reading History Table
- **What:** Paginated, scrollable table of all `DailyReading` rows with edit and delete actions
- **Where:** New composable `ReadingHistoryTab` in `ShrimpAppScreens.kt`
- **DAO:** Add `deleteReading(id: Long)` and `updateReading(reading: DailyReading)` to `DailyReadingDao`

### T2.2 — Daily Feed Log
- **What:** Input field in "Log Reading" screen to record actual feed dispensed (kg); accumulate total feed used for the cycle
- **Where:** Add `feedKg: Float` column to `DailyReading` entity (Room migration v2→v3)
- **Use:** Replace hardcoded feed cost estimation with `totalFeedKg × feedCostPerKg`

### T2.3 — Disease Risk Composite Score
- **What:** 0–100 index combining: cycle age (weight 20%), TAN deviation from optimal (30%), survival rate deviation (30%), temperature deviation (20%)
- **Where:** New `calculateDiseaseRisk(reading: DailyReading, cycle: PondCycle): Int` in `AdvisorEngine.kt`
- **Display:** Gauge or coloured badge in DashboardOverviewTab

### T2.4 — Harvest Calendar View
- **What:** Visual date-range picker showing stocking date → projected harvest date on a calendar, with shaded "optimal harvest window" based on AdvisorEngine output
- **Where:** Replace DOC day-count text in HarvestOptimizerTab with a calendar composable

### T2.5 — Full-Cycle Gemini Context
- **What:** Compress week-by-week summaries for cycles > 14 days so Gemini context stays within token limits; send week N as: `"Week N: avg DO=X, pH=Y, survival=Z%, FCR=W"`
- **Where:** `AiRepository.kt` → `buildPromptContext()` function
- **Benefit:** Eliminates context truncation on 60+ day cycles

### T2.6 — RegionProfile Auto-Population
- **What:** When user selects a RegionProfile, auto-populate `feedCostPerKg`, `labourCostPerDay`, `electricityCostPerDay` into the Cost Tracker input fields
- **Where:** Callback chain from `RegionProfileTab` → ViewModel → `CostTrackerTab`

---

## Tier 3 — Platform Extension

These add significant scope and may require external services or hardware.

### T3.1 — Firebase Firestore Cloud Sync
- **What:** Mirror `PondCycle` and `DailyReading` to Firestore for cross-device access and backup
- **Why feasible:** `firebase.bom` is already in `libs.versions.toml`; `firebase.ai` dependency is commented-in but unused
- **Pattern:** Repository pattern already abstracts Room; add `FirestoreDataSource` as secondary sink

### T3.2 — Bluetooth Sensor Integration
- **What:** Read DO/pH/temperature directly from Bluetooth probes (e.g. Hach LDO, YSI ProDSS)
- **Why feasible:** Camera dependencies already declared (commented out); BLE stack available
- **Pattern:** `SensorViewModel` → `BleRepository` → scan → pair → stream readings

### T3.3 — Performance Benchmarking Across Cycles
- **What:** Archive completed cycles with final KPIs; render trend lines across N cycles (FCR improvement, survival improvement, profit per kg)
- **Where:** New `CycleArchiveDao` + `PerformanceBenchmarkTab`

### T3.4 — XLSX Export
- **What:** Export daily readings in Excel format for farm record-keeping requirements
- **Library:** Apache POI (add to dependencies) or `poi-android` fork
- **Note:** APK size impact ~2 MB; consider gating behind a settings toggle

### T3.5 — Offline Gemini Fallback
- **What:** When network unavailable, generate rule-based advisor text from AdvisorEngine outputs instead of returning an error
- **Where:** `AiRepository.kt` → catch `IOException` → call `AdvisorEngine.generateOfflineAdvice()`
- **Output:** Template string filled with live computed values: "FCR is {fcr}, which is {rating}. Harvest in {days} days is {recommendation}."

---

## Implementation Priority Order (Recommended)

```
Phase A (Week 1):  Bug fixes #1–3
Phase B (Week 2):  T1.1 Feed engine + T1.5 Temp FCR  (same data, same module)
Phase C (Week 3):  T1.2 Predictive mortality + T1.3 WQ notifications
Phase D (Week 4):  T2.1 Reading history + T2.2 Feed log + Room migration
Phase E (Week 5):  T1.4 Multi-pond comparison + T2.3 Disease risk score
Phase F (Week 6):  T2.4 Harvest calendar + T2.5 Gemini context fix + T2.6 RegionProfile wiring
Phase G (Later):   Tier 3 items as separate feature branches
```

---

## Architecture Notes

- All new calculation methods belong in `AdvisorEngine.kt` (single responsibility: agronomy logic)
- All new StateFlows belong in `ShrimpViewModel.kt` (exposed as `WhileSubscribed(5000)`)
- All new Room queries belong in the relevant `Dao` interface (never query from ViewModel directly)
- New Worker classes follow `FeedingReminderWorker.kt` pattern exactly
- Room schema version must increment for any entity column addition (currently v2)
