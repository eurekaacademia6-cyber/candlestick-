# CandleVision Expert 2.0

A standalone Android camera app for visual candlestick/chart reading. No MT5, broker, bridge, login, or network connection is required.

## Build

The repository uses a conventional Android `app/` module.

- Android Gradle Plugin: 8.13.0
- Gradle: 8.13
- JDK: 17
- compileSdk / targetSdk: 36
- minSdk: 24
- CameraX: 1.6.1

GitHub Actions builds the debug APK using the installed Gradle executable; the repository does not depend on a Gradle wrapper JAR.

## What the app actually does

The current version is a visual heuristic engine. It analyzes camera-frame luminance/contrast and extracts a rough candle-like feature state, then combines sequence, momentum, rejection geometry, chart quality, and a small on-device adaptive learner.

It is intentionally not presented as a guaranteed or pre-trained market oracle. A real high-performance vision model would require a large, labeled, leakage-controlled dataset of chart crops and subsequent out-of-sample validation.

## Usage

1. Install the APK.
2. Allow camera access.
3. Hold the phone square to a clear candlestick chart.
4. Fill the camera guide with the chart's candle area.
5. Use LABEL UP / LABEL DOWN only when you have an actual verified outcome; those labels adjust the local adaptive weights.
