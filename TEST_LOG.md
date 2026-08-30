# TEST_LOG — device-verified UI passes

| Date | Screen | Mode | Verdict | Notes |
|---|---|---|---|---|
| 2026-08-23 | Login / OTP | light+dark | PASS | (P2) baseline login-{light,dark}.png |
| 2026-08-23 | Home dashboard | light+dark | PASS | (P3) home-{light,dark}*.png |
| 2026-08-23 | Create Indent | light+dark | PASS | (P3) create-indent-*.png |
| 2026-08-23 | Manage Truck Owners/Trucks/Clients | light+dark | PASS | (P4) mto/mt/mc-*.png |
| 2026-08-24 | Trips tab — All Trips default | light+dark | PASS | trips-all-{light,dark}-p4.png — ONGOING empty-filter bug fixed (RPC ignores ONGOING; replaced with ALL=null) |
| 2026-08-24 | Trip Details (ASSIGNED + IN_TRANSIT rows) | light+dark | PASS | tripdetails-{light,dark}-p4.png — docs auto-expand on pending/missing LR, memo muted, ₹0→—, bullet separator |
| 2026-08-24 | Charges & Deductions rows | light | PASS | zero rows render muted em-dash, chevrons intact |
| 2026-08-24 | Payment Breakdown | light | PASS | zero rows plain muted, real amounts keep +/- signs |
| 2026-08-24 | Timeline | light | PASS | connectors continuous (intrinsic-height rows) |
| 2026-08-24 | Halting Charges | light | PASS | ₹1,000 / ₹0 (was ₹1000.0/₹0.0) |
| 2026-08-24 | LU / Other Deductions banners | light | PASS | unified primaryContainer info style |
| 2026-08-24 | Create Trip phone field | light | PASS | IME trailing space no longer breaks validation (digits-only filter) |
| 2026-08-24 | Trip Success screen | light | PASS | flow end-to-end; PH0NE→PHONE typo fixed in code (post-screenshot) |
| 2026-08-24 | Indent Details currency | light | PASS | ₹95,000 (no space) |
| 2026-08-24 | Assign Truck results | light | PASS | "1 TRUCK FOUND" pluralized |
| 2026-08-24 | App Settings theme preview | light | PASS | "Available" chip padded via modifier |
| 2026-08-24 | Interest Inbox empty state | light | PASS | icon+title+subtitle empty state |
