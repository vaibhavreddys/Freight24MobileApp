# Freight24 Refresh Token Android Implementation

Date: 2026-05-03

This document describes the Android-side implementation of the refresh-token feature for Freight24 across both `app-connect` and `app-trucker`.

## Goals

- Persist the refresh token securely on device.
- Bind refresh-token sessions to a stable installation identifier.
- Refresh sessions automatically before the user is forced back to login.
- Keep the auth behavior shared between `app-connect` and `app-trucker`.
- Preserve compatibility with the existing access-token bearer flow.

## Overall Design

The Android apps now treat authentication as a refresh-aware session instead of a single access token.

### What is stored locally

The shared auth layer stores:

- access token
- refresh token
- phone number
- user ID
- installation ID
- access-token expiry timestamp
- refresh-token expiry timestamp
- refresh session ID
- refresh family ID

### What stays the same

- Protected API calls still use the access token in the `Authorization` header.
- The interceptor still clears auth state on a `401` as a fallback.

### What changed

- Login now also captures refresh-token metadata.
- Startup now performs a bootstrap step before deciding whether to show login or home.
- The client proactively refreshes before access token expiry.
- Logout calls the backend logout endpoint with `refresh_token` + `installation_id`.

## Shared Auth Layer

The shared auth behavior lives in `core/auth`.

### `AuthDataStore`

File: [core/auth/src/main/java/com/freight24/auth/AuthDataStore.kt](/Users/vaibhav/Documents/Projects/F24/Freight24MobileApp/core/auth/src/main/java/com/freight24/auth/AuthDataStore.kt)

Responsibilities:

- Securely encrypts and stores auth/session values in DataStore.
- Generates a persistent installation ID on first use.
- Preserves installation ID even when auth/session state is cleared.
- Exposes flows for:
  - `authToken`
  - `refreshToken`
  - `phoneNumber`
  - `userId`
  - `installationId`
  - `accessTokenExpiresAt`
  - `refreshTokenExpiresAt`
  - `refreshSessionId`
  - `refreshFamilyId`

### `SessionManager`

File: [core/auth/src/main/java/com/freight24/auth/SessionManager.kt](/Users/vaibhav/Documents/Projects/F24/Freight24MobileApp/core/auth/src/main/java/com/freight24/auth/SessionManager.kt)

Responsibilities:

- Provides the public session API used by both apps.
- Keeps refresh logic centralized.
- Owns backend refresh and logout calls.
- Proactively schedules refreshes.
- Exposes:
  - `ensureInstallationId()`
  - `saveSession(...)`
  - `bootstrapSession()`
  - `refreshSession()`
  - `logout()`

### `AuthInterceptor`

File: [core/auth/src/main/java/com/freight24/auth/AuthInterceptor.kt](/Users/vaibhav/Documents/Projects/F24/Freight24MobileApp/core/auth/src/main/java/com/freight24/auth/AuthInterceptor.kt)

Behavior:

- Injects bearer access tokens into outgoing requests.
- On `401`, clears auth state as a fallback.

This remains intentionally simple so refresh logic does not live inside network interception.

## Installation ID

The installation ID is the key device binding input for the backend.

### Behavior

- A UUID is generated on first launch.
- The value is stored locally and reused across restarts.
- The same value is used for:
  - OTP verification requests as `installation_id`
  - refresh/logout requests as `installation_id`
  - FCM device registration as `device_id`

### Why it matters

- It gives the backend a stable per-installation identifier.
- It allows refresh sessions to be bound to a specific app install.
- It avoids relying on hardware identifiers or device fingerprints.

## Shared REST DTOs

The Android app consumes the updated contract from `core/rest`.

### `VerifyOtpRequest`

File: [core/rest/src/main/java/com/freight24/rest/request/VerifyOtpRequest.kt](/Users/vaibhav/Documents/Projects/F24/Freight24MobileApp/core/rest/src/main/java/com/freight24/rest/request/VerifyOtpRequest.kt)

Added field:

- `installation_id`

### `RefreshSessionRequest`

File: [core/rest/src/main/java/com/freight24/rest/request/RefreshSessionRequest.kt](/Users/vaibhav/Documents/Projects/F24/Freight24MobileApp/core/rest/src/main/java/com/freight24/rest/request/RefreshSessionRequest.kt)

Fields:

- `refresh_token`
- `installation_id`

### `LogoutRequest`

