# 🕐 Hub Display

A minimal always-on display app for Android — turns old phones into smart bedroom clocks with live weather.

## Features

| Feature | Description |
|---------|-------------|
| ⏰ **Large Clock** | Full-half landscape display with thin sans-serif font |
| 🌤️ **Live Weather** | Temperature, condition & emoji — auto-refreshes every 10 min |
| ♟️ **Burn-in Shield** | Checkerboard pixel alternation every 5 s protects AMOLED screens |
| 🌙 **Night Mode** | Red-on-black at sunset, normal colors at sunrise — easy on the eyes |
| 🔒 **Always On** | Screen never turns off; fullscreen immersive mode |
| 🔋 **Battery Friendly** | Pure black background (OLED pixels off), no background services |
| 📍 **City Picker** | Search & select any city via Open-Meteo geocoding (no API key!) |

## Screenshots

> *Landscape split-screen: Clock on the left, Weather on the right.*

## Supported Devices

- Samsung Galaxy J5 (Android 6.0 / API 23)
- Samsung Galaxy A21s (Android 11 / API 30)
- Any Android device API 23+

## Build

```bash
./gradlew assembleDebug
```

APK → `app/build/outputs/apk/debug/app-debug.apk`

Or download the latest APK from [**GitHub Actions**](../../actions) → latest run → Artifacts.

## Setup

1. Install the APK on your phone
2. Open **Hub Display**
3. Tap the ⚙️ icon (bottom-right)
4. Search & pick your city
5. Lay the phone in landscape — done!

## Tech Stack

- **Language:** Kotlin
- **Min SDK:** 23 (Android 6.0)
- **UI:** Android Views (XML layouts)
- **Weather:** [Open-Meteo API](https://open-meteo.com/) — free, no key required
- **Dependencies:** AndroidX AppCompat only

## How Burn-in Protection Works

The app draws a 2×2 checkerboard overlay — at any moment, 50 % of pixels are true-black (OLED off). Every 5 seconds the pattern flips: pixels that were ON go OFF and vice-versa. Each pixel is active only half the time, dramatically extending AMOLED lifespan.

## License

MIT
