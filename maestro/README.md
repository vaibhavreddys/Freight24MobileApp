# Maestro UI Tests

End-to-end UI flows for `app-trucker` and `app-connect`, written in
[Maestro](https://maestro.dev/) YAML.

## Layout

```
maestro/
├── run.sh                  # ENTRY POINT — env setup + maestro test
├── config.yaml
├── flows/
│   ├── _helpers/           # shared building blocks (not tests)
│   │   ├── ensure_login_trucker.yaml     # reuse session or full OTP login
│   │   ├── login_trucker.yaml            # trucker OTP login (env-driven)
│   │   ├── login_connect.yaml            # connect OTP login (env-driven)
│   │   ├── fetch_otp_from_backend.yaml   # standalone OTP debug helper
│   │   ├── dismiss_system_prompts.yaml   # permission dialog handling
│   │   ├── assert_home_loaded.yaml       # parameterized by HOME_TEXT
│   │   ├── assert_load_board.yaml        # Loads tab: cards or empty state
│   │   ├── open_tab.yaml                 # switch bottom-nav tab (TAB_ID)
│   │   ├── log_and_settle.yaml           # LOG_STEP marker + pause for AI screenshot (LOG_MESSAGE, SETTLE_MS)
│   │   ├── trip_details_walkthrough.yaml # Documents→Payments→Timeline drill on an open Trip Details screen
│   │   └── gallery_upload_first_photo.yaml # upload-card → Gallery → first photo → Confirm (UPLOAD_CARD_ID)
│   ├── smoke/
│   │   ├── app-trucker/
│   │   │   ├── 01_launch_login_home.yaml
│   │   │   ├── 02_logout.yaml
│   │   │   ├── loads/                    # Loads tab flows
│   │   │   │   ├── 01_load_board_city_cards.yaml
│   │   │   │   ├── 02_city_expand_load_cards.yaml
│   │   │   │   ├── 03_load_bookmark_interest.yaml
│   │   │   │   ├── 04_load_detail_bottom_sheet.yaml
│   │   │   │   └── 05_load_call_dialer.yaml
│   │   │   ├── trips/                    # Trips tab flows
│   │   │   │   ├── 01_tab_switch.yaml
│   │   │   │   ├── 02_trips_filters.yaml               # all 4 filter chips; cards OR empty state
│   │   │   │   ├── 03_trip_details_active.yaml         # ACTIVE trip → details walkthrough
│   │   │   │   └── 04_trip_details_pod_completed_all.yaml # same drill for POD/COMPLETED/ALL
│   │   │   ├── trucks/                   # Trucks tab flows
│   │   │   │   ├── 01_tab_switch.yaml
│   │   │   │   ├── 02_trucks_filters.yaml             # 4 filter chips + onboard button; empty-safe
│   │   │   │   ├── 03_truck_details_filters.yaml      # first card per filter → Truck Details
│   │   │   │   └── 04_onboard_truck.yaml              # full onboarding: form + 3 gallery uploads
│   │   │   └── profile/01_tab_switch.yaml
│   │   └── app-connect/
│   │       └── 01_launch_login_home.yaml
│   └── regression/         # add longer E2E flows here
├── ci/run_maestro.sh
└── TAB_FLOW_IMPLEMENTATION_PROMPT.md  # template for adding new tab flows
```

Convention: one sub-folder per bottom-nav tab under each app, numbered
flows define execution order. Add new tabs as new sub-folders.

## Prerequisites

- [Maestro CLI](https://maestro.dev/docs/getting-started/install-maestro)
- The Samsung test device over adb (`adb devices`), screen **on and unlocked**
  (it re-locks when it sleeps; `adb shell input keyevent KEYCODE_WAKEUP` +
  manual unlock, or `svc power stayon true` while charging).
- The QA debug APK installed:
  `./scripts/dev-loop.sh` (trucker) or `APP_MODULE=app-connect ./scripts/dev-loop.sh`
- **The backend running in dev mode** (OTP logins need it):
  ```bash
  cd backendapi && ./scripts/dev.sh restart-dev
  ```

## Run

`run.sh` supplies everything flows need as Maestro env vars and pins the
device:

- `BASE_URL` — read from `Freight24MobileApp/local.properties` (single source
  of truth, same backend the app was built against)
- `PHONE_NUMBER` — 10-digit test login number (default `7022457678`)
- `DEVICE_SERIAL` — adb serial (default `001196478009045`)

```bash
cd Freight24MobileApp/maestro

./run.sh flows/smoke/app-trucker/01_launch_login_home.yaml   # one flow
./run.sh "flows/smoke/app-trucker/loads"                     # one tab folder
# whole app — NOTE: folder args are NOT recursive, so glob the levels:
./run.sh flows/smoke/app-trucker/*.yaml flows/smoke/app-trucker/*/*.yaml

# Overrides
PHONE_NUMBER=9876543210 ./run.sh flows/smoke/app-trucker/01_launch_login_home.yaml
DEVICE_SERIAL=abc123 ./run.sh flows/smoke/app-connect/01_launch_login_home.yaml
```

If Maestro fails with `Command failed (tcp:xxxxx): closed`, restart the adb
server first: `adb kill-server && adb start-server`.

> `adb install` and `maestro test` must not run in the same shell script —
> it breaks Maestro's adb forward tunnel (`io.grpc StatusRuntimeException:
> UNAVAILABLE`). This is why install lives in `dev-loop.sh`, not here.

## How login works (no SMS involved)

`login_trucker.yaml` / `login_connect.yaml` request the OTP from the app as
usual, then fetch the code from the backend's dev-only endpoint
`GET /auth/dev/last-otp` — active only when the server runs with
`DEVELOPMENT_MODE=true` (`./scripts/dev.sh restart-dev`). The old
notification-shade scraping is gone.

Two Maestro 2.6.1 quirks the helpers are written around — keep them in mind
when editing:

1. **Env vars inside `evalScript` are bare JS identifiers** (`BASE_URL`,
   `PHONE_NUMBER`), not `'${VAR}'`. The outer `${...}` template does not
   nest, so quoted interpolation inside evalScript breaks with
   `Missing close quote`.
2. **`output.*` only persists from a single-line `${...}` evalScript.** A raw
   multi-line script body runs in a throwaway scope, and `output` set inside
   a nested `runFlow` does not propagate to the parent.
3. **There is no sleep/wait command, and the JS sandbox has no timers.**
   Top-level `await` in `runScript` and `Java.type(...)` interop are both
   unavailable, so timed pauses are a JS busy-wait inside a single-line
   evalScript (see `_helpers/log_and_settle.yaml`). `takeScreenshot` also
   only writes files for literal paths — an interpolated `${...}` path
   silently saves nothing.

## Selectors (test tags)

Maestro's `id` selector only sees Compose test tags exposed via
`.semantics { testTagsAsResourceId = true }` — content descriptions are
*not* matched by `id` (but `tapOn`/`assertVisible` `text` does match them).

| Tag | What it marks |
|---|---|
| `phone_input` / `send_otp_btn` / `otp_input` / `verify_btn` | Login screens (both apps) |
| `tab_loads` / `tab_trips` / `tab_trucks` / `tab_profile` | Bottom nav (FreightBottomNavBar) |
| `city_card` | City card on the Loads board |
| `load_card` | Load card on the Loads board |
| `load_bookmark_btn` | Show-interest (bookmark) button on a load card |
| `load_call_btn` | Call button on a load card |
| `load_detail_sheet` | Load detail bottom sheet container |
| `sheet_close_btn` / `sheet_call_btn` / `express_interest_btn` | Bottom sheet actions |
| `trips_filter_active` / `trips_filter_pod` / `trips_filter_completed` / `trips_filter_all` | Filter chips at the top of the Trips tab |
| `trip_card` | Trip card on the Trips list |
| `trip_details_back_btn` | Back button on the Trip Details screen |
| `trip_details_documents_card` / `trip_details_payments_card` / `trip_details_timeline_card` | Expandable-section headers on Trip Details (tap toggles) |
| `trucks_filter_all` / `trucks_filter_available` / `trucks_filter_on_trip` / `trucks_filter_blacklisted` | Filter chips at the top of the Trucks tab |
| `truck_card` | Truck card on the Trucks list |
| `trucks_onboard_btn` | "Onboard Your New Truck" pill at the list bottom |
| `truck_details_back_btn` | Back button on the Truck Details screen |
| `onboard_reg_input` / `onboard_type_dropdown` / `onboard_make_input` / `onboard_model_input` / `onboard_year_input` | Onboarding step 1 (Basic Info) fields |
| `onboard_next_btn` / `onboard_complete_btn` / `onboard_back_btn` | Onboarding step navigation buttons |
| `onboard_upload_rc_card` / `onboard_upload_insurance_card` / `onboard_upload_fc_card` | Dashed upload cards on onboarding step 2 |
| `onboard_view_fleet_btn` | "View My Fleet" on the onboarding success screen |
| `source_picker_camera` / `source_picker_gallery` / `source_picker_document` | "Select Document Source" bottom-sheet options |
| `profile_logout` / `logout_confirm` | Profile logout (content-desc, matched via `text`) |

When adding UI, always add a test tag + `testTagsAsResourceId` — see
`TAB_FLOW_IMPLEMENTATION_PROMPT.md` for the full recipe.

## Data prerequisites

- The Loads flows need at least one indent on the board
  (`01_load_board_city_cards.yaml` passes on an empty board; the rest fail
  with a clear "element not found" if the board is empty).
- Interest (bookmark) flows toggle twice so backend state is unchanged after
  each run — safe to re-run.
- The Trips flows pass on an empty account: every filter asserts either trip
  cards or the filter's "No ... Trips Available" empty state, and the details
  walkthrough only runs when the filter actually lists trips.
- The Trucks flows are empty-safe the same way ("No trucks found"). Exceptions:
  - `trucks/04_onboard_truck.yaml` **creates a real truck on the backend on
    every run** (registration number `TS09AB` + 4 semi-random digits keeps
    re-runs collision-free) — the fleet grows by one per run.
  - The onboarding uploads need **at least one photo** in the device's photo
    picker (the flow always picks the first one).

## AI feedback loop (LOG_STEP markers)

Trips flows (via `_helpers/log_and_settle.yaml`) emit a `LOG_STEP: <what
happened> (paused Nms for screenshot)` marker after every meaningful click
and then pause (default 2.5–3s) so an observing AI agent can screenshot the
screen mid-flow. Markers appear:

- on stdout — every evalScript command line is echoed as it runs
- in `maestro.log` inside Maestro's per-run debug dir
  (`/Users/vaibhav/.maestro/tests/<timestamp>/`) as `JsConsole:` lines

Pass `SETTLE_MS` to lengthen/shorten a pause; the message itself is the
`LOG_MESSAGE` env passed by each call site.
