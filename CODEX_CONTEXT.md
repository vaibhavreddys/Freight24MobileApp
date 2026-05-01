# Freight24MobileApp Context Snapshot

Date: 2026-05-01

This file captures the current working context for the Freight24MobileApp workspace so future work can resume without re-reading the entire conversation.

## Repo Scope

- Android multi-module project with two app targets:
  - `app-connect`
  - `app-trucker`
- Shared modules used by both apps:
  - `core/auth`
  - `core/common`
  - `core/network`
  - `core/rest`
  - `core/ui`
  - `core/notifications`
  - `core/database`

## Auth Plan Implemented

The refresh-token integration is now wired into the Android apps and shared auth layer.

### Shared auth layer

- `core/auth/src/main/java/com/freight24/auth/AuthDataStore.kt`
  - Persists:
    - access token
    - refresh token
    - phone number
    - user ID
    - installation ID
    - access token expiry timestamp
    - refresh token expiry timestamp
    - refresh session ID
    - refresh family ID
  - Generates and persists a stable `installation_id` once per install.
  - `clearAuthData()` clears session/auth fields but keeps the installation ID.

- `core/auth/src/main/java/com/freight24/auth/SessionManager.kt`
  - Owns refresh-aware session persistence.
  - Provides:
    - `ensureInstallationId()`
    - `saveSession(...)`
    - `bootstrapSession()`
    - `refreshSession()`
    - `logout()`
  - Proactively refreshes before access-token expiry.
  - Uses `AuthSessionApi` for backend refresh/logout calls.
  - `logout()` calls backend logout with `refresh_token` + `installation_id`, then clears local state.

- `core/auth/src/main/java/com/freight24/auth/AuthInterceptor.kt`
  - Still clears auth on `401` as a fallback path.

- `core/auth/build.gradle.kts`
  - Now depends on `:core:rest`.

### Shared REST contracts

Changed files in `core/rest`:

- `src/main/java/com/freight24/rest/request/VerifyOtpRequest.kt`
  - Added `installation_id`.
- `src/main/java/com/freight24/rest/request/RegisterDeviceRequest.kt`
  - `deviceId` is now required.
  - File is `@Serializable`.
- `src/main/java/com/freight24/rest/request/RefreshSessionRequest.kt`
  - New.
- `src/main/java/com/freight24/rest/request/LogoutRequest.kt`
  - New.
- `src/main/java/com/freight24/rest/api/AuthSessionApi.kt`
  - New `POST /auth/refresh`
  - New `POST /auth/logout`
- `src/main/java/com/freight24/rest/response/AuthResponse.kt`
  - `AuthData` now carries refresh-token metadata:
    - `access_token`
    - `refresh_token`
    - `refresh_expires_in`
    - `refresh_expires_at`
    - `refresh_session_id`
    - `refresh_family_id`

### `app-connect`

- `src/main/java/com/freight24/connect/MainActivity.kt`
  - No longer routes purely off synchronous session validation.
  - Bootstraps session first, then chooses login/home.
  - Shows a loading indicator during bootstrap.

- `src/main/java/com/freight24/connect/di/ApiModule.kt`
  - Provides `AuthSessionApi`.

- Login flow updates:
  - `feature/login/repository/LoginConnectRepositoryImpl.kt`
    - Sends `installation_id` in OTP verify request.
    - Saves auth response through `SessionManager`.
  - `feature/login/domain/usecase/VerifyOtpUseCaseImpl.kt`
    - No longer writes directly to `AuthDataStore`.
  - `feature/login_new/viewmodel/NewOtpViewModel.kt`
    - Sends `installation_id`.
    - Saves refresh-aware session metadata via `SessionManager`.

- FCM/device registration:
  - `repository/FcmRepositoryImpl.kt`
    - Uses persisted `installation_id` as `device_id`.
  - `feature/home/HomeScreen.kt`
    - Now triggers `homeViewModel.registerDevice()` on entry.
    - Screen was made `internal` to match the internal `HomeViewModel`.

### `app-trucker`

- `src/main/java/com/freight24/trucker/MainActivity.kt`
  - Now bootstraps session first.
  - Shows loading indicator while bootstrapping.

- `src/main/java/com/freight24/trucker/di/ApiModule.kt`
  - Provides `AuthSessionApi`.

- Login flow updates:
  - `feature/login/data/repository/LoginRepositoryImpl.kt`
    - Sends `installation_id` in OTP verify request.
  - `feature/login/domain/usecase/VerifyOtpUseCaseImpl.kt`
    - Reads `access_token` fallback from response and no longer persists auth directly.

## Validation

Both app targets compile successfully after the changes:

- `:app-connect:compileQaDebugKotlin`
- `:app-trucker:compileQaDebugKotlin`

## Current Caveats

- `SessionManager` currently keeps the refresh logic centralized and proactive, but the UI does not yet surface a dedicated “refresh failed” error state.
- `HomeScreen` in `app-connect` is now `internal` because it depends on `HomeViewModel`.
- There are existing deprecation warnings in unrelated files, but the builds are green.

## Likely Next Work

- Add user-facing handling for refresh failure/revocation.
- Optionally add logout-all support.
- Consider cleaning up deprecated `hiltViewModel()` imports in the screen code if desired.

## Important Files To Resume From

- `core/auth/src/main/java/com/freight24/auth/AuthDataStore.kt`
- `core/auth/src/main/java/com/freight24/auth/SessionManager.kt`
- `core/rest/src/main/java/com/freight24/rest/api/AuthSessionApi.kt`
- `app-connect/src/main/java/com/freight24/connect/MainActivity.kt`
- `app-trucker/src/main/java/com/freight24/trucker/MainActivity.kt`
- `app-connect/src/main/java/com/freight24/connect/feature/home/HomeScreen.kt`
- `app-connect/src/main/java/com/freight24/connect/repository/FcmRepositoryImpl.kt`

