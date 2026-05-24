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
VERSION_CODE="${VERSION_CODE:-120}"
VERSION_NAME="${VERSION_NAME:-1.0.120-java-android-nativ-loose-table}"
ANDROID_HOME="${ANDROID_HOME:-$HOME/android-sdk}"
ANDROID_JAR="${ANDROID_JAR:-$ANDROID_HOME/platforms/android-$COMPILE_API/android.jar}"
AAPT2="${AAPT2:-$(command -v aapt2 || true)}"
D8="${D8:-$ANDROID_HOME/cmdline-tools/latest/bin/d8}"
APKSIGNER="${APKSIGNER:-$(command -v apksigner || true)}"
ZIPALIGN="${ZIPALIGN:-$(command -v zipalign || true)}"
JAVAC="${JAVAC:-javac}"
JAVAC_RELEASE="${JAVAC_RELEASE:-17}"
ZIP="${ZIP:-zip}"
UNZIP="${UNZIP:-$(command -v unzip || true)}"
JAR="${JAR:-jar}"
JAVA="${JAVA:-java}"
CURL="${CURL:-$(command -v curl || true)}"
WGET="${WGET:-$(command -v wget || true)}"
COMMONMARK_VERSION="${COMMONMARK_VERSION:-0.28.0}"
AUTOLINK_VERSION="${AUTOLINK_VERSION:-0.12.0}"
INCLUDE_COMMONMARK_DEPS="${INCLUDE_COMMONMARK_DEPS:-1}"
COMMONMARK_REPO_URL="${COMMONMARK_REPO_URL:-https://repo1.maven.org/maven2}"
COMMONMARK_DEPS_DIR="${COMMONMARK_DEPS_DIR:-$PROJECT_DIR/build/markdown-deps}"
COMMONMARK_COORDS=(
  "org.commonmark:commonmark:$COMMONMARK_VERSION"
  "org.commonmark:commonmark-ext-autolink:$COMMONMARK_VERSION"
  "org.nibor.autolink:autolink:$AUTOLINK_VERSION"
  "org.commonmark:commonmark-ext-gfm-strikethrough:$COMMONMARK_VERSION"
  "org.commonmark:commonmark-ext-gfm-tables:$COMMONMARK_VERSION"
  "org.commonmark:commonmark-ext-gfm-alerts:$COMMONMARK_VERSION"
  "org.commonmark:commonmark-ext-footnotes:$COMMONMARK_VERSION"
  "org.commonmark:commonmark-ext-heading-anchor:$COMMONMARK_VERSION"
  "org.commonmark:commonmark-ext-ins:$COMMONMARK_VERSION"
  "org.commonmark:commonmark-ext-task-list-items:$COMMONMARK_VERSION"
  "org.commonmark:commonmark-ext-image-attributes:$COMMONMARK_VERSION"
  "org.commonmark:commonmark-ext-yaml-front-matter:$COMMONMARK_VERSION"
)
COMMONMARK_JARS=()
COMMONMARK_RESOURCE_COUNT=0
COMMONMARK_REQUIRED_RESOURCE="org/commonmark/internal/util/entities.txt"
JAVAC_LANG_ARGS=()
KEYSTORE="${KEYSTORE:-$HOME/.android/debug.keystore}"
KEY_ALIAS="${KEY_ALIAS:-androiddebugkey}"
STOREPASS="${STOREPASS:-android}"
KEYPASS="${KEYPASS:-android}"

need_file() { [ -f "$1" ] || { echo "Fehlt: $1" >&2; exit 1; }; }
need_exec() { [ -n "$1" ] && [ -x "$1" ] || { echo "Fehlt/ nicht ausführbar: $2" >&2; exit 1; }; }
need_cmd() { command -v "$1" >/dev/null 2>&1 || { echo "Fehlt im PATH: $1" >&2; exit 1; }; }

download_file() {
  local url="$1"
  local target="$2"
  if [ -n "$CURL" ] && [ -x "$CURL" ]; then
    "$CURL" -fL --retry 2 --connect-timeout 20 -o "$target" "$url"
  elif [ -n "$WGET" ] && [ -x "$WGET" ]; then
    "$WGET" -O "$target" "$url"
  else
    echo "Fehlt: curl oder wget für CommonMark-Abhängigkeiten. Setze INCLUDE_COMMONMARK_DEPS=0 für den Fallback-Renderer." >&2
    exit 1
  fi
}

maven_path_for_coord() {
  local group="$1"
  local artifact="$2"
  local version="$3"
  printf '%s/%s/%s/%s-%s.jar' "${group//.//}" "$artifact" "$version" "$artifact" "$version"
}

