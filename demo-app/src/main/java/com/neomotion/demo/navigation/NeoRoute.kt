package com.neoninnovationlab.neomotion.demo.navigation

/**
 * Type-safe navigation route definitions.
 *
 * Using sealed class > string constants because:
 * - Compile-time route safety
 * - Easy to pass arguments (route + "/{id}" pattern)
 * - Single file to update when adding routes
 *
 * MVVM role: Navigation infrastructure. No UI or business logic.
 */
sealed class NeoRoute(val route: String) {
    data object Feed        : NeoRoute("feed")
    data object Detail      : NeoRoute("detail/{itemId}") {
        fun buildRoute(itemId: String) = "detail/$itemId"
    }
    data object LiveJourney : NeoRoute("live_journey")
    data object Playground  : NeoRoute("playground")
    data object Identity    : NeoRoute("identity")
}
