# Freight24 Login Workflow (Trucker & Connect)

This doc is the single source of truth for driving the Freight24 mobile apps through the OTP login flow from an automated test or another agent.

It was captured on **2026-06-15** using the **app-trucker** module against the local Node backend. It applies to both `app-trucker` and `app-connect` because they share the auth layer in `core/auth/`.

## Actors & preconditions

- **Device:** wireless ADB device — `adb-001196478009045-leuQws._adb-tls-connect._tcp` (model A015, Android 16).
- **Backend:** Node + Express + Supabase, running locally on the Mac.
  - The phone reaches the Mac via `http://192.168.1.98:3000/api/v1/` (set in `local.properties`).
  - `process.env.DEVELOPMENT_MODE=true` must be set for the OTP to be visible to humans (otherwise it's only sent via SMS).
- **Phone number:** any Indian mobile, e.g. `7022457678`. The workflow is identical for new and existing users — the backend auto-creates a `truck_owner_profiles` row on first verify.
- **App state:** for a clean reference, run `adb -s <serial> shell pm clear com.freight24.trucker.debug` before starting.

## What the device sees

| Step | What happens on the phone | What happens on the backend | How an agent can detect it |
|------|---------------------------|-----------------------------|---------------------------|
| 1. App launch | `MainActivity` boots, `SessionManager.bootstrapSession()` reads encrypted DataStore. With no session, it stays null. `MainActivity` then picks `TruckerDestination.Login.route` as start. | n/a | UI dump shows `text="Welcome back"` and an empty `EditText` at `bounds=[63,1137][1017,1284]`. |
| 2. Type phone | The user types a 10-digit Indian mobile. The field max length is enforced by `LoginViewModel.onPhoneNumberChange`. | n/a | UI dump shows `text="7022457678"` inside the EditText. |
| 3. Tap **Send OTP** | `LoginViewModel` calls `LoginUseCase` → `LoginRepository.requestOtp(phone)`. The `POST /api/v1/auth/request-otp` is dispatched. | `backendapi/src/routes/auth.js:240` generates a 6-digit OTP, stores it in an in-memory `Map` keyed by phone, and prints `📱 Mock OTP sent to ${phoneNumber}: ${otp}` via `console.log` in `services/otpService.js:14`. When `DEVELOPMENT_MODE=true`, the response body also includes `otp_code`. | After the tap, the next screen shows the **Verifying your number** title with 6 OTP boxes. |
| 4. Read OTP | n/a (phone is idle) | n/a | **The OTP is NOT in the device's logcat.** The only places it lives are (a) your Mac's backend Terminal `console.log` line, and (b) the in-memory `otpStore` map inside the Node process. To drive the app, you must paste the OTP from the Terminal. |
| 5. Type OTP | The 6 individual `BasicTextField` boxes in `OtpInputField.kt` accept digits. After 6 digits, the keyboard auto-hides and the **Verify** button is enabled. | n/a | UI dump shows six single-character texts `1, 8, 5, 5, 6, 4` and the `text="Verify"` button (no longer disabled, no more countdown). |
| 6. Tap **Verify** | `VerifyOtpViewModel.verify()` calls `VerifyOtpUseCase` → `LoginRepository.verifyOtp(phone, otp, roleId?, installationId)`. The `POST /api/v1/auth/verify-otp` returns an `AuthData` payload. `LoginRepositoryImpl` saves it through `SessionManager.saveSession(...)` which writes the encrypted DataStore and starts the proactive refresh job. | `backendapi/src/routes/auth.js:338` checks the in-memory OTP, deletes it on success, and issues a JWT access token + refresh token + installation-bound session via `refreshTokenService.issueAuthTokenPair(...)`. | After tap, the bottom-nav scaffold `TruckerHomeScreen` is shown with the **Loads** tab selected. |
| 7. Bootstrap on next launch | `SessionManager.bootstrapSession()` proactively refreshes ~5 min before access-token expiry. If the refresh fails, `_lastRefreshError` is set; this is consumed on the login screen via `MainActivity.consumeLastRefreshError()`. (Recent fix: `SessionManager.clearLocalSession()` also nulls the error to prevent carry-over.) | n/a | `isLoggedIn` flow flips to true; `bootstrapSession` returns true; MainActivity picks `TruckerDestination.Main.route`. |

## Reading the OTP in practice

**Recommended way for headless agents:** the backend prints
```
📱 Mock OTP sent to 7022457678: 123456
OTP for 7022457678: 123456 (Valid for 5 minutes)
```
to its own stdout. Your human operator must read it from the Terminal and paste it back. This is the only reliable source today.

**Why logcat won't work:** the device logcat only contains logs from processes running on the device (the app's own OkHttp interceptor, etc.). The OTP is never written to the device. App-side, only the `LoginRepositoryImpl` sees the response; the OTP is not displayed in the UI for security.

