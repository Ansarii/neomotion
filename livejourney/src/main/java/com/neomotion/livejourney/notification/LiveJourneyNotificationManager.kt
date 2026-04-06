package com.neoninnovationlab.neomotion.livejourney.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.neoninnovationlab.neomotion.livejourney.data.JourneyStatus
import com.neoninnovationlab.neomotion.livejourney.data.LiveJourneyState
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val CHANNEL_ID   = "neomotion_live_journey"
private const val CHANNEL_NAME = "Live Journeys"

private const val API_36 = 36

/**
 * Manages Android system notifications for [LiveJourneyState].
 *
 * On API 36+: uses [Notification.ProgressStyle] for promoted Live Updates
 *   (lock screen chip, status bar, top of shade).
 *
 * On API < 36: falls back to a standard determinate progress notification.
 */
@Singleton
class LiveJourneyNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val notificationManager: NotificationManager =
        context.getSystemService(NotificationManager::class.java)

    init {
        createChannel()
    }

    fun post(journey: LiveJourneyState) {
        if (Build.VERSION.SDK_INT >= API_36) {
            postProgressStyle(journey)
        } else {
            postLegacy(journey)
        }
    }

    fun cancel(journeyId: Int) {
        notificationManager.cancel(journeyId)
    }

    @RequiresApi(API_36)
    private fun postProgressStyle(journey: LiveJourneyState) {
        val progressStyle = Notification.ProgressStyle().apply {
            if (journey.segments.isNotEmpty()) {
                val segmentLength = 100 / journey.segments.size
                val segments = journey.segments.map { _ ->
                    Notification.ProgressStyle.Segment(segmentLength)
                }
                setProgressSegments(segments)
            }
            setProgress((journey.progress * 100).toInt())
            setProgressTrackerIcon(
                android.graphics.drawable.Icon.createWithResource(context, journey.iconRes)
            )
        }

        val isComplete = journey.status == JourneyStatus.Completed

        val builder = Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(journey.iconRes)
            .setContentTitle(journey.title)
            .setContentText(journey.subtitle.ifBlank { progressLabel(journey.progress) })
            .setOngoing(!isComplete)
            .setStyle(progressStyle)
            .setCategory(Notification.CATEGORY_PROGRESS)
            .setColor(0xFF00E5FF.toInt())

        if (isComplete) {
            builder.setContentText("Upload complete! \u2705")
            builder.setOngoing(false)
        }

        notificationManager.notify(journey.id, builder.build())
    }

    private fun postLegacy(journey: LiveJourneyState) {
        val progressInt = (journey.progress * 100).toInt()
        val isComplete = journey.status == JourneyStatus.Completed

        val builder = Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(journey.iconRes)
            .setContentTitle(journey.title)
            .setContentText(journey.subtitle.ifBlank { progressLabel(journey.progress) })
            .setProgress(100, progressInt, false)
            .setOngoing(!isComplete)
            .setCategory(Notification.CATEGORY_PROGRESS)
            .setColor(0xFF00E5FF.toInt())

        if (isComplete) {
            builder.setContentText("Upload complete! \u2705")
            builder.setProgress(0, 0, false)
            builder.setOngoing(false)
        }

        notificationManager.notify(journey.id, builder.build())
    }

    private fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Live progress updates for ongoing actions"
            enableVibration(false)
            setSound(null, null)
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun progressLabel(progress: Float): String =
        "${(progress * 100).toInt()}% complete"
}
