# PL-Advisor-App — Improvement Roadmap

> Updated: 2026-05-24 | Post-Audit Update

---

## ✅ Completed Phase (v1.1)
- [x] **T1.1 Daily Feed Recommendation Engine:** Adjusted for temp/metabolism.
- [x] **T1.2 Predictive Mortality Alert:** Linear regression for 3/7-day forecasts.
- [x] **T1.3 WQ Push Notifications:** WorkManager background monitoring.
- [x] **T1.4 Multi-Pond Comparison:** Side-by-side economics dashboard.
- [x] **T1.5 Temp-Adjusted FCR:** Biological correction factor applied.
- [x] **T2.1 Reading History:** List view with delete/edit actions.
- [x] **T2.3 Disease Risk Score:** Multi-factor composite index.
- [x] **T2.4 Harvest Calendar:** Visual timeline for optimal windows.
- [x] **T3.4 XLSX Export:** Native Excel generation.
- [x] **T3.5 Offline AI Fallback:** Rule-based summary when network is down.

---

## 🚀 Next Frontier: The "Best Tool Ever" Vision

### Tier 1: Hardware & Automation (Reducing Friction)
- **T4.1 Bluetooth Probe Sync:** Direct integration with BLE water quality sensors (YSI, Hach).
- **T4.2 Computer Vision Sampling:** Estimating shrimp ABW via camera photos on a reference grid.
- **T4.3 Automated Weather Stress Alerts:** Integration with OpenWeatherMap to predict pH drops after heavy rain.

### Tier 2: Machine Learning & Intelligence
- **T5.1 Adaptive Modeling:** System learns from historical cycle errors to tune growth coefficients (local ML).
- **T5.2 Market Price Integration:** Live feed of regional shrimp prices to refine Harvest Optimizer revenue projections.
- **T5.3 Anonymized Benchmarking:** Compare pond performance against regional averages.

### Tier 3: Enterprise & Scale
- **T6.1 Multi-Farm Management:** Switch between different farm locations/entities.
- **T6.2 Technician Workflows:** Guided daily task lists with "Proof of Work" (photo verification).
- **T6.3 Financial Ledger:** Full P&L reporting beyond just operational costs.

---

## 🛠 Technical Debt & Refactoring
- [ ] **Modularize UI:** Split `ShrimpAppScreens.kt` into smaller, feature-focused files.
- [ ] **Configurable Constants:** Move hardcoded agronomic rules to a `Settings` or `Config` database table.
- [ ] **Pagination:** Implement Room Pagination for `DailyReadings` to support very long cycles.
- [ ] **Conversational AI:** Update `GeminiAdvisor` to support stateful multi-turn conversations.
