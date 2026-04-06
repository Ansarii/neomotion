package com.neoninnovationlab.neomotion.demo.features.morphback

import androidx.lifecycle.ViewModel
import com.neoninnovationlab.neomotion.morphback.data.MorphBackConfig
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
 * Sealed interface for the Feed + Detail screen state.
 *
 * MVVM role: UI STATE MODEL. Drives exactly what the Composable renders.
 * One sealed type per logical screen state — no `isLoading: Boolean` hell.
 */
sealed interface FeedDetailUiState {

    /** Feed list is loading (first launch). */
    data object Loading : FeedDetailUiState

    /** Feed list is ready. No card is selected. */
    data class Feed(
        val items: List<FeedItem>,
    ) : FeedDetailUiState

    /**
     * A card was tapped and the detail is visible.
     *
     * [selectedItem] is the item currently expanded.
     * Navigation happens at the NavGraph level, but the selected item
     * is kept in the ViewModel so the shared element transition
     * has a stable reference across the navigation stack.
     */
    data class Detail(
        val items: List<FeedItem>,    // still needed so feed re-renders behind the shared element
        val selectedItem: FeedItem,
    ) : FeedDetailUiState
}

// ──────────────────────────────────────────────────────────────────────────────
// ViewModel
// ──────────────────────────────────────────────────────────────────────────────

/**
 * ViewModel for the Feed → Detail morphback demo.
 *
 * Responsibilities:
 * - Owns [FeedDetailUiState]
 * - Responds to user intents (select item, navigate back)
 * - Does NOT know about Composables, NavController, or animations
 *
 * MVVM role: VIEWMODEL. Survives configuration changes.
 * Injected by Hilt — no manual factory needed.
 */
@HiltViewModel
class FeedDetailViewModel @Inject constructor(
    private val morphBackConfigRepository: MorphBackConfigRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<FeedDetailUiState>(FeedDetailUiState.Loading)
    val uiState: StateFlow<FeedDetailUiState> = _uiState.asStateFlow()

    /** Live MorphBackConfig — updated by Playground, applied to DetailScreen gesture. */
    val morphConfig: StateFlow<MorphBackConfig> = morphBackConfigRepository.config

    init {
        loadFeed()
    }

    private fun loadFeed() {
        // In a real app: call a repository / use-case here
        _uiState.update { FeedDetailUiState.Feed(items = sampleFeedItems) }
    }

    /**
     * User tapped a card in the feed.
     * The Composable will navigate and use the MorphBackBox for the transition.
     */
    fun onItemSelected(item: FeedItem) {
        val currentItems = when (val s = _uiState.value) {
            is FeedDetailUiState.Feed   -> s.items
            is FeedDetailUiState.Detail -> s.items
            else                        -> sampleFeedItems
        }
        _uiState.update {
            FeedDetailUiState.Detail(items = currentItems, selectedItem = item)
        }
    }

    /**
     * User navigated back (either via system back or MorphBackBox commit).
     * Returns state to the feed.
     */
    fun onNavigateBack() {
        val currentItems = when (val s = _uiState.value) {
            is FeedDetailUiState.Detail -> s.items
            else                        -> sampleFeedItems
        }
        _uiState.update { FeedDetailUiState.Feed(items = currentItems) }
    }
}