**Why the response field is not enough either:** the `otp_code` field is included in the JSON but the app does not auto-fill it — the user (or the agent) still has to type it.

## Driving the loop with adb

```bash
ADB="/Users/vaibhav/Library/Android/sdk/platform-tools/adb"
SERIAL="adb-001196478009045-leuQws._adb-tls-connect._tcp"
PKG="com.freight24.trucker.debug"
LAUNCH="com.freight24.trucker.MainActivity"

# 0. Clean slate
"$ADB" -s "$SERIAL" shell pm clear "$PKG"

# 1. Launch
"$ADB" -s "$SERIAL" shell am start -n "$PKG/$LAUNCH"
sleep 4

# 2. Tap the phone field and type
"$ADB" -s "$SERIAL" shell input tap 540 1210         # field center y=1210 in 1080x2400
"$ADB" -s "$SERIAL" shell input text "7022457678"
"$ADB" -s "$SERIAL" shell input keyevent KEYCODE_BACK   # hide keyboard

# 3. Tap Send OTP (text bounds [408,1396][599,1445]; tappable parent is wider)
"$ADB" -s "$SERIAL" shell input tap 503 1420
sleep 4

# 4. → wait for human to paste OTP, then type it
"$ADB" -s "$SERIAL" shell input text "185564"   # example
sleep 1

# 5. Tap Verify (text bounds [446,1629][561,1678]; tappable parent is wider)
"$ADB" -s "$SERIAL" shell input tap 503 1653
sleep 5

# 6. Verify: bottom nav shows Loads / Trips / Trucks / Profile
"$ADB" -s "$SERIAL" shell uiautomator dump /sdcard/window_dump.xml
"$ADB" -s "$SERIAL" pull /sdcard/window_dump.xml /tmp/after-login.xml
grep -oE 'text="(Loads|Trips|Trucks|Profile)"' /tmp/after-login.xml | sort -u
```

## Source-of-truth files

| Concern | File |
|---------|------|
| Login screen UI | `app-trucker/src/main/java/com/freight24/trucker/feature/login/view/signin/LoginScreen.kt` |
| Login ViewModel | `app-trucker/src/main/java/com/freight24/trucker/feature/login/view/signin/LoginViewModel.kt` |
| Verify OTP screen | `app-trucker/src/main/java/com/freight24/trucker/feature/login/view/verify/VerifyOtpScreen.kt` |
| Verify OTP ViewModel | `app-trucker/src/main/java/com/freight24/trucker/feature/login/view/verify/VerifyOtpViewModel.kt` |
| OTP input field (6 boxes) | `app-trucker/src/main/java/com/freight24/trucker/feature/login/view/verify/OtpInputField.kt` |
| Login repo / use case | `app-trucker/src/main/java/com/freight24/trucker/feature/login/data/repository/LoginRepositoryImpl.kt` and the corresponding use cases under `…/domain/usecase/` |
| Session manager (refresh + bootstrap) | `core/auth/src/main/java/com/freight24/auth/SessionManager.kt` |
| Encrypted DataStore | `core/auth/src/main/java/com/freight24/auth/AuthDataStore.kt` |
| Auth interceptor (401 handler) | `core/auth/src/main/java/com/freight24/auth/AuthInterceptor.kt` |
| Backend OTP routes | `backendapi/src/routes/auth.js` (lines 240, 338) |
| Backend OTP service | `backendapi/src/services/otpService.js` |
| Backend refresh-token pair | `backendapi/src/services/refreshTokenService.js` |

## Known issues / gotchas

- **Stale "Session expired" leak (recently fixed):** old `SessionManager` carried `_lastRefreshError` past `clearLocalSession()`. The fix is committed and verified on device. The login screen now stays clean on fresh installs.
- **Permission dialog on first launch:** Android 13+ shows a notifications permission prompt on first launch. The login flow works regardless of the user's choice, but if you want to dismiss programmatically, send `KEYCODE_TAB` + `KEYCODE_ENTER` to select "Don't allow".
- **In-memory OTP store:** the backend uses `otpStore = new Map()` — restarting the Node process wipes all pending OTPs. Don't restart nodemon mid-flow.
- **Single OTP per phone at a time:** calling `/auth/request-otp` twice for the same phone overwrites the previous OTP. If you re-request after the user has already typed the previous one, the typed OTP will be invalid.
- **`PHONE_NUMBER` validation:** the app currently only sends the 10-digit value to the backend. The backend's `request-otp` accepts any 10 digits, so leading `+91` is not strictly required.

## Verifying on a CI / sandboxed host

You **cannot** reach `192.168.1.98:3000` from the Codex desktop sandbox (the seatbelt policy blocks loopback). The agent must drive the device via adb and read the OTP from the human operator's Terminal — there is no headless way to retrieve the OTP without modifying the backend.
