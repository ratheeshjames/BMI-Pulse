package com.example.domain.model

import androidx.compose.runtime.Immutable

enum class UnitSystem(val label: String) {
    METRIC("Metric (kg / cm)"),
    IMPERIAL("Imperial (lb / ft·in)"),
    MIXED("Mixed (st·lb / cm)")
}

enum class BiologicalSex(val label: String) {
    FEMALE("Female"),
    MALE("Male")
}

enum class ActivityLevel(val label: String, val description: String, val multiplier: Double) {
    SEDENTARY("Sedentary", "Desk job, minimal planned exercise", 1.20),
    LIGHT("Lightly Active", "Light exercise 1–3 days/week", 1.375),
    MODERATE("Moderately Active", "Moderate workouts 3–5 days/week", 1.55),
    ACTIVE("Very Active", "Hard training 6–7 days/week", 1.725),
    ATHLETE("Extra Active / Athlete", "Intense daily training or physical job", 1.90)
}

enum class BmiStandard(val label: String, val subtitle: String) {
    WHO_STANDARD("WHO Standard", "Global adult thresholds (Normal: 18.5–24.9)"),
    ASIAN_PACIFIC("Asian-Pacific (WPRO)", "Adjusted cutoffs (Normal: 18.5–22.9, Overweight ≥ 23)"),
    CHILD_CDC("Child/Teen CDC LMS", "Ages 2–20 yrs BMI-for-Age Percentile (CDC)"),
    CHILD_WHO("Child/Teen WHO LMS", "Ages 2–19 yrs Z-Score & Percentile (WHO)")
}

enum class BmiCategory(
    val title: String,
    val shortTitle: String,
    val minBmi: Double,
    val maxBmi: Double,
    val healthRiskSummary: String,
    val recommendation: String
) {
    SEVERE_THINNESS(
        title = "Severe Thinness",
        shortTitle = "Underweight III",
        minBmi = 0.0,
        maxBmi = 16.0,
        healthRiskSummary = "High risk of nutritional deficiency, bone mineral loss, and immune suppression.",
        recommendation = "Consult a healthcare provider for a structured nutrient-dense refeeding plan."
    ),
    MODERATE_THINNESS(
        title = "Moderate Thinness",
        shortTitle = "Underweight II",
        minBmi = 16.0,
        maxBmi = 17.0,
        healthRiskSummary = "Moderate risk of fatigue, anemia, and reduced muscle recovery.",
        recommendation = "Aim for a gradual +300 to +450 kcal daily surplus with strength training."
    ),
    MILD_THINNESS(
        title = "Mild Thinness",
        shortTitle = "Underweight I",
        minBmi = 17.0,
        maxBmi = 18.5,
        healthRiskSummary = "Slightly below optimal energy reserves; may be normal for naturally slender frames.",
        recommendation = "Focus on protein adequacy (1.6g/kg) and whole-food calorie density."
    ),
    NORMAL(
        title = "Healthy Normal Weight",
        shortTitle = "Healthy",
        minBmi = 18.5,
        maxBmi = 25.0,
        healthRiskSummary = "Lowest statistical risk for cardiometabolic conditions when paired with healthy waist ratios.",
        recommendation = "Maintain balanced nutrition, regular resistance exercise, and sleep consistency."
    ),
    OVERWEIGHT(
        title = "Overweight / Pre-Obese",
        shortTitle = "Overweight",
        minBmi = 25.0,
        maxBmi = 30.0,
        healthRiskSummary = "Elevated cardiometabolic risk unless caused by high skeletal muscle mass (check Waist-to-Height).",
        recommendation = "A modest 300–500 kcal deficit and 8,000+ daily steps yields sustainable fat loss."
    ),
    OBESE_CLASS_I(
        title = "Obese Class I",
        shortTitle = "Obese I",
        minBmi = 30.0,
        maxBmi = 35.0,
        healthRiskSummary = "Increased risk of hypertension, insulin resistance, and joint loading.",
        recommendation = "Target 0.5–0.8 kg/week reduction with high-satiety protein and low-impact cardio."
    ),
    OBESE_CLASS_II(
        title = "Obese Class II",
        shortTitle = "Obese II",
        minBmi = 35.0,
        maxBmi = 40.0,
        healthRiskSummary = "High risk of metabolic syndrome, sleep apnea, and cardiovascular strain.",
        recommendation = "Partner with a clinician or registered dietitian for a personalized long-term plan."
    ),
    OBESE_CLASS_III(
        title = "Obese Class III",
        shortTitle = "Obese III",
        minBmi = 40.0,
        maxBmi = 99.0,
        healthRiskSummary = "Very high systemic cardiometabolic and orthopedic risk.",
        recommendation = "Seek comprehensive medical supervision for safe, multi-disciplinary care."
    )
}

