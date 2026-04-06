package com.neoninnovationlab.neomotion.morphback.ui

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Velocity
import com.neoninnovationlab.neomotion.core.haptics.NeoHaptics
import com.neoninnovationlab.neomotion.morphback.data.MorphBackConfig
import com.neoninnovationlab.neomotion.morphback.data.MorphBackDefaults
import com.neoninnovationlab.neomotion.morphback.domain.MorphBackStateMachine
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/**
 * A container that drives custom predictive back transitions via a real-time [Animatable].
 *
 * ## Two gesture sources
 *
 * 1. **System back gesture** (left-edge swipe): [PredictiveBackHandler] — always works,
 *    maps 0→1 as the user swipes right.
 *
 * 2. **Over-scroll drag** (slow downward drag when already at the top of content):
 *    [NestedScrollConnection.onPostScroll] with source == [NestedScrollSource.Drag].
 *
 *    Critical: we filter to DRAG source only, never FLING.
 *    - Normal scroll fling to top → Fling source → ignored here → no dismiss.
 *    - Slow intentional downward drag at top → Drag source → drives dismiss.
 *    This is what prevents "scroll up fast = goes back to main screen".
 */
@Composable
fun MorphBackBox(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    config: MorphBackConfig = MorphBackDefaults,
    enabled: Boolean = true,
    content: @Composable (progress: Float) -> Unit,
) {
    val stateMachine = remember { MorphBackStateMachine(config) }
    val progressAnim = remember { Animatable(0f) }
    val haptics      = LocalHapticFeedback.current
    val scope        = rememberCoroutineScope()

    // ── 1. System back gesture ────────────────────────────────────────────────
    PredictiveBackHandler(enabled = enabled) { backEventFlow ->
        try {
            backEventFlow.collect { backEvent ->
                progressAnim.snapTo(backEvent.progress)
                stateMachine.onSeek(backEvent.progress).forEach { t ->
                    when {
                        t >= config.commitThreshold -> NeoHaptics.onThresholdCrossed(haptics)
                        else -> NeoHaptics.onFirstContact(haptics)
                    }
                }
            }
            // Flow completed = OS released the back gesture.
            // We own this event entirely (enableOnBackInvokedCallback=true).
            // Only navigate if the user dragged past our threshold.
            // If they only dragged halfway and lifted — spring back, let them try again.
            if (progressAnim.value >= config.commitThreshold) {
                scope.launch {
                    progressAnim.animateTo(1f)
                    NeoHaptics.onCommit(haptics)
                    onBack()
                }
                stateMachine.onCommit()
            } else {
                // Not enough drag — cancel visually, user stays on screen.
                stateMachine.onCancel()
                scope.launch { progressAnim.animateTo(0f) }
            }
        } catch (_: CancellationException) {
            // User reversed direction before lifting — cancel.
            stateMachine.onCancel()
            scope.launch { progressAnim.animateTo(0f) }
        }
    }

    // ── 2. Over-scroll drag dismiss ───────────────────────────────────────────
    //
    // onPostScroll fires AFTER the child scroll consumes what it can.
    // available = unconsumed remainder.
    //
    // The source filter is the critical fix for "scroll up goes back":
    //
    //   source == Drag   → user is actively touching and dragging slowly.
    //                       If available.y > 0, they are dragging DOWN while already at
    //                       the top → intentional dismiss gesture → accumulate progress.
    //
    //   source == Fling  → momentum after the user lifted their finger.
    //                       A fast fling TO the top of content also produces available.y > 0
    //                       here (the leftover momentum that can't scroll further up).
    //                       We must NOT accumulate this as dismiss progress — it's just the
    //                       user scrolling back to the top normally. Ignored.
    //
    // onPostFling: if the user built up some drag progress and then flings,
    // commit if past threshold, otherwise always snap back.
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                // Only accumulate during an active touch drag, never during fling momentum.
                if (!enabled
                    || available.y <= 0f
                    || source != NestedScrollSource.Drag
                ) return Offset.Zero

                // available.y > 0: user is dragging DOWN while content can't scroll further up.
                // Drive dismiss. 800px for full 0→1 travel (needs a deliberate downward pull).
                val sensitivity   = 800f
                val newProgress   = (progressAnim.value + available.y / sensitivity).coerceIn(0f, 1f)
                scope.launch {
                    progressAnim.snapTo(newProgress)
                    stateMachine.onSeek(newProgress).forEach { t ->
                        if (t >= config.commitThreshold) NeoHaptics.onThresholdCrossed(haptics)
                    }
                }
                return available.copy(x = 0f)
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (!enabled) return Velocity.Zero
                // If user's slow drag built up enough progress, commit. Otherwise snap back.
                if (progressAnim.value >= config.commitThreshold) {
                    scope.launch {
                        progressAnim.animateTo(1f)
                        NeoHaptics.onCommit(haptics)
                        onBack()
                    }
                } else if (progressAnim.value > 0f) {
                    stateMachine.onCancel()
                    scope.launch { progressAnim.animateTo(0f) }
                }
                return Velocity.Zero
            }
        }
    }

    Box(modifier = modifier.nestedScroll(nestedScrollConnection)) {
        content(progressAnim.value)
    }
}
