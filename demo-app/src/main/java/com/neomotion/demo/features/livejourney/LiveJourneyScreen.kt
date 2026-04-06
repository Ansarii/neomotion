package com.neoninnovationlab.neomotion.demo.features.livejourney

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.neoninnovationlab.neomotion.livejourney.data.JourneySegment
import com.neoninnovationlab.neomotion.livejourney.data.JourneyStatus
import com.neoninnovationlab.neomotion.livejourney.data.LiveJourneyState

@Composable
fun LiveJourneyScreen(
    onBack: () -> Unit,
    viewModel: LiveJourneyViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    // Runtime permission request for POST_NOTIFICATIONS (required on API 33+)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.startJourney()
        }
    }

    val startWithPermission: () -> Unit = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            viewModel.startJourney()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // ── Top bar ───────────────────────────────────────────────────────────
        Row(
            modifier         = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilledTonalIconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(Modifier.weight(1f))
            Text(
                text  = "Live Journey",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.weight(1f))
            // Invisible spacer to center the title
            Box(Modifier.size(40.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement   = Arrangement.spacedBy(20.dp),
        ) {
            Spacer(Modifier.height(8.dp))

            // ── Explainer ─────────────────────────────────────────────────────
            ApiCallout()

            // ── Journey Card ─────────────────────────────────────────────────
            JourneyCard(
                uiState   = uiState,
                onStart   = startWithPermission,
                onCancel  = viewModel::cancelJourney,
            )

            // ── WOW moment instructions — shown only while running ────────────
            if (uiState.isRunning) {
                WowMomentCard()
            }

            // ── How it works ─────────────────────────────────────────────────
            HowItWorksBlock()
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Sub-composables
// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun WowMomentCard() {
    val wowSteps = listOf(
        "🔒" to "Lock your screen now",
        "👀" to "See the live chip on the lock screen — updating in real time",
        "📲" to "Unlock and pull down the status bar — watch the progress pill",
        "🔔" to "It's at the TOP of your notification shade — above everything",
    )
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        color    = Color(0xFF003322),
        tonalElevation = 0.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("✦", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.size(8.dp))
                Text(
                    text  = "Experience Android 16 Live Updates",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF00E5FF),
                )
            }
            Spacer(Modifier.height(12.dp))
            wowSteps.forEach { (emoji, step) ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(emoji, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text  = step,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = Color(0xFFB2DFDB),
                    )
                }
            }
        }
    }
}

@Composable
private fun ApiCallout() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        color    = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text  = "Notification.ProgressStyle",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.secondary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text  = "New in Android 16 (API 36). Promotes live updates to lock screen, " +
                        "status bar chip, and top of notification shade.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

@Composable
private fun JourneyCard(
    uiState: LiveJourneyUiState,
    onStart: () -> Unit,
    onCancel: () -> Unit,
) {
    val journey     = uiState.journey
    val isRunning   = uiState.isRunning
    val isDone      = journey?.status == JourneyStatus.Completed
    val progress    = journey?.progress ?: 0f

    val animatedProgress by animateFloatAsState(
        targetValue   = progress,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label         = "journey_progress",
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(20.dp),
        color    = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            // ── Status icon + title ─────────────────────────────────────────
            Row(
                verticalAlignment    = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier          = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (isDone) Color(0xFF006E1C).copy(alpha = 0.15f)
                            else        MaterialTheme.colorScheme.primaryContainer
                        ),
                    contentAlignment  = Alignment.Center,
                ) {
                    Icon(
                        imageVector        = if (isDone) Icons.Filled.CheckCircle else Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint               = if (isDone) Color(0xFF006E1C)
                                            else        MaterialTheme.colorScheme.primary,
                        modifier           = Modifier.size(22.dp),
                    )
                }

                Column {
                    AnimatedContent(
                        targetState    = journey?.title ?: "Ready to start",
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label          = "journey_title",
                    ) { title ->
                        Text(
                            text  = title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    AnimatedContent(
                        targetState    = journey?.subtitle ?: "Tap Start to begin the simulation",
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label          = "journey_subtitle",
                    ) { subtitle ->
                        Text(
                            text  = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            if (journey != null) {
                Spacer(Modifier.height(16.dp))

                // ── Progress bar ──────────────────────────────────────────
                LinearProgressIndicator(
                    progress       = { animatedProgress },
                    modifier       = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color          = if (isDone) Color(0xFF006E1C)
                                    else        MaterialTheme.colorScheme.primary,
                    trackColor     = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap      = StrokeCap.Round,
                )

                Spacer(Modifier.height(8.dp))

                // ── Segment markers ───────────────────────────────────────
                SegmentRow(
                    segments = journey.segments,
                    progress = animatedProgress,
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text  = "${(animatedProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(Modifier.height(20.dp))

            // ── Action buttons ────────────────────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (!isRunning && !isDone) {
                    Button(
                        onClick  = onStart,
                        modifier = Modifier.weight(1f),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(6.dp))
                        Text("Start")
                    }
                }

                if (isRunning) {
                    OutlinedButton(
                        onClick  = onCancel,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Cancel")
                    }
                }

                if (isDone) {
                    Button(
                        onClick  = {
                            onCancel()
                        },
                        modifier = Modifier.weight(1f),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        ),
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(6.dp))
                        Text("Reset")
                    }
                }
            }
        }
    }
}

@Composable
private fun SegmentRow(segments: List<JourneySegment>, progress: Float) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        segments.forEach { segment ->
            val reached = progress >= segment.reachedAt
            Text(
                text  = segment.label,
                style = MaterialTheme.typography.labelSmall,
                color = if (reached) MaterialTheme.colorScheme.primary
                        else         MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                fontWeight = if (reached) FontWeight.Bold else FontWeight.Normal,
            )
        }
    }
}

@Composable
private fun HowItWorksBlock() {
    val steps = listOf(
        "1" to "startJourney() creates a LiveJourneyState with segments",
        "2" to "LiveJourneyRepository broadcasts updates as StateFlow",
        "3" to "LiveJourneyViewModel collects state and posts notification",
        "4" to "Notification.ProgressStyle promotes to lock screen chip",
        "5" to "In-app UI and notification stay in perfect sync",
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text  = "How it works",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(8.dp))
            steps.forEach { (number, step) ->
                Row(
                    modifier = Modifier.padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text  = number,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Text(
                        text  = step,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
