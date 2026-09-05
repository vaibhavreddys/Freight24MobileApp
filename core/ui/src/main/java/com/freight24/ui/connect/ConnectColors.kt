package com.freight24.ui.connect

import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------------------------
// Freight24 Connect — premium operational palette.
//
// Design language: deep forest green (trust, logistics heritage), graphite
// slate (calm structure), bronze (restrained premium accent). Surfaces are
// crisp off-whites in light mode and deep charcoal-greens in dark mode —
// never pure black, never saturated neon.
//
// This file only ADDS tokens; shared theme files used by app-trucker are
// untouched. Wired into Freight24Theme via FreightAppTheme.CONNECT.
// ---------------------------------------------------------------------------

// Primary — Deep Forest Green
internal val ConnectPrimary40 = Color(0xFF1B5E40)
internal val ConnectPrimary30 = Color(0xFF00492C)
internal val ConnectPrimary20 = Color(0xFF00381F)
internal val ConnectPrimary10 = Color(0xFF002013)
internal val ConnectPrimary80 = Color(0xFFA6D3BB)
internal val ConnectPrimary90 = Color(0xFFC2EBD3)
internal val ConnectOnPrimary100 = Color(0xFFFFFFFF)
internal val ConnectOnPrimary20 = Color(0xFF002013)
internal val ConnectOnPrimaryContainer100 = Color(0xFF002013)
internal val ConnectInversePrimaryLight = Color(0xFFA6D3BB)
internal val ConnectInversePrimaryDark = Color(0xFF1B5E40)

// Secondary — Graphite Slate (structure, secondary chips & buttons)
internal val ConnectSecondary40 = Color(0xFF49524C)
internal val ConnectSecondary80 = Color(0xFFCBD3CD)
internal val ConnectSecondary90 = Color(0xFFDFE7E1)
internal val ConnectSecondary30 = Color(0xFF333B36)
internal val ConnectOnSecondary100 = Color(0xFFFFFFFF)
internal val ConnectOnSecondary20 = Color(0xFF1F2622)
internal val ConnectOnSecondaryContainer100 = Color(0xFF101814)

// Tertiary — Bronze (premium accent; use sparingly: highlights, premium tags)
internal val ConnectTertiary40 = Color(0xFF6F5A2A)
internal val ConnectTertiary80 = Color(0xFFDCC38A)
internal val ConnectTertiary90 = Color(0xFFF5DFB2)
internal val ConnectTertiary30 = Color(0xFF564415)
internal val ConnectOnTertiary100 = Color(0xFFFFFFFF)
internal val ConnectOnTertiary20 = Color(0xFF231B03)
internal val ConnectOnTertiaryContainer100 = Color(0xFF231B03)

// ---------------------------------------------------------------------------
// Surfaces — light mode (crisp paper white with a whisper of green)
// ---------------------------------------------------------------------------
internal val ConnectBackgroundLight98 = Color(0xFFFAFBFA)
internal val ConnectOnBackgroundLight10 = Color(0xFF181C1A)
internal val ConnectSurfaceLight98 = Color(0xFFFAFBFA)
internal val ConnectOnSurfaceLight10 = Color(0xFF181C1A)
internal val ConnectSurfaceVariantLight90 = Color(0xFFEDF1ED)
internal val ConnectOnSurfaceVariantLight30 = Color(0xFF414843)
internal val ConnectSurfaceDimLight87 = Color(0xFFDADDD9)
internal val ConnectSurfaceBrightLight98 = Color(0xFFFAFBFA)
internal val ConnectSurfaceContainerLowestLight100 = Color(0xFFFFFFFF)
internal val ConnectSurfaceContainerLowLight96 = Color(0xFFF3F6F3)
internal val ConnectSurfaceContainerLight94 = Color(0xFFEEF1EE)
internal val ConnectSurfaceContainerHighLight92 = Color(0xFFE8ECE8)
internal val ConnectSurfaceContainerHighestLight90 = Color(0xFFE2E6E2)

