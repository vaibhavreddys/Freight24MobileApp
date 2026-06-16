#!/usr/bin/env bash
# maestro/ci/run_maestro.sh
#
# Runs all smoke flows against the QA debug APKs.
#
# IMPORTANT: install the APKs yourself first with `adb install`. The
# install step is intentionally not part of this script because `adb
# install` disrupts the adb forward tunnel that Maestro relies on,
# causing the run to fail with:
#   io.grpc.StatusRuntimeException: UNAVAILABLE
#   Caused by: java.io.IOException: Command failed (tcp:NNNNN): closed
#
# Usage:
#   adb install -r -t app-trucker/build/outputs/apk/qa/debug/app-trucker-qa-debug.apk
#   adb install -r -t app-connect/build/outputs/apk/qa/debug/app-connect-qa-debug.apk
#   ./maestro/ci/run_maestro.sh
#
# Optional env:
#   EMULATOR_NAME   - adb device to use (default: first ready device)

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
MAESTRO_ROOT="$REPO_ROOT/maestro"
RESULTS_DIR="$MAESTRO_ROOT/ci/.results"
mkdir -p "$RESULTS_DIR"

# --- Helpers ---------------------------------------------------------------
log()  { printf '\033[1;34m[maestro]\033[0m %s\n' "$*"; }
fail() { printf '\033[1;31m[maestro]\033[0m %s\n' "$*" >&2; exit 1; }

# --- Locate device ---------------------------------------------------------
emulator_serial="${EMULATOR_NAME:-}"
if [[ -z "$emulator_serial" ]]; then
  emulator_serial="$(adb devices | awk 'NR>1 && $2=="device" {print $1; exit}')"
fi
[[ -n "$emulator_serial" ]] || fail "No Android device/emulator available (adb devices is empty)"

# Verify device is actually reachable on a basic adb command. This catches
# the case where `adb devices` lists a stale entry but the socket is dead.
adb -s "$emulator_serial" shell true >/dev/null 2>&1 \
  || fail "Device $emulator_serial is not responsive to adb shell"
log "Using device: $emulator_serial"

# --- Verify APKs are already installed ------------------------------------
# We don't install here (see header). Just check that the apps are present
# on the device so we get a clear error early.
trucker_pkg="com.freight24.trucker.debug"
connect_pkg="com.freight24.connect.debug"
if ! adb -s "$emulator_serial" shell pm list packages --user 0 | grep -q "$trucker_pkg"; then
  fail "$trucker_pkg is not installed. Run: adb -s $emulator_serial install -r -t <trucker-apk>"
fi
if ! adb -s "$emulator_serial" shell pm list packages --user 0 | grep -q "$connect_pkg"; then
  fail "$connect_pkg is not installed. Run: adb -s $emulator_serial install -r -t <connect-apk>"
fi

# --- Run flows -------------------------------------------------------------
overall_status=0
for flow in \
  "$MAESTRO_ROOT/flows/smoke/app-trucker/01_launch_login_home.yaml" \
  "$MAESTRO_ROOT/flows/smoke/app-connect/01_launch_login_home.yaml"
do
  log "Running: $flow"
  if maestro test "$flow" --output "$RESULTS_DIR"; then
    log "PASS: $flow"
  else
    printf '\033[1;31m[maestro]\033[0m FAIL: %s\n' "$flow" >&2
    overall_status=1
  fi
done

exit "$overall_status"
