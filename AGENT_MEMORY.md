# AGENT_MEMORY — Freight24 app-connect Premium UI Mission

## North Star
Make Freight24 `app-connect` UI look and feel **premium, trustworthy, clean, operation-first,
highly polished**. Uber-tier logistics product. NOT generic AI slop. This mission persists
across sessions, compaction, restarts.

## Context Restoration Rule
On session start / suspected compaction:
1. Read `STATE.json` + `LEAN_PROTOCOL.md` (token-efficient operating rules — mandatory)
2. Read `DETAILED_PROTOCOL.md` only when its reference sections are needed (nav map, pixel
   techniques, gotchas, backend patterns)
3. Resume exactly from `STATE.json.next_action`
4. Update `STATE.json` after every meaningful step
5. Then say briefly: "Restored context. Continuing from <task_id>: <next_action>"

## Critical Rules (never violate)
- **Visual only**: hierarchy/spacing/colors/type/components/microcopy/animations/states OK.
  NEVER remove info, change API behavior, business rules, navigation destinations,
  validation rules, DB schema. DB changes => mark BLOCKED/PENDING, move on.
- **app-trucker must always compile.** core:ui is shared — never delete/alter existing shared
  tokens; add NEW connect-specific files (`connect` sub-package or Connect* naming).
- Light AND dark mode mandatory for every screen, both tested.
- No AI slop: no random gradients, no purple/blue defaults, no glassmorphism abuse,
  no fake dashboards, no low-contrast grey-on-grey, no cluttered cards.
- Small atomic commits on branch `feat/connect-premium-ui` after each verified screen.
  Format: `feat(connect-ui): redesign <screen>` / `feat(core-ui): ...`. Never force push.
- Review loop after every iteration with VERDICT: PASS/FAIL stored in TEST_LOG.md.
  Do not hallucinate visual review — read actual screenshots.
- A screen is DONE only per full DoD list in mission prompt (compile/install/launch/no-crash/
  light+dark/no-info-removed/functionality-same/insets/states handled/PASS verdict/evidence/
  board updated/commit).

## Key Facts
- Build/test loop: `APP_MODULE=app-connect ./scripts/dev-loop.sh`; interact via
  `./scripts/dev-act.sh`; artifacts in `.devloop/latest/` (screen.png, ui.xml, texts.txt)
- JDK 23 pinned (Gradle 8.14.3). Device USB serial `001196478009045` (auto-preferred by scripts).
- Backend on :3000, logs at `../backendapi/backend_live_status.log`, DEVELOPMENT_MODE=true.
- Autonomous login: phone 988030300 → tap Request OTP → grep OTP from backend log → type → verify.
  OTP grep: `grep -oE 'OTP for \+919880300300: [0-9]{6}' ../backendapi/backend_live_status.log | tail -1 | grep -oE '[0-9]{6}'`
- Login lives under FEATURE_NEW_LOGIN=true flag: app-connect `feature/login_new/`
  (NewLoginScreen.kt, NewOtpScreen.kt) routed from ConnectNavGraph.kt:62.
- Stale Maestro driver breaks uiautomator dump (exit 137) — dev scripts auto-clear it.
