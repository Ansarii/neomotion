package com.neoninnovationlab.neomotion.demo.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.neoninnovationlab.neomotion.demo.features.livejourney.LiveJourneyScreen
import com.neoninnovationlab.neomotion.demo.features.morphback.FeedScreen
import com.neoninnovationlab.neomotion.demo.features.morphback.DetailScreen
import com.neoninnovationlab.neomotion.demo.features.playground.PlaygroundScreen
import com.neoninnovationlab.neomotion.demo.features.identity.SilentLoginScreen

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun NeoMotionNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    SharedTransitionLayout(modifier = modifier) {
        NavHost(
            navController    = navController,
            startDestination = NeoRoute.Feed.route,
            // We use simple crossfades for the non-shared pieces so the shared elements pop perfectly
            enterTransition  = { fadeIn() },
            exitTransition   = { fadeOut() },
            popEnterTransition = { fadeIn() },
            popExitTransition  = { fadeOut() },
        ) {
            composable(NeoRoute.Feed.route) {
                FeedScreen(
                    animatedVisibilityScope = this,
                    onItemClick = { item ->
                        navController.navigate(NeoRoute.Detail.buildRoute(item.id))
                    },
                    onOpenLiveJourney = {
                        navController.navigate(NeoRoute.LiveJourney.route)
                    },
                    onOpenPlayground = {
                        navController.navigate(NeoRoute.Playground.route)
                    },
                    onOpenIdentity = {
                        navController.navigate(NeoRoute.Identity.route)
                    }
                )
            }

            composable(
                route     = NeoRoute.Detail.route,
                arguments = listOf(navArgument("itemId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val itemId = backStackEntry.arguments?.getString("itemId") ?: return@composable
                DetailScreen(
                    itemId = itemId,
                    animatedVisibilityScope = this,
                    onBack = { navController.popBackStack() },
                    onOpenIdentity = { navController.navigate(NeoRoute.Identity.route) },
                    onOpenPlayground = { navController.navigate(NeoRoute.Playground.route) },
                    onOpenLiveJourney = { navController.navigate(NeoRoute.LiveJourney.route) },
                )
            }

            composable(NeoRoute.LiveJourney.route) {
                LiveJourneyScreen(onBack = { navController.popBackStack() })
            }

            composable(NeoRoute.Playground.route) {
                PlaygroundScreen(onBack = { navController.popBackStack() })
            }

            composable(NeoRoute.Identity.route) {
                SilentLoginScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