ensure_commonmark_deps() {
  COMMONMARK_JARS=()
  if [ "$INCLUDE_COMMONMARK_DEPS" = "0" ]; then
    echo "==> CommonMark-Abhängigkeiten übersprungen; der lokale Markdown-Fallback bleibt aktiv."
    return
  fi
  mkdir -p "$COMMONMARK_DEPS_DIR"
  local coord group artifact version path jar url tmp
  for coord in "${COMMONMARK_COORDS[@]}"; do
    IFS=':' read -r group artifact version <<< "$coord"
    path="$(maven_path_for_coord "$group" "$artifact" "$version")"
    jar="$COMMONMARK_DEPS_DIR/${artifact}-${version}.jar"
    if [ ! -s "$jar" ]; then
      url="$COMMONMARK_REPO_URL/$path"
      tmp="$jar.tmp"
      rm -f "$tmp"
      printf '==> Lade Markdown-Abhängigkeit: %s:%s:%s\n' "$group" "$artifact" "$version"
      download_file "$url" "$tmp"
      mv "$tmp" "$jar"
    fi
    COMMONMARK_JARS+=("$jar")
  done
}

join_by_colon() {
  local joined=""
  local item
  for item in "$@"; do
    if [ -z "$joined" ]; then joined="$item"; else joined="$joined:$item"; fi
  done
  printf '%s' "$joined"
}

