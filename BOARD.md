# BOARD — app-connect Premium UI

## Backlog
- [P0] Baseline captures: Login (new), OTP, Home — light + dark
- [P0] Audit core:ui theme tokens -> DESIGN_SYSTEM.md
- [P1] ConnectColors/ConnectTypography/ConnectTheme in core:ui connect package
- [P1] Splash screen (premium, SplashScreen API / Compose)
- [P3] Seed test data via API/UI if Home is empty (Home currently populated: 1 trip, 1 indent, 10 verifications)
- [P4] Remaining screens: Post Load, My Loads/Indents, Trip Details, Profile, Settings
- [P4] App icon (premium vector)

## In Progress
- (nothing)

## Done
- [P0] Memory files initialized
- [P0] Baselines: login L/D (FAIL verdicts logged), home L/D captured; core:ui audited
- [P0] Autonomous OTP login flow verified end-to-end
- [P1] Connect theme + splash committed (0f2aae9)
- [P2] Login screen redesign (light+dark PASS, autonomous login verified)
- [P2] OTP screen redesign (light+dark PASS, end-to-end verify verified)
- [P3] Home redesign v2: solid CTA (per-mode), unified 16dp flat cards, semantic badges, dots removed, headers contrast — light+dark PASS incl. scroll positions; insets verified

## Blocked / Pending
- (none)

## Premium pass round 2 (2026-08-30) — DONE
- Repo-wide normalization: 74 raw fontSize -> theme styles; off-scale radii (10/12/14/18/20/22/28/32) -> 8/16/24 tokens; fractional sp removed (commit b42ddc5)
- H4 complete: freightBrandGradient deleted (last usages: Home FAB banding artifact, IndentCard call chip); solid primary FAB (4bf0a92)
- IndentsList: designed empty state (FreightEmptyState)
- FindTrucks: disabled "Search Trucks" CTA was white-on-pale (auto-fail) -> proper disabled pair, verified on device
- IndentDetails: truckType no longer title-cased ("32 ft mxlhq" -> raw "32 ft MXLHQ")
- core-ui Connect theme: dark onPrimary leaked indigo purple (#381E72) -> ConnectOnPrimary20 dark forest; verified on device
- Dark sweep: Indents/Trips/More/detail CTA verified; tertiary=bronze confirmed INTENTIONAL in Connect palette (pending/loading accent) — not a violation
