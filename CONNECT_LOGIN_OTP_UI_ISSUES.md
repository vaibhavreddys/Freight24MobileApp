# CONNECT LOGIN/OTP UI ISSUES

Open items — none blocking a PASS. Deferred with rationale.

- [open — backend, not UI] QA server rejects even fresh resent OTPs with
  "OTP not found or expired" (observed 2026-08-31 ~02:44 with code 571989 resent seconds
  earlier). Possibly the failed-attempt path invalidates the stored OTP server-side, or QA
  env instability. UI handled it correctly (red cells + banner + retry paths). Success path
  itself was proven earlier the same day (code 611784 → Home). Needs a backend look, outside
  UI scope.
- [deferred] Login auto-send label contrast flips at `fillFraction > 0.5f` — during the 2s
  fill animation the white label can straddle the fill edge for a few frames at the midpoint.
  Transient animation nuance, invisible at rest, auto-resolves; not worth a layout change.
- [deferred] "Terms of Service & Privacy Policy" footer is a single non-clickable Text.
  Splitting into two links = functionality change (H5) → out of scope for this pass.
- [deferred] Login D1 strict count: 4 text sizes by role (28 title / 16 input / 14 subtitle+CTA /
  12 caption). Judged PASS — hierarchy is unambiguous and the input size must equal the typed
  text size. Noted for honesty, not as a defect.
- [note] Old `feature/login` LoginScreen/VerifyOtpScreen are dead code behind
  `FEATURE_NEW_LOGIN=true` and were NOT touched (H9 scope lock). If the flag is ever flipped,
  they need their own pass.
- [note] Device left logged in after end-to-end verification (BLOCKERS.md forbids logging out).
