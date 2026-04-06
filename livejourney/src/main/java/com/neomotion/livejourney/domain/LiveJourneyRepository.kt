package com.neoninnovationlab.neomotion.livejourney.domain

import com.neoninnovationlab.neomotion.livejourney.data.JourneyStatus
import com.neoninnovationlab.neomotion.livejourney.data.LiveJourneyState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository that owns all active [LiveJourneyState] instances.
 *
 * Multiple journeys can run simultaneously (e.g. a delivery AND an upload).
 * The notification layer and UI both observe from this single repository.
 *
 * MVVM role: REPOSITORY (Domain layer). Injected into ViewModels via Hilt.
 * Does NOT know about notifications, UI, or ViewModels — pure data management.
 */
@Singleton
class LiveJourneyRepository @Inject constructor() {

    private val _journeys = MutableStateFlow<Map<Int, LiveJourneyState>>(emptyMap())

    /** All currently active journeys, keyed by ID. */
    val journeys: StateFlow<Map<Int, LiveJourneyState>> = _journeys.asStateFlow()

    /** Starts a new journey or replaces an existing one with the same ID. */
    fun startJourney(journey: LiveJourneyState) {
        _journeys.update { current -> current + (journey.id to journey) }
    }

    /**
     * Updates the progress of an existing journey.
     * Clamps [progress] to [0f, 1f]. If reaching 1f, sets status to [JourneyStatus.Completed].
     */
    fun updateProgress(id: Int, progress: Float) {
        val clamped = progress.coerceIn(0f, 1f)
        _journeys.update { current ->
            val journey = current[id] ?: return
            val updatedStatus = if (clamped >= 1f) JourneyStatus.Completed else journey.status
            current + (id to journey.copy(progress = clamped, status = updatedStatus))
        }
    }

    /** Updates subtitle text (e.g. "Driver is 2 min away"). */
    fun updateSubtitle(id: Int, subtitle: String) {
        _journeys.update { current ->
            val journey = current[id] ?: return
            current + (id to journey.copy(subtitle = subtitle))
        }
    }

    /** Marks a journey as cancelled and removes it from active tracking after a brief delay. */
    fun cancelJourney(id: Int) {
        _journeys.update { current ->
            val journey = current[id] ?: return
            current + (id to journey.copy(status = JourneyStatus.Cancelled))
        }
    }

    /** Removes a journey entirely. Call after dismissing the notification and UI. */
    fun removeJourney(id: Int) {
        _journeys.update { current -> current - id }
    }

    /** Returns a specific journey, or null if not found. */
    fun getJourney(id: Int): LiveJourneyState? = _journeys.value[id]
}
