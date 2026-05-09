#!/usr/bin/env bash
cd ~/storage/downloads
umask 022
set -Eeuo pipefail

ARCHIVE="${1:-}"
VERSION="${2:-1.0.8}"
PKG_NAME="notizen-android-native"
PREFIX="${PREFIX:-/data/data/com.termux/files/usr}"
OUT_DIR="${OUT_DIR:-$PWD}"

if [ -z "$ARCHIVE" ]; then
  echo "Usage: bash $0 <NotizenAndroidNative.tar.gz.tar> [version]" >&2
  exit 1
fi

if [ ! -f "$ARCHIVE" ]; then
  echo "Archiv nicht gefunden: $ARCHIVE" >&2
  exit 1
fi

for cmd in tar find cp mkdir rm chmod du cut sed grep mktemp; do
  command -v "$cmd" >/dev/null 2>&1 || {
    echo "Fehlt: $cmd" >&2
    exit 1
  }
done

if ! command -v dpkg-deb >/dev/null 2>&1; then
  echo "Fehlt: dpkg-deb" >&2
  echo "Installiere: pkg install dpkg" >&2
  exit 1
fi

WORK="$(mktemp -d)"
SRCROOT="$WORK/src"
PKGROOT="$WORK/pkgroot"

cleanup() {
  if [ "${KEEP_WORK:-0}" != "1" ]; then
    rm -rf "$WORK"
  else
    echo "Arbeitsordner behalten: $WORK"
  fi
}
trap cleanup EXIT

mkdir -p "$SRCROOT"

echo "==> Entpacke Archiv: $ARCHIVE"

if tar -tzf "$ARCHIVE" >/dev/null 2>&1; then
  tar -xzf "$ARCHIVE" -C "$SRCROOT"
else
  tar -xf "$ARCHIVE" -C "$SRCROOT"
fi

PROJECT="$(
  find "$SRCROOT" -maxdepth 8 -type f \
    -path '*/app/src/main/AndroidManifest.xml' \
    -print -quit |
  sed 's#/app/src/main/AndroidManifest.xml$##'
)"

if [ -z "$PROJECT" ] || [ ! -d "$PROJECT" ]; then
  echo "Konnte Android-Projekt nicht finden." >&2
  echo "Erwartet: app/src/main/AndroidManifest.xml" >&2
  exit 1
fi

echo "==> Projekt gefunden: $PROJECT"

echo "==> Ergänze Termux-Buildskripte"

cat > "$PROJECT/termux-setup-sdk.sh" <<'EOF_SETUP'
#!/usr/bin/env bash
set -Eeuo pipefail

ANDROID_API="${ANDROID_API:-34}"
SDK="${ANDROID_HOME:-$HOME/android-sdk}"
CMDLINE_VERSION="14742923"
CMDLINE_ZIP="commandlinetools-linux-${CMDLINE_VERSION}_latest.zip"
CMDLINE_URL="https://dl.google.com/android/repository/${CMDLINE_ZIP}"

log() {
  printf '\n==> %s\n' "$*"
}

need() {
  command -v "$1" >/dev/null 2>&1
}

log "Termux-Pakete prüfen/installieren"

if need pkg; then
  pkg update
  pkg install -y bash coreutils findutils sed grep openjdk-17 wget unzip zip aapt aapt2 d8 apksigner android-tools termux-tools
else
  echo "Hinweis: pkg nicht gefunden; installiere keine Termux-Pakete automatisch."
fi

mkdir -p "$SDK/cmdline-tools"

export ANDROID_HOME="$SDK"
export ANDROID_SDK_ROOT="$SDK"
export PATH="$SDK/cmdline-tools/latest/bin:$PATH"

if [ ! -x "$SDK/cmdline-tools/latest/bin/sdkmanager" ]; then
  log "Android SDK Command-Line-Tools herunterladen"
  echo "Quelle: $CMDLINE_URL"
  echo
  echo "Damit akzeptierst du die Android SDK / Command-Line-Tools-Bedingungen von Google."
  printf "Fortfahren? [y/N] "
  read -r answer || true

  case "${answer:-}" in
    y|Y|yes|YES|j|J|ja|JA) ;;
    *) echo "Abgebrochen."; exit 1 ;;
  esac

  tmpzip="$HOME/$CMDLINE_ZIP"
  rm -f "$tmpzip"

  wget -O "$tmpzip" "$CMDLINE_URL"

  rm -rf "$SDK/cmdline-tools/latest" "$SDK/cmdline-tools/cmdline-tools"
  unzip -q "$tmpzip" -d "$SDK/cmdline-tools"
  mv "$SDK/cmdline-tools/cmdline-tools" "$SDK/cmdline-tools/latest"
fi

