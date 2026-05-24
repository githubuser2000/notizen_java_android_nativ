#!/usr/bin/env bash
set -Eeuo pipefail

PROJECT_DIR="${1:-$(cd "$(dirname "$0")/.." && pwd)}"
if [ -n "${1:-}" ] && [ -d "$1" ]; then
  shift || true
else
  PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
fi

find_latest_apk() {
  find "$PROJECT_DIR/build/termux-apk" -type f -name '*.apk' \
    ! -name 'notizen-unsigned.apk' \
    ! -name 'notizen-unaligned.apk' \
    ! -name 'notizen-aligned.apk' \
    2>/dev/null |
  while IFS= read -r apk; do
    [ -f "$apk" ] && printf '%s\t%s\n' "$(stat -c %Y "$apk" 2>/dev/null || stat -f %m "$apk")" "$apk"
  done | sort -n | tail -n 1 | cut -f2-
}

APK="${1:-}"
if [ -z "$APK" ]; then
  APK="$(find_latest_apk || true)"
fi

if [ -z "$APK" ] || [ ! -f "$APK" ]; then
  echo "APK nicht gefunden." >&2
  echo "Erst lokal bauen: ./notizen-build-apk" >&2
  echo "Gesucht wurde unter: $PROJECT_DIR/build/termux-apk" >&2
  exit 1
fi

case "$APK" in
  /*) ;;
  *) APK="$(cd "$(dirname "$APK")" && pwd)/$(basename "$APK")" ;;
esac

echo "Installiere exakt diese APK: $APK"
echo "Wichtig: Wenn der Android-Paketinstaller erscheint, dort Aktualisieren/Installieren bestätigen."
echo "Erwarteter APK-Build: md-table-runtime-verify-v119 / NotizenJavaAndroidNativ-v119-debug.apk"

if command -v termux-open >/dev/null 2>&1; then
  termux-open "$APK"
elif command -v adb >/dev/null 2>&1; then
  adb install -r "$APK"
else
  echo "Weder termux-open noch adb gefunden." >&2
  echo "APK liegt hier: $APK" >&2
  exit 1
fi
