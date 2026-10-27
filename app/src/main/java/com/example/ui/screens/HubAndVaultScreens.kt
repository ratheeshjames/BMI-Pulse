package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.ThemePreference
import com.example.domain.model.BiologicalSex
import com.example.ui.components.ExpressiveButtonGroup
import com.example.ui.components.ExpressiveWavyProgressIndicator
import com.example.ui.components.WheelSliderNumberInput
import com.example.ui.viewmodel.BmiPulseUiState
import com.example.ui.viewmodel.BmiPulseViewModel
import com.example.ui.viewmodel.SubScreen
import java.util.Locale

@Composable
fun HubAndVaultMainScreen(
    state: BmiPulseUiState,
    viewModel: BmiPulseViewModel,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("hub_vault_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Health Hub, Integrations & Encrypted Vault",
                style = MaterialTheme.typography.headlineSmall
            )
        }

        item {
            HubNavigationCard(
                title = "Profiles & Side-by-Side Comparison",
                subtitle = "${state.profiles.size} active profiles • Compare BMI, body fat & waist trends",
                icon = Icons.Default.Group,
                onClick = { viewModel.openSubScreen(SubScreen.PROFILES_AND_COMPARE) },
                testTag = "hub_profiles_card"
            )
        }

        item {
            HubNavigationCard(
                title = "Goals, Milestones & Achievement Badges",
                subtitle = "${state.achievements.count { it.isUnlocked }}/${state.achievements.size} badges unlocked • Safe-rate timeline planner",
                icon = Icons.Default.EmojiEvents,
                onClick = { viewModel.openSubScreen(SubScreen.GOALS_AND_ACHIEVEMENTS) },
                testTag = "hub_goals_card"
            )
        }

        item {
            HubNavigationCard(
                title = "Smart Reminders & Notification Quick-Log",
                subtitle = "${state.reminders.count { it.isEnabled }} active schedules • RemoteInput weigh-in & DND support",
                icon = Icons.Default.NotificationsActive,
                onClick = { viewModel.openSubScreen(SubScreen.REMINDERS) },
                testTag = "hub_reminders_card"
            )
        }

        item {
            HubNavigationCard(
                title = "BLE Smart Scale & Health Connect Bridge",
                subtitle = "Bluetooth GATT 0x181D/0x181B importer, Widgets & Quick Settings Tile",
                icon = Icons.Default.Bluetooth,
                onClick = { viewModel.openSubScreen(SubScreen.BLE_SMART_SCALE) },
                testTag = "hub_ble_card"
            )
        }

        item {
            HubNavigationCard(
                title = "Offline Articles, Glossary, FAQ & Sources",
                subtitle = "BMI limitations, WHtR 0.50 rule, pediatric LMS & medical disclaimer",
                icon = Icons.AutoMirrored.Filled.MenuBook,
                onClick = { viewModel.openSubScreen(SubScreen.INSIGHTS_AND_ARTICLES) },
                testTag = "hub_articles_card"
            )
        }

        item {
            HubNavigationCard(
                title = "Export CSV, PDF Report & Encrypted JSON Backup",
                subtitle = "Generate clinical PDF charts, CSV spreadsheets & Drive appDataFolder sync",
                icon = Icons.Default.PictureAsPdf,
                onClick = { viewModel.openSubScreen(SubScreen.EXPORT_AND_BACKUP) },
                testTag = "hub_export_card"
            )
        }

        item {
            HubNavigationCard(
                title = "Privacy Dashboard, PIN & Biometric Vault",
                subtitle = "AES-256-GCM hardware keystore, FLAG_SECURE recents shield & data wipe",
                icon = Icons.Default.Security,
                onClick = { viewModel.openSubScreen(SubScreen.PRIVACY_AND_SECURITY) },
                testTag = "hub_privacy_card"
            )
        }

        item {
            HubNavigationCard(
                title = "Material 3 Expressive Theme & 20+ Languages",
                subtitle = "Dynamic color, high contrast, units, decimal precision & locales",
                icon = Icons.Default.Settings,
                onClick = { viewModel.openSubScreen(SubScreen.SETTINGS) },
                testTag = "hub_settings_card"
            )
        }

        // Medical Disclaimer Footer
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = stringResource(R.string.medical_disclaimer_title),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.medical_disclaimer_body),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun HubNavigationCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(2.dp))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/**
 * SubScreen: Multi-Profile Manager & Side-by-Side Profile Comparison
 */
