# app-trucker — Agent Reference

A complete picture of the `app-trucker` Android app: how it's structured, every reachable screen, the source files that back each one, the API endpoints they hit, and gaps that would benefit from stable testTags.

> **Captured:** 2026-06-15, against the QA debug build installed on the wireless device (`adb-001196478009045-leuQws._adb-tls-connect._tcp`). Login as `7022457678`. See `login-workflow.md` for how to get to the home screen.
>
> **How to read this:** each screen has a "What you see" section (from a live UI dump), a "How to drive" section (stable taps / coordinates), and a "Source" section pointing at the exact files.

---

## 1. Top-level architecture

```
app-trucker/
  di/                       Hilt modules
  navigation/
    TruckerDestination.kt   sealed class of routes
    TruckerNavGraph.kt      NavHost wiring (Login, VerifyOtp, Main, TruckDetail, …)
    TruckerTabs.kt          bottom-nav tab metadata
  feature/
    home/
      TruckerHomeScreen.kt  bottom-nav scaffold (Crossfade between tabs)
    login/
      view/signin/          LoginScreen, LoginViewModel
      view/verify/          VerifyOtpScreen, VerifyOtpViewModel, OtpInputField, ExpressiveVerifyButton
      data/                 LoginRepository(Impl), models
      domain/usecase/       LoginUseCase, VerifyOtpUseCase
    loads/                  LoadBoardScreen + ViewModel + data/di/domain/mapper/model
    trips/
      view/                 TripsScreen, TripsViewModel
      view/details/         TripDetailsScreen, TripDetailsViewModel
    trucks/
      view/                 TrucksScreen, TrucksViewModel
      view/details/         TruckDetailsScreen, TruckDetailsViewModel
      view/onboard/         OnboardTruckScreen + EditTruckScreen + ViewModels
    profile/view/           ProfileScreen, ProfileViewModel, AppSettingsViewModel
  ui.components/            app-specific helpers (LogoutButton, CallAction, etc.)

core/auth/                  SessionManager, AuthDataStore, AuthInterceptor, TokenManager
core/network/               OkHttp + Retrofit factory, NetworkModule, error adapter
core/rest/                  *Api.kt interfaces + request/response DTOs
core/ui/                    shared Compose components (LoadCard, TripCard, AppTopBar, …)
core/notifications/         AppNotification + NotificationDispatcher
```

Login state is held in `SessionManager` (singleton, Hilt). The `MainActivity` bootstraps it on launch and chooses `Login` vs `Main` as the start destination.

---

## 2. Navigation graph

`TruckerDestination` is a sealed class:

| Destination | Route | Notes |
|-------------|-------|-------|
| `Login` | `login` | `LoginScreen` |
| `VerifyOtp` | `verifyOtp/{phone}` | `VerifyOtpScreen` |
| `Main` | `main` | `TruckerHomeScreen` (bottom-nav scaffold) |
| `TripDetail` | `tripDetail/{tripId}` | `TripDetailsScreen` |
| `TruckDetail` | `truckDetail/{truckId}` | `TruckDetailsScreen` |
| `OnboardTruck` | `onboardTruck` | `OnboardTruckScreen` |
| `EditTruck` | `editTruck/{truckId}` | `EditTruckScreen` |
| `Settings` | `settings` | `AppSettingsScreen` (from `core/ui`) |

The `Main` route renders a `FreightBottomNavScaffold` with `TruckerTab.entries` = `Loads · Trips · Trucks · Profile`. The 4 tabs are NOT separate `NavHost` destinations — they live inside the same composable and `Crossfade` between them. Use `BackHandler` to fall back to Loads from any other tab.

---

## 3. Bottom-nav tabs (the home screen)

### 3.1 Loads (default)