log "SDK-Lizenzen akzeptieren und Android Platform $ANDROID_API installieren"

yes | "$SDK/cmdline-tools/latest/bin/sdkmanager" --sdk_root="$SDK" --licenses >/dev/null || true
"$SDK/cmdline-tools/latest/bin/sdkmanager" --sdk_root="$SDK" "platforms;android-${ANDROID_API}"

log "Shell-Umgebung dauerhaft setzen"

for rc in "$HOME/.zshrc" "$HOME/.bashrc"; do
  touch "$rc"

  if ! grep -q 'ANDROID_HOME=.*/android-sdk' "$rc" 2>/dev/null; then
    cat >> "$rc" <<'RC'

# Android SDK for Notizen native APK builds
export ANDROID_HOME=$HOME/android-sdk
export ANDROID_SDK_ROOT=$ANDROID_HOME
export PATH=$ANDROID_HOME/cmdline-tools/latest/bin:$PATH
RC
  fi
done

log "Fertig"
echo "ANDROID_HOME=$SDK"
echo "android.jar: $SDK/platforms/android-${ANDROID_API}/android.jar"
echo
echo "Standard ist API 34, weil Termux-aapt2 mit API 35/36 oft android.jar nicht laden kann."
EOF_SETUP

cat > "$PROJECT/termux-build-apk-manual.sh" <<'EOF_BUILD'
#!/usr/bin/env bash
set -Eeuo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

ANDROID_API="${ANDROID_API:-34}"
SDK="${ANDROID_HOME:-$HOME/android-sdk}"
ANDROID_JAR="$SDK/platforms/android-${ANDROID_API}/android.jar"

PACKAGE="${PACKAGE:-de.notizen.android}"
MIN_SDK="${MIN_SDK:-23}"
TARGET_SDK="${TARGET_SDK:-$ANDROID_API}"
VERSION_CODE="${VERSION_CODE:-108}"
VERSION_NAME="${VERSION_NAME:-1.0.8-java-native}"

OUTDIR="$ROOT/build/termux-apk"
INT="$OUTDIR/intermediates"
GEN="$INT/generated-src"
CLASSES="$INT/classes"
DEX="$INT/dex"
FINAL_APK="${APK_OUT:-$OUTDIR/notizen-debug.apk}"

AAPT2_BIN="${AAPT2_BIN:-$(command -v aapt2 2>/dev/null || true)}"
D8_BIN="${D8_BIN:-$(command -v d8 2>/dev/null || true)}"
APKSIGNER_BIN="${APKSIGNER_BIN:-$(command -v apksigner 2>/dev/null || true)}"
ZIPALIGN_BIN="${ZIPALIGN_BIN:-$(command -v zipalign 2>/dev/null || true)}"

log() {
  printf '\n==> %s\n' "$*"
}

missing=0

for cmd in javac jar keytool zip sed grep find; do
  if ! command -v "$cmd" >/dev/null 2>&1; then
    echo "Fehlt: $cmd" >&2
    missing=1
  fi
done

for pair in \
  "aapt2:$AAPT2_BIN" \
  "d8:$D8_BIN" \
  "apksigner:$APKSIGNER_BIN" \
  "zipalign:$ZIPALIGN_BIN"
do
  name="${pair%%:*}"
  value="${pair#*:}"

  if [ -z "$value" ]; then
    echo "Fehlt: $name" >&2
    missing=1
  fi
done

if [ "$missing" != 0 ]; then
  cat >&2 <<'MSG'

Installiere zuerst:
  pkg install openjdk-17 aapt aapt2 d8 apksigner zip
MSG
  exit 1
fi

if [ ! -f "$ANDROID_JAR" ]; then
  cat >&2 <<MSG
Android Platform fehlt:
  $ANDROID_JAR

Einmal ausführen:
  ANDROID_API=$ANDROID_API notizen-setup-sdk
MSG
  exit 1
fi

rm -rf "$OUTDIR"
mkdir -p "$GEN" "$CLASSES" "$DEX" "$(dirname "$FINAL_APK")"

log "Build-Parameter"
echo "compile/android.jar API: $ANDROID_API"
echo "targetSdk: $TARGET_SDK"
echo "minSdk: $MIN_SDK"
echo "package: $PACKAGE"
echo "aapt2: $AAPT2_BIN"
echo "d8: $D8_BIN"
echo "apksigner: $APKSIGNER_BIN"
echo "zipalign: $ZIPALIGN_BIN"

log "Manifest vorbereiten"

MANIFEST_IN="$ROOT/app/src/main/AndroidManifest.xml"
MANIFEST_OUT="$INT/AndroidManifest.xml"

if grep -q ' package=' "$MANIFEST_IN"; then
  cp "$MANIFEST_IN" "$MANIFEST_OUT"
