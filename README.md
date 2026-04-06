# NeoMotion 💫

[![API](https://img.shields.io/badge/API-26%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=26)
[![Android 16 Ready](https://img.shields.io/badge/Android-16%20(API%2036)-00D4C8.svg?logo=android)](https://developer.android.com/about/versions/16)
[![Compose](https://img.shields.io/badge/Compose-1.7.0%2B-blue.svg?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![JitPack](https://img.shields.io/badge/JitPack-Ready-7C5CBF.svg)](https://jitpack.io)

**NeoMotion** is a modular, production-ready Jetpack Compose library that implements advanced, platform-aligned interaction patterns for Modern Android (specifically targeting Android 16 / API 36).

Instead of reinventing the wheel, NeoMotion wraps real, stable platform APIs into clean Compose-first interfaces with a premium, high-end aesthetic.

---

## 🚀 Features

### 1. `morphback` — Real-time Predictive Back Morphing
Android 16 enforces Predictive Back. `MorphBackBox` intercepts the system back gesture and translates it into a real-time `SeekableTransitionState` progress value (`0f` to `1f`).
- Bind gesture progress directly to scale, rotation, blur, and alpha.
- Perfect for Card-to-Detail Shared Element transitions.
- Fully compatible with Compose Navigation `2.8.0+`.

### 2. `livejourney` — Promoted Live Updates
A complete wrapper around Android 16's new `Notification.ProgressStyle`.
- **Experience Android 16 Live Updates**: Promotes ongoing actions (uploads, rides, deployments) to the Lock Screen chip, Status Bar pill, and top of the notification shade.
- **Dynamic Pacing**: Realistic simulation of deployment stages (Scanning, Uploading, Verifying, Deploying).
- **Graceful Fallback**: Automatically degrades to standard determinate progress notifications for APIs 26–35.
- **Permission Safe**: Built-in handling for the mandatory `POST_NOTIFICATIONS` runtime requirement.

### 3. `adaptivemotion` — Fold-Aware Behaviors
Utilities for `WindowInfoTracker` and `WindowWidthSizeClass`.
- `DevicePosture` state flow (Tabletop, LargeScreen, Normal) to effortlessly adapt layouts when users fold or unfold their devices.

### 4. Premium Iconography & UX
- **3D Glassmorphism Launcher Icon**: A state-of-the-art launcher experience.
- **Neon Glowing UI**: Custom-crafted neon icons optimized for modern dark themes.

---

## 📦 Modules & Installation

NeoMotion is highly modular. You only include what you need.

**Step 1:** Add JitPack to your `settings.gradle.kts`:
```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

**Step 2:** Add the modules you need to your `build.gradle.kts`:
```kotlin
dependencies {
    // Shared motion math, interpolators, and haptics
    implementation("com.github.neoninnovationlab.neomotion:core:1.0.0")
    
    // Predictive back morphing transitions
    implementation("com.github.neoninnovationlab.neomotion:morphback:1.0.0")
    
    // API 36 Notification.ProgressStyle wrappers
    implementation("com.github.neoninnovationlab.neomotion:livejourney:1.0.0")
    
    // Foldable and adaptive layout utilities
    implementation("com.github.neoninnovationlab.neomotion:adaptivemotion:1.0.0")
}
```

---

## 🎮 The Demo App & Playground
Check out the `:demo-app` module to see these features in action. It includes:
1. **Feed list to detail Shared Element transition** that unwinds realistically as you swipe back.
2. A **Live Journey Simulation**: A 30-second realistic deployment journey showing in-app progress perfectly synced to a system notification (Lock screen & Status Bar).
3. **Identity & Playground**: Real-world entry points for biometric login patterns and gesture tuning sandbox.

---

## 🏗 Architecture
NeoMotion follows a strict **MVVM Model**:
* **Presentation Layer:** Lean Composables (`MorphBackBox`). No complex UI state calculations.
* **Domain Layer:** Pure Kotlin mathematics (`NeoInterpolators`) handling gesture commits, cancel thresholds, and visual lerping.
* **Infrastructure Layer:** `LiveJourneyNotificationManager` abstracts away `SDK_INT` version checks and `ProgressStyle` implementation securely.

---

## ✦ Crafted by Neon Innovation Lab
Pioneering next-gen Android experiences.
- **Website**: [neoninnovationlab.com](https://www.neoninnovationlab.com/)
- **Contact**: hello@neoninnovationlab.com

## 📝 License
Apache License 2.0