@Composable
fun ProfilesAndCompareSubScreen(
    state: BmiPulseUiState,
    viewModel: BmiPulseViewModel
) {
    BackHandler { viewModel.navigateBack() }
    var newName by remember { mutableStateOf("") }
    var newEmoji by remember { mutableStateOf("🌟") }
    var newAge by remember { mutableIntStateOf(28) }
    var newHeight by remember { mutableDoubleStateOf(170.0) }
    var newTargetWeight by remember { mutableDoubleStateOf(65.0) }
    var newSex by remember { mutableStateOf(BiologicalSex.FEMALE) }

    val p1 = state.activeProfile
    val p2 = state.comparisonSecondProfile
    val p1Latest = state.profileLogs.firstOrNull()
    val p2Latest = state.secondProfileLogs.firstOrNull()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text("Profiles & Side-by-Side Comparison", style = MaterialTheme.typography.headlineSmall)
            }
        }

        // Side-by-side comparison card
        if (p1 != null && p2 != null) {
            item {
                Card(
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Side-by-Side Profile Comparison", style = MaterialTheme.typography.titleMedium)

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.profiles.filter { it.id != p1.id }.forEach { candidate ->
                                FilterChip(
                                    selected = candidate.id == p2.id,
                                    onClick = { viewModel.selectComparisonProfile(candidate.id) },
                                    label = { Text("Compare vs ${candidate.name}") }
                                )
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("${p1.avatarEmoji} ${p1.name}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("BMI: ${String.format(Locale.US, "%.1f", p1Latest?.bmi ?: 22.9)}", style = MaterialTheme.typography.headlineSmall)
                                    Text("Weight: ${p1Latest?.weightKg ?: p1.targetWeightKg} kg")
                                    Text("Body Fat: ${String.format(Locale.US, "%.1f", p1Latest?.bodyFatPct ?: 21.0)}%")
                                    Text("Height: ${p1.heightCm.toInt()} cm • ${p1.ageYears}y")
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("${p2.avatarEmoji} ${p2.name}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("BMI: ${String.format(Locale.US, "%.1f", p2Latest?.bmi ?: 24.4)}", style = MaterialTheme.typography.headlineSmall)
                                    Text("Weight: ${p2Latest?.weightKg ?: p2.targetWeightKg} kg")
                                    Text("Body Fat: ${String.format(Locale.US, "%.1f", p2Latest?.bodyFatPct ?: 16.0)}%")
                                    Text("Height: ${p2.heightCm.toInt()} cm • ${p2.ageYears}y")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Create New Profile Card
        item {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Create New Profile", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newName,
                            onValueChange = { newName = it },
                            label = { Text("Profile Name") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = newEmoji,
                            onValueChange = { newEmoji = it },
                            label = { Text("Icon") },
                            modifier = Modifier.width(80.dp)
                        )
                    }
                    ExpressiveButtonGroup(
                        options = BiologicalSex.entries,
                        selectedOption = newSex,
                        onOptionSelected = { newSex = it },
                        labelProvider = { it.label }
                    )
                    Button(
                        onClick = {
                            viewModel.createNewProfile(newName, newEmoji, newAge, newSex, newHeight, newTargetWeight)
                            newName = ""
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add & Switch to Profile")
                    }
                }
            }
        }
    }
}

/**
 * SubScreen: Goals, Safe-Rate Planner, Milestones & Achievement Badges
 */
@Composable
fun GoalsAndAchievementsSubScreen(
    state: BmiPulseUiState,
    viewModel: BmiPulseViewModel
) {
    BackHandler { viewModel.navigateBack() }
    val goal = state.goalProjection
    var targetWeight by remember(state.activeProfile) { mutableDoubleStateOf(state.activeProfile?.targetWeightKg ?: 61.5) }
    var targetDays by remember(state.activeProfile) { mutableIntStateOf(state.activeProfile?.targetDays ?: 70) }
    var tolerance by remember(state.activeProfile) { mutableDoubleStateOf(state.activeProfile?.maintainToleranceKg ?: 1.0) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text("Goals, Milestones & Achievements", style = MaterialTheme.typography.headlineSmall)
            }
        }

        item {
            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Wavy Goal Progress • ${(goal.progressFraction * 100).toInt()}%", style = MaterialTheme.typography.titleLarge)
                    ExpressiveWavyProgressIndicator(progress = goal.progressFraction)
                    Text("Projected Completion: ${goal.projectedDateLabel} (Required pace: ${String.format(Locale.US, "%.2f", goal.requiredWeeklyRateKg)} kg/wk)")

                    goal.milestones.forEach { m ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(m.title, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = "${String.format(Locale.US, "%.1f", m.targetWeightKg)} kg ${if (m.isReached) "✅" else "⏳"}",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
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
                    Text("Configure Goal & Maintain Tolerance Band", style = MaterialTheme.typography.titleMedium)
                    WheelSliderNumberInput(
                        label = "Goal Weight",
                        value = targetWeight,
                        unitLabel = "kg",
                        valueRange = 35f..180f,
                        onValueChange = { targetWeight = it }
                    )
                    WheelSliderNumberInput(
                        label = "Target Timeline",
                        value = targetDays.toDouble(),
                        unitLabel = "days",
                        valueRange = 14f..365f,
                        step = 7.0,
                        onValueChange = { targetDays = it.toInt() }
                    )
                    Button(
                        onClick = { viewModel.updateActiveProfileGoal(targetWeight, targetDays, tolerance) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Goal Target")
                    }
                }
            }
        }

        item {
            Text("Achievement Badges", style = MaterialTheme.typography.titleLarge)
        }

        items(state.achievements, key = { it.id }) { badge ->
            Card(
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = if (badge.isUnlocked) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceContainer
                ),
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
                        Text(badge.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(badge.description, style = MaterialTheme.typography.bodySmall)
                    }
                    Text(badge.progressText, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

/**
 * SubScreen: Smart Reminders & RemoteInput Quick-Log Notification Tester
 */
@Composable
fun RemindersSubScreen(
    state: BmiPulseUiState,
    viewModel: BmiPulseViewModel
) {
    BackHandler { viewModel.navigateBack() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text("Smart Reminders & Quick-Log", style = MaterialTheme.typography.headlineSmall)
            }
        }

        item {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Notification Quick-Log (RemoteInput)", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "Reminders automatically skip if you already logged today, respect Do Not Disturb, reschedule after reboot, and let you type your weight directly inside the notification shade.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Button(
                        onClick = { viewModel.triggerTestNotification() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Send Test Quick-Log Notification Now")
                    }
                }
            }
        }

        items(state.reminders, key = { it.id }) { rem ->
            Card(
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(rem.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = String.format(Locale.US, "%02d:%02d • %s", rem.hourOfDay, rem.minute, rem.daysOfWeekCsv),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Smart-Skip if Logged: ${if (rem.smartSkipIfLogged) "On" else "Off"} • DND-Friendly: ${if (rem.respectDnd) "On" else "Off"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Switch(
                        checked = rem.isEnabled,
                        onCheckedChange = { viewModel.toggleReminder(rem, it) }
                    )
                }
            }
        }
    }
}

/**
 * SubScreen: Offline Education Articles, BMI Limitations, Glossary, FAQ & Medical Disclaimer
 */
@Composable
fun InsightsAndArticlesSubScreen(
    state: BmiPulseUiState,
    viewModel: BmiPulseViewModel
) {
    BackHandler { viewModel.navigateBack() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text("Offline Knowledge Base & Sources", style = MaterialTheme.typography.headlineSmall)
            }
        }

        items(state.articles, key = { it.id }) { article ->
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "${article.category.uppercase(Locale.US)} • ${article.readTimeMinutes} MIN READ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(article.title, style = MaterialTheme.typography.titleLarge)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Key Takeaway: ${article.keyTakeaway}",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    article.contentParagraphs.forEach { p ->
                        Text(p, style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(
                        text = "Source: ${article.scientificReference}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * SubScreen: BLE Smart Scale GATT Importer, Health Connect, Widgets & Quick Settings Tile
 */
@Composable
fun BleSmartScaleSubScreen(
    state: BmiPulseUiState,
    viewModel: BmiPulseViewModel
) {
    BackHandler { viewModel.navigateBack() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text("BLE Smart Scale & Android Integrations", style = MaterialTheme.typography.headlineSmall)
            }
        }

        item {
            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Bluetooth LE Smart Scale GATT (0x181D & 0x181B)", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "Parses standard IEEE-11073 Weight Scale Measurement (0x2A9D) and Body Composition Measurement (0x2A9C) GATT characteristics.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    state.calcInput.lastBleMeasurement?.let { m ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Device: ${m.deviceName}", style = MaterialTheme.typography.titleSmall)
                                Text("Weight: ${String.format(Locale.US, "%.2f", m.weightKg)} kg • BF: ${m.bodyFatPct ?: 21.4}% • Bio-Impedance: ${m.impedanceOhms} Ω")
                                Text("Raw GATT Frame: ${m.rawHexFrame}", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                    Button(
                        onClick = { viewModel.scanAndImportBleScaleReading() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Bluetooth, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Scan & Import GATT 0x181D Scale Packet")
                    }
                }
            }
        }

        item {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Health Connect Two-Way Sync", style = MaterialTheme.typography.titleMedium)
                            Text("Syncs WeightRecord, HeightRecord, and BodyFatRecord", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(
                            checked = state.settings.healthConnectSyncEnabled,
                            onCheckedChange = { viewModel.setHealthConnectSync(it) }
                        )
                    }
                    HorizontalDivider()
                    Text("Home-Screen Widget & Quick Settings Tile", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "• Long-press the BMI Pulse app icon on your launcher to access Quick BMI, Log Weight, and Body Fat shortcuts, or pin the BMI Pulse Home Widget.\n• Edit your Android Notification Quick Settings shade to add the 'Log BMI Weight' Tile.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

/**
 * SubScreen: Export CSV, PDF Clinical Report & Encrypted JSON Backup
 */
@Composable
fun ExportAndBackupSubScreen(
    state: BmiPulseUiState,
    viewModel: BmiPulseViewModel
) {
    BackHandler { viewModel.navigateBack() }
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text("Export CSV, PDF & Encrypted Backup", style = MaterialTheme.typography.headlineSmall)
            }
        }

        item {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { viewModel.exportAndShareCsv(context) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Export & Share CSV Spreadsheet")
                    }

                    Button(
                        onClick = { viewModel.exportAndSharePdf(context) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Generate PDF Clinical Report with Trend Chart")
                    }

                    OutlinedButton(
                        onClick = { viewModel.exportAndShareEncryptedJson(context) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Export AES-256-GCM Encrypted JSON Backup")
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Google Drive appDataFolder Backup", style = MaterialTheme.typography.titleSmall)
                            Text("Optional zero-knowledge encrypted backup to your personal Drive", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(
                            checked = state.settings.driveBackupEnabled,
                            onCheckedChange = { viewModel.setDriveBackup(it) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * SubScreen: Privacy Dashboard, PIN / Biometric Lock & FLAG_SECURE
 */
@Composable
fun PrivacyAndSecuritySubScreen(
    state: BmiPulseUiState,
    viewModel: BmiPulseViewModel
) {
    BackHandler { viewModel.navigateBack() }
    var newPin by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text("Privacy Dashboard & Security Vault", style = MaterialTheme.typography.headlineSmall)
            }
        }

        item {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Zero-Telemetry Offline Architecture", style = MaterialTheme.typography.titleMedium)
                    }
                    Text(
                        text = "• No INTERNET permission requested in AndroidManifest.xml\n• Zero ads, zero analytics SDKs, zero cloud accounts\n• AES-256-GCM Android Keystore encryption + SHA-256 salted PIN lock",
                        style = MaterialTheme.typography.bodySmall
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
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("App Lock (PIN & BiometricPrompt)", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = newPin,
                        onValueChange = { if (it.length <= 6) newPin = it },
                        label = { Text("Set 4–6 Digit PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                if (newPin.length >= 4) {
                                    viewModel.configurePinLock(newPin, true)
                                    newPin = ""
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Enable PIN Lock")
                        }
                        if (state.settings.pinLockEnabled) {
                            OutlinedButton(
                                onClick = { viewModel.configurePinLock("", false) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Disable Lock")
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Hide Screen in App Switcher (FLAG_SECURE)", style = MaterialTheme.typography.titleSmall)
                            Text("Prevents screenshots and blurs preview in Android Recents", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(
                            checked = state.settings.flagSecureHideInRecents,
                            onCheckedChange = { viewModel.setFlagSecure(it) }
                        )
                    }

                    HorizontalDivider()

                    Button(
                        onClick = { viewModel.wipeAllDataAndReset() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Wipe All Personal Data & Reset Vault")
                    }
                }
            }
        }
    }
}

/**
 * SubScreen: Material 3 Expressive Theme, High Contrast, Precision & 20+ Languages
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsSubScreen(
    state: BmiPulseUiState,
    viewModel: BmiPulseViewModel
) {
    BackHandler { viewModel.navigateBack() }
    val supportedLocales = listOf(
        "en" to "English", "es" to "Español", "fr" to "Français", "de" to "Deutsch",
        "it" to "Italiano", "pt" to "Português", "ja" to "日本語", "ko" to "한국어",
        "zh" to "中文", "ar" to "العربية (RTL)", "he" to "עברית (RTL)", "hi" to "हिन्दी",
        "tr" to "Türkçe", "nl" to "Nederlands", "pl" to "Polski", "sv" to "Svenska",
        "vi" to "Tiếng Việt", "id" to "Bahasa Indonesia", "th" to "ไทย", "uk" to "Українська"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text("Material 3 Expressive Settings", style = MaterialTheme.typography.headlineSmall)
            }
        }

        item {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Theme & Visual Expressiveness", style = MaterialTheme.typography.titleMedium)
                    ExpressiveButtonGroup(
                        options = ThemePreference.entries,
                        selectedOption = state.settings.themePreference,
                        onOptionSelected = { viewModel.setThemePreference(it) },
                        labelProvider = { it.label }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Android 12+ Dynamic Wallpaper Color")
                        Switch(
                            checked = state.settings.dynamicColorEnabled,
                            onCheckedChange = { viewModel.setDynamicColor(it) }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("WCAG AAA High-Contrast Mode")
                        Switch(
                            checked = state.settings.highContrastEnabled,
                            onCheckedChange = { viewModel.setHighContrast(it) }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("BMI Decimal Precision (1 vs 2 decimals)")
                        Switch(
                            checked = state.settings.decimalPlaces == 2,
                            onCheckedChange = { viewModel.setDecimalPlaces(if (it) 2 else 1) }
                        )
                    }
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
                    Text("Per-App Language & RTL Support (20 Languages)", style = MaterialTheme.typography.titleMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        supportedLocales.forEach { (code, name) ->
                            FilterChip(
                                selected = state.settings.appLanguageCode == code,
                                onClick = { viewModel.setLanguageCode(code) },
                                label = { Text(name) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Full-Screen Security Gate when PIN / Biometric Lock is active
 */
@Composable
fun PinLockOverlayScreen(
    viewModel: BmiPulseViewModel
) {
    var enteredPin by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                Text("BMI Pulse Vault Locked", style = MaterialTheme.typography.headlineSmall)
                OutlinedTextField(
                    value = enteredPin,
                    onValueChange = {
                        enteredPin = it
                        errorText = null
                    },
                    label = { Text("Enter PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                errorText?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                Button(
                    onClick = {
                        if (!viewModel.attemptPinUnlock(enteredPin)) {
                            errorText = "Incorrect PIN. Try again."
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Unlock with PIN")
                }
                OutlinedButton(
                    onClick = { viewModel.unlockViaBiometrics() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Fingerprint, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Unlock with BiometricPrompt")
                }
            }
        }
    }
}
