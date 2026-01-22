# NetWatch - Build Instructions

## Prerequisites

### Android Development Environment
1. **Android Studio** (Recommended) or
2. **Android SDK Command Line Tools**

### Required SDK Components
- Android SDK Platform 34
- Android SDK Build-Tools 34.0.0+
- Android SDK Platform-Tools
- Kotlin 1.9.20+

## Building the App

### Option 1: Android Studio (Easiest)
1. Open Android Studio
2. Select "Open an Existing Project"
3. Navigate to the `netwatch` folder
4. Wait for Gradle sync to complete
5. Click "Build" → "Make Project" (Ctrl+F9)
6. To build APK: "Build" → "Build Bundle(s) / APK(s)" → "Build APK(s)"

### Option 2: Command Line
```bash
# Navigate to project directory
cd /workspaces/netwatch

# Make gradlew executable
chmod +x gradlew

# Build debug APK
./gradlew assembleDebug

# Build release APK (unsigned)
./gradlew assembleRelease

# Install on connected device
./gradlew installDebug
```

### Output Locations
- **Debug APK:** `app/build/outputs/apk/debug/app-debug.apk`
- **Release APK:** `app/build/outputs/apk/release/app-release-unsigned.apk`

## Environment Variables (if using command line)
```bash
export ANDROID_HOME=/path/to/android-sdk
export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools
```

## Running the App

### On Emulator
1. Create an AVD in Android Studio (Tools → Device Manager)
2. Start the emulator
3. Run: `./gradlew installDebug`

### On Physical Device
1. Enable "Developer Options" on your Android device
2. Enable "USB Debugging"
3. Connect device via USB
4. Run: `./gradlew installDebug`

## Testing VPN Functionality

### Required Permissions
The app will request VPN permission on first use. You must grant this to enable firewall functionality.

### Test Flow
1. Launch NetWatch
2. Wait for app list to load
3. Tap the "Start VPN" FAB button
4. Accept VPN permission dialog
5. Toggle apps to "Blocked" status
6. Open blocked apps and verify they cannot access internet
7. Data usage should appear for allowed apps

### Troubleshooting
- **VPN won't start:** Check VPN permission was granted
- **Apps still connect:** Ensure VPN notification is visible
- **No apps shown:** Grant "Query All Packages" permission
- **Data usage not updating:** Start VPN service first

## Project Structure
```
netwatch/
├── app/
│   ├── src/main/
│   │   ├── kotlin/com/netwatch/firewall/
│   │   │   ├── MainActivity.kt
│   │   │   ├── data/              # Data layer
│   │   │   ├── domain/            # Business logic
│   │   │   ├── presentation/      # UI (Compose)
│   │   │   ├── service/vpn/       # VPN Service
│   │   │   └── ui/theme/          # Theming
│   │   ├── res/                   # Resources
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── build.gradle.kts
└── settings.gradle.kts
```

## Clean Architecture Layers
- **Domain:** Models and repository interfaces
- **Data:** Repository implementations, DataStore
- **Presentation:** ViewModels, Compose UI
- **Service:** VPN service, packet processing

## Tech Stack Summary
- Kotlin 100%
- Jetpack Compose + Material 3
- MVVM Architecture
- Coroutines + Flow
- DataStore for preferences
- VpnService API for traffic interception
