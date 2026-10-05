package com.freight24.connect.feature.more.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freight24.connect.feature.leads.domain.viewmodel.LeadsBadgeViewModel
import com.freight24.connect.feature.more.MoreViewModel
import com.freight24.ui.components.DocketCard
import com.freight24.ui.components.FreightTopAppBar
import com.freight24.ui.components.MetaTag
import com.freight24.ui.components.PerforationRule
import com.freight24.ui.theme.FreightThemeTokens

// ===========================================================================
// More — "the operations desk".
//
// The employee's own desk: their badge on top, then the dockets they can
// pull — partner registries, the lead tray, settings. Same paper language as
// the verification queue: lane spine keyed to intent (brand = registries,
// amber = fresh leads to work, error = leaving the desk), dog-eared corners,
// perforated row separators, tracked-caps section eyebrows.
// ===========================================================================

/** Tracked-caps label above each docket group. */
@Composable
private fun DeskSectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.4.sp),
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier =
            Modifier
                .padding(horizontal = 18.dp)
                .padding(top = 16.dp, bottom = 7.dp),
    )
}

@Composable
private fun deskEyebrow() =
    MaterialTheme.typography.labelSmall.copy(
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.4.sp,
    )

/** Initials for the badge avatar: first letters of the first and last words. */
private fun initialsOf(name: String): String =
    name.trim().split(Regex("\\s+"))
        .filter { it.isNotEmpty() }
        .let { words ->
            if (words.isEmpty()) "" else "${words.first().first()}${words.last().last()}".uppercase()
        }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    onBackClick: () -> Unit,
    onManageClients: () -> Unit,
    onManageTrucks: () -> Unit,
    onManageTruckOwners: () -> Unit,
    onLeadsManagement: () -> Unit,
    onSettings: () -> Unit,
    moreViewModel: MoreViewModel = hiltViewModel(),
    leadsBadgeViewModel: LeadsBadgeViewModel = hiltViewModel(),
) {
    val leadsBadgeCount = leadsBadgeViewModel.badgeCount.collectAsStateWithLifecycle().value
    LaunchedEffect(Unit) { leadsBadgeViewModel.refreshBadge() }
    val employeeInfo = moreViewModel.employeeInfo.collectAsStateWithLifecycle().value
    val showLogoutSheet = moreViewModel.showLogoutSheet.collectAsStateWithLifecycle().value

    Scaffold(
        topBar = {
            FreightTopAppBar(
                title = {
                    Text(
                        text = "More",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(innerPadding)
                    .padding(bottom = 32.dp),
        ) {
            // -- The employee badge ------------------------------------------------
            ProfileDocket(
                name = employeeInfo.fullName,
                role = employeeInfo.designation,
                tagCode = employeeInfo.tagCode,
                department = employeeInfo.department,
                phone = employeeInfo.phone,
                email = employeeInfo.email,
                status = employeeInfo.status,
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            // -- Partner registries ------------------------------------------------
            DeskSectionLabel("Fleet & Partners")
            RegistryDocket(
                accent = MaterialTheme.colorScheme.primary,
                testTag = "more_fleet_docket",
                modifier = Modifier.padding(horizontal = 16.dp),
            ) {
                DeskRow(
                    icon = Icons.Default.Person,
                    title = "Manage Clients",
                    subtitle = "Onboarded client accounts",
                    testTag = "more_clients_row",
                    onClick = onManageClients,
                )
                PerforationRule()
                DeskRow(
                    icon = Icons.Default.LocalShipping,
                    title = "Manage Trucks",
                    subtitle = "Fleet registry & documents",
                    testTag = "more_trucks_row",
                    onClick = onManageTrucks,
                )
                PerforationRule()
                DeskRow(
                    icon = Icons.Default.PeopleAlt,
                    title = "Manage Truck Owners",
                    subtitle = "Owner directory & onboarding",
                    testTag = "more_owners_row",
                    onClick = onManageTruckOwners,
                )
            }

            // -- The lead tray -----------------------------------------------------
            DeskSectionLabel("Lead Pipeline")
            RegistryDocket(
                accent = FreightThemeTokens.colors.warning,
                testTag = "more_leads_docket",
                modifier = Modifier.padding(horizontal = 16.dp),
            ) {
                DeskRow(
                    icon = Icons.Default.Call,
                    title = "Leads Management",
                    subtitle = "Cold-call pipeline & follow-ups",
                    testTag = "more_leads_row",
                    onClick = onLeadsManagement,
                    trailing = {
                        if (leadsBadgeCount > 0) {
                            MetaTag(
                                text = "$leadsBadgeCount NEW",
                                container = FreightThemeTokens.colors.surfaceWarning,
                                content = FreightThemeTokens.colors.onSurfaceWarning,
                            )
                        }
                    },
                )
            }

            // -- The desk itself ---------------------------------------------------
            DeskSectionLabel("Desk")
            RegistryDocket(
                accent = FreightThemeTokens.colors.settings,
                testTag = "more_desk_docket",
                modifier = Modifier.padding(horizontal = 16.dp),
            ) {
                DeskRow(
                    icon = Icons.Outlined.Settings,
                    title = "Settings",
                    subtitle = "Appearance & app preferences",
                    testTag = "more_settings_row",
                    onClick = onSettings,
                )
            }

            RegistryDocket(
                accent = MaterialTheme.colorScheme.error,
                testTag = "more_logout_docket",
                modifier =
                    Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 10.dp),
            ) {
                DeskRow(
                    icon = Icons.AutoMirrored.Filled.Logout,
                    title = "Log Out",
                    subtitle = "End this session on Connect",
                    testTag = "more_logout_row",
                    tone = DeskRowTone.DANGER,
                    onClick = moreViewModel::requestLogout,
                )
            }
        }
    }

    if (showLogoutSheet) {
        LogoutConfirmationSheet(
            onConfirm = {
                moreViewModel.dismissLogoutSheet()
                moreViewModel.logout()
            },
            onDismiss = { moreViewModel.dismissLogoutSheet() },
        )
    }
}

/** The employee badge: dog-eared docket, brand spine, initials roundel. */
@Composable
private fun ProfileDocket(
    name: String,
    role: String,
    tagCode: String,
    department: String,
    phone: String,
    email: String,
    status: String,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme

    DocketCard(
        accent = scheme.primary,
        testTag = "more_profile_card",
        onClick = null,
        modifier =
            modifier
                .padding(top = 12.dp)
                .fillMaxWidth(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = CircleShape,
                color = scheme.primaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (name.isNotBlank()) {
                        Text(
                            text = initialsOf(name),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onPrimaryContainer,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = scheme.onPrimaryContainer,
                            modifier = Modifier.size(26.dp),
                        )
                    }
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name.ifBlank { "Connect user" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface,
                    maxLines = 1,
                )
                Text(
                    text = role,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                )
                if (tagCode.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = tagCode,
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.1.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                }
            }
            status.takeIf { it.isNotBlank() }?.let {
                MetaTag(
                    text = it.uppercase(),
                    container = scheme.primaryContainer,
                    content = scheme.onPrimaryContainer,
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        PerforationRule()
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ProfileDetail(
                label = "Employee code",
                value = tagCode,
                modifier = Modifier.weight(1f),
            )
            ProfileDetail(
                label = "Department",
                value = department,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(12.dp))
        ProfileDetail(label = "Phone", value = phone)
        Spacer(Modifier.height(12.dp))
        ProfileDetail(label = "Email", value = email)
    }
}

@Composable
private fun ProfileDetail(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = label.uppercase(),
            style = deskEyebrow(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = value.ifBlank { "Not available" },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/** A docket of desk rows; rows are separated by perforation tear lines. */
@Composable
private fun RegistryDocket(
    accent: Color,
    testTag: String,
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    // Rows handle their own taps, so the shell is a static (non-clickable) docket.
    DocketCard(
        accent = accent,
        testTag = testTag,
        onClick = null,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(top = 4.dp, bottom = 4.dp),
        content = content,
    )
}

enum class DeskRowTone { NORMAL, DANGER }

/**
 * One registry row: icon tile, title + scent line, optional trailing stamp,
 * chevron. Whole row is the tap target.
 */
@Composable
private fun DeskRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tone: DeskRowTone = DeskRowTone.NORMAL,
    trailing: (@Composable () -> Unit)? = null,
) {
    val scheme = MaterialTheme.colorScheme
    val danger = tone == DeskRowTone.DANGER
    val iconContainer = if (danger) scheme.errorContainer else scheme.primaryContainer
    val iconTint = if (danger) scheme.error else scheme.primary
    val titleColor = if (danger) scheme.error else scheme.onSurface

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .testTag(testTag)
                .semantics { testTagsAsResourceId = true }
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(10.dp),
            color = iconContainer,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = titleColor,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
        trailing?.invoke()
        Spacer(Modifier.width(8.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = scheme.onSurfaceVariant.copy(alpha = 0.5f),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LogoutConfirmationSheet(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    val scheme = MaterialTheme.colorScheme
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = scheme.surface,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "CONFIRM LOGOUT",
                style = deskEyebrow(),
                color = scheme.error,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Icon badge
            Box(
                modifier =
                    Modifier
                        .size(56.dp)
                        .background(
                            color = scheme.error.copy(alpha = 0.18f),
                            shape = CircleShape,
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = scheme.error,
                    modifier = Modifier.size(28.dp),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Logging out?",
                style = MaterialTheme.typography.headlineSmall,
                color = scheme.onSurface,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Are you sure you want to log out of your account? You'll need to sign back in to manage your indents and trips.",
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Primary action: Logout — the destructive choice is the loud one.
            Button(
                onClick = onConfirm,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("more_logout_confirm")
                        .semantics(mergeDescendants = true) {
                            testTagsAsResourceId = true
                            contentDescription = "logout_confirm"
                        },
                shape = RoundedCornerShape(28.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = scheme.errorContainer,
                        contentColor = scheme.onErrorContainer,
                    ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
            ) {
                Text(text = "Logout", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Secondary action: Stay Logged In
            OutlinedButton(
                onClick = onDismiss,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("more_logout_cancel")
                        .semantics(mergeDescendants = true) {
                            testTagsAsResourceId = true
                            contentDescription = "logout_cancel"
                        },
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, scheme.outlineVariant),
            ) {
                Text(
                    text = "Stay Logged In",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = scheme.onSurface,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
