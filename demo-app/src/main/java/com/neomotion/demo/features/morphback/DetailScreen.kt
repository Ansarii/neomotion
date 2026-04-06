package com.neoninnovationlab.neomotion.demo.features.morphback

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope

import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.animation.core.animateDpAsState
import coil.compose.AsyncImage
import com.neoninnovationlab.neomotion.core.motion.NeoInterpolators
import com.neoninnovationlab.neomotion.morphback.ui.MorphBackBox

/**
 * Detail screen: full-screen expansion of a feed card.
 *
 * Uses [MorphBackBox] to intercept the predictive back gesture and
 * drive a real-time morph transition via [SeekableTransitionState].
 *
 * The visual mapping (scale, rotation, alpha) is computed by [NeoInterpolators]
 * and applied via [graphicsLayer] — no business logic in this composable.
 *
 * MVVM role: VIEW. Reads item by ID from [FeedDetailViewModel].
 * All visual math comes from [NeoInterpolators] (domain layer).
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.DetailScreen(
    itemId: String,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onBack: () -> Unit,
    onOpenIdentity: () -> Unit = {},
    onOpenPlayground: () -> Unit = {},
    onOpenLiveJourney: () -> Unit = {},
    viewModel: FeedDetailViewModel = hiltViewModel(),
) {
    val uiState   by viewModel.uiState.collectAsState()
    val morphConfig by viewModel.morphConfig.collectAsState()
    var simulatedMotionProgress by remember { mutableFloatStateOf(0f) }

    // Resolve the item from whichever state we're in
    val selectedItem: FeedItem? = when (val s = uiState) {
        is FeedDetailUiState.Detail -> s.selectedItem.takeIf { it.id == itemId }
            ?: s.items.find { it.id == itemId }
        is FeedDetailUiState.Feed   -> s.items.find { it.id == itemId }
        else                        -> null
    }

    // Fall back gracefully if item not found
    if (selectedItem == null) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
        return
    }

    val accentColor = runCatching {
        Color(android.graphics.Color.parseColor(selectedItem.accentColorHex))
    }.getOrDefault(MaterialTheme.colorScheme.primary)

    // MorphBackBox intercepts the system back gesture and provides real-time progress [0f..1f].
    // progress = 0f → detail fully visible
    // progress = 1f → back committed, card morphing back to list
    MorphBackBox(
        onBack = {
            viewModel.onNavigateBack()
            onBack()
        },
        config   = morphConfig,
        modifier = Modifier.fillMaxSize(),
    ) { progress ->
        // Animate corner radius: 0dp when fully open → 24dp when back is committed
        // Using animateDpAsState + Modifier.clip() is the reliable way to animate
        // shape clipping in Compose (graphicsLayer.shape does not animate reliably).
        val cornerRadius by animateDpAsState(
            targetValue   = NeoInterpolators.cornerRadius(1f - progress),
            animationSpec = spring(stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow),
            label         = "corner_radius",
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(cornerRadius))   // clip MUST come before graphicsLayer
                .graphicsLayer {
                    val fwd = 1f - progress
                    scaleX         = NeoInterpolators.cardScale(fwd)
                    scaleY         = NeoInterpolators.cardScale(fwd)
                    alpha          = NeoInterpolators.cardAlpha(fwd)
                    rotationX      = NeoInterpolators.cardRotationX(fwd)
                    rotationY      = NeoInterpolators.cardRotationY(fwd)
                    rotationZ      = NeoInterpolators.cardRotationZ(fwd)
                    cameraDistance = NeoInterpolators.cardCameraDistance(fwd)
                }
        ) {
            // Scrim behind the card — dims as progress increases (user going back)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color.Black.copy(alpha = NeoInterpolators.backgroundScrimAlpha(1f - progress))
                    )
            )

            // ── Main content ──────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
            ) {
                // Hero image section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .graphicsLayer {
                            if (simulatedMotionProgress > 0f) {
                                val p = simulatedMotionProgress
                                scaleX = com.neoninnovationlab.neomotion.core.motion.NeoInterpolators.cardScale(p)
                                scaleY = com.neoninnovationlab.neomotion.core.motion.NeoInterpolators.cardScale(p)
                                rotationX = com.neoninnovationlab.neomotion.core.motion.NeoInterpolators.cardRotationX(p)
                                rotationY = com.neoninnovationlab.neomotion.core.motion.NeoInterpolators.cardRotationY(p)
                                rotationZ = com.neoninnovationlab.neomotion.core.motion.NeoInterpolators.cardRotationZ(p)
                                cameraDistance = com.neoninnovationlab.neomotion.core.motion.NeoInterpolators.cardCameraDistance(p)
                            }
                        }
                        .sharedBounds(
                            sharedContentState = rememberSharedContentState(key = "card-${selectedItem.id}"),
                            animatedVisibilityScope = animatedVisibilityScope,
                            clipInOverlayDuringTransition = OverlayClip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                        )
                ) {
                    AsyncImage(
                        model              = selectedItem.imageRes,
                        contentDescription = selectedItem.title,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier
                            .fillMaxSize()
                            .sharedBounds(
                                sharedContentState = rememberSharedContentState(key = "image-${selectedItem.id}"),
                                animatedVisibilityScope = animatedVisibilityScope,
                            ),
                    )

                    // Gradient at bottom of hero
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colorStops = arrayOf(
                                        0.4f to Color.Transparent,
                                        1.0f to MaterialTheme.colorScheme.background,
                                    )
                                )
                            )
                    )

                    // Back button overlaid on hero
                    FilledTonalIconButton(
                        onClick  = {
                            viewModel.onNavigateBack()
                            onBack()
                        },
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(12.dp)
                            .size(40.dp),
                    ) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint               = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                // ── Text content ──────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    Spacer(Modifier.height(8.dp))

                    // Accent tag
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text  = "Feature Detail",
                            style = MaterialTheme.typography.labelMedium,
                            color = accentColor,
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text  = selectedItem.title,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground,
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text  = selectedItem.subtitle,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(Modifier.height(24.dp))

                    // Dynamic Feature Showcase
                    FeatureShowcase(
                        item = selectedItem, 
                        accentColor = accentColor, 
                        progress = progress,
                        onOpenIdentity = onOpenIdentity,
                        onOpenPlayground = onOpenPlayground,
                        onOpenLiveJourney = onOpenLiveJourney,
                        simulatedMotionProgress = simulatedMotionProgress,
                        onSimulateProgressChange = { simulatedMotionProgress = it },
                    )

                    Spacer(Modifier.height(40.dp))
                }

                Spacer(Modifier.windowInsetsPadding(WindowInsets.navigationBars))
            }
        }
    }
}

@Composable
private fun FeatureShowcase(
    item: FeedItem, 
    accentColor: Color, 
    progress: Float,
    onOpenIdentity: () -> Unit,
    onOpenPlayground: () -> Unit,
    onOpenLiveJourney: () -> Unit,
    simulatedMotionProgress: Float,
    onSimulateProgressChange: (Float) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        when (item.id) {
            "1" -> BackMorphDemo(accentColor, progress)
            "2" -> IdentityDemo(accentColor, onOpenIdentity)
            "3" -> MotionDemo(accentColor, simulatedMotionProgress, onSimulateProgressChange)
            "4" -> JourneyDemo(accentColor, onOpenLiveJourney)
            "5" -> AdaptiveDemo(accentColor)
        }
        Spacer(Modifier.height(8.dp))
        ApiDetailBlock(item = item, accentColor = accentColor)
    }
}

@Composable
private fun BackMorphDemo(accentColor: Color, progress: Float = 0f) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        // ── Gesture in progress live visualizer ───────────────────────────────
        // This updates in real time as the user performs the back gesture.
        // scale/rotationX/alpha come from the same NeoInterpolators used by the card.
        val fwd = 1f - progress
        val liveScale    = NeoInterpolators.cardScale(fwd)
        val liveRotX     = NeoInterpolators.cardRotationX(fwd)
        val liveAlpha    = NeoInterpolators.cardAlpha(fwd)
        val liveProgress = progress

        Text(
            text  = "How to experience this",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text  = "Swipe from the left edge of the screen (system back gesture). " +
                    "As you drag, the values below update live — " +
                    "you are seeing the actual numbers driving the animation.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // Live gesture progress bar
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text  = "Gesture progress",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text  = "%.2f".format(liveProgress),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    ),
                    color = accentColor,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(accentColor.copy(alpha = 0.15f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(liveProgress.coerceIn(0f, 1f))
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(accentColor)
                )
            }
        }

        // Live value readout
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            LiveValueRow("scale",      "%.3f".format(liveScale),    accentColor)
            LiveValueRow("rotationX",  "%.1f°".format(liveRotX),    accentColor)
            LiveValueRow("alpha",      "%.3f".format(liveAlpha),    accentColor)
        }

        Spacer(Modifier.height(8.dp))
        Text(
            text  = "You can also drag DOWN from the very top of this page (when the scroll position is 0) " +
                    "to trigger the same animation via overscroll.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // Extra content to make page scrollable so the user can test both:
        // normal scroll (should NOT trigger back) and overscroll at top (SHOULD).
        Spacer(Modifier.height(8.dp))
        Text(
            text  = "How MorphBackBox works",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text  = "Android 16 gives every app a PredictiveBackHandler that streams gesture events " +
                    "as a Kotlin Flow — one event per frame. Each event carries a progress value [0, 1]. " +
                    "MorphBackBox feeds this value into NeoInterpolators (cubic-bezier easing curves) " +
                    "and applies the result to a graphicsLayer. Your finger literally controls the animation frame by frame.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text  = "What you should see",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        listOf(
            "Card scales down from 1.0 to 0.88",
            "Card tilts forward on the X axis (up to 12°)",
            "Card rotates slightly on Y and Z axes",
            "Corners round from 0dp to 24dp",
            "Alpha fades toward 0 near commit threshold",
            "Haptic pulse when you cross 72% progress",
        ).forEach { line ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment     = Alignment.Top,
            ) {
                Text("•", color = accentColor, style = MaterialTheme.typography.bodySmall)
                Text(line, style = MaterialTheme.typography.bodySmall,
                     color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun LiveValueRow(label: String, value: String, accentColor: Color) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text  = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
            ),
            color = accentColor,
        )
    }
}

@Composable
private fun IdentityDemo(accentColor: Color, onOpenIdentity: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Seamless Auth", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            "NeoMotion wraps the Android Restore Credentials API. After signing in, a restore key " +
            "is backed up to the cloud. On a new device, the OS silently returns the key — " +
            "no login screen required. Use the Identity screen to see it live.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(accentColor.copy(alpha = 0.1f))
                .clickable { onOpenIdentity() }
                .padding(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Default.Fingerprint, contentDescription = null, tint = accentColor)
                Column {
                    Text("Tap this box", style = MaterialTheme.typography.labelLarge, color = accentColor)
                    Text("to open the 0-Click Restore Demo screen.",
                         style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun MotionDemo(
    accentColor: Color,
    simulatedProgress: Float,
    onProgressChange: (Float) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text  = "Simulate Motion Interpolator",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text  = "Drag the slider to see how the mathematical interpolators perfectly scale, tilt, and morph the top Hero Image in real-time.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        
        Spacer(Modifier.height(8.dp))
        
        Slider(
            value = simulatedProgress,
            onValueChange = onProgressChange,
            valueRange = 0f..1f,
        )
        
        Text(
            text = "Progress: ${"%.2f".format(simulatedProgress)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun JourneyDemo(accentColor: Color, onOpenLiveJourney: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Notification Progress API", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            "Android 16 allows apps to drive rich, live-updating progress segments directly in " +
            "system notifications. Let's start a simulated live journey and watch the UI and notification sync.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(accentColor.copy(alpha = 0.1f))
                .clickable { onOpenLiveJourney() }
                .padding(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = accentColor)
                Column {
                    Text("Tap this box", style = MaterialTheme.typography.labelLarge, color = accentColor)
                    Text("to open the Live Journey Demo screen.",
                         style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun AdaptiveDemo(accentColor: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Out of Scope for NeoMotion", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            "We intentionally did not build custom adaptive layout components into the NeoMotion library. " +
            "Google has already solved this domain beautifully with the stable androidx.compose.material3.adaptive library.\n\n" +
            "If you are building for foldables or tablets, you should use ListDetailPaneScaffold directly rather than reinventing the wheel.\n\n" +
            "NeoMotion's strict focus is on providing seamless access to brand new Android 16 interaction features.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Sub-composables
// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun ApiDetailBlock(item: FeedItem, accentColor: Color) {
    val apiDetails = mapOf(
        "SeekableTransitionState" to "Stable since Compose 1.7.0",
        "MorphBackBox"            to ":morphback module",
        "NeoInterpolators"        to "Progress → visual mapping",
        "SharedTransitionLayout"  to "Stable since Compose 1.7.0",
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text  = "APIs Used",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground,
        )

        apiDetails.forEach { (api, detail) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Text(
                    text  = api,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = accentColor,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text  = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Thin separator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
        }
    }
}
