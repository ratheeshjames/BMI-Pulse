package com.example

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddChart
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.ThemePreference
import com.example.di.AndroidEntryPoint
import com.example.ui.components.ExpressiveFabMenu
import com.example.ui.components.FabMenuItem
import com.example.ui.screens.AnalyticsChartsScreen
import com.example.ui.screens.BleSmartScaleSubScreen
import com.example.ui.screens.BodyCompositionLabScreen
import com.example.ui.screens.ExportAndBackupSubScreen
import com.example.ui.screens.GoalsAndAchievementsSubScreen
import com.example.ui.screens.HubAndVaultMainScreen
import com.example.ui.screens.InsightsAndArticlesSubScreen
import com.example.ui.screens.LogEntrySubScreen
import com.example.ui.screens.PinLockOverlayScreen
import com.example.ui.screens.PrivacyAndSecuritySubScreen
import com.example.ui.screens.ProfilesAndCompareSubScreen
import com.example.ui.screens.PulseDashboardScreen
import com.example.ui.screens.QuickCalculatorScreen
import com.example.ui.screens.RemindersSubScreen
import com.example.ui.screens.ResultDetailSubScreen
import com.example.ui.screens.SettingsSubScreen
import com.example.ui.screens.UnitConverterSubScreen
import com.example.ui.theme.BmiPulseTheme
import com.example.ui.viewmodel.BmiPulseUiState
import com.example.ui.viewmodel.BmiPulseViewModel
import com.example.ui.viewmodel.PrimaryTab
import com.example.ui.viewmodel.SubScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var viewModel: BmiPulseViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as BmiPulseApplication
        viewModel = ViewModelProvider(
            this,
            BmiPulseViewModel.provideFactory(app.container.repository, this)
        )[BmiPulseViewModel::class.java]

        handleShortcutIntent(intent)

        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(state.settings.flagSecureHideInRecents) {
                if (state.settings.flagSecureHideInRecents) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                }
            }

            val darkTheme = when (state.settings.themePreference) {
                ThemePreference.SYSTEM -> isSystemInDarkTheme()
                ThemePreference.DARK -> true
                ThemePreference.LIGHT -> false
            }

            BmiPulseTheme(
                darkTheme = darkTheme,
                dynamicColor = state.settings.dynamicColorEnabled,
                highContrast = state.settings.highContrastEnabled
            ) {
                if (state.isLockedByPin) {
                    PinLockOverlayScreen(viewModel = viewModel)
                } else {
                    BmiPulseAppShell(state = state, viewModel = viewModel)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleShortcutIntent(intent)
    }

    private fun handleShortcutIntent(intent: Intent?) {
        when (intent?.getStringExtra("shortcut_dest")) {
            "calculator" -> viewModel.selectTab(PrimaryTab.QUICK_CALCULATOR)
            "log" -> viewModel.openSubScreen(SubScreen.LOG_ENTRY)
            "body_comp" -> viewModel.selectTab(PrimaryTab.BODY_LAB)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BmiPulseAppShell(
    state: BmiPulseUiState,
    viewModel: BmiPulseViewModel
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Favorite,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "BMI Pulse",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${state.activeProfile?.avatarEmoji ?: "🌿"} ${state.activeProfile?.name ?: "Quick Mode"} • BMI ${String.format("%.1f", state.bmiAnalysis.bmi)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.openSubScreen(SubScreen.UNIT_CONVERTER) },
                            modifier = Modifier.testTag("topbar_converter_btn")
                        ) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = "Unit Converter")
                        }
                        IconButton(
                            onClick = { viewModel.openSubScreen(SubScreen.SETTINGS) },
                            modifier = Modifier.testTag("topbar_settings_btn")
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            bottomBar = {
                if (!isWideScreen) {
                    NavigationBar(modifier = Modifier.testTag("bottom_nav_bar")) {
                        PrimaryTab.entries.forEach { tab ->
                            val selected = state.activeTab == tab && state.activeSubScreen == SubScreen.NONE
                            NavigationBarItem(
                                selected = selected,
                                onClick = { viewModel.selectTab(tab) },
                                icon = {
                                    Icon(
                                        imageVector = tab.icon(selected),
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label) },
                                modifier = Modifier.testTag("nav_tab_${tab.route}")
                            )
                        }
                    }
                }
            },
            floatingActionButton = {
                if (state.activeSubScreen == SubScreen.NONE) {
                    ExpressiveFabMenu(
                        items = listOf(
                            FabMenuItem("Quick-Log Weight", Icons.Default.MonitorWeight) {
                                viewModel.openSubScreen(SubScreen.LOG_ENTRY)
                            },
                            FabMenuItem("BLE Smart Scale Scan", Icons.Default.Bluetooth) {
                                viewModel.scanAndImportBleScaleReading()
                            },
                            FabMenuItem("Unit Converter", Icons.Default.SwapHoriz) {
                                viewModel.openSubScreen(SubScreen.UNIT_CONVERTER)
                            },
                            FabMenuItem("Body Fat & BMR Lab", Icons.Default.AddChart) {
                                viewModel.selectTab(PrimaryTab.BODY_LAB)
                            }
                        )
                    )
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isWideScreen) {
                    NavigationRail(
                        modifier = Modifier
                            .fillMaxHeight()
                            .testTag("wide_nav_rail")
                    ) {
                        PrimaryTab.entries.forEach { tab ->
                            val selected = state.activeTab == tab && state.activeSubScreen == SubScreen.NONE
                            NavigationRailItem(
                                selected = selected,
                                onClick = { viewModel.selectTab(tab) },
                                icon = {
                                    Icon(
                                        imageVector = tab.icon(selected),
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label) }
                            )
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    // Status toast banner
                    state.calcInput.statusBannerMessage?.let { msg ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { viewModel.dismissBanner() },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Dismiss notification")
                                }
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        com.example.ui.navigation.BmiPulseNavGraph(
                            state = state,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}

private fun PrimaryTab.icon(selected: Boolean): ImageVector {
    return when (this) {
        PrimaryTab.PULSE_DASHBOARD -> if (selected) Icons.Filled.Dashboard else Icons.Outlined.Dashboard
        PrimaryTab.QUICK_CALCULATOR -> if (selected) Icons.Filled.Calculate else Icons.Outlined.Calculate
        PrimaryTab.BODY_LAB -> if (selected) Icons.Filled.Science else Icons.Outlined.Science
        PrimaryTab.ANALYTICS_CHARTS -> if (selected) Icons.Filled.ShowChart else Icons.Outlined.ShowChart
        PrimaryTab.HUB_VAULT -> if (selected) Icons.Filled.Settings else Icons.Outlined.Settings
    }
}