- **What you see:** Top bar reads `Loads`. The body has a single location card showing `Chennai · 1 load available` with a chevron. Tap the card → a load card expands showing the route, truck type, weight, material, posted-by, and a `Call` button.
- **Empty / unknown:** if the API returns nothing, the card area is blank (no empty-state component).
- **Test data observed:** one load — Bengaluru → Chennai, 32 Ft MXL, 7.0 Tons, Steel tubes, ₹150,000, posted by "Babu Mohan" 28 minutes ago, with a Call action.
- **Driving it:**
  - Top-bar search icon at `bounds=[975,159][1038,222]`, `content-desc="Search"`. Tap is wired but the callback is a `TODO` in `TruckerHomeScreen` (`onSearchClick = { /* TODO: trigger search */ }`) — does nothing today.
  - Chennai card center: roughly `540, 380` (or just tap the "Chennai" text at `[221,358][378,407]`).
  - Inside the expanded load: `Call` button (purple pill on the right).
- **Source:** `app-trucker/.../feature/loads/LoadBoardScreen.kt`, `LoadBoardViewModel.kt`. The expanded card uses the shared `core/ui/components/LoadCard.kt` and `LoadDetailBottomSheet.kt`.
- **API:** `GET /api/v1/indents/trucker/load-board-paginated` (paginated) or `GET /api/v1/indents/trucker/load-board` (full list) per `IndentApi.kt`.

### 3.2 Trips

- **What you see:** Top bar reads `My Trips`. Filter chips: `ACTIVE`, `POD`, `COMPLETED`, `ALL`. Empty state: `No Active Trips Available`.
- **Driving it:**
  - Filter chip taps: `ACTIVE` at roughly `[120,250]`, `POD` at `[285,250]`, `COMPLETED` at `[465,250]`, `ALL` at `[640,250]`.
- **Source:** `app-trucker/.../feature/trips/view/TripsScreen.kt`, `TripsViewModel.kt`. Empty + filter chip UI is local; list items use the shared `core/ui/components/TripCard.kt`.
- **API:** `GET /api/v1/trips/trucker/list` (per `TripApi.kt`). When the list is non-empty, tapping a card navigates to `TruckerDestination.TripDetail.createRoute(tripId)`.

### 3.3 Trucks

- **What you see:** Top bar reads `Trucks`. Filter chips: `All`, `Available`, `On trip`, `Blacklisted`. Card per truck: registration number, body type, status chip (`Active` / `Blacklisted` / etc.), and a `Verification Pending` warning row.
- **Test data observed:** 5 trucks — `KH67KJ6767` (32 Feet Single Axle High Cube), `KA01VA1233` (22 Feet Closed), `KA01AV1234` (20 Feet Closed), `KA07KA0707` (20 Feet Open), `HY78HH7777` (18 Whl Open). All `Active` and `Verification Pending`.
- **Driving it:**
  - First truck row at `bounds=[53,500][1027,800]` (approx). Tap to open details.
- **Source:** `app-trucker/.../feature/trucks/view/TrucksScreen.kt`, `TrucksViewModel.kt`. Header card is `core/ui/components/TruckHeaderCard.kt`.
- **API:** `GET /api/v1/v2/trucks/` (and `/search`) per `TruckApi.kt`.

### 3.4 Profile

- **What you see:** Top bar reads `Profile` with an edit (pencil) icon at `bounds=[975,159][1038,222]`, `content-desc="Edit profile"`. The body is a vertical list of expandable sections:
  1. **Identity card** — avatar (initial `V`), name, `Truck Owner` role, `Partner ID: F2401198`, status `Active`.
  2. **PERSONAL INFORMATION** — chip `Unverified`, chevron `Expand`. Inside: full name, phone, alt phones, email, PAN, Aadhaar, DL, DOB, address.
  3. **BUSINESS DETAILS** — chip `Verified`, chevron. Inside: COMPANY NAME ("Trucks And Trips And Everything"), GST NUMBER (`38ABCDE9988F1Z9`), BASE LOCATION (`Bengaluru, Karnataka`), PREFERRED ROUTES (chips: `Bengaluru › Chennai`, `Kolar › Mysore`).
  4. **BANKING INFORMATION** — chip `Verified`, chevron. Inside: primary bank account details.
  5. **DOCUMENTS** — chip `Verified`, chevron. Inside: identity documents list.
  6. **POD COURIER ADDRESS** — non-collapsible card showing `Freight24 / 4th Floor, #3, Srirampur Cross Road, Jakkuru, Bengaluru, Karnataka - 560064` + a `Copy Address` button.
  7. **APP** — list with `App Settings` and `Contact Support` rows.
  8. **LOGOUT** — full-width red button (tapping it opens a confirmation bottom sheet; see section 3.5 below).
