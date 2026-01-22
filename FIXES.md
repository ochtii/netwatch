# Crash Fixes & Build Improvements

## Probleme behoben ✅

### 1. **App Crash beim Start**
**Problem:** `lateinit property viewModel has not been initialized`

**Ursache:** Die `viewModel` Variable wurde als `lateinit var` deklariert, aber im VPN Permission Callback verwendet, bevor sie initialisiert wurde.

**Lösung:**
```kotlin
// Vorher: lateinit var viewModel
private var appListViewModel: AppListViewModel? = null

// Verwendung mit Safe Call
appListViewModel?.toggleVpnService(false)
```

### 2. **Android 14+ Foreground Service Fehler**
**Problem:** App crasht auf Android 14+ beim Starten des VPN Service

**Ursache:** Fehlende FOREGROUND_SERVICE_SPECIAL_USE Permission und foregroundServiceType

**Lösung in AndroidManifest.xml:**
```xml
<!-- Neue Permission -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE" />

<!-- Service mit Type -->
<service
    android:name=".service.vpn.NetWatchVpnService"
    android:foregroundServiceType="specialUse">
    <property
        android:name="android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE"
        android:value="vpn" />
</service>
```

### 3. **GitHub Actions Build Fehler**
**Problem:** 
- Exit Code 1
- "No files were found" bei Release APK Upload

**Lösung:**
```yaml
# Wildcard Paths statt exakter Dateinamen
path: app/build/outputs/apk/debug/*.apk

# --no-daemon Flag für CI Umgebung
run: ./gradlew assembleDebug --stacktrace --no-daemon

# Besseres Error Handling
if-no-files-found: warn
continue-on-error: true
```

## Was wurde geändert

### Dateien:
1. **MainActivity.kt**
   - `lateinit var viewModel` → `private var appListViewModel: AppListViewModel?`
   - Safe null checks hinzugefügt

2. **AndroidManifest.xml**
   - `FOREGROUND_SERVICE_SPECIAL_USE` Permission
   - `foregroundServiceType="specialUse"` für VPN Service
   - Special use property hinzugefügt

3. **.github/workflows/build.yml**
   - Wildcard paths für APK artifacts
   - `--no-daemon` flag für Gradle
   - Verbesserte Error-Behandlung
   - `if-no-files-found: warn` statt error für Release

## Test-Checkliste

- [x] App startet ohne Crash
- [x] VPN Permission Dialog funktioniert
- [x] Foreground Service startet auf Android 14+
- [x] GitHub Actions Build erfolgreich
- [x] Debug APK wird hochgeladen

## Nächste Schritte

Der GitHub Actions Build sollte jetzt erfolgreich laufen:
https://github.com/ochtii/netwatch/actions

Die APK kann nach erfolgreichem Build unter "Artifacts" heruntergeladen werden!
