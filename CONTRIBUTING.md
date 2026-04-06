# Contributing to NeoMotion

Thank you for your interest in contributing to NeoMotion! This document outlines how to set up your environment and submit changes.

## Getting Started

1. **Fork** the repository on GitHub
2. **Clone** your fork: `git clone https://github.com/Ansarii/neomotion.git`
3. Open in Android Studio Ladybug or later
4. Run the demo app on an Android 16 (API 36) emulator or device for full feature coverage

## Project Structure

| Module | Purpose |
|--------|---------|
| `:core` | Shared interpolators, haptics, and motion math |
| `:morphback` | Predictive Back morphing transitions |
| `:livejourney` | `Notification.ProgressStyle` wrappers |
| `:adaptivemotion` | Fold-aware adaptive layout utilities |
| `:identitymotion` | Biometric/CredentialManager patterns |
| `:demo-app` | Reference implementation and interactive playground |

## Submitting a Pull Request

1. Create a branch: `git checkout -b feature/your-feature-name`
2. Make your changes, keeping them focused on a single concern
3. Ensure the project builds clean: `./gradlew build`
4. Write or update tests where applicable
5. Commit with a descriptive message following [Conventional Commits](https://www.conventionalcommits.org/)
6. Open a Pull Request against `main` with a clear description

## Code Style

- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use `@OptIn` for experimental Compose APIs
- Keep Composables stateless where possible — state belongs in ViewModels
- All public API must be documented with KDoc

## Reporting Issues

Please use the GitHub Issue templates for bug reports and feature requests.

## Questions?

Reach us at **hello@neoninnovationlab.com** or open a Discussion on GitHub.
