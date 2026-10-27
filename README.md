# BMI Pulse — Material 3 Expressive BMI, Body Composition & Offline Health Companion

**BMI Pulse** is a complete, offline-first Android application built with **Kotlin 2.x**, **Jetpack Compose**, **Material 3 Expressive** (`MaterialExpressiveTheme`, `MotionScheme.expressive()`, `MaterialShapes`), **Room**, and **Preferences DataStore**.

---

## Highlights & Architecture

- **100% Jetpack Compose UI & Material 3 Expressive**:
  - `MaterialExpressiveTheme` with `MotionScheme.expressive()` spatial/effects springs, dynamic color (Android 12+), and a fallback Emerald/Teal health palette with a WCAG AAA High-Contrast mode.
  - Expressive components: `ExpressiveBmiGauge` (240° sweep arc with morphing `MaterialShapes` center badge), `ExpressiveButtonGroup` (connected unit and time-range pills), `ExpressiveSplitButton` (primary log + secondary menu), `ExpressiveWavyProgressIndicator`, `ExpressiveFabMenu`, and `WheelSliderNumberInput` (ruler/slider + precision keypad).
  - Adaptive navigation: switches automatically between `NavigationBar` on compact phones and `WideNavigationRail` on tablets/foldables (`width >= 600.dp`).
- **Clinical & Anthropometric Engine (`BmiCalculatorEngine`)**:
  - **Core BMI**: Metric (`kg, cm`), Imperial (`lb, ft+in`), and Mixed (`st+lb, cm`) units, WHO 8-tier classification, Asian-Pacific WHO WPRO (`23.0 / 25.0`) cutoffs, BMI Prime, Ponderal Index, healthy weight range, and built-in unit converter.
  - **Child & Teen BMI-for-Age LMS Percentiles (2–20 yrs)**: Bundled CDC 2000 and WHO 2007 LMS (`L, M, S`) reference tables with linear age interpolation, exact Box-Cox Z-score calculation, and Abramowitz-Stegun normal CDF percentiles (`5th`, `85th`, `95th`).
  - **Body Composition & Metabolism**: US Navy Body Fat %, Deurenberg BMI Body Fat %, Waist-to-Hip Ratio (WHR), Waist-to-Height Ratio (WHtR), Lean Body Mass, Fat Mass, Du Bois Body Surface Area (BSA), Ideal Body Weight (Devine, Robinson, Miller, Hamwi), Wrist Frame Size, BMR (Mifflin-St Jeor, Harris-Benedict, Katch-McArdle), TDEE across 5 activity tiers, custom Macro Split (`P/C/F`), hydration target, and pregnancy BMI limitation banner.
- **Multi-Profile, Analytics, Goals & Privacy**:
  - Multi-profile switcher with side-by-side profile comparison, 8-site body tape circumference logs, progress photo milestones with an interactive Before/After comparison slider, and searchable/taggable weight logs.
  - Interactive Canvas trend charts (Weight, BMI, Body Fat %, Waist) with WHO category background bands, 7-day moving average overlay, pinch-zoom & pan, 12-week consistency heatmap, and period comparison.
  - Offline-first Room persistence with Android Keystore AES-256-GCM key management, PIN lock screen, Incognito/Screenshot privacy shield (`FLAG_SECURE`), CSV/JSON export & import, Printable Doctor Summary (`WebView.createPrintDocumentAdapter`), Smart Reminders with `RemoteInput` direct notification logging, Home-Screen AppWidget, Quick Settings Tile, and Bluetooth SIG `0x181D`/`0x181B` Smart Scale GATT frame parser.

---

## Build & Run Instructions

### 1. Build Debug APK
```bash
gradle assembleDebug
```

### 2. Run Unit & Robolectric Compose UI Tests
```bash
gradle :app:testDebugUnitTest
```

### 3. Project Structure
- `com.example.domain.model` — Pure domain models, enums, and state contracts (`HealthModels.kt`)
- `com.example.domain.calculator` — Pure clinical formulas & CDC/WHO LMS tables (`BmiCalculatorEngine.kt`)
- `com.example.domain.usecase` — Clean Architecture use cases (`BmiPulseUseCases.kt`)
- `com.example.data.local` — Room database entities, DAO, and DataStore (`EntitiesAndDao.kt`, `SettingsDataStore.kt`)
- `com.example.data.repository` — Offline-first repository, seed data, CSV/JSON/Print engines (`BmiPulseRepository.kt`)
- `com.example.integrations` — AlarmManager/Notification `RemoteInput`, AppWidget, Quick Settings Tile, BLE GATT parser (`ReminderBroadcastReceiver.kt`, `BmiAppWidgetProvider.kt`)
- `com.example.ui.theme` — `MaterialExpressiveTheme`, `MotionScheme`, `MaterialShapes`, color schemes, and bundled Google Fonts (`Color.kt`, `Type.kt`, `Theme.kt`)
- `com.example.ui.components` — Reusable Material 3 Expressive components (`ExpressiveComponents.kt`)
- `com.example.ui.screens` — Compose screens across 5 primary destinations (`DashboardAndCalculatorScreens.kt`, `BodyLabAndChartsScreens.kt`, `HubAndVaultScreens.kt`)
- `com.example.ui.viewmodel` — Unidirectional `StateFlow` ViewModel (`BmiPulseViewModel.kt`)
