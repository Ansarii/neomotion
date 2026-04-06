package com.neoninnovationlab.neomotion.core.extensions

import android.os.Build
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * API-safe blur modifier.
 *
 * On API 31+: uses real GPU-accelerated [Modifier.blur].
 * On API < 31: falls back to a semi-transparent scrim overlay.
 *
 * MVVM role: View layer utility. Pure UI concern, no business logic.
 */
fun Modifier.neoBlur(
    radius: Dp,
    fallbackScrimColor: Color = Color.Black.copy(alpha = 0.40f),
    edgeTreatment: BlurredEdgeTreatment = BlurredEdgeTreatment.Rectangle,
): Modifier {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && radius > 0.dp) {
        this.blur(radius = radius, edgeTreatment = edgeTreatment)
    } else if (radius > 0.dp) {
        // Graceful fallback: alpha scrim that deepens proportionally to blur radius
        val scrimAlpha = (radius.value / 24f).coerceIn(0f, 0.55f)
        this.drawBehind {
            drawRect(fallbackScrimColor.copy(alpha = scrimAlpha))
        }
    } else {
        this
    }
}

/**
 * Conditionally applies a [Modifier] based on a [condition].
 * Prevents nesting `if` expressions inside Modifier chains in Composables.
 *
 * Usage:
 * ```kotlin
 * Modifier.conditional(isLoading) { alpha(0.5f) }
 * ```
 */
fun Modifier.conditional(condition: Boolean, modifier: Modifier.() -> Modifier): Modifier =
    if (condition) this.then(modifier()) else this
