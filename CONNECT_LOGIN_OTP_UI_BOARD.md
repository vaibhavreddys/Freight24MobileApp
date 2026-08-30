# CONNECT LOGIN/OTP UI BOARD — Premium Pass

Scope: ONLY app-connect Login + OTP Verification screens (`feature/login_new` — NewLoginScreen,
NewOtpScreen, OtpInputField; FEATURE_NEW_LOGIN=true). Old `feature/login` screens are out of scope.

Test phone: 9880300300 (reproducibility requirement).
Device: 001196478009045. Loop: `APP_MODULE=app-connect ./scripts/dev-loop.sh` → `.devloop/`.

## Backlog
- (empty — all done)

## In Progress
- (nothing)

## Done
- Phase 0: core:ui + ConnectTheme audit — premium token system already present
  (Connect forest/slate/bronze M3 schemes, Freight24Shapes 8/16/24, full typography scale).
  No token changes needed; added ONE shared component (InlineErrorBanner). app-trucker compile OK.
- Screen 1: Login — PASS 35/35 (it.1). Evidence: .devloop/it1-login-light.png,
  it1-login-fill.png, it1-login-dark-fill.png, baseline-login-light.png (before).
- Screen 2: OTP Verification — PASS 35/35 (it.1). Evidence: it1-otp-light.png,
  it1-otp-light-full.png, otp-error-state.png (wrong-OTP banner + red cells),
  it1-otp-dark.png, it1-otp-dark-filled.png.
- Login success flow verified end-to-end on device: 9880300300 → auto-send → 611784 → Home.
- Phase 2: cross-screen consistency verified (24dp margins, 56dp CTA @ shapes.large,
  16dp field/cell radius, centered headers, title-case SemiBold labels, shared error banner).

KEY FIX (both screens): removed nested `Freight24Theme { }` re-wrap — it silently forced the
LOGISTICS scheme over the activity's CONNECT identity and ignored the in-app dark override.
See DECISIONS. Both screens now inherit the Connect palette + user dark-mode preference.