File: [core/rest/src/main/java/com/freight24/rest/request/LogoutRequest.kt](/Users/vaibhav/Documents/Projects/F24/Freight24MobileApp/core/rest/src/main/java/com/freight24/rest/request/LogoutRequest.kt)

Fields:

- `refresh_token`
- `installation_id`

### `AuthResponse`

File: [core/rest/src/main/java/com/freight24/rest/response/AuthResponse.kt](/Users/vaibhav/Documents/Projects/F24/Freight24MobileApp/core/rest/src/main/java/com/freight24/rest/response/AuthResponse.kt)

The response payload now includes:

- `token`
- `access_token`
- `refresh_token`
- `refresh_expires_in`
- `refresh_expires_at`
- `refresh_session_id`
- `refresh_family_id`

The login code supports both the legacy `token` field and the new `access_token` field.

## `app-connect` Integration

### App startup

File: [app-connect/src/main/java/com/freight24/connect/MainActivity.kt](/Users/vaibhav/Documents/Projects/F24/Freight24MobileApp/app-connect/src/main/java/com/freight24/connect/MainActivity.kt)

Behavior:

- Shows a loading indicator while session bootstrap is running.
- Calls `sessionManager.bootstrapSession()` before selecting a start destination.
- Routes to:
  - home if the session is valid or refreshable
  - login if refresh fails or no session exists

### Login flows

There are two login paths in Connect:

- legacy OTP flow
- `login_new` OTP flow

#### Legacy flow

Files:

- [app-connect/src/main/java/com/freight24/connect/feature/login/repository/LoginConnectRepositoryImpl.kt](/Users/vaibhav/Documents/Projects/F24/Freight24MobileApp/app-connect/src/main/java/com/freight24/connect/feature/login/repository/LoginConnectRepositoryImpl.kt)
- [app-connect/src/main/java/com/freight24/connect/feature/login/domain/usecase/VerifyOtpUseCaseImpl.kt](/Users/vaibhav/Documents/Projects/F24/Freight24MobileApp/app-connect/src/main/java/com/freight24/connect/feature/login/domain/usecase/VerifyOtpUseCaseImpl.kt)

Behavior:

- Sends `installation_id` in the OTP verification request.
- Saves the full auth response through `SessionManager`.
- Uses `access_token` when present, otherwise falls back to `token`.

#### New login flow

File: [app-connect/src/main/java/com/freight24/connect/feature/login_new/viewmodel/NewOtpViewModel.kt](/Users/vaibhav/Documents/Projects/F24/Freight24MobileApp/app-connect/src/main/java/com/freight24/connect/feature/login_new/viewmodel/NewOtpViewModel.kt)

Behavior:

- Sends `installation_id` during OTP verify.
- Persists auth through `SessionManager`.
- Stores refresh-token metadata when available.

### FCM / device registration

Files:

- [app-connect/src/main/java/com/freight24/connect/repository/FcmRepositoryImpl.kt](/Users/vaibhav/Documents/Projects/F24/Freight24MobileApp/app-connect/src/main/java/com/freight24/connect/repository/FcmRepositoryImpl.kt)
- [app-connect/src/main/java/com/freight24/connect/feature/home/HomeScreen.kt](/Users/vaibhav/Documents/Projects/F24/Freight24MobileApp/app-connect/src/main/java/com/freight24/connect/feature/home/HomeScreen.kt)

Behavior:

- FCM token registration uses the shared installation ID as `device_id`.
- The home screen triggers device registration on entry.

### API wiring

File: [app-connect/src/main/java/com/freight24/connect/di/ApiModule.kt](/Users/vaibhav/Documents/Projects/F24/Freight24MobileApp/app-connect/src/main/java/com/freight24/connect/di/ApiModule.kt)

Behavior:

- Provides `AuthSessionApi` so refresh and logout can be executed from the app.

## `app-trucker` Integration

### App startup

File: [app-trucker/src/main/java/com/freight24/trucker/MainActivity.kt](/Users/vaibhav/Documents/Projects/F24/Freight24MobileApp/app-trucker/src/main/java/com/freight24/trucker/MainActivity.kt)

Behavior:

- Bootstraps session before routing.
- Shows a loading indicator while bootstrap is in progress.
- Uses bootstrap result to decide between main and login.

### Login flow

Files:

