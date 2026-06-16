# Maestro UI Tests

End-to-end UI flows for `app-trucker` and `app-connect`, written in
[Maestro](https://maestro.dev/) YAML.

## Layout

```
maestro/
├── config.yaml
├── flows/
│   ├── _helpers/
│   │   ├── capture_otp_from_notification.yaml  # reads OTP from Android shade
│   │   ├── dismiss_system_prompts.yaml         # dismisses permission dialogs
│   │   ├── assert_home_loaded.yaml             # parameterized by HOME_TEXT
│   │   ├── login_trucker.yaml                  # trucker login (hard-coded phone)
│   │   └── login_connect.yaml                  # connect login (hard-coded phone)
│   ├── smoke/
│   │   ├── app-trucker/01_launch_login_home.yaml
│   │   └── app-connect/01_launch_login_home.yaml
│   └── regression/         # add longer E2E flows here
├── ci/run_maestro.sh
└── snapshots/              # baseline screenshots (optional)
```

## Prerequisites

- [Maestro CLI](https://maestro.dev/docs/getting-started/install-maestro):
  `curl -Ls "https://get.maestro.mobile.dev" | bash`
- An Android device or emulator (`adb devices` should list it).
- The QA debug APKs of both apps, built and installed.

## Build & install the APKs

```bash
cd /Users/vaibhav/Documents/Projects/F24/Freight24MobileApp
export JAVA_HOME=$(/usr/libexec/java_home -v 23)
./gradlew :app-trucker:assembleQaDebug :app-connect:assembleQaDebug

EMULATOR="adb-001196478009045-leuQws._adb-tls-connect._tcp"
adb -s "$EMULATOR" install -r -t \
  app-trucker/build/outputs/apk/qa/debug/app-trucker-qa-debug.apk
adb -s "$EMULATOR" install -r -t \
  app-connect/build/outputs/apk/qa/debug/app-connect-qa-debug.apk
```

> **Note:** `adb install` is run separately, *not* from `run_maestro.sh`.
> Doing both in the same script breaks Maestro's adb forward tunnel and
> causes the run to fail with `io.grpc.StatusRuntimeException: UNAVAILABLE`.

## Run

The runner script is the easiest entry point — it pre-flights the device
and the installed packages, then runs both smoke flows in sequence:

```bash
EMULATOR="adb-001196478009045-leuQws._adb-tls-connect._tcp"
EMULATOR_NAME="$EMULATOR" ./maestro/ci/run_maestro.sh
```

For finer-grained control, call `maestro test` directly.

### Module-specific runs

Maestro accepts a directory and runs every `.yaml` it finds there
(recursively). Use the folder layout to filter by app or by suite:

```bash
# Both apps, all smoke flows
maestro test maestro/flows/smoke

# Trucker only
maestro test maestro/flows/smoke/app-trucker

# Connect only
maestro test maestro/flows/smoke/app-connect

# Future: regression suite (one app, longer flows)
maestro test maestro/flows/regression/app-trucker
```

### A single flow

```bash
# Trucker smoke
maestro test maestro/flows/smoke/app-trucker/01_launch_login_home.yaml

# Connect smoke
maestro test maestro/flows/smoke/app-connect/01_launch_login_home.yaml

# A helper in isolation (e.g. to debug OTP capture)
maestro test maestro/flows/_helpers/capture_otp_from_notification.yaml
```

### Tag-based filtering

Flows are commented with `@smoke`, `@regression`, etc. as a convention,
but Maestro doesn't read those automatically. Two practical approaches:

1. **Folder structure as the tag.** Put smoke flows under
   `flows/smoke/`, regression under `flows/regression/`, and run the
   folder you want.
2. **Glob by filename prefix.** Numbered flows (`01_*`, `02_*`) can be
   selected with a shell glob:
   ```bash
   maestro test "maestro/flows/smoke/app-trucker/0[12]_*"
   ```

If you want true tag-based filtering, check whether your Maestro
version supports `--include-tags` / `--exclude-tags`:
```bash
maestro test --help
```

### Targeting a specific device

```bash
# Via env var (used by run_maestro.sh)
EMULATOR_NAME="adb-001196478009045-leuQws._adb-tls-connect._tcp"   maestro test maestro/flows/smoke/app-trucker

# Via Maestro's --device flag, if your version supports it
maestro test --device "adb-001196478009045-leuQws._adb-tls-connect._tcp"   maestro/flows/smoke/app-trucker
```

If multiple devices are connected, `maestro test` will fail with
"multiple devices" — set `EMULATOR_NAME` (or `--device`) to disambiguate.


## Phone numbers

The phone numbers are hard-coded in the per-app login helpers
(`login_trucker.yaml` and `login_connect.yaml`). To change them, edit
the `inputText` line in the relevant file. This is intentional —
template-based env propagation was unreliable in our setup, and a
hard-coded value is the most predictable path for a smoke test.

## OTP capture

The Freight24 backend does not expose a test-mode OTP, so the smoke
test reads the actual SMS from the Android notification shade. Make
sure the device can receive SMS, or trigger a code by tapping "Send
OTP" in the app. If the SMS body template changes, update the offset
in `capture_otp_from_notification.yaml` (see the `FIX:` comment).

## Selectors used

The flows target test tags that must be present in the Compose code
on the four interactive elements of the login screens:

| Tag | What it marks |
|---|---|
| `phone_input` | Phone number `OutlinedTextField` |
| `send_otp_btn` | "Send OTP" `Button` |
| `otp_input` | The visible row of OTP boxes (clickable wrapper) |
| `verify_btn` | "Verify" `Button` |

Each tag is paired with `.semantics { testTagsAsResourceId = true }`
so Maestro sees them as resource IDs.
