# GitHub Actions Workflow

## Automatischer APK Build

Der GitHub Actions Workflow wurde erfolgreich eingerichtet und baut automatisch die NetWatch APK bei jedem Push.

### Workflow-Details

**Trigger:**
- Push zu `main` oder `develop` Branch
- Pull Requests zu `main`
- Manueller Trigger via `workflow_dispatch`

**Build-Schritte:**
1. ✅ Checkout Code
2. ✅ Setup JDK 17
3. ✅ Setup Android SDK
4. ✅ Build Debug APK
5. ✅ Upload Debug APK als Artifact (30 Tage Retention)
6. ✅ Build Release APK (optional)
7. ✅ Upload Release APK als Artifact (30 Tage Retention)

### APK Herunterladen

1. Gehe zu: https://github.com/ochtii/netwatch/actions
2. Klicke auf den neuesten erfolgreichen Workflow-Run
3. Scrolle zu "Artifacts"
4. Lade `netwatch-debug-apk` herunter
5. Entpacke die ZIP-Datei
6. Installiere `app-debug.apk` auf deinem Android-Gerät

### Lokaler Build

```bash
# Debug APK
./gradlew assembleDebug

# Release APK
./gradlew assembleRelease
```

### Workflow-Datei

Die Workflow-Konfiguration befindet sich in:
`.github/workflows/build.yml`

## Status

[![Android CI](https://github.com/ochtii/netwatch/actions/workflows/build.yml/badge.svg)](https://github.com/ochtii/netwatch/actions/workflows/build.yml)

Der Badge zeigt den aktuellen Build-Status an!
