package com.neoninnovationlab.neomotion.demo.features.morphback

import com.neoninnovationlab.neomotion.demo.R

/**
 * Immutable data model for a single feed card.
 *
 * MVVM role: DATA MODEL. Pure data, zero logic.
 *
 * @param id             Unique stable key for Compose list diffing.
 * @param title          Card headline.
 * @param subtitle       Supporting text shown on both card and detail.
 * @param imageRes       Local drawable resource ID for the hero image.
 * @param accentColorHex Six-digit hex color (e.g. "#6750A4") used to tint the
 *                       detail screen accent elements.
 * @param tag            Short badge string shown on the card chip.
 */
data class FeedItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val imageRes: Int,
    val accentColorHex: String,
    val tag: String,
)

/**
 * Five curated demo items, one per NeoMotion feature.
 * Each maps to a dedicated [DetailScreen] interactive showcase.
 *
 * Drawable resource names match files in res/drawable/ (e.g. back_morph_hero.png).
 */
val sampleFeedItems: List<FeedItem> = listOf(
    FeedItem(
        id             = "1",
        title          = "Predictive Back Morph",
        subtitle       = "Real-time card transformation driven by your gesture progress.",
        imageRes       = R.drawable.back_morph_hero,
        accentColorHex = "#6750A4",
        tag            = "Gesture",
    ),
    FeedItem(
        id             = "2",
        title          = "Restore Credentials",
        subtitle       = "Zero-click login on a new device via OS-level backup.",
        imageRes       = R.drawable.identity_hero,
        accentColorHex = "#006A6A",
        tag            = "Identity",
    ),
    FeedItem(
        id             = "3",
        title          = "Motion Interpolators",
        subtitle       = "Scrub through premium easing curves in real time.",
        imageRes       = R.drawable.motion_hero,
        accentColorHex = "#7C4DFF",
        tag            = "Motion",
    ),
    FeedItem(
        id             = "4",
        title          = "Live Journey",
        subtitle       = "Notification.ProgressStyle on Android 16 — lock screen chip.",
        imageRes       = R.drawable.journey_hero,
        accentColorHex = "#006E1C",
        tag            = "Live",
    ),
    FeedItem(
        id             = "5",
        title          = "Adaptive Layouts",
        subtitle       = "WindowSizeClass and foldable posture detection.",
        imageRes       = R.drawable.adaptive_hero,
        accentColorHex = "#B5450B",
        tag            = "Adaptive",
    ),
)
