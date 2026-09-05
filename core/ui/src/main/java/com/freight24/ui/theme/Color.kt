package com.freight24.ui.theme

import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------------------------
// Primary (indigo tonal palette — T0..T100, Tonal Spot)
// Light-mode primary lives in T40; dark-mode primary lives in T80.
// Container pairs: T90/T10 (light), T30/T90 (dark).
// ---------------------------------------------------------------------------

internal val Primary40 = Color(0xFF6750A4)
internal val Primary80 = Color(0xFFD0BCFF)
internal val Primary90 = Color(0xFFEADDFF)
internal val Primary30 = Color(0xFF4F378B)
internal val OnPrimary100 = Color(0xFFFFFFFF)
internal val OnPrimary20 = Color(0xFF381E72)
internal val OnPrimaryContainer100 = Color(0xFF21005D)
internal val InversePrimaryLight = Color(0xFFD0BCFF)
internal val InversePrimaryDark = Color(0xFF6750A4)

// ---------------------------------------------------------------------------
// Secondary (mauve — supports primary)
// ---------------------------------------------------------------------------

internal val Secondary40 = Color(0xFF625B71)
internal val Secondary80 = Color(0xFFCCC2DC)
internal val Secondary90 = Color(0xFFE8DEF8)
internal val Secondary30 = Color(0xFF4A4458)
internal val OnSecondary100 = Color(0xFFFFFFFF)
internal val OnSecondary20 = Color(0xFF332D41)
internal val OnSecondaryContainer100 = Color(0xFF1D192B)

// ---------------------------------------------------------------------------
// Tertiary (rose/pink — accent for status badges, tags)
// ---------------------------------------------------------------------------

internal val Tertiary40 = Color(0xFFC90A48)
internal val Tertiary80 = Color(0xFFC71146)
internal val Tertiary90 = Color(0xFFFFD8E4)
internal val Tertiary30 = Color(0xFF633B48)
internal val OnTertiary100 = Color(0xFFFFFFFF)
internal val OnTertiary20 = Color(0xFF492532)
internal val OnTertiaryContainer100 = Color(0xFF31111D)

// ---------------------------------------------------------------------------
// Error (Material 3 standard tonal)
// ---------------------------------------------------------------------------

internal val Error40 = Color(0xFFBA1A1A)
internal val Error80 = Color(0xFFFFB4AB)
internal val Error90 = Color(0xFFFFDAD6)
internal val Error30 = Color(0xFF93000A)
internal val OnError100 = Color(0xFFFFFFFF)
internal val OnError20 = Color(0xFF690005)
internal val OnErrorContainer100 = Color(0xFF410002)

// ---------------------------------------------------------------------------
// Surfaces — light mode (very subtly lavender-tinted, M3 default for indigo)
// Background/surface = #FEF7FF (T98); containers step down to T90.
// ---------------------------------------------------------------------------

internal val BackgroundLight98 = Color(0xFFFEF7FF)
internal val OnBackgroundLight10 = Color(0xFF1D1B20)
internal val SurfaceLight98 = Color(0xFFFEF7FF)
internal val OnSurfaceLight10 = Color(0xFF1D1B20)
internal val SurfaceVariantLight90 = Color(0xFFE7E0EC)
internal val OnSurfaceVariantLight30 = Color(0xFF49454F)
internal val SurfaceDimLight87 = Color(0xFFDED8E1)
internal val SurfaceBrightLight98 = Color(0xFFFEF7FF)
internal val SurfaceContainerLowestLight100 = Color(0xFFFFFFFF)
internal val SurfaceContainerLowLight96 = Color(0xFFF7F2FA)
internal val SurfaceContainerLight94 = Color(0xFFF1ECF4)
internal val SurfaceContainerHighLight92 = Color(0xFFECE6EE)
internal val SurfaceContainerHighestLight90 = Color(0xFFE6E0E9)

// ---------------------------------------------------------------------------
// Surfaces — dark mode (very subtly lavender-tinted)
// Background/surface = #141218 (T6); containers step up to T22.
// ---------------------------------------------------------------------------