- [app-trucker/src/main/java/com/freight24/trucker/feature/login/data/repository/LoginRepositoryImpl.kt](/Users/vaibhav/Documents/Projects/F24/Freight24MobileApp/app-trucker/src/main/java/com/freight24/trucker/feature/login/data/repository/LoginRepositoryImpl.kt)
- [app-trucker/src/main/java/com/freight24/trucker/feature/login/domain/usecase/VerifyOtpUseCaseImpl.kt](/Users/vaibhav/Documents/Projects/F24/Freight24MobileApp/app-trucker/src/main/java/com/freight24/trucker/feature/login/domain/usecase/VerifyOtpUseCaseImpl.kt)

Behavior:

- Sends `installation_id` during OTP verify.
- Saves the auth result using the shared session manager.
- Supports `access_token` or fallback `token`.

### API wiring

File: [app-trucker/src/main/java/com/freight24/trucker/di/ApiModule.kt](/Users/vaibhav/Documents/Projects/F24/Freight24MobileApp/app-trucker/src/main/java/com/freight24/trucker/di/ApiModule.kt)

Behavior:

- Provides `AuthSessionApi`.

## Session Lifecycle on Android

### 1. Login

After OTP verification:

- client receives access token + refresh token
- client stores auth in the shared session manager
- installation ID is generated if needed and included in the request
- the new refresh metadata is persisted locally

### 2. Normal API usage

- access token is attached to requests by `AuthInterceptor`
- if backend returns `401`, local auth is cleared

### 3. Bootstrap on app launch

- app calls `bootstrapSession()`
- if access token is still valid, user enters the app
- if access token is near expiry and refresh token exists, the app refreshes first
- if refresh fails and there is no valid access token, user is sent to login

### 4. Proactive refresh

- `SessionManager` schedules refresh slightly before access token expiry
- this reduces the chance of hitting a `401` during normal usage

### 5. Logout

- app calls backend logout with the current refresh token and installation ID
- local auth/session state is cleared
- installation ID is preserved

## Error Handling

### Refresh failure

If refresh fails:

- if an access token is still valid, the app can continue until it expires
- if no valid access token remains, the session is cleared and the user is sent to login

### 401 fallback

If any protected API call receives `401`:

- `AuthInterceptor` clears auth locally
- UI reacts to missing session state and routes to login

### Backend rejection

Common backend-rejection cases:

- invalid refresh token
- expired refresh token
- absolute max lifetime exceeded
- installation ID mismatch
- refresh token reuse detected
- account deactivated

## Build Verification

The updated Android implementation compiles successfully on the QA debug variants:

- `:app-connect:compileQaDebugKotlin`
- `:app-trucker:compileQaDebugKotlin`

## Important Files

- [core/auth/src/main/java/com/freight24/auth/AuthDataStore.kt](/Users/vaibhav/Documents/Projects/F24/Freight24MobileApp/core/auth/src/main/java/com/freight24/auth/AuthDataStore.kt)
- [core/auth/src/main/java/com/freight24/auth/SessionManager.kt](/Users/vaibhav/Documents/Projects/F24/Freight24MobileApp/core/auth/src/main/java/com/freight24/auth/SessionManager.kt)
- [core/rest/src/main/java/com/freight24/rest/request/VerifyOtpRequest.kt](/Users/vaibhav/Documents/Projects/F24/Freight24MobileApp/core/rest/src/main/java/com/freight24/rest/request/VerifyOtpRequest.kt)
- [core/rest/src/main/java/com/freight24/rest/request/RefreshSessionRequest.kt](/Users/vaibhav/Documents/Projects/F24/Freight24MobileApp/core/rest/src/main/java/com/freight24/rest/request/RefreshSessionRequest.kt)
- [core/rest/src/main/java/com/freight24/rest/request/LogoutRequest.kt](/Users/vaibhav/Documents/Projects/F24/Freight24MobileApp/core/rest/src/main/java/com/freight24/rest/request/LogoutRequest.kt)
- [core/rest/src/main/java/com/freight24/rest/response/AuthResponse.kt](/Users/vaibhav/Documents/Projects/F24/Freight24MobileApp/core/rest/src/main/java/com/freight24/rest/response/AuthResponse.kt)
- [app-connect/src/main/java/com/freight24/connect/MainActivity.kt](/Users/vaibhav/Documents/Projects/F24/Freight24MobileApp/app-connect/src/main/java/com/freight24/connect/MainActivity.kt)
- [app-trucker/src/main/java/com/freight24/trucker/MainActivity.kt](/Users/vaibhav/Documents/Projects/F24/Freight24MobileApp/app-trucker/src/main/java/com/freight24/trucker/MainActivity.kt)

