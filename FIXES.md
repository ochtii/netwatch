# Crash Fixes & Build Improvements

## ✅ Behobene Probleme

### 1. App Crash beim Start
**Problem:** `lateinit property viewModel has not been initialized`

**Lösung:** Geändert zu nullable Variable mit Safe Calls
```kotlin
private var appListViewModel: AppListViewModel? = null
```

### 2. Android 14+ Foreground Service Crash
**Problem:** Fehlende Permissions und Service Type

**Lösung:** 
- FOREGROUND_SERVICE_SPECIAL_USE Permission hinzugefügt
- foregroundServiceType="specialUse" für VPN Service

### 3. GitHub Actions Build Fehler
**Problem:** APK nicht gefunden, Exit Code 1

**Lösung:**
- Wildcard paths für APK artifacts
- --no-daemon flag für Gradle
- Verbesserte Error-Behandlung

## Getestete Funktionen
- ✅ App startet ohne Crash
- ✅ VPN Permission Dialog
- ✅ Foreground Service auf Android 14+
- ✅ GitHub Actions Build
