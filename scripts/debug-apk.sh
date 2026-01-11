#!/bin/bash
set -e
cd "/opt/android/projects/utm-converter"
./gradlew --no-daemon clean assembleDebug
echo
echo "APK:"
echo "app/build/outputs/apk/debug/app-debug.apk"
