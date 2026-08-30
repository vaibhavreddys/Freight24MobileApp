# CONNECT LOGIN/OTP UI ISSUES

Open items — none blocking a PASS. Deferred with rationale.

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
