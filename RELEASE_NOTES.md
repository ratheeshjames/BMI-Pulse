# 🚀 BMI Pulse `v1.0.0` — Initial Production Release

**Release Tag:** `v1.0.0`  
**Version Code:** `1`  
**Package ID:** `com.aistudio.bmipulse.vqxkpl`  
**Minimum Android SDK:** Android 8.0 (API 26) | **Target SDK:** Android 16 (API 36)  
**Architecture:** Kotlin 2.x • Jetpack Compose • Material 3 Expressive • Room • Preferences DataStore

---

## ✨ What's New in `v1.0.0`

### 🎨 Material 3 Expressive Design System
- **`MaterialExpressiveTheme` & `MotionScheme.expressive()`**: Spring-driven spatial and effects physics across all transitions, gauges, and interactive controls.
- **Dynamic Color & High-Contrast Accessibility**: Supports Android 12+ Material You dynamic palettes, a custom Emerald/Teal clinical health fallback palette, Dark/Light/System modes, and a dedicated **WCAG AAA High-Contrast Mode**.
- **Expressive Shape Morphing (`MaterialShapes`)**: Morphing 10-sided Starburst/Pill/Scallop badges, `ExpressiveBmiGauge` (240° sweep arc), `ExpressiveButtonGroup` (connected unit and time-range pills), `ExpressiveSplitButton`, `ExpressiveWavyProgressIndicator`, `ExpressiveFabMenu`, and `WheelSliderNumberInput`.
- **Adaptive Canonical Layouts**: Seamlessly adapts between a bottom `NavigationBar` on compact phones and a `WideNavigationRail` with multi-column panes on foldables and tablets (`>= 600.dp`).

### ⚖️ Core BMI & Child/Teen LMS Percentiles
- **Tri-Mode Unit Support**: Metric (`kg, cm`), Imperial (`lb, ft + in`), and Mixed (`st + lb, cm`) with a built-in instant unit converter.
- **8-Tier WHO & Asian-Pacific Cutoffs**: Switch between standard WHO adult thresholds (`18.5 / 25.0 / 30.0`) and WHO WPRO Asian-Pacific thresholds (`18.5 / 23.0 / 25.0`).
- **BMI Prime, Ponderal Index & Healthy Weight Range**: Live calculation of BMI Prime ratio, Ponderal Index (`kg/m³`), and exact target weight delta to reach the healthy BMI band.
- **CDC 2000 & WHO 2007 Child/Teen LMS Engine (Ages 2–20)**: Bundled age-in-months (`24–240m`) sex-specific `L, M, S` tables with linear interpolation, Box-Cox Z-score computation, and exact normal CDF percentiles (`<5th`, `5th–85th`, `85th–95th`, `≥95th`).

### 🧬 Body Composition & Metabolism Lab
- **Body Fat & Anthropometrics**: US Navy Circumference Body Fat %, Deurenberg BMI-based Body Fat %, Waist-to-Hip Ratio (WHR), Waist-to-Height Ratio (WHtR), Lean Body Mass (LBM), Fat Mass, and Du Bois Body Surface Area (`m²`).
- **Ideal Body Weight & Frame Size**: Simultaneous comparison of **Devine**, **Robinson**, **Miller**, and **Hamwi** formulas alongside wrist-circumference skeletal frame classification.
- **BMR, TDEE & Custom Macro Split**: Compare **Mifflin-St Jeor**, **Revised Harris-Benedict**, and **Katch-McArdle** (LBM-aware) BMR equations, 5-tier TDEE multipliers, Lose/Maintain/Gain calorie targets, interactive Protein/Carb/Fat macro sliders, and daily hydration goals (`mL` & `oz`).

### 📈 Multi-Profile Tracking, Charts & Goals
- **Multi-Profile & Side-by-Side Comparison**: Create multiple family/athlete profiles with custom avatar colors and compare BMI, body fat, WHtR, and TDEE side by side.
- **8-Site Body Measurements & Progress Photos**: Track neck, chest, waist, hip, bicep, thigh, calf, and wrist circumferences plus progress photo milestones with an interactive **Before/After Comparison Slider**.
- **Interactive Trend Charts & Consistency Heatmap**: Canvas-rendered Weight, BMI, Body Fat %, and Waist charts with WHO category background bands, 7-day moving average overlay, pinch-to-zoom/pan, 12-week logging heatmap, and period comparison.
- **Goals, Milestones & Safety Warnings**: Target weight/BMI timeline projection, wavy progress indicator, 10-day streak counter, achievement badges, plateau detector, and clinical safe-rate alerts (`>1.0 kg/week`).

### 🔒 Offline-First Privacy, Reminders & Device Integrations
- **Zero-Cloud Privacy & Keystore Security**: 100% offline Room database with Android Keystore AES-256-GCM key management, optional 4-digit PIN lock, and Screenshot/Recents Privacy Shield (`FLAG_SECURE`).
- **Smart Reminders with `RemoteInput`**: Scheduled weigh-in and hydration notifications with direct inline weight entry from the notification shade and automatic reboot rescheduling.
- **Widgets, Quick Settings Tile, App Shortcuts & BLE Scale**: Home-screen `3x2` widget, Quick Settings tile, launcher shortcuts, CSV/JSON export & import, Printable Doctor Summary (`PrintManager`), and Bluetooth SIG `0x181D`/`0x181B` Smart Scale GATT packet parser.

---

## 📦 Release Artifacts (`Generated APKs/`)

| Artifact | Path | Description |
| :--- | :--- | :--- |
| **Signed Release APK** | `Generated APKs/BMI-Pulse-v1.0.0-release.apk` | Production release APK signed with release upload key |
| **Standard Release APK** | `Generated APKs/app-release.apk` | Copy of the signed release APK |
| **Checksums** | `Generated APKs/SHA256SUMS.txt` | SHA-256 integrity hashes for release verification |

---

## 🛠️ Verification & Installation

```bash
# Verify APK integrity
sha256sum -c "Generated APKs/SHA256SUMS.txt"

# Install on connected Android device (API 26+)
adb install -r "Generated APKs/BMI-Pulse-v1.0.0-release.apk"
```
