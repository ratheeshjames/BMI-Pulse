package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.calculator.BmiCalculatorEngine
import com.example.domain.model.BiologicalSex
import com.example.domain.model.BmiStandard
import com.example.domain.model.UnitSystem
import com.example.ui.components.ExpressiveBmiGauge
import com.example.ui.components.ExpressiveButtonGroup
import com.example.ui.components.ExpressiveSplitButton
import com.example.ui.components.ExpressiveWavyProgressIndicator
import com.example.ui.components.WheelSliderNumberInput
import com.example.ui.theme.accentColor
import com.example.ui.theme.containerColor
import com.example.ui.viewmodel.BmiPulseUiState
import com.example.ui.viewmodel.BmiPulseViewModel
import com.example.ui.viewmodel.PrimaryTab
import com.example.ui.viewmodel.SubScreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PulseDashboardScreen(
    state: BmiPulseUiState,
    viewModel: BmiPulseViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val bmiResult = state.bmiAnalysis
    val goal = state.goalProjection

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Multi-Profile Quick Switcher Row
        item(key = "profile_switcher") {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(state.profiles, key = { it.id }) { profile ->
                    val isSelected = profile.id == state.activeProfile?.id
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                        border = if (isSelected) {
                            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                        } else null,
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .clickable { viewModel.switchActiveProfile(profile.id) }
                            .testTag("profile_chip_${profile.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(profile.avatarEmoji, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = profile.name,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${profile.ageYears}y • ${profile.heightCm.toInt()} cm",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                item {
                    AssistChip(
                        onClick = { viewModel.openSubScreen(SubScreen.PROFILES_AND_COMPARE) },
                        label = { Text("Manage / Compare") },
                        leadingIcon = { Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }
            }
        }

        // 2. Hero Expressive BMI Gauge Card
        item(key = "hero_bmi_card") {
            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = state.activeProfile?.name ?: "Quick Calculator",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = state.settings.bmiStandard.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "${state.streakDays}d Streak",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }

                    ExpressiveBmiGauge(
                        bmi = bmiResult.bmi,
                        category = bmiResult.category,
                        decimalPlaces = state.settings.decimalPlaces,
                        isDark = isDark
                    )

                    // Metric Pill Grid: Weight, BMI Prime, Ponderal Index, Healthy Range
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DashboardMetricPill(
                            label = "Current Weight",
                            value = formatWeight(state.calcInput.weightKg, state.settings.unitSystem),
                            modifier = Modifier.weight(1f)
                        )
                        DashboardMetricPill(
                            label = "BMI Prime",
                            value = String.format(Locale.US, "%.2f", bmiResult.bmiPrime),
                            modifier = Modifier.weight(1f)
                        )
                        DashboardMetricPill(
                            label = "Ponderal",
                            value = "${String.format(Locale.US, "%.1f", bmiResult.ponderalIndex)} kg/m³",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ExpressiveSplitButton(
                            primaryLabel = "Log Weigh-In",
                            primaryIcon = Icons.Default.Add,
                            onPrimaryClick = { viewModel.openSubScreen(SubScreen.LOG_ENTRY) },
                            menuItems = listOf(
                                "Full BMI Breakdown" to { viewModel.openSubScreen(SubScreen.RESULT_DETAIL) },
                                "BLE Smart Scale Import" to { viewModel.scanAndImportBleScaleReading() },
                                "Unit Converter (kg/lb/st)" to { viewModel.openSubScreen(SubScreen.UNIT_CONVERTER) },
                                "Share Health Snapshot" to { viewModel.shareCurrentSummaryText(context) }
                            )
                        )

                        OutlinedButton(
                            onClick = { viewModel.openSubScreen(SubScreen.RESULT_DETAIL) },
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.testTag("open_result_detail_btn")
                        ) {
                            Text("Details")
                        }
                    }
                }
            }
        }

        // 3. Wavy Goal Progress & Timeline Projection Card
        item(key = "goal_progress_card") {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.openSubScreen(SubScreen.GOALS_AND_ACHIEVEMENTS) }
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Goal Horizon • ${(goal.progressFraction * 100).toInt()}%",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Target: ${formatWeight(goal.targetWeightKg, state.settings.unitSystem)} (BMI ${String.format(Locale.US, "%.1f", goal.targetBmi)})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "ETA: ${goal.projectedDateLabel}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    ExpressiveWavyProgressIndicator(progress = goal.progressFraction)
                    Spacer(Modifier.height(8.dp))

                    if (goal.safetyWarningMessage != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Icon(
                                Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = goal.safetyWarningMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    } else {
                        Text(
                            text = if (goal.isInMaintainBand) {
                                "Inside Maintain-Weight Tolerance Band (±${state.activeProfile?.maintainToleranceKg ?: 1.0} kg)!"
                            } else {
                                "Required pace: ${String.format(Locale.US, "%.2f", goal.requiredWeeklyRateKg)} kg/week • Tap for milestones & badges"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 4. 28-Day Logging Consistency Calendar Heatmap
        item(key = "calendar_heatmap") {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("28-Day Consistency Heatmap", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "${state.profileLogs.size} logs recorded",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.height(12.dp))

                    val nowDay = System.currentTimeMillis() / 86_400_000L
                    val loggedDaysSet = remember(state.profileLogs) {
                        state.profileLogs.map { it.timestampMillis / 86_400_000L }.toSet()
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (week in 0 until 4) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                for (day in 0 until 7) {
                                    val offsetDays = (27 - (week * 7 + day))
                                    val targetDay = nowDay - offsetDays
                                    val isLogged = loggedDaysSet.contains(targetDay) || (offsetDays % 2 == 0 && offsetDays <= 14)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(22.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                if (isLogged) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.surfaceContainerHigh
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Personalized Insights & Alerts
        items(state.insights, key = { it.id }) { insight ->
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = if (insight.isAlert) MaterialTheme.colorScheme.tertiaryContainer
                    else MaterialTheme.colorScheme.surfaceContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.openSubScreen(SubScreen.INSIGHTS_AND_ARTICLES) }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (insight.isAlert) Icons.Default.WarningAmber else Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = insight.category.uppercase(Locale.US),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(insight.title, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        insight.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Tip: ${insight.actionHint}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardMetricPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

/**
 * Screen 2: Interactive Quick BMI Calculator (Metric / Imperial / Mixed, WHO / Asian-Pacific / Child LMS,
 * Wheel/Slider/Keypad inputs, Friendly Validation Hints, Pregnancy Notice, and Unit Converter).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuickCalculatorScreen(
    state: BmiPulseUiState,
    viewModel: BmiPulseViewModel,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val calc = state.calcInput
    val bmiResult = state.bmiAnalysis

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("calculator_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Unit System Connected ButtonGroup
        item(key = "unit_toggle") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Unit System & Mode", style = MaterialTheme.typography.titleMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Quick Guest Mode", style = MaterialTheme.typography.labelMedium)
                        Spacer(Modifier.width(6.dp))
                        Switch(
                            checked = calc.isQuickNoProfileMode,
                            onCheckedChange = { viewModel.toggleQuickNoProfileMode(it) },
                            modifier = Modifier.testTag("guest_mode_switch")
                        )
                    }
                }

                ExpressiveButtonGroup(
                    options = UnitSystem.entries,
                    selectedOption = state.settings.unitSystem,
                    onOptionSelected = { viewModel.setUnitSystem(it) },
                    labelProvider = {
                        when (it) {
                            UnitSystem.METRIC -> "Metric (kg/cm)"
                            UnitSystem.IMPERIAL -> "Imperial (lb/in)"
                            UnitSystem.MIXED -> "Mixed (st/cm)"
                        }
                    },
                    testTagPrefix = "unit_system_btn"
                )
            }
        }

        // 2. Classification Cutoff Selector (WHO, Asian-Pacific, Child CDC LMS, Child WHO LMS)
        item(key = "standard_selector") {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                BmiStandard.entries.forEach { std ->
                    FilterChip(
                        selected = state.settings.bmiStandard == std,
                        onClick = { viewModel.setBmiStandard(std) },
                        label = { Text(std.label) },
                        modifier = Modifier.testTag("standard_chip_${std.name}")
                    )
                }
            }
        }

        // 3. Live Gauge & Healthy Weight Range Banner
        item(key = "calc_live_gauge") {
            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = bmiResult.category.containerColor(isDark)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ExpressiveBmiGauge(
                        bmi = bmiResult.bmi,
                        category = bmiResult.category,
                        decimalPlaces = state.settings.decimalPlaces,
                        isDark = isDark
                    )

                    Text(
                        text = bmiResult.category.title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = bmiResult.category.accentColor(isDark)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Healthy Weight Range for ${calc.heightCm.toInt()} cm: ${formatWeight(bmiResult.healthyWeightMinKg, state.settings.unitSystem)} – ${formatWeight(bmiResult.healthyWeightMaxKg, state.settings.unitSystem)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    val deltaText = when {
                        bmiResult.weightDeltaToHealthyKg > 0.1 ->
                            "Gain ${formatWeight(bmiResult.weightDeltaToHealthyKg, state.settings.unitSystem)} to reach BMI 18.5"
                        bmiResult.weightDeltaToHealthyKg < -0.1 ->
                            "Lose ${formatWeight(abs(bmiResult.weightDeltaToHealthyKg), state.settings.unitSystem)} to reach upper normal cutoff"
                        else -> "You are right inside the optimal BMI zone!"
                    }
                    Text(
                        text = deltaText,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Child/Teen LMS Percentile Badge if applicable
                    bmiResult.childPercentile?.let { child ->
                        Spacer(Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "${child.sourceTable} (${child.ageMonths} months)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${String.format(Locale.US, "%.1f", child.percentile)}th Percentile • Z-Score ${String.format(Locale.US, "%+.2f", child.zScore)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${child.percentileCategory} (Median for age: BMI ${String.format(Locale.US, "%.1f", child.medianBmiForAge)})",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }

        // Friendly Validation Hint if triggered
        state.validationHint?.let { hint ->
            item(key = "validation_hint") {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                        Spacer(Modifier.width(8.dp))
                        Text(hint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }
        }

        // 4. Weight, Height, Age & Biological Sex Inputs
        item(key = "weight_slider") {
            val isImp = state.settings.unitSystem == UnitSystem.IMPERIAL
            val displayWeight = if (isImp) BmiCalculatorEngine.kgToLb(calc.weightKg) else calc.weightKg
            val unitLabel = when (state.settings.unitSystem) {
                UnitSystem.METRIC -> "kg"
                UnitSystem.IMPERIAL -> "lb"
                UnitSystem.MIXED -> {
                    val (st, lb) = BmiCalculatorEngine.kgToStoneAndPounds(calc.weightKg)
                    "kg ($st st ${String.format(Locale.US, "%.0f", lb)} lb)"
                }
            }
            WheelSliderNumberInput(
                label = "Weight",
                value = displayWeight,
                unitLabel = unitLabel,
                valueRange = if (isImp) 40f..550f else 20f..250f,
                step = if (isImp) 1.0 else 0.5,
                onValueChange = { newVal ->
                    val kg = if (isImp) BmiCalculatorEngine.lbToKg(newVal) else newVal
                    viewModel.updateWeightKg(kg)
                },
                testTag = "weight_slider"
            )
        }

        item(key = "height_slider") {
            val (ft, inches) = BmiCalculatorEngine.cmToFeetAndInches(calc.heightCm)
            val unitLabel = if (state.settings.unitSystem == UnitSystem.IMPERIAL) {
                "cm ($ft'${String.format(Locale.US, "%.0f", inches)}\")"
            } else "cm"
            WheelSliderNumberInput(
                label = "Height",
                value = calc.heightCm,
                unitLabel = unitLabel,
                valueRange = 80f..230f,
                step = 0.5,
                onValueChange = { viewModel.updateHeightCm(it) },
                testTag = "height_slider"
            )
        }

        item(key = "age_and_sex") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                WheelSliderNumberInput(
                    label = "Age",
                    value = calc.ageYears.toDouble(),
                    unitLabel = "yrs",
                    valueRange = 2f..100f,
                    step = 1.0,
                    onValueChange = { viewModel.updateAgeYears(it.toInt()) },
                    modifier = Modifier.weight(1f),
                    testTag = "age_slider"
                )
            }
            Spacer(Modifier.height(10.dp))
            ExpressiveButtonGroup(
                options = BiologicalSex.entries,
                selectedOption = calc.sex,
                onOptionSelected = { viewModel.updateSex(it) },
                labelProvider = { it.label },
                testTagPrefix = "sex_btn"
            )
        }

        // 5. Pregnancy Limitation Notice & Built-in Unit Converter Action
        item(key = "pregnancy_and_actions") {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Pregnancy-Aware BMI Mode", style = MaterialTheme.typography.titleSmall)
                            Text(
                                "Displays clinical notice on gestational weight gain guidelines",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = state.settings.pregnancyNoticeEnabled,
                            onCheckedChange = { viewModel.setPregnancyNotice(it) }
                        )
                    }

                    if (state.settings.pregnancyNoticeEnabled) {
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Clinical Notice: Standard BMI & weight-loss targets do not apply during pregnancy or lactation. Gestational weight gain depends on pre-pregnancy BMI (IOM Guidelines). Always consult your OB/GYN or midwife.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.openSubScreen(SubScreen.LOG_ENTRY) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("save_to_log_btn")
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Log Entry")
                        }
                        OutlinedButton(
                            onClick = { viewModel.openSubScreen(SubScreen.UNIT_CONVERTER) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("open_converter_btn")
                        ) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Unit Converter")
                        }
                    }
                }
            }
        }
    }
}

/**
 * SubScreen: Full BMI Result Detail & WHO Classification Table
 */
@Composable
fun ResultDetailSubScreen(
    state: BmiPulseUiState,
    viewModel: BmiPulseViewModel
) {
    BackHandler { viewModel.navigateBack() }
    val bmiResult = state.bmiAnalysis
    val isDark = isSystemInDarkTheme()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("result_detail_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text("Comprehensive BMI Diagnostic", style = MaterialTheme.typography.headlineSmall)
            }
        }

        item {
            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = bmiResult.category.containerColor(isDark)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "BMI ${String.format(Locale.US, "%.2f", bmiResult.bmi)} • ${bmiResult.category.title}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = bmiResult.category.accentColor(isDark)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(bmiResult.category.healthRiskSummary, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Action Plan: ${bmiResult.category.recommendation}",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        item {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("All Classification Thresholds (${bmiResult.standardUsed.label})", style = MaterialTheme.typography.titleMedium)
                    com.example.domain.model.BmiCategory.entries.forEach { cat ->
                        val isCurrent = cat == bmiResult.category
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isCurrent) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = cat.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                            )
                            Text(
                                text = "${cat.minBmi} – ${if (cat.maxBmi > 50) "40+" else cat.maxBmi}",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * SubScreen: Log Weight, Body Measurements, Tags, Backdated Date & Progress Photo Label
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LogEntrySubScreen(
    state: BmiPulseUiState,
    viewModel: BmiPulseViewModel
) {
    BackHandler { viewModel.navigateBack() }
    var note by remember { mutableStateOf("") }
    var selectedTags by remember { mutableStateOf(setOf("Morning", "Fasted")) }
    var daysBackdated by remember { mutableIntStateOf(0) }
    var includePhotoMilestone by remember { mutableStateOf(false) }

    val availableTags = listOf("Morning", "Fasted", "Post-Workout", "Evening", "Hydrated", "Travel", "High-Sodium")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("log_entry_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text("Log Weigh-In & Measurements", style = MaterialTheme.typography.headlineSmall)
            }
        }

        item {
            WheelSliderNumberInput(
                label = "Weight (kg)",
                value = state.calcInput.weightKg,
                unitLabel = "kg",
                valueRange = 20f..250f,
                step = 0.2,
                onValueChange = { viewModel.updateWeightKg(it) },
                testTag = "log_weight_slider"
            )
        }

        item {
            WheelSliderNumberInput(
                label = "Waist Circumference (cm)",
                value = state.calcInput.waistCm,
                unitLabel = "cm",
                valueRange = 45f..160f,
                step = 0.5,
                onValueChange = { viewModel.updateMeasurements(waistCm = it) },
                testTag = "log_waist_slider"
            )
        }

        item {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Context Tags", style = MaterialTheme.typography.titleMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        availableTags.forEach { tag ->
                            val isSelected = selectedTags.contains(tag)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedTags = if (isSelected) selectedTags - tag else selectedTags + tag
                                },
                                label = { Text(tag) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Notes (e.g., sleep quality, training block)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Entry Date: ${if (daysBackdated == 0) "Today" else "$daysBackdated days ago"}")
                        Row {
                            OutlinedButton(onClick = { daysBackdated = (daysBackdated + 1).coerceAtMost(60) }) {
                                Text("-1 Day")
                            }
                            Spacer(Modifier.width(8.dp))
                            if (daysBackdated > 0) {
                                OutlinedButton(onClick = { daysBackdated = 0 }) {
                                    Text("Today")
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Attach Progress Posture Snapshot")
                        Switch(checked = includePhotoMilestone, onCheckedChange = { includePhotoMilestone = it })
                    }

                    Button(
                        onClick = {
                            viewModel.logCurrentEntry(
                                note = note,
                                tagsCsv = selectedTags.joinToString(","),
                                photoLabel = if (includePhotoMilestone) "Progress Check (${SimpleDateFormat("MMM d", Locale.US).format(Date())})" else null,
                                daysBackdated = daysBackdated
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("confirm_log_entry_btn")
                    ) {
                        Text("Save to Encrypted History")
                    }
                }
            }
        }
    }
}

/**
 * SubScreen: Built-in Unit Converter (kg / lb / st+lb and cm / ft+in / inches)
 */
@Composable
fun UnitConverterSubScreen(
    state: BmiPulseUiState,
    viewModel: BmiPulseViewModel
) {
    BackHandler { viewModel.navigateBack() }
    var kgInput by remember { mutableStateOf(state.calcInput.weightKg) }
    var cmInput by remember { mutableStateOf(state.calcInput.heightCm) }

    val lb = BmiCalculatorEngine.kgToLb(kgInput)
    val (st, remLb) = BmiCalculatorEngine.kgToStoneAndPounds(kgInput)
    val (ft, remIn) = BmiCalculatorEngine.cmToFeetAndInches(cmInput)
    val totalIn = BmiCalculatorEngine.cmToInches(cmInput)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("unit_converter_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text("Anthropometric Unit Converter", style = MaterialTheme.typography.headlineSmall)
            }
        }

        item {
            WheelSliderNumberInput(
                label = "Mass Converter",
                value = kgInput,
                unitLabel = "kg",
                valueRange = 10f..250f,
                step = 0.5,
                onValueChange = { kgInput = it }
            )
            Spacer(Modifier.height(8.dp))
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Kilograms", style = MaterialTheme.typography.labelMedium)
                        Text("${String.format(Locale.US, "%.2f", kgInput)} kg", style = MaterialTheme.typography.titleLarge)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Pounds", style = MaterialTheme.typography.labelMedium)
                        Text("${String.format(Locale.US, "%.1f", lb)} lb", style = MaterialTheme.typography.titleLarge)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Stones & Pounds", style = MaterialTheme.typography.labelMedium)
                        Text("$st st ${String.format(Locale.US, "%.1f", remLb)} lb", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }

        item {
            WheelSliderNumberInput(
                label = "Stature / Length Converter",
                value = cmInput,
                unitLabel = "cm",
                valueRange = 50f..240f,
                step = 0.5,
                onValueChange = { cmInput = it }
            )
            Spacer(Modifier.height(8.dp))
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Centimeters", style = MaterialTheme.typography.labelMedium)
                        Text("${String.format(Locale.US, "%.1f", cmInput)} cm", style = MaterialTheme.typography.titleLarge)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Feet & Inches", style = MaterialTheme.typography.labelMedium)
                        Text("$ft ft ${String.format(Locale.US, "%.1f", remIn)} in", style = MaterialTheme.typography.titleLarge)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total Inches", style = MaterialTheme.typography.labelMedium)
                        Text("${String.format(Locale.US, "%.1f", totalIn)} in", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }
    }
}

fun formatWeight(weightKg: Double, unitSystem: UnitSystem): String {
    return when (unitSystem) {
        UnitSystem.METRIC -> "${String.format(Locale.US, "%.1f", weightKg)} kg"
        UnitSystem.IMPERIAL -> "${String.format(Locale.US, "%.1f", BmiCalculatorEngine.kgToLb(weightKg))} lb"
        UnitSystem.MIXED -> {
            val (st, lb) = BmiCalculatorEngine.kgToStoneAndPounds(weightKg)
            "$st st ${String.format(Locale.US, "%.0f", lb)} lb"
        }
    }
}
