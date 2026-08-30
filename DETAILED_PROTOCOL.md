# DETAILED PROTOCOL — reference, rationale, and technique catalog
# (Companion to LEAN_PROTOCOL.md — that file holds the operating rules; this holds the WHY,
# the reference data, and the techniques. Read on demand, not every session.)

## 1. Why the protocol looks like this (token economics)
- Cost ≈ (number of model round trips) × (bytes of accumulated context per round trip).
  After ~100k tokens of history, a trivial `adb tap` round trip costs ~100k input tokens.
  Hence: one screen per session, fewest calls, smallest outputs.
- Images are the densest payloads. Processing the same screenshot twice (Read + analyze_image)
  doubles that cost with little benefit — hence the one-path rule.
- Raw `uiautomator` dumps are single-line XML (14–34KB). Any unfiltered print floods context.
  android_agent.py's filtered JSON is typically 10–30× smaller.
- Verbose third-party outputs (build logs, vision responses) get filtered at the shell with
  `grep`, never dumped.

## 2. Tool decision matrix
| Need | Tool | Notes |
|---|---|---|
| Element refs / visible texts | `android_agent.py state` + python filter | Run from repo cwd; refs expire after each action |
| Tap known coords | `adb shell input tap X Y` | Zero output = zero tokens |
| Type text | `adb shell input text "x"` (`%s`=space) or android_agent `type` | Clear fields first (KEYCODE_DEL loop) |
| Screenshot | `adb exec-out screencap -p > /tmp/x.png` | Then ONE review path (Read) |
| Color verification | python PIL scanline/crop script | Deterministic, no vision needed |
| Alignment verification | bounds from android_agent/ui dump vs screen center (540) | See §4 |
| Build+install+launch | `APP_MODULE=app-connect ./scripts/dev-loop.sh` | grep-filter output |
| Compile-only guard | `./scripts/build.sh trucker` (after core:ui edits) | grep `OK|FAIL` |
| Backend log tail | `grep '"path"' backend_live_status.log \| tail -N` + python parse | statuses per endpoint |

## 3. Device & environment gotchas (hard-won)
- Single UiAutomation slot: uiautomator2 (android_agent), `uiautomator dump` (dev-act), and
  Maestro all compete. Don't interleave; scripts force-stop Maestro first.
- Wireless adb flaps (Samsung rotates ports); USB serial `001196478009045` preferred.
  If device "not found": `./scripts/dev-connect.sh`.
- `cmd uimode night` toggles recreate the activity: scroll position resets, hidden-field focus
  and typed OTP digits can be lost. Re-enter state after toggling.
- uiautomator dump includes off-screen nodes in scrollables — texts.txt ≠ visible viewport.
- Keyboard open hides below-fold elements (they vanish from dumps); they still exist —
  scroll within viewport (not on keyboard) to reach.
- Screenshots of identical content return identical CDN URLs (content-hash dedupe) —
  copy to a fresh filename before Read to avoid serving a stale cached image to review.

## 4. Verification techniques (beyond eyeballing)
- **Alignment audit**: extract bounds, compare center x to screen center (1080-wide device → 540).
  Icon+text groups read +20–30px for the text node alone — that's correct group centering.
  Example caught: OTP countdown chip at cx=210 (330px off), Resend at cx=305.
- **Pixel sampling** (PIL):
  - Scanline runs: find full-width bands of one color (progress tracks, chips).
  - Color-count: `Counter()` on a grid → verify a fill exists (e.g. forest CTA ≈ 9–10k sampled
    px) or is gone (bright mint block removed → count ~10× drop).
  - Dominant palette per region: catches dark-mode leakage, wrong containers.
- **Zoom-crop review**: crop a 1080×600 region and Read it — cheaper and more accurate than
  full-screen second opinions; use to confirm/deny ambiguous verdicts.
- Known-good anchors (Connect theme): light bg #FAFBFA, dark bg #101412, primary light #1B5E40,
  primary dark (mint) #A6D3BB, primaryContainer light #C2EBD3, dark container #00492C,
  outlineVariant dark #3F4842 (= surfaceVariant dark), success light #1E6B43 / dark #8FD0AA,
  warning light #8A5800 / dark #FFC96B.

## 5. Design rules established this project (apply, don't re-derive)
- Radii scale: 16dp cards, 14dp fields, 24–28dp pill buttons/CTAs. No 28dp cards, no 12dp mixed in.
- Cards: flat (0 elevation), 1dp outlineVariant border, container surfaceContainerLow on background
  (light) / native tonal surfaces (dark). Dark mode = tonal elevation, never grey mush.
- Dark-mode CTAs: filled primary (mint) pill is fine for BUTTONS; large CARD fills must use
  elevated surface + primary accents (anti-headlight rule).
- Semantic color discipline: success/warning/error reserved for STATUS. Navigation chips, FABs,
  and count badges use brand primary. Red only for genuinely blocking states.
- Info never removed; status never color-only (icon/text accompanies); contrast floor ~4.5:1
  text, 3:1 UI parts — the 0.3–0.5-alpha text alphas were systematic violations, now 0.55–0.8.
- Reg numbers and TAG codes: monospace, prominent — they are operational identifiers.

## 6. Saved navigation map (avoids discovery calls)
- Bottom nav: Home ≈ (100,2274), Indents (320,2232), Trips, Find Trucks, More (980,2274).
- More screen: Manage Clients (221,882), Manage Trucks (540,882), Manage Truck Owners (859,882),
  Settings (540,1306), Log Out (540,1476).
- Login flow: phone field ≈ (540,975); type 9880300300 → auto-send OTP →
  `grep -oE 'OTP for \+919880300300: [0-9]{6}' ../backendapi/backend_live_status.log | tail -1`
  (split on ': ' — phone digits also match) → type OTP → VERIFY ≈ (540,1350).
- OTP dev shortcut: request-otp response contains `otp_code` directly (curl) — no grep needed:
  `curl -s -X POST localhost:3000/api/v1/auth/request-otp -d '{"phone_number":"+919880300300"}' -H 'Content-Type: application/json'`.
- ⚠️ Phone login is currently BROKEN for the app user (DB row missing — see BLOCKERS.md).
  Do NOT log out; the running session token still works. Cold start preserves it.

## 7. Backend quick reference
- nodemon auto-reloads on file save — no manual restart needed (keeps user's tee workflow).
- Schema drift pattern (client_profiles): select `full_name:display_name, phone:company_phone`
  (PostgREST aliases) — mappers keep old keys. Apply wherever a 500 traces to 42703.
- PostgREST debugging via env keys in backendapi/.env (SUPABASE_URL/ANON_KEY) — curl the REST
  API directly to test query shapes before touching route code.
- Route log lines are JSON: `{"time","method","path","status",...}` — python-parse them.

## 8. Session lifecycle
1. Fresh session per screen/task. Read STATE.json + LEAN_PROTOCOL.md only.
2. Do the work: audit (targeted reads) → fix (surgical edits) → build (filtered) →
   verify (one-path image review + pixel/alignment checks, light+dark).
3. On PASS: evidence → TEST_LOG row → STATE.json update → commit → end session.
4. On FAIL: fix and re-verify ONLY the affected mode/region, not the whole sweep.
5. Blocked on DB/backend data → BLOCKERS.md entry, move on (never stall a session on it).