else
  sed "1s/<manifest /<manifest package=\"$PACKAGE\" /" "$MANIFEST_IN" > "$MANIFEST_OUT"
fi

log "Ressourcen mit aapt2 kompilieren"

"$AAPT2_BIN" compile \
  --dir "$ROOT/app/src/main/res" \
  -o "$INT/resources.zip"

log "APK-Basis mit aapt2 linken"

if ! "$AAPT2_BIN" link \
  -I "$ANDROID_JAR" \
  --manifest "$MANIFEST_OUT" \
  --java "$GEN" \
  --min-sdk-version "$MIN_SDK" \
  --target-sdk-version "$TARGET_SDK" \
  --version-code "$VERSION_CODE" \
  --version-name "$VERSION_NAME" \
  -o "$INT/unsigned.apk" \
  "$INT/resources.zip"
then
  cat >&2 <<MSG

aapt2 konnte die Platform-Datei nicht laden:
  $ANDROID_JAR

In Termux ist das bei API 35/36 typisch.
Stabiler Fallback:

  ANDROID_API=34 TARGET_SDK=34 notizen-setup-sdk
  ANDROID_API=34 TARGET_SDK=34 notizen-build-apk
MSG
  exit 1
fi

log "Java nach .class kompilieren"

find "$ROOT/app/src/main/java" "$GEN" -name '*.java' | sort > "$INT/sources.txt"

javac \
  -encoding UTF-8 \
  -source 8 \
  -target 8 \
  -Xlint:none \
  -classpath "$ANDROID_JAR:$GEN" \
  -d "$CLASSES" \
  @"$INT/sources.txt"

log "DEX erzeugen"

(cd "$CLASSES" && jar cf "$INT/classes.jar" .)

"$D8_BIN" \
  --min-api "$MIN_SDK" \
  --output "$DEX" \
  "$INT/classes.jar"

log "APK zusammensetzen"

cp "$INT/unsigned.apk" "$INT/unaligned.apk"
(cd "$DEX" && zip -q "$INT/unaligned.apk" classes.dex)

"$ZIPALIGN_BIN" \
  -p \
  -f \
  4 \
  "$INT/unaligned.apk" \
  "$INT/aligned.apk"

log "Debug-Keystore prüfen/erzeugen"

DEBUG_KEYSTORE="${DEBUG_KEYSTORE:-$HOME/.android/debug.keystore}"

if [ -f "$DEBUG_KEYSTORE" ] && ! keytool -list \
  -keystore "$DEBUG_KEYSTORE" \
  -storepass android \
  -alias androiddebugkey >/dev/null 2>&1
then
  echo "Vorhandener Debug-Keystore hat nicht das Standardpasswort; nutze lokalen Keystore."
  DEBUG_KEYSTORE="$INT/debug.keystore"
fi

mkdir -p "$(dirname "$DEBUG_KEYSTORE")"

if [ ! -f "$DEBUG_KEYSTORE" ]; then
  keytool -genkeypair \
    -keystore "$DEBUG_KEYSTORE" \
    -storepass android \
    -keypass android \
    -alias androiddebugkey \
    -keyalg RSA \
    -keysize 2048 \
    -validity 10000 \
    -dname "CN=Android Debug,O=Android,C=US" >/dev/null
fi

log "APK signieren"

"$APKSIGNER_BIN" sign \
  --ks "$DEBUG_KEYSTORE" \
  --ks-pass pass:android \
  --key-pass pass:android \
  --out "$FINAL_APK" \
  "$INT/aligned.apk"

"$APKSIGNER_BIN" verify --verbose "$FINAL_APK" >/dev/null

log "Fertig"
echo "APK: $FINAL_APK"
echo
echo "Installieren:"
echo "  notizen-install-apk"
echo
echo "Oder direkt:"
echo "  termux-open \"$FINAL_APK\""
EOF_BUILD

cat > "$PROJECT/termux-install-apk.sh" <<'EOF_INSTALL'
#!/usr/bin/env bash
set -Eeuo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APK="${1:-$ROOT/build/termux-apk/notizen-debug.apk}"

if [ ! -f "$APK" ]; then
  echo "APK nicht gefunden: $APK" >&2
  echo "Erst bauen: bash ./termux-build-apk-manual.sh" >&2
  exit 1
fi

if command -v termux-open >/dev/null 2>&1; then
  termux-open "$APK"
elif command -v adb >/dev/null 2>&1; then
  adb install -r "$APK"
else
  echo "Weder termux-open noch adb gefunden." >&2
  echo "APK liegt hier: $APK" >&2
  exit 1
fi
EOF_INSTALL

chmod +x \
  "$PROJECT/termux-setup-sdk.sh" \
  "$PROJECT/termux-build-apk-manual.sh" \
  "$PROJECT/termux-install-apk.sh"