extract_commonmark_java_resources() {
  COMMONMARK_RESOURCE_COUNT=0
  JAVA_RES_DIR="$BUILD_DIR/java-res"
  rm -rf "$JAVA_RES_DIR"
  mkdir -p "$JAVA_RES_DIR"
  if [ "${#COMMONMARK_JARS[@]}" -eq 0 ]; then
    return
  fi

  local jar entry target
  for jar in "${COMMONMARK_JARS[@]}"; do
    while IFS= read -r entry; do
      case "$entry" in
        ""|*/|*.class|META-INF/*) continue ;;
      esac
      # D8 converts classes only. CommonMark also needs package resources at
      # runtime, especially org/commonmark/internal/util/entities.txt for HTML
      # entity handling. Put runtime package resources into the APK root so
      # Class.getResourceAsStream(...) can find them on Android.
      case "$entry" in
        org/*) ;;
        *) continue ;;
      esac
      target="$JAVA_RES_DIR/$entry"
      mkdir -p "$(dirname "$target")"
      "$UNZIP" -p "$jar" "$entry" > "$target"
      COMMONMARK_RESOURCE_COUNT=$((COMMONMARK_RESOURCE_COUNT + 1))
    done < <("$JAR" tf "$jar")
  done

  if [ ! -s "$JAVA_RES_DIR/$COMMONMARK_REQUIRED_RESOURCE" ]; then
    echo "Fehlt CommonMark-Runtime-Ressource: $COMMONMARK_REQUIRED_RESOURCE" >&2
    echo "Ohne diese Datei lädt commonmark-java auf Android nicht zuverlässig und die App fällt in den Legacy-Fallback." >&2
    exit 1
  fi
}

add_commonmark_java_resources_to_apk() {
  local apk="$1"
  if [ "${#COMMONMARK_JARS[@]}" -eq 0 ] || [ "${COMMONMARK_RESOURCE_COUNT:-0}" -eq 0 ]; then
    return
  fi
  if [ ! -d "$JAVA_RES_DIR" ]; then
    echo "CommonMark-Ressourcenordner fehlt: $JAVA_RES_DIR" >&2
    exit 1
  fi
  ( cd "$JAVA_RES_DIR" && find . -type f -print | sed 's#^\./##' | "$ZIP" -q -u "$apk" -@ )
}

apk_dex_contains_string() {
  local apk="$1"
  local needle="$2"
  local tmp_dir="$BUILD_DIR/verify-apk-dex"
  local dex_names name out found
  found=0
  rm -rf "$tmp_dir"
  mkdir -p "$tmp_dir"

  # Do not use `unzip -p ... | grep -q ...` here.  With `set -o pipefail`,
  # grep may exit as soon as it finds the marker, unzip then receives SIGPIPE,
  # and the whole pipeline looks like a failure although the marker exists.
  dex_names="$($JAR tf "$apk" | grep -E '^classes([0-9]+)?\.dex$' || true)"
  if [ -z "$dex_names" ]; then
    rm -rf "$tmp_dir"
    return 2
  fi

  while IFS= read -r name; do
    [ -n "$name" ] || continue
    out="$tmp_dir/$(basename "$name")"
    "$UNZIP" -p "$apk" "$name" > "$out"
    if grep -aF "$needle" "$out" >/dev/null; then
      found=1
    fi
  done <<< "$dex_names"

  rm -rf "$tmp_dir"
  [ "$found" -eq 1 ]
}

verify_commonmark_dex_dir_payload() {
  if [ "${#COMMONMARK_JARS[@]}" -eq 0 ]; then
    return
  fi
  local dex found_bridge found_marker
  found_bridge=0
  found_marker=0
  for dex in "$BUILD_DIR/dex"/classes*.dex; do
    [ -f "$dex" ] || continue
    if grep -aF 'CommonmarkMarkdownRenderer' "$dex" >/dev/null; then found_bridge=1; fi
    if grep -aF 'md-loose-table-runtime-verify-v120' "$dex" >/dev/null; then found_marker=1; fi
  done
  if [ "$found_bridge" -ne 1 ]; then
    echo "D8-Ausgabe enthält keinen CommonmarkMarkdownRenderer. Diese APK würde in den Fallback gehen." >&2
    echo "DEX-Ordner: $BUILD_DIR/dex" >&2
    exit 1
  fi
  if [ "$found_marker" -ne 1 ]; then
    echo "D8-Ausgabe enthält nicht den aktuellen Markdown-Tabellen-Fix md-loose-table-runtime-verify-v120." >&2
    echo "DEX-Ordner: $BUILD_DIR/dex" >&2
    exit 1
  fi
  echo "CommonMark DEX geprüft: CommonmarkMarkdownRenderer + md-loose-table-runtime-verify-v120"
}

verify_commonmark_apk_payload() {
  local apk="$1"
  if [ "${#COMMONMARK_JARS[@]}" -eq 0 ]; then
    return
  fi
  if ! "$UNZIP" -l "$apk" "$COMMONMARK_REQUIRED_RESOURCE" >/dev/null 2>&1; then
    echo "APK enthält die notwendige CommonMark-Runtime-Ressource nicht: $COMMONMARK_REQUIRED_RESOURCE" >&2
    echo "APK: $apk" >&2
    exit 1
  fi
  if ! apk_dex_contains_string "$apk" 'CommonmarkMarkdownRenderer'; then
    echo "APK enthält keinen CommonmarkMarkdownRenderer im DEX. Diese APK würde in den Fallback gehen." >&2
    echo "APK: $apk" >&2
    echo "DEX-Einträge in der APK:" >&2
    "$JAR" tf "$apk" | grep -E '^classes([0-9]+)?\.dex$' >&2 || true
    exit 1
  fi
  if ! apk_dex_contains_string "$apk" 'md-loose-table-runtime-verify-v120'; then
    echo "APK enthält nicht den aktuellen Markdown-Tabellen-Fix md-loose-table-runtime-verify-v120." >&2
    echo "APK: $apk" >&2
    echo "DEX-Einträge in der APK:" >&2
    "$JAR" tf "$apk" | grep -E '^classes([0-9]+)?\.dex$' >&2 || true
    exit 1
  fi
  echo "CommonMark APK-DEX geprüft: CommonmarkMarkdownRenderer + md-loose-table-runtime-verify-v120"
}

run_commonmark_jvm_smoke_test() {
  if [ "${#COMMONMARK_JARS[@]}" -eq 0 ]; then
    return
  fi
  local cp output
  cp="$BUILD_DIR/classes:$COMMONMARK_CP"
  if ! output="$("$JAVA" -classpath "$cp" de.notizen.android.markdown.CommonmarkMarkdownRenderer --health-check 2>&1)"; then
    echo "CommonMark JVM smoke-test fehlgeschlagen:" >&2
    echo "$output" >&2
    exit 1
  fi
  printf 'CommonMark JVM smoke-test: %s\n' "$output"
}

prepare_manifest_for_aapt2() {
  local source_manifest="$1"
  local target_manifest="$2"
  if grep -Eq '<manifest[^>]*[[:space:]]package[[:space:]]*=' "$source_manifest"; then
    cp "$source_manifest" "$target_manifest"
    return
  fi
  awk -v pkg="$PACKAGE" '
    !done && /<manifest([[:space:]>])/ {
      sub(/<manifest/, "<manifest package=\"" pkg "\"")
      done=1
    }
    { print }
  ' "$source_manifest" > "$target_manifest"
  if ! grep -Eq '<manifest[^>]*[[:space:]]package[[:space:]]*=' "$target_manifest"; then
    echo "Konnte kein package-Attribut in das Manifest für aapt2 einfügen: $target_manifest" >&2
    exit 1
  fi
}

build_javac_language_args() {
  JAVAC_LANG_ARGS=()
  if "$JAVAC" --help 2>&1 | grep -q -- '--release'; then
    JAVAC_LANG_ARGS=(--release "$JAVAC_RELEASE")
  else
    JAVAC_LANG_ARGS=(-Xlint:-options -source "$JAVAC_RELEASE" -target "$JAVAC_RELEASE")
  fi
}

need_file "$ANDROID_JAR"
need_exec "$AAPT2" "aapt2"
need_exec "$D8" "d8"
need_exec "$APKSIGNER" "apksigner"
need_exec "$ZIPALIGN" "zipalign"
need_cmd "$JAVAC"
need_cmd "$ZIP"
need_cmd "$UNZIP"
need_cmd "$JAR"
need_cmd "$JAVA"
need_cmd keytool
build_javac_language_args
ensure_commonmark_deps

rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR"/res "$BUILD_DIR"/gen "$BUILD_DIR"/classes "$BUILD_DIR"/dex "$BUILD_DIR"/out
extract_commonmark_java_resources

MANIFEST="$APP_DIR/AndroidManifest.xml"
AAPT2_MANIFEST="$BUILD_DIR/AndroidManifest.aapt2.xml"
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
printf 'javac language mode: %s\n' "${JAVAC_LANG_ARGS[*]}"
printf 'aapt2: %s\n' "$AAPT2"
printf 'd8: %s\n' "$D8"
printf 'apksigner: %s\n' "$APKSIGNER"
printf 'zipalign: %s\n' "$ZIPALIGN"
if [ "${#COMMONMARK_JARS[@]}" -gt 0 ]; then
  printf 'CommonMark/GFM jars: %s\n' "${#COMMONMARK_JARS[@]}"
  printf 'CommonMark Java resources: %s\n' "${COMMONMARK_RESOURCE_COUNT:-0}"
  printf 'CommonMark required resource: %s\n' "$COMMONMARK_REQUIRED_RESOURCE"
fi
printf '\n'

printf '==> Ressourcen mit aapt2 kompilieren\n'
"$AAPT2" compile --dir "$RES_DIR" -o "$RES_ZIP"

prepare_manifest_for_aapt2 "$MANIFEST" "$AAPT2_MANIFEST"

printf '\n==> APK-Basis mit aapt2 linken\n'
"$AAPT2" link \
  -I "$ANDROID_JAR" \
  --manifest "$AAPT2_MANIFEST" \
  --custom-package "$PACKAGE" \
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
if [ "${#COMMONMARK_JARS[@]}" -eq 0 ]; then
  grep -v '/de/notizen/android/markdown/CommonmarkMarkdownRenderer.java$' "$BUILD_DIR/sources.txt" > "$BUILD_DIR/sources.filtered.txt"
  mv "$BUILD_DIR/sources.filtered.txt" "$BUILD_DIR/sources.txt"
fi
COMMONMARK_CP="$(join_by_colon "${COMMONMARK_JARS[@]}")"
JAVAC_CP="$ANDROID_JAR:$BUILD_DIR/gen"
if [ -n "$COMMONMARK_CP" ]; then JAVAC_CP="$JAVAC_CP:$COMMONMARK_CP"; fi
"$JAVAC" "${JAVAC_LANG_ARGS[@]}" \
  -encoding UTF-8 \
  -classpath "$JAVAC_CP" \
  -sourcepath "$JAVA_SRC:$BUILD_DIR/gen" \
  -d "$BUILD_DIR/classes" \
  @"$BUILD_DIR/sources.txt"
run_commonmark_jvm_smoke_test

printf '\n==> .class nach DEX konvertieren\n'
find "$BUILD_DIR/classes" -name '*.class' -print > "$BUILD_DIR/classes.txt"
mapfile -t CLASS_FILES < "$BUILD_DIR/classes.txt"
PROGRAM_FILES=("${CLASS_FILES[@]}")
if [ "${#COMMONMARK_JARS[@]}" -gt 0 ]; then PROGRAM_FILES+=("${COMMONMARK_JARS[@]}"); fi
"$D8" --lib "$ANDROID_JAR" --min-api "$MIN_SDK" --output "$BUILD_DIR/dex" "${PROGRAM_FILES[@]}"
verify_commonmark_dex_dir_payload

printf '\n==> DEX-Dateien in APK einfügen\n'
cp "$UNALIGNED_APK" "$UNSIGNED_APK"
( cd "$BUILD_DIR/dex" && "$ZIP" -q -u "$UNSIGNED_APK" classes*.dex )

if [ "${COMMONMARK_RESOURCE_COUNT:-0}" -gt 0 ]; then
  printf '\n==> CommonMark-Java-Ressourcen in APK einfügen\n'
  add_commonmark_java_resources_to_apk "$UNSIGNED_APK"
  verify_commonmark_apk_payload "$UNSIGNED_APK"
  printf 'CommonMark APK-Ressource geprüft: %s\n' "$COMMONMARK_REQUIRED_RESOURCE"
fi

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
verify_commonmark_apk_payload "$SIGNED_APK"

printf '\nFertig: %s\n' "$SIGNED_APK"
printf 'Installieren genau diese APK:\n  termux-open "%s"\n' "$SIGNED_APK"
if [ -x "$PROJECT_DIR/notizen-install-apk" ]; then
  printf 'Oder lokal:\n  "%s/notizen-install-apk" "%s"\n' "$PROJECT_DIR" "$SIGNED_APK"
fi