enum class BmrFormula(val label: String, val subtitle: String) {
    MIFFLIN_ST_JEOR("Mifflin-St Jeor", "Gold standard for general clinical accuracy"),
    HARRIS_BENEDICT("Revised Harris-Benedict", "Classic Roza & Shizgal thermogenic equation"),
    KATCH_MCARDLE("Katch-McArdle", "Lean-mass driven (best for athletic or known Body Fat %)")
}

enum class FrameSize(val label: String, val description: String) {
    SMALL("Small Frame", "Slender skeletal structure (higher height-to-wrist ratio)"),
    MEDIUM("Medium Frame", "Average skeletal breadth and joint proportion"),
    LARGE("Large Frame", "Broad skeletal structure; naturally carries +5–10% more lean mass")
}

enum class GoalMode(val label: String) {
    LOSE("Lose Weight"),
    MAINTAIN("Maintain Zone"),
    GAIN("Build / Gain")
}

enum class MacroPreset(val label: String, val proteinPct: Int, val carbsPct: Int, val fatPct: Int) {
    BALANCED("Balanced (30/40/30)", 30, 40, 30),
    HIGH_PROTEIN("High Protein (40/35/25)", 40, 35, 25),
    LOW_CARB("Low Carb (35/20/45)", 35, 20, 45),
    ENDURANCE("Endurance (25/55/20)", 25, 55, 20),
    CUSTOM("Custom Ratio", 30, 40, 30)
}

@Immutable
data class ChildPercentileResult(
    val ageMonths: Int,
    val zScore: Double,
    val percentile: Double,
    val percentileCategory: String,
    val medianBmiForAge: Double,
    val sourceTable: String
)

@Immutable
data class BmiAnalysisResult(
    val bmi: Double,
    val bmiPrime: Double,
    val ponderalIndex: Double,
    val category: BmiCategory,
    val standardUsed: BmiStandard,
    val healthyWeightMinKg: Double,
    val healthyWeightMaxKg: Double,
    val weightDeltaToHealthyKg: Double, // negative = need to lose, positive = need to gain, 0 = inside healthy range
    val childPercentile: ChildPercentileResult? = null,
    val isPregnantNoticeActive: Boolean = false
)

@Immutable
data class BodyCompositionResult(
    val navyBodyFatPct: Double,
    val bmiBodyFatPct: Double,
    val primaryBodyFatPct: Double,
    val bodyFatCategory: String,
    val fatMassKg: Double,
    val leanBodyMassKg: Double,
    val waistToHipRatio: Double,
    val whrRiskLevel: String,
    val waistToHeightRatio: Double,
    val whtrStatus: String,
    val bodySurfaceAreaM2: Double,
    val frameSize: FrameSize,
    val idealWeightDevineKg: Double,
    val idealWeightRobinsonKg: Double,
    val idealWeightMillerKg: Double,
    val idealWeightHamwiKg: Double
)

@Immutable
data class MacroBreakdown(
    val targetCalories: Int,
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatGrams: Int,
    val proteinMinGramsPerDay: Int,
    val proteinMaxGramsPerDay: Int,
    val dailyWaterMl: Int
)

@Immutable
data class MetabolismResult(
    val bmrMifflin: Int,
    val bmrHarrisBenedict: Int,
    val bmrKatchMcArdle: Int,
    val activeBmr: Int,
    val tdeeCalories: Int,
    val mildLossCalories: Int,
    val standardLossCalories: Int,
    val mildGainCalories: Int,
    val standardGainCalories: Int,
    val macroBreakdown: MacroBreakdown
)

@Immutable
data class GoalTimelineProjection(
    val currentWeightKg: Double,
    val targetWeightKg: Double,
    val targetBmi: Double,
    val remainingDeltaKg: Double,
    val progressFraction: Float,
    val requiredWeeklyRateKg: Double,
    val isUnsafeRate: Boolean,
    val safetyWarningMessage: String?,
    val projectedDaysToGoal: Int,
    val projectedDateLabel: String,
    val milestones: List<GoalMilestone>,
    val isInMaintainBand: Boolean
)

@Immutable
data class GoalMilestone(
    val title: String,
    val targetWeightKg: Double,
    val isReached: Boolean
)

@Immutable
data class AchievementBadge(
    val id: String,
    val title: String,
    val description: String,
    val iconName: String,
    val isUnlocked: Boolean,
    val progressText: String
)

@Immutable
data class InsightCard(
    val id: String,
    val category: String,
    val title: String,
    val summary: String,
    val actionHint: String,
    val isAlert: Boolean = false
)

@Immutable
data class ArticleItem(
    val id: String,
    val category: String,
    val title: String,
    val readTimeMinutes: Int,
    val keyTakeaway: String,
    val contentParagraphs: List<String>,
    val scientificReference: String
)
