#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
BUILD_DIR="${TMPDIR:-/tmp}/notizen-core-tests-$$"
trap 'rm -rf "$BUILD_DIR"' EXIT
rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR" "$BUILD_DIR/empty-sourcepath"
echo "[1/3] Java-Kern kompilieren"
javac -proc:none -XDcompilePolicy=simple -J-Xmx256m -J-XX:ActiveProcessorCount=1 -encoding UTF-8 -d "$BUILD_DIR" "$ROOT"/app/src/main/java/de/notizen/android/core/*.java
echo "[2/3] TestCore kompilieren"
javac -proc:none -XDcompilePolicy=simple -J-Xmx256m -J-XX:ActiveProcessorCount=1 -encoding UTF-8 -sourcepath "$BUILD_DIR/empty-sourcepath" -cp "$BUILD_DIR" -d "$BUILD_DIR" "$ROOT"/tools/TestCore.java
echo "[3/3] Core-Tests ausführen"
java -cp "$BUILD_DIR" TestCore
