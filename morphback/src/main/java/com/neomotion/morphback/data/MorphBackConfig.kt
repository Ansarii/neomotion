package com.neoninnovationlab.neomotion.morphback.data

import androidx.compose.runtime.Immutable

/**
 * Configuration for a MorphBack transition.
 *
 * Passed to [MorphBackBox] to control how the morph behaves.
 * Immutable — created once, never mutated.
 *
 * MVVM role: MODEL — pure configuration data.
 */
@Immutable
data class MorphBackConfig(
    /**
     * Commitment threshold in [0f, 1f].
     * If the user releases the back gesture below this point, it cancels.
     */
    val commitThreshold: Float = 0.72f,

    /**
     * Whether to fire haptic feedback at [hapticThresholds].
     */
    val hapticsEnabled: Boolean = true,

    /**
     * Progress checkpoints where haptic ticks fire.
     */
    val hapticThresholds: List<Float> = listOf(0.25f, 0.50f, 0.72f),

    /**
     * Corner radius morphing: start (detail screen = 0dp-ish) → end (card = 16dp).
     * Apply via Modifier.sharedBounds on both sides for a seamless card shape morph.
     */
    val startCornerRadius: Float = 0f,
    val endCornerRadius: Float = 16f,
)

/** Sensible defaults — zero config required for basic usage. */
val MorphBackDefaults = MorphBackConfig()