// ---------------------------------------------------------------------------
// Surfaces — dark mode (deep charcoal-green night; elevated ≠ grey mush)
// ---------------------------------------------------------------------------
internal val ConnectBackgroundDark6 = Color(0xFF101412)
internal val ConnectOnBackgroundDark90 = Color(0xFFDFE4E0)
internal val ConnectSurfaceDark6 = Color(0xFF101412)
internal val ConnectOnSurfaceDark90 = Color(0xFFDFE4E0)
internal val ConnectSurfaceVariantDark30 = Color(0xFF3F4842)
internal val ConnectOnSurfaceVariantDark80 = Color(0xFFBFCAC3)
internal val ConnectSurfaceDimDark6 = Color(0xFF101412)
internal val ConnectSurfaceBrightDark22 = Color(0xFF363B38)
internal val ConnectSurfaceContainerLowestDark4 = Color(0xFF0B0E0D)
internal val ConnectSurfaceContainerLowDark10 = Color(0xFF181C1A)
internal val ConnectSurfaceContainerDark12 = Color(0xFF1C201E)
internal val ConnectSurfaceContainerHighDark17 = Color(0xFF262B28)
internal val ConnectSurfaceContainerHighestDark20 = Color(0xFF313632)

// ---------------------------------------------------------------------------
// Outlines & inverse
// ---------------------------------------------------------------------------
internal val ConnectOutlineLight50 = Color(0xFF707973)
internal val ConnectOutlineDark60 = Color(0xFF89938C)
internal val ConnectOutlineVariantLight80 = Color(0xFFC0C9C2)
internal val ConnectOutlineVariantDark30 = Color(0xFF3F4842)
internal val ConnectInverseSurfaceLight20 = Color(0xFF2D312E)
internal val ConnectInverseSurfaceDark90 = Color(0xFFDFE4E0)
internal val ConnectInverseOnSurfaceLight95 = Color(0xFFEFF1EF)
internal val ConnectInverseOnSurfaceDark10 = Color(0xFF181C1A)

// ---------------------------------------------------------------------------
// Semantic status pairs — restrained, AA-contrast on their containers.
// Light values pass ≥ 4.5:1 on white/near-white surfaces.
// ---------------------------------------------------------------------------
// Success / available
internal val ConnectSuccessLight = Color(0xFF1E6B43)
internal val ConnectOnSuccessLight = Color(0xFFFFFFFF)
internal val ConnectSurfaceSuccessLight = Color(0xFFC9EFDA)
internal val ConnectOnSurfaceSuccessLight = Color(0xFF07301B)
internal val ConnectSuccessDark = Color(0xFF8FD0AA)
internal val ConnectOnSuccessDark = Color(0xFF00391E)
internal val ConnectSurfaceSuccessDark = Color(0xFF10331F)
internal val ConnectOnSurfaceSuccessDark = Color(0xFFC9EFDA)

// Warning / pending
internal val ConnectWarningLight = Color(0xFF8A5800)
internal val ConnectOnWarningLight = Color(0xFFFFFFFF)
internal val ConnectSurfaceWarningLight = Color(0xFFFFE9C2)
internal val ConnectOnSurfaceWarningLight = Color(0xFF332300)
internal val ConnectWarningDark = Color(0xFFFFC96B)
internal val ConnectOnWarningDark = Color(0xFF3F2A00)
internal val ConnectSurfaceWarningDark = Color(0xFF3A2C10)
internal val ConnectOnSurfaceWarningDark = Color(0xFFFFE9C2)

// Info
internal val ConnectInfoLight = Color(0xFF35606F)
internal val ConnectOnInfoLight = Color(0xFFFFFFFF)
internal val ConnectSurfaceInfoLight = Color(0xFFD8EAF0)
internal val ConnectOnSurfaceInfoLight = Color(0xFF0A2833)
internal val ConnectInfoDark = Color(0xFF9BC8DA)
internal val ConnectOnInfoDark = Color(0xFF0A2530)
internal val ConnectSurfaceInfoDark = Color(0xFF12303B)
internal val ConnectOnSurfaceInfoDark = Color(0xFFD8EAF0)

// Critical — reserved for genuinely blocking states (payment blocked,
// trip exceptions). Deliberately deeper than M3 error so it never reads
// as "loud" at a glance.
internal val ConnectCriticalLight = Color(0xFF93221A)
internal val ConnectOnCriticalLight = Color(0xFFFFFFFF)
internal val ConnectSurfaceCriticalLight = Color(0xFFFFDAD5)
internal val ConnectOnSurfaceCriticalLight = Color(0xFF3B0703)
internal val ConnectCriticalDark = Color(0xFFFFB4A8)
internal val ConnectOnCriticalDark = Color(0xFF5F1509)
internal val ConnectSurfaceCriticalDark = Color(0xFF441A12)
internal val ConnectOnSurfaceCriticalDark = Color(0xFFFFDAD5)

// ---------------------------------------------------------------------------
// Splash
// ---------------------------------------------------------------------------
internal val ConnectSplashBackgroundLight = Color(0xFFFAFBFA)
internal val ConnectSplashBackgroundDark = Color(0xFF101412)