- **Edit mode:** tapping the pencil flips the relevant rows into editable fields and shows `Cancel` + `Save Changes` buttons. Sections whose `Verified` chip is green are read-only even in edit mode.
- **Driving it:**
  - Pencil at `[975,159][1038,222]`.
  - Each section row is a `View` with `clickable="true"` parent — tap anywhere on the row text.
  - `Cancel` button at `bounds=[242,1964][356,2017]` in edit mode.
  - `LOGOUT` button is a `View` at the bottom; tapping it calls `ProfileViewModel.requestLogout()` which opens a confirmation `ModalBottomSheet`. The sheet has two actions: `logout_confirm` (Logout) calls `viewModel.logout()` → `SessionManager.logout()`; `logout_cancel` (Stay Logged In) calls `viewModel.dismissLogoutSheet()`.
  - `App Settings` row opens `TruckerDestination.Settings.route`.
- **Source:** `app-trucker/.../feature/profile/view/ProfileScreen.kt` (~1400 lines, the biggest file in the module), `ProfileViewModel.kt`, `AppSettingsViewModel.kt`. Sections use `core/ui/components/ProfileUtilityRow.kt`, `PODAddressCard.kt`, `AccountStatusCard.kt`.
- **API:** `GET /api/v1/truck-owners/profile` (per `TruckOwnerApi.kt`); PATCH calls go to `/api/v1/truck-owners/:id`.

---

### 3.5 Logout confirmation bottom sheet

- **What you see:** A modal bottom sheet slides up from the bottom of the screen. It contains (top to bottom): a drag handle, a 56dp circular badge tinted with `colorScheme.error` at 18% alpha containing a white logout arrow icon, the bold headline `Logging out?`, the body text `Are you sure you want to log out of your account? You will need to sign back in to access your loads.`, a wide light pill button labelled `Logout` (the primary confirm action), and a wide dark pill button with a 1dp outline border labelled `Stay Logged In` (the secondary cancel action). The sheet has `navigationBarsPadding` so it sits above the system gesture bar.
- **Driving it:** the sheet uses `rememberModalBottomSheetState(skipPartiallyExpanded = true)` so it opens fully and cannot be partially dismissed by swiping only partway down. The `onDismissRequest` (back press / scrim tap) maps to `dismissLogoutSheet`.
  - `logout_confirm` is the wide pill in the middle (taps confirm logout).
  - `logout_cancel` is the wide pill at the bottom (taps dismiss the sheet and stay logged in).
- **Verification (2026-06-15):** built, installed, and walked through on `adb-001196478009045-leuQws._adb-tls-connect._tcp`. Both content-desc IDs are present in the UIAutomator dump. Stay Logged In correctly returns to the Profile screen with the session intact; Logout returns to the Login screen (phone field empty, `Welcome back` title). Artifacts in `Freight24MobileApp/.devloop/logout-sheet/`.
- **Source:** `app-trucker/.../feature/profile/view/ProfileScreen.kt` (the new private composable `LogoutConfirmationSheet` plus the `if (showLogoutSheet) { ... }` render block) and `ProfileViewModel.kt` (new `showLogoutSheet: StateFlow<Boolean>` + `requestLogout()` / `dismissLogoutSheet()`).

## 4. Drill-down screens

### 4.1 Truck Details (`TruckDetail` route)

