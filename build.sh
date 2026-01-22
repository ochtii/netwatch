#!/bin/bash
set -e

echo "=== NetWatch Build Script ==="
echo "Starting Android build..."

# Set reduced heap size for CI environment
export GRADLE_OPTS="-Xmx1536m -XX:MaxMetaspaceSize=384m"

# Clean build
echo "Cleaning previous build..."
./gradlew clean --no-daemon

# Build debug APK with full output
echo "Building debug APK..."
./gradlew assembleDebug --no-daemon --stacktrace --info 2>&1 | tee build.log

# Check if APK was created
if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
    echo "✅ Debug APK created successfully!"
    ls -lh app/build/outputs/apk/debug/app-debug.apk
else
    echo "❌ Debug APK not found!"
    exit 1
fi

echo "=== Build Complete ==="
