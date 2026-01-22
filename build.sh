#!/bin/bash
set -e

echo "=== NetWatch Build Script ==="
echo "Starting Android build..."

# Set heap size for Gradle
export GRADLE_OPTS="-Xmx2048m -XX:MaxMetaspaceSize=512m"

# Clean build
echo "Cleaning previous build..."
./gradlew clean --no-daemon

# Build debug APK
echo "Building debug APK..."
./gradlew assembleDebug --no-daemon --stacktrace --warning-mode all

# Check if APK was created
if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
    echo "✅ Debug APK created successfully!"
    ls -lh app/build/outputs/apk/debug/app-debug.apk
else
    echo "❌ Debug APK not found!"
    exit 1
fi

echo "=== Build Complete ==="
