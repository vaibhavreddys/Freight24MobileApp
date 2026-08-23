# LEAN PROTOCOL — token-efficient UI loop (read this every session; ~1KB)

Golden rule: **round trips × context size = cost.** Minimize calls, minimize bytes per call,
minimize what you load into context.

## Session start
1. Read `STATE.json` → resume `next_action`. Read this file. Nothing else unless needed.
2. One task/screen per session. Close when committed.

## Rules
- **Files >200 lines**: never Read whole. `grep -n "symbol"` first, then Read ±40-line window.
- **UI state**: `python3 scripts/android_agent.py state | python3 -c "<filter>"` — print only what
  you need (refs/texts). NEVER print raw ui.xml (single-line, 14–34KB).
- **Taps**: `adb shell input tap X Y` when coords known (zero output). android_agent `tap eN` only
  when discovering.
- **Screenshots**: `adb exec-out screencap -p > /tmp/x.png` (not dev-act screenshot — avoids dump).
- **Image review — ONE path only**: `Read` the PNG (model sees it directly).
  `analyze_image` ONLY when the Read verdict is genuinely ambiguous. Crop regions before review
  when checking one area: `python3 -c "from PIL import Image; Image.open('/tmp/x.png').convert('RGB').crop((0,400,1080,1000)).save('/tmp/c.png')"`.
- **Builds**: `APP_MODULE=app-connect ./scripts/dev-loop.sh 2>&1 | grep -E "error|e: |Done|BUILD"`.
  Skip build entirely if only verifying (screenshot + state suffice).
- **Light/dark**: `adb shell "cmd uimode night yes|no"` + screencap. Night toggle recreates the
  activity → scroll resets, in-progress OTP can drop a digit. Expect it.
- **Batch independent calls** in one message (e.g. capture + grep OTP together).
- **Evidence**: only after PASS verdict, copy to `.devloop/baseline/<screen>-{light,dark}.png`,
  one-line row in TEST_LOG.md, update STATE.json `next_action`, commit. Then stop narrating.
- **TodoWrite**: only when >3 parallel workstreams. Otherwise skip.
- **Don't re-Read** files/images already in context. Don't re-verify unchanged modes.

## Traps (each cost real tokens before)
- CDN image URLs dedupe by content — identical files return same URL; rename copies before Read.
- Hidden OTP field: clear with `for i in $(seq 1 8); do adb shell input keyevent KEYCODE_DEL; done`
  before typing, or digits append to stale content.
- android_agent.py must run from `Freight24MobileApp/` cwd.
- Swipes starting on the keyboard do nothing — swipe within app viewport.
- Error states often don't wire pull-to-refresh — force refetch by tab toggle or cold start
  (`am force-stop` + `am start`).
- Verify suspicious reviewer verdicts with a zoomed crop before acting (one hallucinated
  "plain text" verdict nearly caused a needless rewrite).
