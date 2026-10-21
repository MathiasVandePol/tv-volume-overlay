#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")/.."
exec ./gradlew testReleaseUnitTest lintRelease assembleDebug "$@"
