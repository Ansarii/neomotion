package com.neoninnovationlab.neomotion.demo.features.playground

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.neoninnovationlab.neomotion.core.extensions.neoBlur

/**
 * Gesture Playground screen — the viral developer tool for NeoMotion.
 *
 * Developers tune [progress] and [commitThreshold] via sliders.
 * The preview card updates in real time showing all visual properties.
 * An optional debug overlay displays raw float values — perfect for screenshots.
 *
 * MVVM role: VIEW. All values come pre-computed from [PlaygroundViewModel].
 * The Composable binds to values and sends intents. Zero math here.
 */
@Composable
fun PlaygroundScreen(
    onBack: () -> Unit,
    viewModel: PlaygroundViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // ── Top bar ───────────────────────────────────────────────────────────
        Row(
            modifier         = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilledTonalIconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(Modifier.weight(1f))
            Text(
                text  = "Playground",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.weight(1f))
            FilledTonalIconButton(
                onClick  = viewModel::onToggleDebugOverlay,
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    imageVector        = if (uiState.debugOverlayEnabled) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                    contentDescription = "Toggle debug overlay",
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Spacer(Modifier.height(4.dp))

            // ── Preview card ─────────────────────────────────────────────────
            PreviewCard(uiState = uiState)

            // ── Sliders ──────────────────────────────────────────────────────
            ControlsBlock(
                uiState           = uiState,
                onProgressChanged = viewModel::onProgressChanged,
                onThresholdChanged = viewModel::onThresholdChanged,
            )

            // ── Debug overlay ────────────────────────────────────────────────
            if (uiState.debugOverlayEnabled) {
                DebugOverlay(uiState = uiState)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Sub-composables
// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun PreviewCard(uiState: PlaygroundUiState) {
    val thresholdColor by animateColorAsState(
        targetValue   = if (uiState.isAboveThreshold) MaterialTheme.colorScheme.secondary
                        else                          MaterialTheme.colorScheme.primary,
        animationSpec = spring(),
        label         = "threshold_color",
    )

    Box(
        modifier         = Modifier
            .fillMaxWidth()
            .height(220.dp),
        contentAlignment = Alignment.Center,
    ) {
        // Scrim behind card
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.Black.copy(alpha = uiState.scrimAlpha))
        )

        // The morphing card preview
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.82f)
                .height(160.dp)
                .graphicsLayer {
                    scaleX    = uiState.cardScale
                    scaleY    = uiState.cardScale
                    alpha     = uiState.cardAlpha
                    rotationX = uiState.rotationX
                    rotationY = uiState.rotationY
                    rotationZ = uiState.rotationZ
                }
                .neoBlur(radius = uiState.blurRadiusDp.dp),
            shape  = RoundedCornerShape(20.dp),
            color  = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                thresholdColor.copy(alpha = 0.15f),
                                MaterialTheme.colorScheme.surface,
                            )
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text  = uiState.label,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = thresholdColor,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text  = "progress: ${"%.3f".format(uiState.progress)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun ControlsBlock(
    uiState: PlaygroundUiState,
    onProgressChanged: (Float) -> Unit,
    onThresholdChanged: (Float) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(20.dp),
        color    = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Text(
                text  = "Controls",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text  = "Use the scrubber to manually play the gesture animation in slow motion and watch the math happen.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))

            LabeledSlider(
                label        = "Simulate Swipe (Manual Scrubber)",
                value        = uiState.progress,
                onValueChange = onProgressChanged,
                valueRange   = 0f..1f,
                activeColor  = MaterialTheme.colorScheme.primary,
            )

            Spacer(Modifier.height(8.dp))

            LabeledSlider(
                label        = "Commit Threshold",
                value        = uiState.commitThreshold,
                onValueChange = onThresholdChanged,
                valueRange   = 0.3f..0.95f,
                activeColor  = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

@Composable
private fun LabeledSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    activeColor: Color,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text  = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text  = "%.2f".format(value),
                style = MaterialTheme.typography.labelLarge.copy(fontFamily = FontFamily.Monospace),
                color = activeColor,
            )
        }
        Slider(
            value        = value,
            onValueChange = onValueChange,
            valueRange   = valueRange,
            colors       = SliderDefaults.colors(
                thumbColor          = activeColor,
                activeTrackColor    = activeColor,
                inactiveTrackColor  = MaterialTheme.colorScheme.surfaceVariant,
            ),
        )
    }
}

@Composable
private fun DebugOverlay(uiState: PlaygroundUiState) {
    val rows = listOf(
        "progress"       to "%.3f".format(uiState.progress),
        "scale"          to "%.3f".format(uiState.cardScale),
        "alpha"          to "%.3f".format(uiState.cardAlpha),
        "rotationX"      to "%.2f°".format(uiState.rotationX),
        "rotationY"      to "%.2f°".format(uiState.rotationY),
        "rotationZ"      to "%.2f°".format(uiState.rotationZ),
        "blurRadius"     to "%.1fdp".format(uiState.blurRadiusDp),
        "glowAlpha"      to "%.3f".format(uiState.glowAlpha),
        "scrimAlpha"     to "%.3f".format(uiState.scrimAlpha),
        "threshold"      to "%.2f".format(uiState.commitThreshold),
        "aboveThreshold" to uiState.isAboveThreshold.toString(),
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        color    = Color(0xFF0D1117),  // GitHub dark — recognizable debug aesthetic
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Terminal-style header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                listOf(Color(0xFFFF5F57), Color(0xFFFFBD2E), Color(0xFF28C840)).forEach { dot ->
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(dot)
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text  = "neomotion debug",
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                    color = Color(0xFF8B949E),
                )
            }

            Spacer(Modifier.height(12.dp))

            rows.forEach { (key, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text  = key,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = Color(0xFF79C0FF),   // GitHub blue — key
                    )
                    Text(
                        text  = value,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = if (key == "aboveThreshold" && uiState.isAboveThreshold)
                                    Color(0xFF3FB950)   // GitHub green — threshold hit
                                else
                                    Color(0xFFF0883E),  // GitHub orange — value
                    )
                }
            }
        }
    }
}
