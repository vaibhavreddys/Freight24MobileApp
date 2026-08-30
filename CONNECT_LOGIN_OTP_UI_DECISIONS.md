# CONNECT LOGIN/OTP UI DECISIONS

| Date | Decision | Rationale |
|------|----------|-----------|
| 2026-08-31 | Do NOT replace Connect palette with the brief's flat hex tokens | ConnectTheme already ships a strictly richer M3 tonal system (forest #1B5E40 primary, graphite slate, bronze tertiary, 5-step surface containers, semantic success/warning/info/critical, light+dark). Same call as the trucker pass (2026-08-30). |
| 2026-08-31 | Removed nested `Freight24Theme { }` from NewLoginScreen + NewOtpScreen | The re-wrap used default `appTheme = LOGISTICS`, silently overriding the activity-level CONNECT scheme and any user dark-mode override ("on"/"off" was ignored inside these screens). Their own comments claimed inheritance that the code did not do. Screens now render under the ambient theme. |
| 2026-08-31 | Replaced error Toasts with inline themed banner (`InlineErrorBanner`, new core:ui component) | F3 requires styled error state; system Toast is unthemed, transient, and out of house style (login already used a Snackbar for session expiry). Message content unchanged; banner clears on next keystroke / phone edit. Presentation-only. |
| 2026-08-31 | Wrong-OTP cells show error border while banner visible (`isError` param on OtpInputField) | The shake alone was 350ms; error must persist with red accent per F3. Driven entirely by existing VM error signal — no business logic touched. |
| 2026-08-31 | Radii unified to Freight24Shapes: field/cells/support card = medium(16), CTAs = large(24) | Login field was 12dp (off the 8/16/24 scale, E2); support card/cellShape hardcoded 16 inline. Single radius scale across both screens. |
| 2026-08-31 | OTP cells 46×54 → 48×56 | 4dp grid (B1: 46/54 off-grid); 56 matches the phone input + CTA height (E4/E5); 48dp width clears any touch-target reading (B5). |
| 2026-08-31 | Hidden OTP TextField merged into the cell strip as an overlay | Previously the alpha-0.01 TextField occupied its own 56dp slot above the cells — a phantom layout gap (B7). Overlay keeps identical input behavior; strip is exactly one cell tall. |
| 2026-08-31 | "VERIFY & PROCEED" → "Verify & Proceed", Bold → SemiBold, spinner replaces label while loading | D5 (no all-caps body/button text); consistency with "Send OTP" (E4); loading CTA now mirrors login's (spinner, container stays primary — work-in-progress, not disabled). |
| 2026-08-31 | Disabled verify colors: dropped alpha layering (surfaceContainerHighest + onSurfaceVariant full) | Alpha-stacked disabled label measured ≈3:1 borderline; token-solid pair is ≈7:1 while still reading clearly disabled (A7). |
| 2026-08-31 | Back IconButton 44dp → 48dp (kept −12dp optical offset); inline icons normalized to 16dp | B5 touch target; E3 size rhythm (phone icon stays 18dp inside its 36dp circle, back stays 20dp as nav icon). |
| 2026-08-31 | Off-grid spacings fixed: 10→12 (login title gap, OTP title gap), 7→8, 14→16 | B1 4dp grid. |
| 2026-08-31 | Login kept headlineMedium(28) vs OTP headlineSmall(24) | Deliberate rank difference (hero screen vs sub-screen), same Bold weight — matches Uber/Wise pattern; consistent hierarchy, not inconsistency. |
| 2026-08-31 | `Color.Transparent` → `colorScheme.surface.copy(alpha = 0f)` | A1 zero bare-Color policy (same precedent as trucker's Color.White fix). |
