# Changelog

All notable changes to NeoMotion will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.0.0] — 2026-04-06

### Added
- **`:morphback`** — `MorphBackBox` composable wrapping `PredictiveBackHandler` with real-time `SeekableTransitionState` progress (`0f..1f`) for gesture-driven morph transitions
- **`:livejourney`** — Complete `Notification.ProgressStyle` (API 36) wrapper with:
  - `LiveJourneyState` data model with milestone segments
  - `LiveJourneyRepository` for StateFlow-driven sync between in-app UI and system notification
  - `LiveJourneyNotificationManager` with API 36 promoted notifications and API 26–35 graceful fallback
  - Runtime `POST_NOTIFICATIONS` permission handling
- **`:adaptivemotion`** — `DevicePosture` StateFlow for fold-aware layouts via `WindowInfoTracker`
- **`:identitymotion`** — `RestoreCredentialManager` wrapper for biometric and passkey flows
- **`:core`** — `NeoInterpolators` for gesture math, `NeoHaptics` for semantic haptic feedback
- **`:demo-app`** — Full reference implementation including:
  - Feed → Detail Shared Element transition with Predictive Back morphing
  - Live Journey 30-second deployment simulation
  - Gesture Playground with tunable easing curves
  - Premium neon UI with custom icons and 3D Glassmorphism launcher

### Notes
- Minimum SDK: API 26
- Targets: Android 16 (API 36)
- Namespace: `com.neoninnovationlab.neomotion`