echo "==> Debian-Stagingbaum bauen"

mkdir -p "$PKGROOT/DEBIAN"
mkdir -p "$PKGROOT$PREFIX/bin"
mkdir -p "$PKGROOT$PREFIX/share/$PKG_NAME"

cp -a "$PROJECT" "$PKGROOT$PREFIX/share/$PKG_NAME/NotizenAndroidNative"

APPROOT="$PKGROOT$PREFIX/share/$PKG_NAME/NotizenAndroidNative"

find "$APPROOT" -type d \( \
  -name ".git" -o \
  -name ".gradle" -o \
  -name "build" -o \
  -name "out" \
\) -prune -exec rm -rf {} + 2>/dev/null || true

find "$APPROOT" -type f \( \
  -name "*.apk" -o \
  -name "*.dex" -o \
  -name "*.class" -o \
  -name "*.jar" \
\) -delete 2>/dev/null || true

cat > "$PKGROOT$PREFIX/bin/notizen-build-apk" <<'EOF_WRAPPER_BUILD'
#!/usr/bin/env bash
set -Eeuo pipefail

PREFIX="${PREFIX:-/data/data/com.termux/files/usr}"
SRC="$PREFIX/share/notizen-android-native/NotizenAndroidNative"
WORK="${NOTIZEN_BUILD_DIR:-$HOME/notizen-android-native}"

if [ ! -d "$WORK" ]; then
  echo "Kopiere Projekt nach: $WORK"
  cp -a "$SRC" "$WORK"
else
  echo "Nutze vorhandenen Projektordner: $WORK"
  echo "Zum Zurücksetzen löschen: rm -rf \"$WORK\""
fi

cd "$WORK"
APK_OUT="${APK_OUT:-$HOME/notizen-debug.apk}" exec bash ./termux-build-apk-manual.sh "$@"
EOF_WRAPPER_BUILD

cat > "$PKGROOT$PREFIX/bin/notizen-setup-sdk" <<'EOF_WRAPPER_SETUP'
#!/usr/bin/env bash
set -Eeuo pipefail

PREFIX="${PREFIX:-/data/data/com.termux/files/usr}"
SCRIPT="$PREFIX/share/notizen-android-native/NotizenAndroidNative/termux-setup-sdk.sh"

exec bash "$SCRIPT" "$@"
EOF_WRAPPER_SETUP

cat > "$PKGROOT$PREFIX/bin/notizen-install-apk" <<'EOF_WRAPPER_INSTALL'
#!/usr/bin/env bash
set -Eeuo pipefail

APK="${1:-$HOME/notizen-debug.apk}"

if [ ! -f "$APK" ]; then
  echo "APK nicht gefunden: $APK" >&2
  echo "Erst ausführen: notizen-build-apk" >&2
  exit 1
fi

if command -v termux-open >/dev/null 2>&1; then
  termux-open "$APK"
elif command -v adb >/dev/null 2>&1; then
  adb install -r "$APK"
else
  echo "Weder termux-open noch adb gefunden." >&2
  echo "APK: $APK" >&2
  exit 1
fi
EOF_WRAPPER_INSTALL

chmod 755 \
  "$PKGROOT$PREFIX/bin/notizen-build-apk" \
  "$PKGROOT$PREFIX/bin/notizen-setup-sdk" \
  "$PKGROOT$PREFIX/bin/notizen-install-apk"

INSTALLED_SIZE="$(du -sk "$PKGROOT$PREFIX" | cut -f1)"

cat > "$PKGROOT/DEBIAN/control" <<EOF_CONTROL
Package: $PKG_NAME
Version: $VERSION
Architecture: all
Maintainer: local
Installed-Size: $INSTALLED_SIZE
Depends: bash, coreutils, findutils, sed, grep, openjdk-17, wget, unzip, zip, aapt, aapt2, d8, apksigner, android-tools, termux-tools
Description: Native Java Android Notizen source and Termux APK builder
 Installs the Notizen native Android Java source and helper commands for building
 an installable Android APK directly inside Termux without Android Studio.
EOF_CONTROL

DEB="$OUT_DIR/${PKG_NAME}_${VERSION}_all.deb"
rm -f "$DEB"

echo "==> Baue .deb"

if dpkg-deb --help 2>&1 | grep -q -- "--root-owner-group"; then
  dpkg-deb --build --root-owner-group "$PKGROOT" "$DEB"
else
  dpkg-deb --build "$PKGROOT" "$DEB"
fi

cat <<MSG

Fertig:
  $DEB

Installieren:
  apt install "$DEB"

Dann:
  rm -rf "\$HOME/notizen-android-native"
  notizen-setup-sdk
  notizen-build-apk
  notizen-install-apk
MSG
