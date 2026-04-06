package com.neoninnovationlab.neomotion.demo.features.livejourney

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neoninnovationlab.neomotion.livejourney.data.JourneySegment
import com.neoninnovationlab.neomotion.livejourney.data.JourneyStatus
import com.neoninnovationlab.neomotion.livejourney.data.LiveJourneyState
import com.neoninnovationlab.neomotion.livejourney.domain.LiveJourneyRepository
import com.neoninnovationlab.neomotion.livejourney.notification.LiveJourneyNotificationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val DEMO_JOURNEY_ID = 1001

// ──────────────────────────────────────────────────────────────────────────────
// UI State
// ──────────────────────────────────────────────────────────────────────────────

/**
 * What the Live Journey screen renders.
 *
 * MVVM role: UI STATE MODEL.
 */
data class LiveJourneyUiState(
    val journey: LiveJourneyState? = null,
    val isRunning: Boolean = false,
    val notificationPosted: Boolean = false,
)

// ──────────────────────────────────────────────────────────────────────────────
// ViewModel
// ──────────────────────────────────────────────────────────────────────────────

/**
 * ViewModel for the Live Journey demo.
 *
 * Responsibilities:
 * - Drives a simulated upload/delivery progress
 * - Keeps in-app UI state and system notification in sync via [LiveJourneyRepository]
 * - Delegates all notification posting to [LiveJourneyNotificationManager]
 *
 * MVVM role: VIEWMODEL. Bridges domain (repository) with the UI.
 */
@HiltViewModel
class LiveJourneyViewModel @Inject constructor(
    private val repository: LiveJourneyRepository,
    private val notificationManager: LiveJourneyNotificationManager,
) : ViewModel() {

    val uiState = repository.journeys
        .map { journeys ->
            val journey = journeys[DEMO_JOURNEY_ID]
            LiveJourneyUiState(
                journey       = journey,
                isRunning     = journey?.status == JourneyStatus.InProgress,
                notificationPosted = journey != null,
            )
        }
        .stateIn(
            scope             = viewModelScope,
            started           = SharingStarted.WhileSubscribed(5_000),
            initialValue      = LiveJourneyUiState(),
        )

    private var simulationJob: Job? = null

    /**
     * Starts a simulated 10-second progress journey.
     * Updates [LiveJourneyRepository] every 200ms — which automatically
     * flows into both the in-app UI and the notification.
     */
    fun startJourney() {
        val journey = LiveJourneyState(
            id       = DEMO_JOURNEY_ID,
            title    = "Deploying to Production",
            subtitle = "Initializing…",
            progress = 0f,
            segments = listOf(
                JourneySegment("Scanning",    reachedAt = 0.15f),
                JourneySegment("Compressing", reachedAt = 0.30f),
                JourneySegment("Uploading",   reachedAt = 0.55f),
                JourneySegment("Verifying",   reachedAt = 0.70f),
                JourneySegment("Deploying",   reachedAt = 0.90f),
                JourneySegment("Live! 🚀",    reachedAt = 1.00f),
            ),
            iconRes  = android.R.drawable.ic_menu_upload,
        )
        repository.startJourney(journey)

        simulationJob = viewModelScope.launch {
            // Non-uniform pacing: slow start, fast middle, slow finish
            // Total ~30 seconds to give time to explore lock screen / status bar
            val stages = listOf(
                // (toProgress, delayPerStep, steps)
                Triple(0.15f, 400L, 15),  // Scanning  — slow ramp up
                Triple(0.30f, 300L, 15),  // Compressing
                Triple(0.55f, 200L, 25),  // Uploading  — fast bulk transfer
                Triple(0.70f, 350L, 15),  // Verifying  — careful
                Triple(0.90f, 250L, 20),  // Deploying
                Triple(1.00f, 500L, 10),  // Going live — dramatic slow finish
            )

            val subtitles = mapOf(
                0.02f to "🔍 Scanning project files…",
                0.15f to "📦 Compressing assets…",
                0.30f to "⬆️ Uploading to cloud · Lock your screen!",
                0.55f to "✅ Verifying integrity…",
                0.70f to "🚀 Deploying to production…",
                0.90f to "⚡ Going live…",
                1.00f to "🎉 Deployed successfully!",
            )

            var lastSubtitleThreshold = -1f
            var currentProgress = 0f

            for ((toProgress, stepDelay, steps) in stages) {
                val fromProgress = currentProgress
                val progressStep = (toProgress - fromProgress) / steps

                for (step in 1..steps) {
                    if (!isActive) break
                    currentProgress = (fromProgress + progressStep * step).coerceAtMost(1f)

                    repository.updateProgress(DEMO_JOURNEY_ID, currentProgress)

                    // Update subtitle at each milestone
                    subtitles.entries
                        .filter { it.key <= currentProgress && it.key > lastSubtitleThreshold }
                        .maxByOrNull { it.key }
                        ?.let {
                            repository.updateSubtitle(DEMO_JOURNEY_ID, it.value)
                            lastSubtitleThreshold = it.key
                        }

                    withContext(Dispatchers.IO) {
                        repository.getJourney(DEMO_JOURNEY_ID)?.let {
                            notificationManager.post(it)
                        }
                    }

                    delay(stepDelay)
                }
            }
        }
    }

    /** Cancels the running simulation and dismisses the notification. */
    fun cancelJourney() {
        simulationJob?.cancel()
        repository.cancelJourney(DEMO_JOURNEY_ID)
        notificationManager.cancel(DEMO_JOURNEY_ID)
        repository.removeJourney(DEMO_JOURNEY_ID)
    }

    override fun onCleared() {
        super.onCleared()
        simulationJob?.cancel()
    }
}
