package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.domain.model.BmiCategory
import com.example.ui.theme.ExperimentalMaterial3ExpressiveApi
import com.example.ui.theme.LocalMotionScheme
import com.example.ui.theme.MaterialShapes
import com.example.ui.theme.accentColor
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * 1. Expressive Animated Color-Coded BMI Gauge with Shape-Morphing Center Badge
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExpressiveBmiGauge(
    bmi: Double,
    category: BmiCategory,
    decimalPlaces: Int = 1,
    isDark: Boolean = false,
    modifier: Modifier = Modifier
) {
    val motion = LocalMotionScheme.current
    val animatedBmi by animateFloatAsState(
        targetValue = bmi.toFloat().coerceIn(12f, 42f),
        animationSpec = motion.defaultSpatialSpec,
        label = "bmi_gauge_value"
    )
    val morphFraction by animateFloatAsState(
        targetValue = if (category == BmiCategory.NORMAL) 1f else 0.45f,
        animationSpec = motion.defaultSpatialSpec,
        label = "badge_morph"
    )
    val accent = category.accentColor(isDark)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(230.dp)
            .semantics { contentDescription = "BMI Gauge showing ${String.format(Locale.US, "%.1f", bmi)} ${category.title}" },
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(220.dp)
                .padding(12.dp)
        ) {
            val strokeWidth = 20.dp.toPx()
            val arcDiameter = min(size.width, size.height) - strokeWidth
            val topLeft = Offset((size.width - arcDiameter) / 2f, (size.height - arcDiameter) / 2f)
            val arcSize = Size(arcDiameter, arcDiameter)

            // Draw 4 Expressive Category Arc Segments from 150 deg to 390 deg (total 240 deg sweep)
            // Range mapped: BMI 12 -> 42 (30 BMI units across 240 degrees = 8 deg per BMI unit)
            val segments = listOf(
                Triple(12f, 18.5f, Color(0xFF0EA5E9)), // Underweight (52 deg)
                Triple(18.5f, 25f, Color(0xFF22C55E)), // Healthy Normal (52 deg)
                Triple(25f, 30f, Color(0xFFF59E0B)),   // Overweight (40 deg)
                Triple(30f, 42f, Color(0xFFEF4444))    // Obese (96 deg)
            )

            segments.forEach { (startBmi, endBmi, segColor) ->
                val startAngle = 150f + (startBmi - 12f) * 8f + 2f
                val sweepAngle = ((endBmi - startBmi) * 8f) - 4f
                drawArc(
                    color = segColor.copy(alpha = 0.85f),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Needle pointer Position
            val pointerAngleDeg = 150f + (animatedBmi - 12f) * 8f
            val pointerAngleRad = pointerAngleDeg * (PI / 180.0)
            val radius = arcDiameter / 2f
            val cx = size.width / 2f
            val cy = size.height / 2f
            val indicatorX = cx + (radius * cos(pointerAngleRad)).toFloat()
            val indicatorY = cy + (radius * sin(pointerAngleRad)).toFloat()

            drawCircle(
                color = Color.White,
                radius = strokeWidth * 0.68f,
                center = Offset(indicatorX, indicatorY)
            )
            drawCircle(
                color = accent,
                radius = strokeWidth * 0.46f,
                center = Offset(indicatorX, indicatorY)
            )
        }

        // Morphing MaterialShapes Center Badge
        Surface(
            shape = MaterialShapes.morphingBadgeShape(lobes = 12, morphProgress = morphFraction),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
            modifier = Modifier
                .size(136.dp)
                .border(
                    width = 2.dp,
                    color = accent.copy(alpha = 0.5f),
                    shape = MaterialShapes.morphingBadgeShape(lobes = 12, morphProgress = morphFraction)
                )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = "BMI",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val fmt = if (decimalPlaces == 2) "%.2f" else "%.1f"
                Text(
                    text = String.format(Locale.US, fmt, bmi),
                    style = MaterialTheme.typography.displaySmall,
                    color = accent,
                    modifier = Modifier.testTag("bmi_value_text")
                )
                Text(
                    text = category.shortTitle,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * 2. Expressive Connected ButtonGroup (Morphs corner radii on press and active selection)
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <T> ExpressiveButtonGroup(
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    labelProvider: (T) -> String,
    modifier: Modifier = Modifier,
    testTagPrefix: String = "btn_group"
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEachIndexed { index, item ->
            val isSelected = item == selectedOption
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()

            val cornerRadius by animateDpAsState(
                targetValue = when {
                    isPressed -> 8.dp
                    isSelected -> 24.dp
                    else -> 12.dp
                },
                label = "button_group_corner"
            )

            val shape = when {
                isSelected -> RoundedCornerShape(cornerRadius)
                index == 0 -> RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp, topEnd = 8.dp, bottomEnd = 8.dp)
                index == options.lastIndex -> RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp, topEnd = 20.dp, bottomEnd = 20.dp)
                else -> RoundedCornerShape(8.dp)
            }

            Surface(
                shape = shape,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(shape)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) { onOptionSelected(item) }
                    .testTag("${testTagPrefix}_$index")
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 6.dp)) {
                    Text(
                        text = labelProvider(item),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * 3. Expressive SplitButton (Primary Action + Connected Menu Trigger)
 */
@Composable
fun ExpressiveSplitButton(
    primaryLabel: String,
    primaryIcon: ImageVector,
    onPrimaryClick: () -> Unit,
    menuItems: List<Pair<String, () -> Unit>>,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = onPrimaryClick,
                shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp, topEnd = 4.dp, bottomEnd = 4.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Icon(primaryIcon, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(primaryLabel, style = MaterialTheme.typography.labelLarge)
            }
            Spacer(Modifier.width(2.dp))
            Button(
                onClick = { expanded = true },
                shape = RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp, topEnd = 24.dp, bottomEnd = 24.dp),
                contentPadding = PaddingValues(horizontal = 12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.height(48.dp)
            ) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "More actions")
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            menuItems.forEach { (title, action) ->
                DropdownMenuItem(
                    text = { Text(title) },
                    onClick = {
                        expanded = false
                        action()
                    }
                )
            }
        }
    }
}

/**
 * 4. Expressive WavyLinearProgressIndicator for Goal Progress
 */
@Composable
fun ExpressiveWavyProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_phase")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase_anim"
    )
    val clamped = progress.coerceIn(0f, 1f)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(20.dp)
    ) {
        val midY = size.height / 2f
        val waveLength = 28.dp.toPx()
        val amplitude = 4.dp.toPx()
        val activeEnd = size.width * clamped

        // Draw track line from activeEnd to end
        if (activeEnd < size.width) {
            drawLine(
                color = trackColor,
                start = Offset(activeEnd, midY),
                end = Offset(size.width, midY),
                strokeWidth = 6.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Draw animated sine wave for active progress
        if (activeEnd > 2f) {
            val wavePath = Path()
            var x = 0f
            while (x <= activeEnd) {
                val y = midY + amplitude * sin((2f * PI * x / waveLength) + phase).toFloat()
                if (x == 0f) wavePath.moveTo(x, y) else wavePath.lineTo(x, y)
                x += 3f
            }
            drawPath(
                path = wavePath,
                color = color,
                style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

/**
 * 5. Expressive FloatingActionButtonMenu (Expandable Multi-Action FAB)
 */
data class FabMenuItem(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun ExpressiveFabMenu(
    items: List<FabMenuItem>,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom)
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items.forEach { item ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        tonalElevation = 6.dp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable {
                                expanded = false
                                item.onClick()
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Icon(item.icon, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(item.label, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { expanded = !expanded },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(if (expanded) 28.dp else 20.dp),
            modifier = Modifier.testTag("expressive_fab_menu")
        ) {
            Icon(
                imageVector = if (expanded) Icons.Default.Close else Icons.Default.Add,
                contentDescription = if (expanded) "Close quick actions" else "Open quick actions"
            )
        }
    }
}

/**
 * 6. Expressive Wheel/Slider + Stepper + Keypad Input Card
 */
@Composable
fun WheelSliderNumberInput(
    label: String,
    value: Double,
    unitLabel: String,
    valueRange: ClosedFloatingPointRange<Float>,
    step: Double = 0.5,
    onValueChange: (Double) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "slider_input"
) {
    var showKeypadDialog by remember { mutableStateOf(false) }
    var keypadText by remember(value) { mutableStateOf(String.format(Locale.US, "%.1f", value)) }

    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            keypadText = String.format(Locale.US, "%.1f", value)
                            showKeypadDialog = true
                        }
                        .testTag("${testTag}_keypad_btn")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${String.format(Locale.US, "%.1f", value)} $unitLabel",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Enter $label with keypad",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = {
                        val next = (value - step).coerceIn(valueRange.start.toDouble(), valueRange.endInclusive.toDouble())
                        onValueChange(next)
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape)
                        .testTag("${testTag}_minus")
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease $label")
                }

                Slider(
                    value = value.toFloat().coerceIn(valueRange.start, valueRange.endInclusive),
                    onValueChange = { onValueChange(it.toDouble()) },
                    valueRange = valueRange,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                        .testTag(testTag)
                )

                IconButton(
                    onClick = {
                        val next = (value + step).coerceIn(valueRange.start.toDouble(), valueRange.endInclusive.toDouble())
                        onValueChange(next)
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape)
                        .testTag("${testTag}_plus")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Increase $label")
                }
            }
        }
    }

    if (showKeypadDialog) {
        AlertDialog(
            onDismissRequest = { showKeypadDialog = false },
            title = { Text("Enter $label ($unitLabel)") },
            text = {
                OutlinedTextField(
                    value = keypadText,
                    onValueChange = { keypadText = it },
                    label = { Text("$label in $unitLabel") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        keypadText.toDoubleOrNull()?.let { parsed ->
                            val clamped = parsed.coerceIn(valueRange.start.toDouble(), valueRange.endInclusive.toDouble())
                            onValueChange(clamped)
                        }
                        showKeypadDialog = false
                    }
                ) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { showKeypadDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * 7. Expressive Morphing LoadingIndicator
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExpressiveLoadingIndicator(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "expressive_loader")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(2400)),
        label = "rotation"
    )
    val morph by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "morph"
    )
    Box(
        modifier = modifier
            .size(48.dp)
            .rotate(rotation)
            .clip(MaterialShapes.morphingBadgeShape(lobes = 8, morphProgress = morph))
            .background(MaterialTheme.colorScheme.primary)
    )
}
