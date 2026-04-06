package com.neoninnovationlab.neomotion.livejourney.data

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable

/**
 * Models a single ongoing user journey (delivery, upload, ride, verification).
 *
 * This is the single source of truth for both:
 * - The in-app progress UI
 * - The system [Notification.ProgressStyle] notification
 *
 * MVVM role: MODEL. Pure data. No Android framework imports beyond annotations.
 *
 * @param id Unique stable ID for this journey. Used as the notification ID.
 * @param title Primary label (e.g. "Your ride is arriving").
 * @param subtitle Secondary detail (e.g. "Driver is 2 min away").
 * @param progress Current progress in [0f, 1f]. 1.0 = complete.
 * @param segments Named milestones along the journey.
 * @param status Current semantic status.
 * @param iconRes Small icon res ID for the notification.
 */
@Immutable
data class LiveJourneyState(
    val id: Int,
    val title: String,
    val subtitle: String = "",
    val progress: Float = 0f,
    val segments: List<JourneySegment> = emptyList(),
    val status: JourneyStatus = JourneyStatus.InProgress,
    @DrawableRes val iconRes: Int = android.R.drawable.ic_popup_sync,
)

/**
 * A named milestone point in a [LiveJourneyState].
 *
 * @param label Display name (e.g. "Pickup", "En Route", "Arrived").
 * @param reachedAt Progress value [0f, 1f] at which this segment is considered reached.
 */
@Immutable
data class JourneySegment(
    val label: String,
    val reachedAt: Float,
)

/**
 * Semantic status of the journey. Drives icon, color, and label in both
 * the in-app UI and the system notification.
 */
enum class JourneyStatus {
    InProgress,
    Completed,
    Cancelled,
    Error,
}