- **What you see:** back arrow + `Truck Details` title. Card with registration, body type, status, `Verification Pending` warning. **Specifications** grid: `BODY TYPE`, `MAKE`, `MODEL`, `TRUCK TYPE`. **Documents** section with rows for `RC Book`, `Insurance Policy`, `Fitness Certificate`, all currently `Verification Pending` with a chevron. Bottom: full-width purple `Edit Truck` button.
- **Driving it:** Back at `[43,135][115,200]`. Edit Truck is the bottom purple pill.
- **Source:** `app-trucker/.../feature/trucks/view/details/TruckDetailsScreen.kt` + `TruckDetailsViewModel.kt`.
- **API:** `GET /api/v1/v2/trucks/:id`, `PATCH /api/v1/v2/trucks/:id/status`, etc.

### 4.2 Trip Details (`TripDetail` route)

- Not visited during the smoke sweep (no trips in the test account). Per source: status timeline, POD/LR uploaders, financial summary. File: `feature/trips/view/details/TripDetailsScreen.kt`.

### 4.3 Onboard / Edit Truck

- Both routes are present in the nav graph but were not exercised. Sources:
  - `feature/trucks/view/onboard/OnboardTruckScreen.kt`
  - `feature/trucks/view/onboard/EditTruckScreen.kt`

### 4.4 App Settings

- Reached via Profile → App Settings. Reuses the shared `core/ui/components/AppSettingsScreen.kt` (theme + dark-mode picker). Theme choices: Sora + Be Vietnam Pro M3, multiple color accents (`FreightAppTheme`).

---

## 5. Element index (stable identifiers from the sweep)

These are the `text` / `content-desc` values that are most useful for re-driving. Anything not in this table is driven by coordinates (fragile across screen sizes / font scales).

| Screen | Identifier | Value | Notes |
|--------|------------|-------|-------|
| Login | Phone field | `EditText` at `[63,1137][1017,1284]` | type via `input text` |
| Login | CTA button | `content-desc="Send OTP"` |  |
| Verify | OTP boxes | six `BasicTextField`s | type via `input text`, no need to focus individual boxes |
| Verify | CTA button | `text="Verify"` at `[446,1629][561,1678]` |  |
| Verify | Resend | `text="Resend Code"` after countdown | `text="Resend Code in 00:N"` during |
| Top bar | Search | `content-desc="Search"` at `[975,159][1038,222]` | Loads tab only — TODO no-op |
| Top bar | Edit profile | `content-desc="Edit profile"` at `[975,159][1038,222]` | Profile tab only |
| Loads | City card | `text="Chennai"` at `[221,358][378,407]` | tap to expand the cluster |
| Loads | Load card | `text="Bengaluru"` / `text="Chennai"` / `text="₹ 150,000"` | inside expanded card |
| Trips | Filter chips | `text="ACTIVE" / "POD" / "COMPLETED" / "ALL"` | at y ~250 |
| Trucks | Filter chips | `text="All" / "Available" / "On trip" / "Blacklisted"` | at y ~325 |
| Trucks | Truck cards | `text="KH67KJ6767"` etc. | at `[232,520][488,569]` and below |
| Profile | Pencil | `content-desc="Edit profile"` | same coords as top bar icon |
| Profile | Sections | `text="PERSONAL INFORMATION"` etc. | each has `content-desc="Expand"` (when collapsed) or `"Collapse"` (when expanded) |
| Profile | Cancel (edit mode) | `text="Cancel"` at `[242,1964][356,2017]` |  |
| Profile | Save Changes | `text="Save Changes"` |  |
| Profile | App Settings | `text="App Settings"` at `[226,1359][440,1402]` |  |
| Profile | Logout | `text="LOGOUT"` (red) |  |

---

## 6. testTag / contentDescription audit

`testTag(...)` is **not used anywhere** in `app-trucker` or the shared `core/ui` components. The app relies on `contentDescription` (partial) and `text` (good for headers and chips) for UIAutomator introspection.

### 6.1 Well-covered (good)

- `core/ui/components/LoadCard.kt` — `Delivery truck`, `Agent`, `Mark as interested`, `Call <name>`.
- `core/ui/components/AppTopBar.kt` — `Search`, `Filter`.
- `app-trucker/.../feature/profile/view/ProfileScreen.kt` — `Expand` / `Collapse`, `Base location`, `Preferred routes`, `Upload`, `View`, `verificationStatus` chip.

