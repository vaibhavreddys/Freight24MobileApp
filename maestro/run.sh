#!/usr/bin/env bash
# maestro/run.sh — single entry point for running Maestro flows on the
# Samsung test device.
#
# Supplies the environment every flow expects (see README.md):
#   BASE_URL      - backend base URL. Read from Freight24MobileApp/local.properties
#                   (the same value baked into the app), so there is one source
#                   of truth. Override with -e BASE_URL=... after this flag.
#   PHONE_NUMBER  - 10-digit test login number (no +91 prefix).
#   DEVICE_SERIAL - adb serial; passed to maestro test --udid.
#
# Usage:
#   ./run.sh <flow.yaml | flows-folder> [extra maestro args...]
#
# Examples:
#   ./run.sh flows/smoke/app-trucker/01_launch_login_home.yaml
#   ./run.sh flows/smoke/app-trucker/loads
#   ./run.sh flows/smoke/app-trucker/01_launch_login_home.yaml -e PHONE_NUMBER=9876543210
#
# Requires the backend running in dev mode for OTP logins:
#   cd ../../backendapi && ./scripts/dev.sh restart-dev

set -euo pipefail

DIR="$(cd "$(dirname "$0")" && pwd)"
MOBILE_DIR="$(cd "$DIR/.." && pwd)"
LOCAL_PROPERTIES="$MOBILE_DIR/local.properties"

# --- Defaults (overridable via env or trailing -e flags) --------------------
PHONE_NUMBER="${PHONE_NUMBER:-7022457678}"
DEVICE_SERIAL="${DEVICE_SERIAL:-001196478009045}"

# BASE_URL: prefer local.properties so tests always target the same backend
# the installed app was built against.
BASE_URL="${BASE_URL:-}"
if [ -z "$BASE_URL" ] && [ -f "$LOCAL_PROPERTIES" ]; then
  BASE_URL="$(grep -E '^BASE_URL=' "$LOCAL_PROPERTIES" | head -1 | cut -d= -f2- | tr -d '[:space:]')"
fi
BASE_URL="${BASE_URL:-http://192.168.88.4:3000/api/v1/}"
# Flows concatenate "${BASE_URL}auth/..." — guarantee exactly one trailing slash.
BASE_URL="${BASE_URL%/}/"

# --- Run ---------------------------------------------------------------------
# Per-run overrides: pass env vars directly, e.g.
#   PHONE_NUMBER=9876543210 ./run.sh flows/...
# or add extra maestro flags after the flow path, e.g.
#   ./run.sh flows/... --format JUNIT

echo "[run.sh] device=$DEVICE_SERIAL"
echo "[run.sh] BASE_URL=$BASE_URL"
echo "[run.sh] PHONE_NUMBER=$PHONE_NUMBER"

cd "$DIR"
exec maestro test \
  --udid "$DEVICE_SERIAL" \
  -e BASE_URL="$BASE_URL" \
  -e PHONE_NUMBER="$PHONE_NUMBER" \
  "$@"
