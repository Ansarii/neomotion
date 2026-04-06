package com.neoninnovationlab.neomotion.morphback.domain

import com.neoninnovationlab.neomotion.core.motion.DEFAULT_COMMIT_THRESHOLD
import com.neoninnovationlab.neomotion.core.motion.MotionPhase
import com.neoninnovationlab.neomotion.core.motion.MotionState
import com.neoninnovationlab.neomotion.morphback.data.MorphBackConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Domain-level state machine for a MorphBack gesture.
 *
 * This class owns:
 * - Gesture progress tracking
 * - Phase transitions (Idle → Seeking → Committed/Cancelled)
 * - Haptic threshold tracking
 *
 * NOT a ViewModel — it is a plain class owned BY a ViewModel.
 * This separation makes it unit-testable without Android dependencies.
 *
 * MVVM role: DOMAIN / USE CASE layer.
 */
class MorphBackStateMachine(
    private val config: MorphBackConfig = MorphBackConfig()
) {
    private val _state = MutableStateFlow(MotionState())
    val state: StateFlow<MotionState> = _state.asStateFlow()

    /** Tracks which haptic thresholds have already fired this gesture. */
    private val firedThresholds = mutableSetOf<Float>()

    /**
     * Called by the Composable's [PredictiveBackHandler] on each back event.
     * [fraction] is the raw gesture progress in [0f, 1f].
     *
     * @return list of haptic thresholds newly crossed — the ViewModel fires haptics from these.
     */
    fun onSeek(fraction: Float): List<Float> {
        val newlyFired = mutableListOf<Float>()

        if (config.hapticsEnabled) {
            for (threshold in config.hapticThresholds) {
                if (fraction >= threshold && threshold !in firedThresholds) {
                    firedThresholds.add(threshold)
                    newlyFired.add(threshold)
                }
            }
        }

        _state.update { it.copy(progress = fraction, phase = MotionPhase.Seeking) }
        return newlyFired
    }

    /**
     * Called when the back gesture is completed (user committed).
     * Returns true if progress was at or above the commit threshold.
     */
    fun onCommit(): Boolean {
        val shouldNavigate = _state.value.progress >= config.commitThreshold
        _state.update {
            it.copy(phase = if (shouldNavigate) MotionPhase.Committed else MotionPhase.Cancelled)
        }
        reset()
        return shouldNavigate
    }

    /**
     * Called when the back gesture is cancelled (user swiped back).
     */
    fun onCancel() {
        _state.update { it.copy(phase = MotionPhase.Cancelled) }
        reset()
    }

    private fun reset() {
        firedThresholds.clear()
        _state.update { MotionState() }
    }
}
