package com.example.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import com.example.ui.screens.AnalyticsChartsScreen
import com.example.ui.screens.BleSmartScaleSubScreen
import com.example.ui.screens.BodyCompositionLabScreen
import com.example.ui.screens.ExportAndBackupSubScreen
import com.example.ui.screens.GoalsAndAchievementsSubScreen
import com.example.ui.screens.HubAndVaultMainScreen
import com.example.ui.screens.InsightsAndArticlesSubScreen
import com.example.ui.screens.LogEntrySubScreen
import com.example.ui.screens.PrivacyAndSecuritySubScreen
import com.example.ui.screens.ProfilesAndCompareSubScreen
import com.example.ui.screens.PulseDashboardScreen
import com.example.ui.screens.QuickCalculatorScreen
import com.example.ui.screens.RemindersSubScreen
import com.example.ui.screens.ResultDetailSubScreen
import com.example.ui.screens.SettingsSubScreen
import com.example.ui.screens.UnitConverterSubScreen
import com.example.ui.viewmodel.BmiPulseUiState
import com.example.ui.viewmodel.BmiPulseViewModel
import com.example.ui.viewmodel.PrimaryTab
import com.example.ui.viewmodel.SubScreen

/**
 * Type-safe route definitions for all 21 screens and destinations in BMI Pulse.
 */
@Immutable
sealed interface BmiPulseRoute {
    val routeId: String

    data object Onboarding : BmiPulseRoute { override val routeId = "onboarding" }
    data object Dashboard : BmiPulseRoute { override val routeId = "dashboard" }
    data object QuickCalculator : BmiPulseRoute { override val routeId = "calculator" }
    data object ResultDetail : BmiPulseRoute { override val routeId = "result_detail" }
    data object LogEntry : BmiPulseRoute { override val routeId = "log_entry" }
    data object BodyCompositionLab : BmiPulseRoute { override val routeId = "body_lab" }
    data object AnalyticsCharts : BmiPulseRoute { override val routeId = "charts" }
    data object HubAndVault : BmiPulseRoute { override val routeId = "hub_vault" }
    data object ProfilesAndCompare : BmiPulseRoute { override val routeId = "profiles_compare" }
    data object GoalsAndAchievements : BmiPulseRoute { override val routeId = "goals_achievements" }
    data object Reminders : BmiPulseRoute { override val routeId = "reminders" }
    data object InsightsAndArticles : BmiPulseRoute { override val routeId = "insights_articles" }
    data object BleSmartScale : BmiPulseRoute { override val routeId = "ble_smart_scale" }
    data object ExportAndBackup : BmiPulseRoute { override val routeId = "export_backup" }
    data object PrivacyAndSecurity : BmiPulseRoute { override val routeId = "privacy_security" }
    data object Settings : BmiPulseRoute { override val routeId = "settings" }
    data object UnitConverter : BmiPulseRoute { override val routeId = "unit_converter" }
}

/**
 * Expressive spring-driven navigation host coordinating primary tabs and detail sub-screens
 * with spatial scale + effects fade transitions and predictive back support.
 */
@Composable
fun BmiPulseNavGraph(
    state: BmiPulseUiState,
    viewModel: BmiPulseViewModel,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = state.activeSubScreen to state.activeTab,
        transitionSpec = {
            (fadeIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ) + scaleIn(
                initialScale = 0.96f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )) togetherWith (fadeOut(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessHigh
                )
            ) + scaleOut(targetScale = 0.98f))
        },
        label = "bmi_pulse_expressive_nav",
        modifier = modifier
    ) { (subScreen, tab) ->
        when (subScreen) {
            SubScreen.RESULT_DETAIL -> ResultDetailSubScreen(state, viewModel)
            SubScreen.LOG_ENTRY -> LogEntrySubScreen(state, viewModel)
            SubScreen.UNIT_CONVERTER -> UnitConverterSubScreen(state, viewModel)
            SubScreen.PROFILES_AND_COMPARE -> ProfilesAndCompareSubScreen(state, viewModel)
            SubScreen.GOALS_AND_ACHIEVEMENTS -> GoalsAndAchievementsSubScreen(state, viewModel)
            SubScreen.REMINDERS -> RemindersSubScreen(state, viewModel)
            SubScreen.INSIGHTS_AND_ARTICLES -> InsightsAndArticlesSubScreen(state, viewModel)
            SubScreen.BLE_SMART_SCALE -> BleSmartScaleSubScreen(state, viewModel)
            SubScreen.EXPORT_AND_BACKUP -> ExportAndBackupSubScreen(state, viewModel)
            SubScreen.PRIVACY_AND_SECURITY -> PrivacyAndSecuritySubScreen(state, viewModel)
            SubScreen.SETTINGS -> SettingsSubScreen(state, viewModel)
            SubScreen.ONBOARDING_TOUR, SubScreen.NONE -> {
                when (tab) {
                    PrimaryTab.PULSE_DASHBOARD -> PulseDashboardScreen(state, viewModel)
                    PrimaryTab.QUICK_CALCULATOR -> QuickCalculatorScreen(state, viewModel)
                    PrimaryTab.BODY_LAB -> BodyCompositionLabScreen(state, viewModel)
                    PrimaryTab.ANALYTICS_CHARTS -> AnalyticsChartsScreen(state, viewModel)
                    PrimaryTab.HUB_VAULT -> HubAndVaultMainScreen(state, viewModel)
                }
            }
        }
    }
}