### 6.2 Applied (2026-06-15) — semantics { contentDescription = "..." }

All 11 IDs from the original proposal were added in one pass, using `Modifier.semantics(mergeDescendants = true) { contentDescription = "..." }` rather than `Modifier.testTag(...)`. **Why semantics over testTag:** `Modifier.testTag(...)` is only picked up by the Compose test framework (Espresso, Compose UI test). It does not surface in the Android accessibility tree, which is what UIAutomator (and adb shell uiautomator dump) reads. `Modifier.semantics { contentDescription = "..." }` shows up as a `content-desc` attribute on the AccessibilityNodeInfo — that's what adb sees, and it's also what screen readers (TalkBack) announce.

**Tradeoff:** semantics IDs are now visible to screen readers as well. That's a good thing for accessibility, but if the wording is awkward for humans ("loads_empty_state" announced on a no-loads screen), consider adding `Modifier.semantics(mergeDescendants = true) { contentDescription = "<human>"; testTag = "<id>" }` later — the testTag can stay hidden from TalkBack while contentDescription is announced.

### 6.3 Verified IDs (live on device, post-build)

| ID | Where it appears | Dynamic value observed |
|----|------------------|------------------------|
| `tab_loads` | Bottom nav, Loads | static |
| `tab_trips` | Bottom nav, Trips | static |
| `tab_trucks` | Bottom nav, Trucks | static |
| `tab_profile` | Bottom nav, Profile | static |
| `trips_filter_active` | Trips tab filter chip | static |
| `trips_filter_pod` | Trips tab filter chip | static |
| `trips_filter_completed` | Trips tab filter chip | static |
| `trips_filter_all` | Trips tab filter chip | static |
| `trucks_filter_all` | Trucks tab filter chip | static |
| `trucks_filter_available` | Trucks tab filter chip | static |
| `trucks_filter_on_trip` | Trucks tab filter chip | static |
| `trucks_filter_blacklisted` | Trucks tab filter chip | static |
| `load_board_city_<id>` | Loads tab, each city card | `load_board_city_2` (Chennai) |
| `load_row_<id>` | Loads tab, each load card inside an expanded city | `load_row_8c56756d-1cfc-4571-a6f2-f272f1c0bc97` |
| `load_show_more_<id>` | Loads tab, "Show more" pill | dynamic |
| `loads_empty_state` | Loads tab, empty body | static |
| `trips_empty_state` | Trips tab, empty body | static |
| `trip_row_<id>` | Trips tab, each trip card | dynamic |
| `truck_row_<id>` | Trucks tab, each truck card | `truck_row_0d78929f-6b1e-40f5-b4c9-2a13c9ab3bdd` etc. |
| `truck_edit_button` | Truck Details, bottom CTA | static |
| `profile_logout` | Profile, bottom red LOGOUT button | static |
| `logout_confirm` | Logout sheet, primary "Logout" pill | static |
| `logout_cancel` | Logout sheet, secondary "Stay Logged In" pill | static |

Verification dumps: `Freight24MobileApp/.devloop/testtag-verify/{02..10}-*.xml`.

### 6.4 What was changed (file list)

- `app-trucker/.../feature/loads/LoadBoardScreen.kt` — 4 semantics modifiers + import
- `app-trucker/.../feature/trips/view/TripsScreen.kt` — 3 semantics modifiers (filter, trip row, empty state) + import
- `app-trucker/.../feature/trucks/view/TrucksScreen.kt` — 2 semantics modifiers (filter chip, truck card) + import
- `app-trucker/.../feature/trucks/view/details/TruckDetailsScreen.kt` — 1 semantics modifier (Edit Truck) + import
- `app-trucker/.../feature/profile/view/ProfileScreen.kt` — 1 semantics modifier (Logout) + import
- `core/ui/.../components/FreightBottomNavBar.kt` — 1 semantics modifier on each `NavigationBarItem` + import

