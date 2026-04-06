package com.neoninnovationlab.neomotion.core.motion

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp as composeLerp

/**
 * Pure math utilities for mapping gesture progress to visual properties.
 *
 * MVVM role: Domain / utility layer. No UI, no state. Stateless functions only.
 * Used by ViewModels and Composables alike.
 *
 * All functions take [progress] in [0f, 1f] and output a mapped value.
 */
object NeoInterpolators {

    // A more "expensive" feeling easing curve (Emphasized/Standard Expressive)
    private val PremiumEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)

    /**
     * Maps progress linearly from [start] to [end].
     */
    fun lerp(start: Float, end: Float, progress: Float): Float =
        composeLerp(start, end, progress.coerceIn(0f, 1f))

    fun lerpDp(start: Dp, end: Dp, progress: Float): Dp =
        lerp(start.value, end.value, progress).dp

    fun lerpEased(
        start: Float,
        end: Float,
        progress: Float,
        easing: Easing = PremiumEasing
    ): Float = lerp(start, end, easing.transform(progress.coerceIn(0f, 1f)))

    /**
     * Maps progress to a card scale value.
     * Cards scale from 0.88 (deep resting) to 1.0 (committed).
     */
    fun cardScale(progress: Float): Float = lerpEased(0.88f, 1.00f, progress)

    /**
     * Maps progress to X-axis tilt (card "lifting" toward viewer).
     * Increased to 12° for a more pronounced 3D effect.
     */
    fun cardRotationX(progress: Float): Float = lerpEased(12f, 0f, progress)

    /**
     * Maps progress to Y-axis tilt (lateral swing).
     */
    fun cardRotationY(progress: Float): Float = lerpEased(-8f, 0f, progress)

    /**
     * Subtle Z-axis counter-tilt — makes the card feel "picked up".
     */
    fun cardRotationZ(progress: Float): Float = lerpEased(-5f, 0f, progress)

    /**
     * Z-axis depth (camera distance / projection).
     */
    fun cardCameraDistance(progress: Float): Float = lerp(8f, 12f, progress)

    /**
     * Alpha from transparent to fully visible with a late-fade curve.
     */
    fun cardAlpha(progress: Float): Float = lerpEased(0.0f, 1.0f, progress)

    /**
     * Blur radius in dp. Only meaningful on API 31+.
     * Maps from 32dp (heavy) to 0dp (sharp).
     */
    fun blurRadius(progress: Float): Dp = lerpDp(32.dp, 0.dp, progress)

    /**
     * Background scrim alpha — dims background as card comes forward.
     */
    fun backgroundScrimAlpha(progress: Float): Float = lerp(0f, 0.65f, progress)

    /**
     * Corner radius interpolation.
     */
    fun cornerRadius(progress: Float): Dp = lerpDp(24.dp, 0.dp, progress)

    /**
     * Glow / ambient light intensity.
     */
    fun glowAlpha(progress: Float): Float = lerpEased(0.1f, 0.8f, progress)

    /**
     * Haptic trigger points on the progress scale.
     */
    val hapticThresholds = listOf(0.15f, 0.40f, 0.70f, DEFAULT_COMMIT_THRESHOLD)
}
