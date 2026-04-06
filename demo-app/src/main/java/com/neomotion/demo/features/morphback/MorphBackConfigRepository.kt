package com.neoninnovationlab.neomotion.demo.features.morphback

import com.neoninnovationlab.neomotion.morphback.data.MorphBackConfig
import com.neoninnovationlab.neomotion.morphback.data.MorphBackDefaults
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for MorphBackConfig across the entire app.
 *
 * The Playground screen writes to this (when user moves the threshold slider).
 * DetailScreen reads from this (so Playground tuning applies to real gestures).
 *
 * Injected as @Singleton so both ViewModels share the same instance.
 */
@Singleton
class MorphBackConfigRepository @Inject constructor() {

    private val _config = MutableStateFlow(MorphBackDefaults)
    val config: StateFlow<MorphBackConfig> = _config.asStateFlow()

    fun updateCommitThreshold(threshold: Float) {
        _config.update { it.copy(commitThreshold = threshold.coerceIn(0.3f, 0.95f)) }
    }

    fun reset() {
        _config.value = MorphBackDefaults
    }
}
