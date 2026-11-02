#!/usr/bin/env bash
# Builds the debug app, installs it on the emulator (starting one if needed) and opens it.
# Usage: ./run.sh            build + install + launch
#        ./run.sh --logs     same, then stream the app's logs (Ctrl+C to stop)
set -euo pipefail

export ANDROID_HOME="${ANDROID_HOME:-$HOME/Android/Sdk}"
ADB="$ANDROID_HOME/platform-tools/adb"
EMULATOR="$ANDROID_HOME/emulator/emulator"
AVD="${AVD:-zetroit}"
PACKAGE="com.zetroit.music.debug"

cd "$(dirname "$0")"

if ! "$ADB" devices | grep -q "device$"; then
    echo "Starting emulator '$AVD'..."
    setsid nohup "$EMULATOR" -avd "$AVD" -memory 3072 -no-snapshot-save -no-boot-anim \
        > /dev/null 2>&1 < /dev/null &
    "$ADB" wait-for-device
    "$ADB" shell 'while [ "$(getprop sys.boot_completed)" != 1 ]; do sleep 2; done'
fi

./gradlew installDebug
"$ADB" shell am force-stop "$PACKAGE"
"$ADB" shell am start -n "$PACKAGE/com.zetroit.music.MainActivity" > /dev/null
echo "Zetroit Music is running on the emulator."

if [[ "${1:-}" == "--logs" ]]; then
    "$ADB" logcat --pid="$("$ADB" shell pidof -s "$PACKAGE")"
fi
