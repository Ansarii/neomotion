package com.neoninnovationlab.neomotion.adaptivemotion

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import androidx.window.core.layout.WindowWidthSizeClass
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import kotlinx.coroutines.flow.map

/**
 * Describes the current device posture for foldable-aware layouts.
 *
 * MVVM role: MODEL. Emitted by [rememberPosture].
 */
enum class DevicePosture {
    /** Normal phone or flat tablet. Single pane layout. */
    Normal,
    /** Device folded to ~90°. Top half = content, bottom half = controls. */
    Tabletop,
    /** Device is a large screen (unfolded foldable or big tablet). Two panes. */
    LargeScreen,
}

/**
 * Observes the current device posture using [WindowInfoTracker].
 * Recomposes whenever the fold state changes (e.g. user partially folds device).
 *
 * MVVM role: VIEW layer utility composable (state producer).
 * Can be hoisted to ViewModel via a use-case if needed.
 *
 * Based on verified Jetpack WindowManager API (androidx.window:window:1.3.0).
 * [WindowWidthSizeClass] is from androidx.window.core.layout (stable in window 1.3+).
 */
@Composable
fun rememberDevicePosture(): DevicePosture {
    val context      = LocalContext.current
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val isExpanded   = adaptiveInfo.windowSizeClass.windowWidthSizeClass ==
                           WindowWidthSizeClass.EXPANDED

    // Observe FoldingFeature from WindowInfoTracker
    val posture by produceState(initialValue = DevicePosture.Normal) {
        WindowInfoTracker
            .getOrCreate(context)
            .windowLayoutInfo(context)
            .map { layoutInfo ->
                val fold = layoutInfo.displayFeatures
                    .filterIsInstance<FoldingFeature>()
                    .firstOrNull()

                when {
                    // Half-opened in tabletop orientation
                    fold?.state == FoldingFeature.State.HALF_OPENED &&
                    fold.orientation == FoldingFeature.Orientation.HORIZONTAL ->
                        DevicePosture.Tabletop

                    // Large screen (expanded width class) or fully flat foldable
                    isExpanded || fold?.state == FoldingFeature.State.FLAT ->
                        DevicePosture.LargeScreen

                    else -> DevicePosture.Normal
                }
            }
            .collect { value = it }
    }
    return posture
}
