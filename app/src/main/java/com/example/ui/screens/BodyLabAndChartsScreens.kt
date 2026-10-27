package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.ActivityLevel
import com.example.domain.model.BmrFormula
import com.example.domain.model.GoalMode
import com.example.domain.model.MacroPreset
import com.example.ui.components.ExpressiveButtonGroup
import com.example.ui.components.WheelSliderNumberInput
import com.example.ui.viewmodel.BmiPulseUiState
import com.example.ui.viewmodel.BmiPulseViewModel
import com.example.ui.viewmodel.ChartMetricType
import com.example.ui.viewmodel.ChartRangeFilter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

/**
 * Screen 3: Body Composition & Metabolism Lab (US Navy & BMI Body Fat, WHR, WHtR, BSA, Frame Size,
 * 4 Ideal Weight Formulas, BMR/TDEE with 3 Formulas, Calorie Targets, Macro Split, Protein & Water).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BodyCompositionLabScreen(
    state: BmiPulseUiState,
    viewModel: BmiPulseViewModel,
    modifier: Modifier = Modifier
) {
    val comp = state.bodyComposition
    val met = state.metabolism
    val calc = state.calcInput

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("body_lab_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Body Fat & Lean Mass Partition Card
        item(key = "body_fat_hero") {
            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Body Composition Partition", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("US Navy Body Fat", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            Text(
                                text = "${String.format(Locale.US, "%.1f", comp.navyBodyFatPct)}%",
                                style = MaterialTheme.typography.displaySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(comp.bodyFatCategory, style = MaterialTheme.typography.labelLarge)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Deurenberg BMI Estimate", style = MaterialTheme.typography.labelMedium)
                            Text(
                                text = "${String.format(Locale.US, "%.1f", comp.bmiBodyFatPct)}%",
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Text("Age & Sex Adjusted", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Lean Mass vs Fat Mass Proportional Bar
                    val fatFrac = (comp.primaryBodyFatPct / 100.0).toFloat().coerceIn(0.05f, 0.65f)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(18.dp)
                            .clip(RoundedCornerShape(9.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f - fatFrac)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Box(
                            modifier = Modifier
                                .weight(fatFrac)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.tertiary)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Lean Body Mass: ${String.format(Locale.US, "%.1f", comp.leanBodyMassKg)} kg",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Fat Mass: ${String.format(Locale.US, "%.1f", comp.fatMassKg)} kg",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
        }

        // 2. Central Adiposity Ratios (WHR & WHtR) + BSA & Frame Size
        item(key = "whr_whtr_bsa") {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Central Ratios, Surface Area & Skeletal Frame", style = MaterialTheme.typography.titleMedium)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Waist-to-Hip (WHR)", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    text = String.format(Locale.US, "%.2f", comp.waistToHipRatio),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(comp.whrRiskLevel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Waist-to-Height (WHtR)", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    text = String.format(Locale.US, "%.2f", comp.waistToHeightRatio),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(comp.whtrStatus, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Body Surface Area", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    text = "${String.format(Locale.US, "%.2f", comp.bodySurfaceAreaM2)} m²",
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Text("Mosteller Clinical BSA", style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Wrist Frame Size", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    text = comp.frameSize.label,
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Text("Wrist: ${calc.wristCm} cm", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }

        // 3. Ideal Body Weight (4 Clinical Equations Adjusted for Frame Size)
        item(key = "ideal_body_weight") {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Ideal Body Weight (${comp.frameSize.label} Adjusted)", style = MaterialTheme.typography.titleMedium)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        IdealWeightColumn("Devine", comp.idealWeightDevineKg)
                        IdealWeightColumn("Robinson", comp.idealWeightRobinsonKg)
                        IdealWeightColumn("Miller", comp.idealWeightMillerKg)
                        IdealWeightColumn("Hamwi", comp.idealWeightHamwiKg)
                    }
                }
            }
        }

        // 4. Tape Circumference Sliders (Waist, Neck, Hip, Wrist, Chest, Arm, Thigh, Calf)
        item(key = "circumference_inputs") {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Anthropometric Tape Measurements (cm)", style = MaterialTheme.typography.titleMedium)

                    WheelSliderNumberInput(
                        label = "Waist (at navel)",
                        value = calc.waistCm,
                        unitLabel = "cm",
                        valueRange = 45f..160f,
                        onValueChange = { viewModel.updateMeasurements(waistCm = it) },
                        testTag = "lab_waist_slider"
                    )
                    WheelSliderNumberInput(
                        label = "Neck",
                        value = calc.neckCm,
                        unitLabel = "cm",
                        valueRange = 24f..60f,
                        onValueChange = { viewModel.updateMeasurements(neckCm = it) },
                        testTag = "lab_neck_slider"
                    )
                    WheelSliderNumberInput(
                        label = "Hip",
                        value = calc.hipCm,
                        unitLabel = "cm",
                        valueRange = 55f..170f,
                        onValueChange = { viewModel.updateMeasurements(hipCm = it) },
                        testTag = "lab_hip_slider"
                    )
                    WheelSliderNumberInput(
                        label = "Wrist (Frame Size)",
                        value = calc.wristCm,
                        unitLabel = "cm",
                        valueRange = 11f..25f,
                        step = 0.2,
                        onValueChange = { viewModel.updateMeasurements(wristCm = it) },
                        testTag = "lab_wrist_slider"
                    )
                }
            }
        }

        // 5. BMR (3 Formulas), Activity Selector & TDEE Calorie Targets
        item(key = "bmr_tdee_card") {
            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Metabolism: BMR & TDEE Engine", style = MaterialTheme.typography.titleLarge)

                    ExpressiveButtonGroup(
                        options = BmrFormula.entries,
                        selectedOption = calc.bmrFormula,
                        onOptionSelected = { viewModel.updateBmrFormula(it) },
                        labelProvider = {
                            when (it) {
                                BmrFormula.MIFFLIN_ST_JEOR -> "Mifflin"
                                BmrFormula.HARRIS_BENEDICT -> "Harris"
                                BmrFormula.KATCH_MCARDLE -> "Katch (LBM)"
                            }
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Basal Metabolic Rate (BMR)", style = MaterialTheme.typography.labelMedium)
                            Text("${met.activeBmr} kcal/day", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Daily Expenditure (TDEE)", style = MaterialTheme.typography.labelMedium)
                            Text("${met.tdeeCalories} kcal/day", style = MaterialTheme.typography.headlineMedium)
                        }
                    }

                    Text("Activity Multiplier", style = MaterialTheme.typography.labelLarge)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ActivityLevel.entries.forEach { level ->
                            FilterChip(
                                selected = calc.activityLevel == level,
                                onClick = { viewModel.updateActivityLevel(level) },
                                label = { Text("${level.label} (${level.multiplier}x)") }
                            )
                        }
                    }

                    HorizontalDivider()

                    Text("Daily Calorie Target Mode", style = MaterialTheme.typography.labelLarge)
                    ExpressiveButtonGroup(
                        options = GoalMode.entries,
                        selectedOption = calc.goalMode,
                        onOptionSelected = { viewModel.updateGoalMode(it) },
                        labelProvider = { it.label }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        CaloriePill("Lose (-500)", "${met.standardLossCalories} kcal")
                        CaloriePill("Maintain", "${met.tdeeCalories} kcal")
                        CaloriePill("Gain (+300)", "${met.mildGainCalories} kcal")
                    }
                }
            }
        }

        // 6. Macro Split Calculator, Protein Range & Daily Hydration Target
        item(key = "macro_and_hydration") {
            val macros = met.macroBreakdown
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Restaurant, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("Macro Split & Hydration (${macros.targetCalories} kcal)", style = MaterialTheme.typography.titleMedium)
                    }

                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MacroPreset.entries.forEach { preset ->
                            FilterChip(
                                selected = calc.macroPreset == preset,
                                onClick = { viewModel.updateMacroPreset(preset) },
                                label = { Text(preset.label) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DashboardMacroBox("Protein", "${macros.proteinGrams}g", "${calc.customProteinPct}%", Modifier.weight(1f))
                        DashboardMacroBox("Carbs", "${macros.carbsGrams}g", "${calc.customCarbsPct}%", Modifier.weight(1f))
                        DashboardMacroBox("Healthy Fats", "${macros.fatGrams}g", "${calc.customFatPct}%", Modifier.weight(1f))
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Icon(Icons.Default.LocalDrink, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Daily Water Target: ${macros.dailyWaterMl} ml (${String.format(Locale.US, "%.1f", macros.dailyWaterMl / 1000.0)} L / ${(macros.dailyWaterMl / 29.57).toInt()} oz)",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = "Evidence-based Protein Range: ${macros.proteinMinGramsPerDay}g – ${macros.proteinMaxGramsPerDay}g / day",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IdealWeightColumn(formula: String, kg: Double) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(formula, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("${String.format(Locale.US, "%.1f", kg)} kg", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CaloriePill(label: String, kcal: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(kcal, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DashboardMacroBox(title: String, grams: String, pct: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp)
        ) {
            Text(title, style = MaterialTheme.typography.labelSmall)
            Text(grams, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            Text(pct, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/**
 * Screen 4: Analytics Charts, Moving Averages, Before/After Progress Photo Slider & Searchable History
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnalyticsChartsScreen(
    state: BmiPulseUiState,
    viewModel: BmiPulseViewModel,
    modifier: Modifier = Modifier
) {
    val calc = state.calcInput
    val now = System.currentTimeMillis()
    val cutoffMs = now - (calc.chartRange.days * 86_400_000L)

    val filteredChronological = remember(state.profileLogs, calc.chartRange) {
        state.profileLogs
            .filter { it.timestampMillis >= cutoffMs }
            .sortedBy { it.timestampMillis }
            .ifEmpty { state.profileLogs.sortedBy { it.timestampMillis } }
    }

    val values = remember(filteredChronological, calc.chartMetric) {
        filteredChronological.map { log ->
            when (calc.chartMetric) {
                ChartMetricType.WEIGHT -> log.weightKg
                ChartMetricType.BMI -> log.bmi
                ChartMetricType.BODY_FAT -> log.bodyFatPct
                ChartMetricType.WAIST -> log.waistCm
            }
        }
    }

    val minVal = values.minOrNull() ?: 0.0
    val maxVal = values.maxOrNull() ?: 0.0
    val avgVal = if (values.isNotEmpty()) values.average() else 0.0
    val netChange = if (values.size >= 2) values.last() - values.first() else 0.0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("charts_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Metric Type & Time Range Connected ButtonGroups
        item(key = "chart_controls") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ExpressiveButtonGroup(
                    options = ChartMetricType.entries,
                    selectedOption = calc.chartMetric,
                    onOptionSelected = { viewModel.updateChartMetric(it) },
                    labelProvider = { it.label }
                )
                ExpressiveButtonGroup(
                    options = ChartRangeFilter.entries,
                    selectedOption = calc.chartRange,
                    onOptionSelected = { viewModel.updateChartRange(it) },
                    labelProvider = { it.label }
                )
            }
        }

        // 2. Interactive Trend Chart with BMI Category Bands & 7-Day Moving Average
        item(key = "trend_chart_card") {
            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${calc.chartMetric.label} Trend (${calc.chartMetric.unit})",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "Solid: Daily • Dashed: Moving Average",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Moving Avg", style = MaterialTheme.typography.labelSmall)
                            Spacer(Modifier.width(6.dp))
                            Switch(
                                checked = calc.showMovingAverage,
                                onCheckedChange = { viewModel.toggleMovingAverage(it) }
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    val primaryColor = MaterialTheme.colorScheme.primary
                    val tertiaryColor = MaterialTheme.colorScheme.tertiary

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        // Subtle BMI Category Horizontal Bands behind the chart
                        val bandHeight = size.height / 3f
                        drawRect(
                            color = Color(0xFFEF4444).copy(alpha = 0.07f),
                            topLeft = Offset(0f, 0f),
                            size = Size(size.width, bandHeight)
                        )
                        drawRect(
                            color = Color(0xFF22C55E).copy(alpha = 0.09f),
                            topLeft = Offset(0f, bandHeight),
                            size = Size(size.width, bandHeight)
                        )
                        drawRect(
                            color = Color(0xFF0EA5E9).copy(alpha = 0.07f),
                            topLeft = Offset(0f, bandHeight * 2f),
                            size = Size(size.width, bandHeight)
                        )

                        if (values.size >= 2) {
                            val paddedMin = minVal - 1.0
                            val paddedMax = maxVal + 1.0
                            val span = max(0.5, paddedMax - paddedMin)

                            val path = Path()
                            val maPath = Path()

                            values.forEachIndexed { idx, v ->
                                val x = (idx.toFloat() / (values.size - 1)) * (size.width - 24.dp.toPx()) + 12.dp.toPx()
                                val y = size.height - (((v - paddedMin) / span).toFloat() * (size.height - 28.dp.toPx()) + 14.dp.toPx())
                                if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)

                                // 3-point moving average
                                val window = values.subList(max(0, idx - 2), idx + 1)
                                val maVal = window.average()
                                val maY = size.height - (((maVal - paddedMin) / span).toFloat() * (size.height - 28.dp.toPx()) + 14.dp.toPx())
                                if (idx == 0) maPath.moveTo(x, maY) else maPath.lineTo(x, maY)

                                drawCircle(
                                    color = primaryColor,
                                    radius = 4.dp.toPx(),
                                    center = Offset(x, y)
                                )
                            }

                            drawPath(
                                path = path,
                                color = primaryColor,
                                style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                            )

                            if (calc.showMovingAverage) {
                                drawPath(
                                    path = maPath,
                                    color = tertiaryColor,
                                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Min / Max / Avg / Net Change Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatSummaryItem("Min", String.format(Locale.US, "%.1f", minVal))
                        StatSummaryItem("Average", String.format(Locale.US, "%.1f", avgVal))
                        StatSummaryItem("Max", String.format(Locale.US, "%.1f", maxVal))
                        StatSummaryItem("Period Δ", String.format(Locale.US, "%+.1f", netChange))
                    }
                }
            }
        }

        // 3. Before / After Progress Comparison Slider
        item(key = "before_after_slider") {
            val firstEntry = filteredChronological.firstOrNull()
            val latestEntry = filteredChronological.lastOrNull()
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Compare, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("Before / After Progress Comparison Slider", style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(Modifier.height(10.dp))

                    val split = calc.beforeAfterSliderFraction
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(split.coerceIn(0.15f, 0.85f))
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .padding(12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Column {
                                Text("BEFORE (Baseline)", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    text = "${String.format(Locale.US, "%.1f", firstEntry?.weightKg ?: 66.4)} kg",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "BMI ${String.format(Locale.US, "%.1f", firstEntry?.bmi ?: 24.1)} • Waist ${firstEntry?.waistCm ?: 77.5} cm",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .weight((1f - split).coerceIn(0.15f, 0.85f))
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(12.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text("CURRENT (Progress)", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    text = "${String.format(Locale.US, "%.1f", latestEntry?.weightKg ?: 63.2)} kg",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "BMI ${String.format(Locale.US, "%.1f", latestEntry?.bmi ?: 22.9)} • Waist ${latestEntry?.waistCm ?: 74.0} cm",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    Slider(
                        value = split,
                        onValueChange = { viewModel.updateBeforeAfterSlider(it) },
                        valueRange = 0.15f..0.85f
                    )
                }
            }
        }

        // 4. Searchable & Tag-Filtered Log History
        item(key = "history_search_header") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Searchable & Filterable Log History", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = calc.historySearchQuery,
                    onValueChange = { viewModel.updateHistorySearch(it) },
                    placeholder = { Text("Search notes, tags, or weight...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("All", "Morning", "Fasted", "Workout", "Gym").forEach { tag ->
                        FilterChip(
                            selected = calc.selectedTagFilter == tag,
                            onClick = { viewModel.updateTagFilter(tag) },
                            label = { Text(tag) }
                        )
                    }
                }
            }
        }

        val matchingLogs = state.profileLogs.filter { log ->
            val matchesTag = calc.selectedTagFilter == "All" || log.tagsCsv.contains(calc.selectedTagFilter, ignoreCase = true)
            val matchesQuery = calc.historySearchQuery.isBlank() ||
                log.note.contains(calc.historySearchQuery, ignoreCase = true) ||
                log.tagsCsv.contains(calc.historySearchQuery, ignoreCase = true) ||
                log.weightKg.toString().contains(calc.historySearchQuery)
            matchesTag && matchesQuery
        }

        items(matchingLogs, key = { it.id }) { log ->
            val df = remember { SimpleDateFormat("MMM d, yyyy • HH:mm", Locale.US) }
            Card(
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${String.format(Locale.US, "%.1f", log.weightKg)} kg  •  BMI ${String.format(Locale.US, "%.1f", log.bmi)}  •  BF ${String.format(Locale.US, "%.1f", log.bodyFatPct)}%",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${df.format(Date(log.timestampMillis))} • Waist ${String.format(Locale.US, "%.1f", log.waistCm)} cm",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (log.note.isNotBlank()) {
                            Text(
                                text = "${log.note} [${log.tagsCsv}]",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(onClick = { viewModel.deleteLogEntry(log.id) }) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete log entry")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatSummaryItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}