internal val BackgroundDark6 = Color(0xFF141218)
internal val OnBackgroundDark90 = Color(0xFFE6E0E9)
internal val SurfaceDark6 = Color(0xFF141218)
internal val OnSurfaceDark90 = Color(0xFFE6E0E9)
internal val SurfaceVariantDark30 = Color(0xFF49454F)
internal val OnSurfaceVariantDark80 = Color(0xFFCAC4D0)
internal val SurfaceDimDark6 = Color(0xFF141218)
internal val SurfaceBrightDark22 = Color(0xFF3B383E)
internal val SurfaceContainerLowestDark4 = Color(0xFF0F0D13)
internal val SurfaceContainerLowDark10 = Color(0xFF1D1B20)
internal val SurfaceContainerDark12 = Color(0xFF211F26)
internal val SurfaceContainerHighDark17 = Color(0xFF2B2930)
internal val SurfaceContainerHighestDark20 = Color(0xFF36343B)

// ---------------------------------------------------------------------------
// Outlines & inverse
// ---------------------------------------------------------------------------

internal val OutlineLight50 = Color(0xFF79747E)
internal val OutlineDark60 = Color(0xFF938F99)
internal val OutlineVariantLight80 = Color(0xFFCAC4D0)
internal val OutlineVariantDark30 = Color(0xFF49454F)
internal val InverseSurfaceLight20 = Color(0xFF322F35)
internal val InverseSurfaceDark90 = Color(0xFFE6E0E9)
internal val InverseOnSurfaceLight95 = Color(0xFFF5EFF7)
internal val InverseOnSurfaceDark10 = Color(0xFF1D1B20)

// ---------------------------------------------------------------------------
// Extended — status (success / warning / info) tonal pairs
// Status colors stay green/amber/blue even with indigo brand — they're
// semantic (success=green, warning=amber, info=blue) and matching the
// universal mental model beats brand purity here.
// ---------------------------------------------------------------------------

// Success — green, distinct from indigo brand
internal val SuccessLight = Color(0xFF0F6E56)
internal val OnSuccessLight = Color(0xFFFFFFFF)
internal val SurfaceSuccessLight = Color(0xFFCEEBDF)
internal val OnSurfaceSuccessLight = Color(0xFF07211A)
internal val SuccessDark = Color(0xFF74D6B0)
internal val OnSuccessDark = Color(0xFF003828)
internal val SurfaceSuccessDark = Color(0xFF21513F)
internal val OnSurfaceSuccessDark = Color(0xFFCEEBDF)

// Warning — amber, distinct from green/blue
internal val WarningLight = Color(0xFF854F0B)
internal val OnWarningLight = Color(0xFFFFFFFF)
internal val SurfaceWarningLight = Color(0xFFFFDDB1)
internal val OnSurfaceWarningLight = Color(0xFF2A1700)
internal val WarningDark = Color(0xFFFFB95C)
internal val OnWarningDark = Color(0xFF462900)
internal val SurfaceWarningDark = Color(0xFF623F12)
internal val OnSurfaceWarningDark = Color(0xFFFFDDB1)

// Info — blue, distinct from indigo/green/amber
internal val InfoLight = Color(0xFF185FA5)
internal val OnInfoLight = Color(0xFFFFFFFF)
internal val SurfaceInfoLight = Color(0xFFD4E3FF)
internal val OnSurfaceInfoLight = Color(0xFF001B3D)
internal val InfoDark = Color(0xFFA6C8FF)
internal val OnInfoDark = Color(0xFF003063)
internal val SurfaceInfoDark = Color(0xFF284777)
internal val OnSurfaceInfoDark = Color(0xFFD4E3FF)

// ---------------------------------------------------------------------------
// Extended — brand extras (settings accent + scrim)
// ---------------------------------------------------------------------------

// Settings — deeper indigo, for settings/profile chrome accents
internal val SettingsLight = Color(0xFF534AB7)
internal val OnSettingsLight = Color(0xFFFFFFFF)
internal val SettingsDark = Color(0xFFBDB3FF)
internal val OnSettingsDark = Color(0xFF1E1561)

// Scrim — dialog scrim opacity differs per mode for legibility on M3 surfaces
internal val DialogScrimLight = Color(0x66000000)
internal val DialogScrimDark = Color(0x99000000)

// ---------------------------------------------------------------------------
// Shared brand anchor — F24 forest green.
//
// Single source of truth for the deep forest green used as the brand mark
// across families (trucker auth fill, auth masthead field, connect settings
// accent). Refer to this instead of re-stating the hex per file.
// ---------------------------------------------------------------------------

internal val FreightBrandForest = Color(0xFF14422C)
