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

EXPECTED_MARKER="md-table-runtime-verify-v119"
EXPECTED_VERSION_LINE='VERSION_CODE="${VERSION_CODE:-119}"'
if ! grep -Rqs "$EXPECTED_MARKER" "$PROJECT/app/src/main/java" "$PROJECT/tools" 2>/dev/null; then
  cat >&2 <<MSG

Dieses Archiv ist zu alt und enthält den aktuellen Markdown-Tabellen-Fix nicht:
  $ARCHIVE

Erwarteter Marker: $EXPECTED_MARKER

Du baust sonst wieder ein .deb aus altem Code. Nimm das neue Archiv
notizen_java_android_nativ_markdown_table_pipefail_fixed_v119.tar.bz2
oder spiele zuerst den Patch in das Archiv/Projekt ein.
MSG
  exit 1
fi
if [ ! -f "$PROJECT/tools/notizen-build-apk-termux.sh" ] || ! grep -Fq "$EXPECTED_VERSION_LINE" "$PROJECT/tools/notizen-build-apk-termux.sh"; then
  cat >&2 <<MSG

Dieses Archiv enthält nicht den v119-Termux-Buildpfad.
Erwartet in tools/notizen-build-apk-termux.sh:
  $EXPECTED_VERSION_LINE

Abbruch, damit nicht versehentlich wieder eine alte APK gebaut wird.
MSG
  exit 1
fi

echo "==> Archiv-Marker geprüft: $EXPECTED_MARKER"

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
MODERN_BUILDER="$ROOT/tools/notizen-build-apk-termux.sh"

find_latest_apk() {
  find "$ROOT/build/termux-apk" -type f -name '*.apk' \
    ! -name 'notizen-unsigned.apk' \
    ! -name 'notizen-unaligned.apk' \
    ! -name 'notizen-aligned.apk' \
    2>/dev/null |
  while IFS= read -r apk; do
    [ -f "$apk" ] && printf '%s\t%s\n' "$(stat -c %Y "$apk" 2>/dev/null || stat -f %m "$apk")" "$apk"
  done | sort -n | tail -n 1 | cut -f2-
}

if [ -x "$MODERN_BUILDER" ]; then
  bash "$MODERN_BUILDER" "$ROOT" "$@"
  if [ -n "${APK_OUT:-}" ]; then
    latest="$(find_latest_apk || true)"
    if [ -z "$latest" ] || [ ! -f "$latest" ]; then
      echo "Build erfolgreich, aber keine APK unter $ROOT/build/termux-apk gefunden." >&2
      exit 1
    fi
    mkdir -p "$(dirname "$APK_OUT")"
    cp -f "$latest" "$APK_OUT"
    echo
    echo "APK_OUT-Kopie: $APK_OUT"
  fi
  exit 0
fi

cat >&2 <<MSG
Aktuelles Buildskript fehlt:
  $MODERN_BUILDER

Dieses Projektarchiv enthält CommonMark-Quellcode, aber nicht den dazugehörigen
Termux-Buildpfad. Verwende ein aktuelles Archiv oder baue direkt aus einem
Projektordner mit tools/notizen-build-apk-termux.sh.
MSG
exit 1
EOF_BUILD

cat > "$PROJECT/termux-install-apk.sh" <<'EOF_INSTALL'
#!/usr/bin/env bash
set -Eeuo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MODERN_INSTALLER="$ROOT/tools/notizen-install-apk-termux.sh"

if [ -x "$MODERN_INSTALLER" ]; then
  exec bash "$MODERN_INSTALLER" "$ROOT" "$@"
fi

find_latest_apk() {
  find "$ROOT/build/termux-apk" -type f -name '*.apk' \
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
  echo "Erst bauen: bash ./termux-build-apk-manual.sh" >&2
  echo "Gesucht wurde unter: $ROOT/build/termux-apk" >&2
  exit 1
fi

echo "Installiere APK: $APK"
echo "Wichtig: Wenn der Android-Paketinstaller erscheint, dort Aktualisieren/Installieren bestätigen."
echo "Erwarteter APK-Build: md-table-runtime-verify-v117 / NotizenJavaAndroidNativ-v117-debug.apk"

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

PREFIX="${PREFIX:-/data/data/com.termux/files/usr}"
WORK="${NOTIZEN_BUILD_DIR:-$HOME/notizen-android-native}"

find_latest_apk() {
  {
    [ -f "$HOME/notizen-debug.apk" ] && printf '%s\n' "$HOME/notizen-debug.apk"
    find "$WORK/build/termux-apk" -type f -name '*.apk' \
      ! -name 'notizen-unsigned.apk' \
      ! -name 'notizen-unaligned.apk' \
      ! -name 'notizen-aligned.apk' \
      2>/dev/null || true
  } | while IFS= read -r apk; do
    [ -f "$apk" ] && printf '%s\t%s\n' "$(stat -c %Y "$apk" 2>/dev/null || stat -f %m "$apk")" "$apk"
  done | sort -n | tail -n 1 | cut -f2-
}

APK="${1:-}"
if [ -z "$APK" ]; then
  APK="$(find_latest_apk || true)"
fi

if [ -z "$APK" ] || [ ! -f "$APK" ]; then
  echo "APK nicht gefunden." >&2
  echo "Erst ausführen: notizen-build-apk" >&2
  echo "Gesucht wurde u.a. unter:" >&2
  echo "  $HOME/notizen-debug.apk" >&2
  echo "  $WORK/build/termux-apk" >&2
  exit 1
fi

echo "Installiere APK: $APK"
echo "Wichtig: Wenn der Android-Paketinstaller erscheint, dort Aktualisieren/Installieren bestätigen."
echo "Erwarteter APK-Build: md-table-runtime-verify-v117 / NotizenJavaAndroidNativ-v117-debug.apk"

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