No changes to the public APIs of shared components (`CityCard`, `LoadCard`, `TruckHeaderCard`, `TripCard`, `FilterSelectionChip`, `AppTopBar`) — the testTags are layered in by the call sites via `Modifier`. This means the same shared components stay clean for `app-connect` to use, and we can apply the same approach there later without changing `core/ui`.

### 6.5 Driving the app from adb (post-patch)

```bash
# Tap the Loads tab (any screen)
"$ADB" -s "$SERIAL" shell uiautomator dump /sdcard/window_dump.xml
"$ADB" -s "$SERIAL" pull /sdcard/window_dump.xml /tmp/cur.xml
# Find the node by content-desc and its bounds, then tap center
python3 -c "
import re; xml=open('/tmp/cur.xml').read()
m=re.search(r'content-desc="tab_trips"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', xml)
if m: x=(int(m[1])+int(m[3]))//2; y=(int(m[2])+int(m[4]))//2; print(f'tap {x} {y}')
"
```

Or a one-liner using `xargs` to compute and tap:
```bash
"$ADB" -s "$SERIAL" exec-out grep -oE 'content-desc="tab_trips"[^>]*bounds="\[[0-9]+,[0-9]+\]\[[0-9]+,[0-9]+\]"' /sdcard/window_dump.xml
```

For interactive scripting, prefer dumping + parsing over hard-coded coordinates.

### 6.6 Coordinate-based driving that is currently fragile

A few elements still don't have a stable ID and must be driven by coordinates. (Future patches can fix these, but they weren't on the priority list.)

- Top bar search icon: `content-desc="Search"` is on a `View` that's not always tappable directly — use bounds `[975,159][1038,222]` center.
- Section expand chevrons in Profile: `content-desc="Expand"` (or `"Collapse"`) — those are stable.
- App Settings / Contact Support rows: no testTag yet.
- Verify button on OTP screen: `text="Verify"` works.

Top-level coords that are stable (1080×2400 device):
- Bottom nav: y ≈ 2274, x per tab: Loads=120, Trips=400, Trucks=678, Profile=952.
- Top bar trailing icon: y ≈ 175, x ≈ 1000.
- Snackbars / transient banners: y ≈ 2230.

---

## 7. Open questions / follow-ups

- **Search on Loads tab:** the callback is a TODO. Either wire it to a real search API or remove the icon.
- **Empty states:** the Loads tab has no visible empty state when the cluster card has no loads — we just see "1 load available" with no row beneath. Trips has a clear "No Active Trips Available". Trucks shows the full list (no empty state because the test account has 5).
- **Edit mode UX:** when tapping the pencil on Profile, **all** sections go into edit mode, but already-`Verified` rows stay read-only with no visual indication of which fields are editable. Easy follow-up: dim or hide the pencil when there's nothing to edit.
- **Logout confirmation:** ~~tapping LOGOUT fires immediately with no confirm dialog — risk of accidental tap.~~ **Fixed (2026-06-15)** — LOGOUT now opens a Material 3 `ModalBottomSheet` with a `Logging out?` headline and two actions (`logout_confirm` / `logout_cancel`). See section 3.5 for the full spec.
- **Notification permission flow:** the first launch shows a system permission dialog. We tested with "Don't allow"; the login still completes. There's a `BuildConfig.FEATURE_NEW_LOGIN` flag in `app-connect` that gates the new login screen; not used in `app-trucker`.

---

## 8. Saved artifacts (this sweep)

All screenshots + UI dumps from this walkthrough are saved under
```
Freight24MobileApp/.devloop/login-walkthrough/    # 01-login-screen through 04-after-verify
Freight24MobileApp/.devloop/sweep/                # 00-loads-home through 12-app-settings
Freight24MobileApp/.devloop/logout-sheet/           # 12-profile through 17-after-confirm - logout confirmation bottom sheet
```

Re-run the sweep with:
```bash
cd /Users/vaibhav/Documents/Projects/F24/Freight24MobileApp
./scripts/dev-loop.sh
```

Then drive the device using the coords in section 5.
