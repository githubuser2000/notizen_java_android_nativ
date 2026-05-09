#!/usr/bin/env bash
set -euo pipefail

# Manual Android APK build for Termux-style environments without Gradle.
# Defaults match the API-34 build parameters reported during v9 testing.

PROJECT_DIR="${1:-$(cd "$(dirname "$0")/.." && pwd)}"
APP_DIR="$PROJECT_DIR/app/src/main"
BUILD_DIR="$PROJECT_DIR/build/termux-apk"
PACKAGE="de.notizen.android"
MIN_SDK="${MIN_SDK:-23}"
TARGET_SDK="${TARGET_SDK:-34}"
COMPILE_API="${COMPILE_API:-34}"
VERSION_CODE="${VERSION_CODE:-103}"
VERSION_NAME="${VERSION_NAME:-1.0.103-java-android-nativ}"
ANDROID_HOME="${ANDROID_HOME:-$HOME/android-sdk}"
ANDROID_JAR="${ANDROID_JAR:-$ANDROID_HOME/platforms/android-$COMPILE_API/android.jar}"
AAPT2="${AAPT2:-$(command -v aapt2 || true)}"
D8="${D8:-$ANDROID_HOME/cmdline-tools/latest/bin/d8}"
APKSIGNER="${APKSIGNER:-$(command -v apksigner || true)}"
ZIPALIGN="${ZIPALIGN:-$(command -v zipalign || true)}"
JAVAC="${JAVAC:-javac}"
ZIP="${ZIP:-zip}"
KEYSTORE="${KEYSTORE:-$HOME/.android/debug.keystore}"
KEY_ALIAS="${KEY_ALIAS:-androiddebugkey}"
STOREPASS="${STOREPASS:-android}"
KEYPASS="${KEYPASS:-android}"

need_file() { [ -f "$1" ] || { echo "Fehlt: $1" >&2; exit 1; }; }
need_exec() { [ -n "$1" ] && [ -x "$1" ] || { echo "Fehlt/ nicht ausführbar: $2" >&2; exit 1; }; }
need_cmd() { command -v "$1" >/dev/null 2>&1 || { echo "Fehlt im PATH: $1" >&2; exit 1; }; }

need_file "$ANDROID_JAR"
need_exec "$AAPT2" "aapt2"
need_exec "$D8" "d8"
need_exec "$APKSIGNER" "apksigner"
need_exec "$ZIPALIGN" "zipalign"
need_cmd "$JAVAC"
need_cmd "$ZIP"
need_cmd keytool

rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR"/res "$BUILD_DIR"/gen "$BUILD_DIR"/classes "$BUILD_DIR"/dex "$BUILD_DIR"/out

MANIFEST="$APP_DIR/AndroidManifest.xml"
RES_DIR="$APP_DIR/res"
JAVA_SRC="$APP_DIR/java"
RES_ZIP="$BUILD_DIR/res/resources.zip"
UNSIGNED_APK="$BUILD_DIR/out/notizen-unsigned.apk"
UNALIGNED_APK="$BUILD_DIR/out/notizen-unaligned.apk"
ALIGNED_APK="$BUILD_DIR/out/notizen-aligned.apk"
SIGNED_APK="$BUILD_DIR/out/NotizenJavaAndroidNativ-v${VERSION_CODE}-debug.apk"

printf '==> Build-Parameter\n'
printf 'compile/android.jar API: %s\n' "$COMPILE_API"
printf 'targetSdk: %s\n' "$TARGET_SDK"
printf 'minSdk: %s\n' "$MIN_SDK"
printf 'package: %s\n' "$PACKAGE"
printf 'aapt2: %s\n' "$AAPT2"
printf 'd8: %s\n' "$D8"
printf 'apksigner: %s\n' "$APKSIGNER"
printf 'zipalign: %s\n\n' "$ZIPALIGN"

printf '==> Ressourcen mit aapt2 kompilieren\n'
"$AAPT2" compile --dir "$RES_DIR" -o "$RES_ZIP"

printf '\n==> APK-Basis mit aapt2 linken\n'
"$AAPT2" link \
  -I "$ANDROID_JAR" \
  --manifest "$MANIFEST" \
  --java "$BUILD_DIR/gen" \
  --min-sdk-version "$MIN_SDK" \
  --target-sdk-version "$TARGET_SDK" \
  --version-code "$VERSION_CODE" \
  --version-name "$VERSION_NAME" \
  --auto-add-overlay \
  -R "$RES_ZIP" \
  -o "$UNALIGNED_APK"

printf '\n==> Java nach .class kompilieren\n'
find "$JAVA_SRC" "$BUILD_DIR/gen" -name '*.java' -print > "$BUILD_DIR/sources.txt"
"$JAVAC" -source 17 -target 17 \
  -classpath "$ANDROID_JAR:$BUILD_DIR/gen" \
  -sourcepath "$JAVA_SRC:$BUILD_DIR/gen" \
  -d "$BUILD_DIR/classes" \
  @"$BUILD_DIR/sources.txt"

printf '\n==> .class nach DEX konvertieren\n'
find "$BUILD_DIR/classes" -name '*.class' -print > "$BUILD_DIR/classes.txt"
mapfile -t CLASS_FILES < "$BUILD_DIR/classes.txt"
"$D8" --lib "$ANDROID_JAR" --min-api "$MIN_SDK" --output "$BUILD_DIR/dex" "${CLASS_FILES[@]}"

printf '\n==> classes.dex in APK einfügen\n'
cp "$UNALIGNED_APK" "$UNSIGNED_APK"
( cd "$BUILD_DIR/dex" && "$ZIP" -q -u "$UNSIGNED_APK" classes.dex )

printf '\n==> zipalign\n'
"$ZIPALIGN" -f 4 "$UNSIGNED_APK" "$ALIGNED_APK"

if [ ! -f "$KEYSTORE" ]; then
  printf '\n==> Debug-Keystore erzeugen\n'
  mkdir -p "$(dirname "$KEYSTORE")"
  keytool -genkeypair -v \
    -keystore "$KEYSTORE" \
    -storepass "$STOREPASS" \
    -keypass "$KEYPASS" \
    -alias "$KEY_ALIAS" \
    -keyalg RSA \
    -keysize 2048 \
    -validity 10000 \
    -dname "CN=Android Debug,O=Android,C=US" >/dev/null
fi

printf '\n==> APK signieren\n'
"$APKSIGNER" sign \
  --ks "$KEYSTORE" \
  --ks-key-alias "$KEY_ALIAS" \
  --ks-pass "pass:$STOREPASS" \
  --key-pass "pass:$KEYPASS" \
  --out "$SIGNED_APK" \
  "$ALIGNED_APK"

printf '\n==> Signatur prüfen\n'
"$APKSIGNER" verify "$SIGNED_APK"

printf '\nFertig: %s\n' "$SIGNED_APK"
