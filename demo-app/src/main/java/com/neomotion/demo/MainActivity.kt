package com.neoninnovationlab.neomotion.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge          // Mandatory for API 36 targets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.neoninnovationlab.neomotion.demo.navigation.NeoMotionNavGraph
import com.neoninnovationlab.neomotion.demo.ui.theme.NeoMotionTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single Activity host for the NeoMotion demo app.
 *
 * Key points:
 * - [enableEdgeToEdge] is mandatory for apps targeting API 36 (Android 16).
 *   Without it, the system ignores any attempt to opt out.
 * - Hilt injects into this Activity via [@AndroidEntryPoint].
 * - Contains NO business logic. Delegates everything to Composables + ViewModels.
 *
 * MVVM role: VIEW root. Entry point only.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Must be called BEFORE super.onCreate for proper edge-to-edge setup
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            NeoMotionTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color    = MaterialTheme.colorScheme.background,
                ) {
                    val navController = rememberNavController()
                    NeoMotionNavGraph(navController = navController)
                }
            }
        }
    }
}
