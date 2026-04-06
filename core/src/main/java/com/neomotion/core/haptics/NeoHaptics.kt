package com.neoninnovationlab.neomotion.core.haptics

import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

/**
 * Centralized haptic language for NeoMotion.
 *
 * Maps semantic events to physical feedback types.
 * Keeps haptic decisions in one place — not scattered across Composables.
 *
 * MVVM role: Domain utility. Called by ViewModels via a [HapticFeedback] reference
 * passed as a parameter (not stored — avoids leaking Compose context into ViewModel).
 */
object NeoHaptics {

    /**
     * Fired on first contact with an interactive surface.
     * Very subtle — communicates "I see your touch".
     */
    fun onFirstContact(haptics: HapticFeedback) {
        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    /**
     * Fired when progress crosses a threshold boundary.
     * Communicates "you crossed a meaningful point".
     */
    fun onThresholdCrossed(haptics: HapticFeedback) {
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    /**
     * Fired when the user commits (releases at or beyond the threshold).
     * Communicates "action confirmed".
     */
    fun onCommit(haptics: HapticFeedback) {
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    /**
     * Fired on action success (e.g. auth success, upload complete).
     * Communicates "it worked".
     */
    fun onSuccess(haptics: HapticFeedback) {
        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    /**
     * Fired on action error.
     * Communicates "something went wrong".
     */
    fun onError(haptics: HapticFeedback) {
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    }
}
