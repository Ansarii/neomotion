package com.neoninnovationlab.neomotion.demo.features.morphback

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalUriHandler
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage

/**
 * Feed screen: a scrollable list of cards, each representing a NeoMotion feature.
 *
 * Tapping a card navigates to [DetailScreen] where the back morph transition runs.
 *
 * MVVM role: VIEW. Reads [FeedDetailUiState] from [FeedDetailViewModel].
 * Computes nothing. Calls ViewModel intents on user actions.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.FeedScreen(
    animatedVisibilityScope: AnimatedVisibilityScope,
    onItemClick: (FeedItem) -> Unit,
    onOpenLiveJourney: () -> Unit,
    onOpenPlayground: () -> Unit,
    onOpenIdentity: () -> Unit,
    viewModel: FeedDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        topBar = {
            @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { 
                    androidx.compose.foundation.layout.Column {
                        Text("NeoMotion", style = MaterialTheme.typography.titleLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold))
                        Text("Android 16 Motion Patterns", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    androidx.compose.material3.IconButton(onClick = onOpenIdentity) {
                        Image(
                            painter = painterResource(id = com.neoninnovationlab.neomotion.demo.R.drawable.ic_neon_fingerprint),
                            contentDescription = "Identity",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0D1117), CircleShape)
                                .border(1.dp, Color(0xFF00E5FF), CircleShape)
                                .padding(4.dp)
                        )
                    }
                    androidx.compose.material3.IconButton(onClick = onOpenPlayground) {
                        Image(
                            painter = painterResource(id = com.neoninnovationlab.neomotion.demo.R.drawable.ic_neon_settings),
                            contentDescription = "Playground",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0D1117), CircleShape)
                                .border(1.dp, Color(0xFF00E5FF), CircleShape)
                                .padding(4.dp)
                        )
                    }
                    androidx.compose.material3.IconButton(onClick = onOpenLiveJourney) {
                        Image(
                            painter = painterResource(id = com.neoninnovationlab.neomotion.demo.R.drawable.ic_neon_play),
                            contentDescription = "Live Journey",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0D1117), CircleShape)
                                .border(1.dp, Color(0xFF00E5FF), CircleShape)
                                .padding(4.dp)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (val state = uiState) {
                is FeedDetailUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                is FeedDetailUiState.Feed, is FeedDetailUiState.Detail -> {
                    val items = when (state) {
                        is FeedDetailUiState.Feed   -> state.items
                        is FeedDetailUiState.Detail -> state.items
                        else                        -> emptyList()
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(items, key = { it.id }) { item ->
                            FeedCard(
                                item    = item,
                                animatedVisibilityScope = animatedVisibilityScope,
                                onClick = {
                                    viewModel.onItemSelected(item)
                                    onItemClick(item)
                                },
                            )
                        }

                        item {
                            // ── Neon Innovation Lab Credits ──
                            val uriHandler = LocalUriHandler.current
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 24.dp, bottom = 40.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Divider line
                                Box(
                                    modifier = Modifier
                                        .width(60.dp)
                                        .height(2.dp)
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Color(0xFF00E5FF), Color(0xFFBB86FC))
                                            ),
                                            RoundedCornerShape(1.dp)
                                        )
                                )

                                Spacer(Modifier.height(16.dp))

                                Text(
                                    text = "Crafted with \u2764\uFE0F by",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(Modifier.height(4.dp))

                                Text(
                                    text = "Neon Innovation Lab",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = Color(0xFF00E5FF)
                                )

                                Spacer(Modifier.height(8.dp))

                                Text(
                                    text = "Pioneering next-gen Android experiences",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(Modifier.height(12.dp))

                                // Website link
                                Text(
                                    text = "neoninnovationlab.com",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        textDecoration = TextDecoration.Underline
                                    ),
                                    color = Color(0xFFBB86FC),
                                    modifier = Modifier.clickable {
                                        uriHandler.openUri("https://www.neoninnovationlab.com/")
                                    }
                                )

                                Spacer(Modifier.height(4.dp))

                                // Email
                                Text(
                                    text = "hello@neoninnovationlab.com",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.clickable {
                                        uriHandler.openUri("mailto:hello@neoninnovationlab.com")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Sub-composables
// ──────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.FeedCard(
    item: FeedItem,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accentColor = runCatching {
        Color(android.graphics.Color.parseColor(item.accentColorHex))
    }.getOrDefault(MaterialTheme.colorScheme.primary)

    androidx.compose.material3.Card(
        modifier = modifier
            .fillMaxWidth()
            .sharedBounds(
                sharedContentState = rememberSharedContentState(key = "card-${item.id}"),
                animatedVisibilityScope = animatedVisibilityScope,
                clipInOverlayDuringTransition = OverlayClip(androidx.compose.foundation.shape.RoundedCornerShape(20.dp))
            )
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape  = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box {
            // Hero image
            coil.compose.AsyncImage(
                model              = item.imageRes,
                contentDescription = item.title,
                contentScale       = androidx.compose.ui.layout.ContentScale.Crop,
                modifier           = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .sharedBounds(
                        sharedContentState = rememberSharedContentState(key = "image-${item.id}"),
                        animatedVisibilityScope = animatedVisibilityScope,
                    ),
            )

            // Gradient overlay so text is always readable
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                accentColor.copy(alpha = 0.85f),
                            )
                        )
                    )
            )

            // Text content over image
            androidx.compose.foundation.layout.Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text  = item.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                    color = Color.White,
                )
                Text(
                    text  = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f),
                )
            }

            // Accent dot top-right
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(10.dp)
                    .background(accentColor, androidx.compose.foundation.shape.CircleShape)
            )
        }
    }
}
