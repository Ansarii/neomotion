package com.neoninnovationlab.neomotion.demo.features.playground

import androidx.lifecycle.ViewModel
import com.neoninnovationlab.neomotion.core.motion.NeoInterpolators
import com.neoninnovationlab.neomotion.demo.features.morphback.MorphBackConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

// ──────────────────────────────────────────────────────────────────────────────
// UI State
// ──────────────────────────────────────────────────────────────────────────────

/**
 * All tunable parameters for the gesture playground.
 *
 * Developers scrub the slider → ViewModel updates state → Composable re-renders.
 * This is the entire loop. The ViewModel computes derived properties so
 * the Composable stays dumb.
 *
 * MVVM role: UI STATE MODEL.
 */
data class PlaygroundUiState(
    val progress: Float = 0f,          // raw gesture progress [0f, 1f]
    val commitThreshold: Float = 0.72f,

    // Derived visual properties (computed by ViewModel from progress)
    val cardScale: Float     = 0.92f,
    val rotationX: Float     = 6f,
    val rotationY: Float     = -4f,
    val rotationZ: Float     = -3f,
    val cardAlpha: Float     = 0.75f,
    val blurRadiusDp: Float  = 20f,
    val glowAlpha: Float     = 0.1f,
    val scrimAlpha: Float    = 0f,

    // Label derived from progress
    val label: String = "Hold to begin",

    // Debug mode shows raw float values on screen
    val debugOverlayEnabled: Boolean = true,

    // Whether we've crossed the threshold
    val isAboveThreshold: Boolean = false,
)

// ──────────────────────────────────────────────────────────────────────────────
// ViewModel
// ──────────────────────────────────────────────────────────────────────────────

/**
 * ViewModel for the Gesture Playground screen.
 *
 * The developer adjusts sliders for [progress] and [commitThreshold].
 * All visual properties are computed here — the Composable only binds to values,
 * never computes them itself.
 *
 * This is the "developer-oriented debug tool" that will be the most viral
 * part of the demo app — devs will screenshot the debug overlay.
 *
 * MVVM role: VIEWMODEL. Owns all playground state.
 */
@HiltViewModel
class PlaygroundViewModel @Inject constructor(
    private val configRepository: MorphBackConfigRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlaygroundUiState())
    val uiState: StateFlow<PlaygroundUiState> = _uiState.asStateFlow()

    /**
     * Called every time the developer moves the progress slider.
     * Recomputes ALL derived visual values from the new [progress].
     */
    fun onProgressChanged(progress: Float) {
        val clamped  = progress.coerceIn(0f, 1f)
        val above    = clamped >= _uiState.value.commitThreshold

        _uiState.update {
            it.copy(
                progress         = clamped,
                cardScale        = NeoInterpolators.cardScale(clamped),
                rotationX        = NeoInterpolators.cardRotationX(clamped),
                rotationY        = NeoInterpolators.cardRotationY(clamped),
                rotationZ        = NeoInterpolators.cardRotationZ(clamped),
                cardAlpha        = NeoInterpolators.cardAlpha(clamped),
                blurRadiusDp     = NeoInterpolators.blurRadius(clamped).value,
                glowAlpha        = NeoInterpolators.glowAlpha(clamped),
                scrimAlpha       = NeoInterpolators.backgroundScrimAlpha(clamped),
                label            = labelForProgress(clamped, it.commitThreshold),
                isAboveThreshold = above,
            )
        }
    }

    fun onThresholdChanged(threshold: Float) {
        val clamped = threshold.coerceIn(0.3f, 0.95f)
        _uiState.update { it.copy(commitThreshold = clamped) }
        // Write to shared repository — DetailScreen MorphBackBox picks this up live.
        configRepository.updateCommitThreshold(clamped)
        // Recompute label immediately.
        onProgressChanged(_uiState.value.progress)
    }

    fun onToggleDebugOverlay() {
        _uiState.update { it.copy(debugOverlayEnabled = !it.debugOverlayEnabled) }
    }

    private fun labelForProgress(progress: Float, threshold: Float): String = when {
        progress < 0.15f -> "Hold to begin"
        progress < 0.45f -> "Slide to enter"
        progress < threshold -> "Keep going…"
        else             -> "Release to commit"
    }
}
