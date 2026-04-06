package com.neoninnovationlab.neomotion.core.motion

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

/**
 * Represents the complete state of a NeoMotion gesture interaction.
 *
 * [progress] drives ALL visual properties (scale, blur, rotation, alpha, etc.)
 * Everything in NeoMotion maps from a single Float in [0f, 1f].
 *
 * MVVM role: This is the MODEL layer — pure data, no UI dependencies.
 */
@Immutable
data class MotionState(
    /** Gesture progress from 0.0 (idle) to 1.0 (committed). */
    val progress: Float = 0f,
    /** Raw finger velocity in px/s. Positive = forward, negative = reversing. */
    val velocity: Float = 0f,
    /** Which phase the gesture is in. */
    val phase: MotionPhase = MotionPhase.Idle,
)

/**
 * The lifecycle phase of any NeoMotion gesture.
 */
@Stable
sealed interface MotionPhase {
    /** No interaction. Progress is 0f. */
    data object Idle : MotionPhase

    /** User is actively dragging. Progress changes continuously. */
    data object Seeking : MotionPhase

    /**
     * User crossed the commitment threshold and released.
     * Navigation / auth / action will execute.
     */
    data object Committed : MotionPhase

    /**
     * User released before the threshold, or cancelled.
     * Progress is animating back to 0f.
     */
    data object Cancelled : MotionPhase
}

/** Default commitment threshold. Cross this and the action fires. */
const val DEFAULT_COMMIT_THRESHOLD = 0.72f
