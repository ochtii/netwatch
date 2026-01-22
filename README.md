# NetWatch - Android Firewall & Data Usage Tracker

[![Android CI](https://github.com/ochtii/netwatch/actions/workflows/build.yml/badge.svg)](https://github.com/ochtii/netwatch/actions/workflows/build.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

A no-root local firewall and network monitoring app for Android, built with Jetpack Compose and Material 3.

## Features
- 🔒 **Block internet access** for specific apps (no root required)
- 📊 **Track data usage** per application in real-time
- 🌙 **Modern dark UI** with Material 3
- ⚡ **MVVM architecture** with Clean Architecture principles
- 🛡️ **VPN-based** traffic interception and control

## Technical Stack
- **Language:** Kotlin (100%)
- **Build System:** Gradle (KTS)
- **UI:** Jetpack Compose + Material 3
- **Architecture:** MVVM with Clean Architecture
- **Async:** Coroutines + Flow
- **Storage:** DataStore
- **Network:** VpnService API

## How It Works
1. Uses Android's `VpnService` API to create a local VPN tunnel
2. Intercepts all network packets at IP level (IPv4)
3. Parses packet headers to identify source application (via UID)
4. Checks blocked status from DataStore
5. Blocks packets from blacklisted apps or forwards to real network
6. Tracks data usage statistics in real-time

## Project Structure
```
app/src/main/kotlin/com/netwatch/firewall/
├── data/
│   ├── di/                    # Dependency injection
│   ├── local/                 # DataStore implementation
│   └── repository/            # Repository implementations
├── domain/
│   ├── model/                 # Data models (AppEntry)
│   └── repository/            # Repository interfaces
├── presentation/
│   └── applist/               # App list screen & ViewModel
├── service/
│   └── vpn/                   # VPN service & packet processing
└── ui/theme/                  # Material 3 theming
```

## Build & Run

### Option 1: Download APK from GitHub Actions
1. Go to [Actions](https://github.com/ochtii/netwatch/actions)
2. Click on the latest successful workflow run
3. Download the `netwatch-debug-apk` artifact
4. Extract and install the APK on your Android device

### Option 2: Build Locally
See [BUILD.md](BUILD.md) for detailed build instructions.

**Quick Start:**
```bash
./gradlew assembleDebug
```

The APK will be generated at: `app/build/outputs/apk/debug/app-debug.apk`

## Development Status
✅ **Complete** - All 4 steps implemented:
- ✅ Step 1: Initial Setup & Dependencies
- ✅ Step 2: Domain & Data Layer
- ✅ Step 3: UI Skeleton (Compose)
- ✅ Step 4: VpnService Implementation

## License
MIT License

## Disclaimer
This app is for educational purposes. Always respect app developers and use firewall features responsibly.