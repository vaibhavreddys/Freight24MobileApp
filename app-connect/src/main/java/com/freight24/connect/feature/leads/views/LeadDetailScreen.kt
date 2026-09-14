package com.freight24.connect.feature.leads.views

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freight24.common.util.DateTimeUtils
import com.freight24.connect.feature.leads.domain.viewmodel.LeadDetailState
import com.freight24.connect.feature.leads.domain.viewmodel.LeadDetailViewModel
import com.freight24.rest.request.LeadRouteEntryRequest
import com.freight24.rest.response.LeadCityDto
import com.freight24.rest.response.LeadDetailDto
import com.freight24.ui.components.DocketCard
import com.freight24.ui.components.FreightTopAppBar
import com.freight24.ui.components.HatchMark
import com.freight24.ui.components.PanelShape
import com.freight24.ui.components.PerforationRule
import com.freight24.ui.components.TagShape
import com.freight24.ui.theme.FreightThemeTokens

// ===========================================================================
// Lead Details — the docket itself.
//
// The list shows the tray; this is one docket pulled out. Status spine and
// eyebrow carry the verdict, the call button is the header's action strip,
// the soft-onboarding form and activity log sit in their own dockets, and the
// decision buttons are pinned to the bottom so they are never below the fold.
// ===========================================================================

private val ACTIVE_STATUSES = listOf("new", "contact_in_progress", "follow_up", "soft_onboarded")
private val CLOSED_STATUSES = listOf("invalid", "not_interested", "do_not_contact")
private val CONVERT_AVAILABLE_STATUSES = listOf("soft_onboarded", "follow_up")

private val NOT_INTERESTED_REASONS = listOf(
    "not_interested" to "Not interested",
    "no_truck" to "No truck",
    "stopped_business" to "Stopped business",
    "too_many_calls" to "Too many calls",
    "competitor" to "Competitor",
    "other" to "Other",
)

private val INVALID_REASONS = listOf(
    "wrong_number" to "Wrong number",
    "not_a_truck_owner" to "Not a truck owner",
    "duplicate" to "Duplicate",
)

private fun callOutcomeLabel(outcome: String?): String = when (outcome) {
    "no_answer" -> "No answer"
    "interested" -> "Interested"
    "not_interested" -> "Not interested"
    "invalid" -> "Invalid"
    null -> ""
    else -> outcome.replace('_', ' ')
}

@Composable
private fun ledgerLabel() = MaterialTheme.typography.labelSmall.copy(
    fontWeight = FontWeight.SemiBold,
    letterSpacing = 1.1.sp,
)

@Composable
private fun docketEyebrow() = MaterialTheme.typography.labelSmall.copy(
    fontWeight = FontWeight.SemiBold,
    letterSpacing = 1.4.sp,
)

/** Tracked-caps label at the top of a docket, optionally hatch-marked. */
@Composable
private fun DocketSectionLabel(text: String, hatch: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (hatch) {
            Surface(
                shape = TagShape,
                color = MaterialTheme.colorScheme.tertiaryContainer,
            ) {
                HatchMark(
                    modifier = Modifier.size(width = 14.dp, height = 16.dp),
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.55f),
                )
            }
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = text.uppercase(),
            style = ledgerLabel(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Form fields for the soft-onboarding docket, hoisted above the pinned bar. */
private class LeadOnboardingForm(detail: LeadDetailDto) {
    var name by mutableStateOf(detail.name ?: "")
    var notes by mutableStateOf(detail.notes ?: "")
    var selectedCity by mutableStateOf(detail.baseCity)
    var selectedTypeIds by mutableStateOf(detail.truckTypes.map { it.id })
    var routes by mutableStateOf(
        detail.routes.map {
            LeadRouteEntryRequest(it.sourceCity?.id ?: 0, it.destinationCity?.id ?: 0, it.priority)
        }.ifEmpty { listOf(LeadRouteEntryRequest(0, 0, 1)) }
    )
    // Cities seen so far (the lead's own data + search results + selections) so
    // selected ids can always resolve to names.
    val knownCities = mutableStateListOf<LeadCityDto>().apply {
        detail.baseCity?.let { add(it) }
        detail.routes.forEach { route ->
            listOfNotNull(route.sourceCity, route.destinationCity).forEach { city ->
                if (none { it.id == city.id }) add(city)
            }
        }
    }

    val isSavable: Boolean
        get() = name.isNotBlank() && selectedTypeIds.isNotEmpty()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeadDetailScreen(
    onBackClick: () -> Unit,
    viewModel: LeadDetailViewModel = hiltViewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    val context = LocalContext.current
    var showCallOutcomeSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            FreightTopAppBar(
                title = { Text(text = "Lead Details", style = MaterialTheme.typography.titleLarge) },
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
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        if (state.error != null && state.detail == null) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                TextButton(onClick = { viewModel.load() }) {
                    Text(text = state.error + " — tap to retry", color = MaterialTheme.colorScheme.error)
                }
            }
            return@Scaffold
        }

        val detail = state.detail ?: return@Scaffold
        val form = remember(detail.id) { LeadOnboardingForm(detail) }
        val isClosed = detail.status in CLOSED_STATUSES
        val isActive = detail.status in ACTIVE_STATUSES
        val canConvert = detail.status in CONVERT_AVAILABLE_STATUSES

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    // keep content clear of the pinned action bar
                    .padding(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                LeadHeaderDocket(
                    detail = detail,
                    onCallClick = {
                        runCatching {
                            context.startActivity(
                                Intent(Intent.ACTION_DIAL, Uri.parse("tel:${detail.phone}"))
                            )
                        }
                        showCallOutcomeSheet = true
                    },
                )

                state.actionError?.let {
                    ActionBanner(text = it, container = MaterialTheme.colorScheme.errorContainer)
                }

                if (isActive) {
                    LeadOnboardingDocket(state = state, form = form, onCityQuery = { viewModel.searchCities(it) })
                }

                ActivityDocket(detail = detail)
            }

            // Pinned decision bar — never below the fold.
            Surface(
                modifier = Modifier.align(Alignment.BottomCenter),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    when {
                        isActive && canConvert -> {
                            ActionButton(
                                text = "Save Details",
                                testTag = "lead_save_button",
                                busy = state.actionBusy == "save_onboarding",
                                enabled = state.actionBusy == null && form.isSavable,
                                filled = false,
                                onClick = {
                                    viewModel.saveOnboarding(
                                        form.name.trim(),
                                        form.selectedCity?.id,
                                        form.selectedTypeIds,
                                        form.routes.filter { it.sourceCityId != 0 && it.destinationCityId != 0 },
                                        form.notes.takeIf { it.isNotBlank() },
                                    )
                                },
                                modifier = Modifier.weight(1f),
                            )
                            ActionButton(
                                text = "Convert to Owner",
                                testTag = "lead_convert_button",
                                busy = state.actionBusy == "convert",
                                enabled = state.actionBusy == null,
                                filled = true,
                                onClick = { viewModel.convertLead() },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        isActive -> {
                            ActionButton(
                                text = "Save Details",
                                testTag = "lead_save_button",
                                busy = state.actionBusy == "save_onboarding",
                                enabled = state.actionBusy == null && form.isSavable,
                                filled = true,
                                onClick = {
                                    viewModel.saveOnboarding(
                                        form.name.trim(),
                                        form.selectedCity?.id,
                                        form.selectedTypeIds,
                                        form.routes.filter { it.sourceCityId != 0 && it.destinationCityId != 0 },
                                        form.notes.takeIf { it.isNotBlank() },
                                    )
                                },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        isClosed -> {
                            ActionButton(
                                text = "Reopen Lead",
                                testTag = "lead_reopen_button",
                                busy = state.actionBusy == "reopen",
                                enabled = state.actionBusy == null,
                                filled = true,
                                onClick = { viewModel.reopenLead() },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCallOutcomeSheet && state.detail != null) {
        CallOutcomeSheet(
            onDismiss = { showCallOutcomeSheet = false },
            onOutcome = { outcome, reason ->
                showCallOutcomeSheet = false
                viewModel.recordCallOutcome(outcome, reason)
            },
        )
    }
}

/** Filled or outlined pill button used in the pinned bar. */
@Composable
private fun ActionButton(
    text: String,
    testTag: String,
    busy: Boolean,
    enabled: Boolean,
    filled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    if (filled) {
        Button(
            onClick = onClick,
            modifier = modifier
                .height(50.dp)
                .testTag(testTag)
                .semantics { testTagsAsResourceId = true },
            enabled = enabled,
            shape = RoundedCornerShape(25.dp),
        ) {
            if (busy) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text(text = text, fontWeight = FontWeight.SemiBold, maxLines = 1)
            }
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier
                .height(50.dp)
                .testTag(testTag)
                .semantics { testTagsAsResourceId = true },
            enabled = enabled,
            shape = RoundedCornerShape(25.dp),
            border = BorderStroke(1.dp, scheme.outlineVariant),
        ) {
            if (busy) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text(
                    text = text,
                    fontWeight = FontWeight.SemiBold,
                    color = if (enabled) scheme.onSurface else scheme.onSurfaceVariant.copy(alpha = 0.5f),
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun ActionBanner(text: String, container: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = PanelShape,
        color = container,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
        )
    }
}

// ===========================================================================
// Header docket
// ===========================================================================

@Composable
private fun LeadHeaderDocket(detail: LeadDetailDto, onCallClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val accent = LeadStatusAccent(detail.status)

    DocketCard(
        accent = accent,
        testTag = "lead_detail_card",
        onClick = null,
    ) {
        Text(
            text = leadStatusLabel(detail.status).uppercase(),
            style = docketEyebrow(),
            color = accent,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = detail.name ?: "Unnamed lead",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = scheme.onSurface,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = detail.phone,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = scheme.primary,
        )
        Spacer(Modifier.height(10.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = scheme.onSurfaceVariant,
            )
            Text(
                text = buildString {
                    append(detail.baseCity?.name ?: "No base city")
                    append(" · ${detail.callAttempts} attempt${if (detail.callAttempts == 1) "" else "s"}")
                    if (detail.lastCallOutcome != null) {
                        append(" · last: ${callOutcomeLabel(detail.lastCallOutcome)}")
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        detail.assignedUser?.let {
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Assigned to ${it.fullName ?: it.tagCode ?: "executive"}",
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.height(12.dp))
        PerforationRule()
        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onCallClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("lead_call_button")
                .semantics { testTagsAsResourceId = true },
            shape = RoundedCornerShape(25.dp),
        ) {
            Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Call ${detail.phone}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 1)
        }
    }
}

// ===========================================================================
// Soft-onboarding docket
// ===========================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LeadOnboardingDocket(
    state: LeadDetailState,
    form: LeadOnboardingForm,
    onCityQuery: (String) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme

    DocketCard(
        accent = scheme.tertiary,
        testTag = "lead_onboarding_card",
        onClick = null,
    ) {
        DocketSectionLabel(text = "Soft Onboarding", hatch = true)
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = form.name,
            onValueChange = { form.name = it },
            label = { Text(text = "Name *") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("lead_name_field")
                .semantics { testTagsAsResourceId = true },
            singleLine = true,
        )

        Spacer(Modifier.height(4.dp))
        Text(
            text = "TRUCK TYPES *",
            style = ledgerLabel(),
            color = scheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(6.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            state.truckTypes.forEach { type ->
                val selected = type.id in form.selectedTypeIds
                FilterChip(
                    selected = selected,
                    onClick = {
                        form.selectedTypeIds =
                            if (selected) form.selectedTypeIds - type.id else form.selectedTypeIds + type.id
                    },
                    label = { Text(text = type.displayName, style = MaterialTheme.typography.labelSmall) },
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        CitySearchField(
            label = "Base Location",
            selectedCity = form.selectedCity,
            cityResults = state.cityResults,
            modifier = Modifier.fillMaxWidth(),
            onQueryChange = onCityQuery,
            onCitySelected = { city ->
                knowCity(form, city)
                form.selectedCity = city
                onCityQuery("") // clears the result list
            },
        )

        Spacer(Modifier.height(8.dp))
        Text(
            text = "PREFERRED ROUTES",
            style = ledgerLabel(),
            color = scheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(6.dp))
        form.routes.forEachIndexed { index, route ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "${index + 1}.",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurfaceVariant,
                )
                CitySearchField(
                    label = "Source",
                    selectedCity = form.knownCities.firstOrNull { it.id == route.sourceCityId },
                    cityResults = state.cityResults,
                    modifier = Modifier.weight(1f),
                    onQueryChange = onCityQuery,
                    onCitySelected = { city ->
                        knowCity(form, city)
                        form.routes = form.routes.mapIndexed { i, r ->
                            if (i == index) r.copy(sourceCityId = city?.id ?: 0) else r
                        }
                        onCityQuery("") // clears the result list
                    },
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = FreightThemeTokens.colors.routeDestination,
                )
                CitySearchField(
                    label = "Destination",
                    selectedCity = form.knownCities.firstOrNull { it.id == route.destinationCityId },
                    cityResults = state.cityResults,
                    modifier = Modifier.weight(1f),
                    onQueryChange = onCityQuery,
                    onCitySelected = { city ->
                        knowCity(form, city)
                        form.routes = form.routes.mapIndexed { i, r ->
                            if (i == index) r.copy(destinationCityId = city?.id ?: 0) else r
                        }
                        onCityQuery("") // clears the result list
                    },
                )
                IconButton(
                    onClick = { form.routes = form.routes.filterIndexed { i, _ -> i != index } },
                    modifier = Modifier.testTag("lead_remove_route_$index"),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove route",
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
        OutlinedButton(
            onClick = { form.routes = form.routes + LeadRouteEntryRequest(0, 0, form.routes.size + 1) },
            modifier = Modifier.testTag("lead_add_route"),
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "Add route", style = MaterialTheme.typography.labelSmall)
        }

        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = form.notes,
            onValueChange = { form.notes = it },
            label = { Text(text = "Notes") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("lead_notes_field")
                .semantics { testTagsAsResourceId = true },
            minLines = 2,
        )
    }
}

private fun knowCity(form: LeadOnboardingForm, city: LeadCityDto?) {
    if (city != null && form.knownCities.none { it.id == city.id }) form.knownCities.add(city)
}

@Composable
private fun CitySearchField(
    label: String,
    selectedCity: LeadCityDto?,
    cityResults: List<LeadCityDto>,
    onQueryChange: (String) -> Unit,
    onCitySelected: (LeadCityDto?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by remember(selectedCity?.id) { mutableStateOf(selectedCity?.name ?: "") }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                onQueryChange(it)
            },
            label = { Text(text = label) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("city_search_${label.lowercase().replace(' ', '_')}")
                .semantics {
                    testTagsAsResourceId = true
                    contentDescription = "city_search_$label"
                },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodySmall,
        )
        // The dropdown simply stops rendering when results are cleared.
        if (cityResults.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = PanelShape,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            ) {
                Column {
                    cityResults.take(6).forEach { city ->
                        DropdownMenuItem(
                            text = { Text(text = city.name, style = MaterialTheme.typography.bodySmall) },
                            onClick = {
                                query = city.name
                                onCitySelected(city)
                            },
                        )
                    }
                }
            }
        }
    }
}

// ===========================================================================
// Activity docket
// ===========================================================================

@Composable
private fun ActivityDocket(detail: LeadDetailDto) {
    val scheme = MaterialTheme.colorScheme

    DocketCard(
        accent = scheme.onSurfaceVariant.copy(alpha = 0.6f),
        testTag = "lead_activity_card",
        onClick = null,
    ) {
        DocketSectionLabel(text = "Activity")
        Spacer(Modifier.height(12.dp))
        if (detail.activities.isEmpty()) {
            Text(
                text = "No activity yet — every call and update is logged here.",
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
            )
        }
        detail.activities.take(20).forEachIndexed { index, activity ->
            Row(verticalAlignment = Alignment.Top) {
                // Timeline spine: action-coloured dot joined by a connector.
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(LeadActivityAccent(activity.actionType), CircleShape),
                    )
                    if (index < minOf(detail.activities.size, 20) - 1) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(36.dp)
                                .background(
                                    scheme.outlineVariant.copy(alpha = 0.6f),
                                    RoundedCornerShape(2.dp),
                                ),
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = activity.actionType.replace('_', ' ').uppercase(),
                            style = ledgerLabel(),
                            fontWeight = FontWeight.Bold,
                            color = LeadActivityAccent(activity.actionType),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = remember(activity.createdAt) { DateTimeUtils.formatRelativeTime(activity.createdAt) },
                            style = MaterialTheme.typography.labelSmall,
                            color = scheme.onSurfaceVariant.copy(alpha = 0.7f),
                        )
                    }
                    Text(
                        text = buildString {
                            append(activity.actor?.fullName ?: activity.actor?.tagCode ?: "System")
                            val statusFrom = activity.statusFrom
                            val statusTo = activity.statusTo
                            // A transition that lands on the same status (archive
                            // bookkeeping) reads as a fake diff — show nothing.
                            if (statusFrom != null && statusTo != null && statusFrom != statusTo) {
                                append(" · ${leadStatusLabel(statusFrom)} → ${leadStatusLabel(statusTo)}")
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    activity.notes?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            if (index < minOf(detail.activities.size, 20) - 1) {
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

// ===========================================================================
// Call outcome sheet
// ===========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CallOutcomeSheet(
    onDismiss: () -> Unit,
    onOutcome: (outcome: String, reason: String?) -> Unit,
) {
    var selectedOutcome by remember { mutableStateOf<String?>(null) }
    var selectedReason by remember { mutableStateOf<String?>(null) }

    val reasonList = when (selectedOutcome) {
        "not_interested" -> NOT_INTERESTED_REASONS
        "invalid" -> INVALID_REASONS
        else -> emptyList()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
        ) {
            Text(
                text = "CALL OUTCOME",
                style = docketEyebrow(),
                color = FreightThemeTokens.colors.warning,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "How did the call go?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(16.dp))

            listOf(
                "no_answer" to "No Answer",
                "interested" to "Interested",
                "not_interested" to "Not Interested",
                "invalid" to "Invalid",
            ).forEach { (value, label) ->
                OutcomeRow(
                    label = label,
                    accent = outcomeAccent(value),
                    isSelected = selectedOutcome == value,
                    onClick = {
                        selectedOutcome = value
                        selectedReason = null
                    },
                    testTag = "lead_outcome_$value",
                )
                Spacer(Modifier.height(8.dp))
            }

            AnimatedVisibility(visible = reasonList.isNotEmpty()) {
                Column {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "SELECT A REASON",
                        style = ledgerLabel(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    reasonList.forEach { (value, label) ->
                        ReasonRow(
                            label = label,
                            isSelected = selectedReason == value,
                            onClick = { selectedReason = value },
                            testTag = "lead_reason_$value",
                        )
                        Spacer(Modifier.height(6.dp))
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { onOutcome(selectedOutcome!!, selectedReason) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("lead_outcome_save")
                    .semantics { testTagsAsResourceId = true },
                enabled = selectedOutcome != null &&
                    (reasonList.isEmpty() || selectedReason != null),
                shape = RoundedCornerShape(25.dp),
            ) {
                Text(text = "Log Call", fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(16.dp))
            Spacer(Modifier.navigationBarsPadding())
        }
    }
}

@Composable
private fun outcomeAccent(outcome: String): Color {
    val ext = FreightThemeTokens.colors
    val scheme = MaterialTheme.colorScheme
    return when (outcome) {
        "interested" -> ext.success
        "not_interested", "invalid" -> scheme.error
        else -> ext.warning // no answer — still to be worked
    }
}

/** Selectable outcome row: status dot + label in a panel silhouette. */
@Composable
private fun OutcomeRow(
    label: String,
    accent: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
            .semantics { testTagsAsResourceId = true }
            .clickable(onClick = onClick),
        shape = PanelShape,
        color = if (isSelected) accent.copy(alpha = 0.14f) else Color.Transparent,
        border = BorderStroke(1.dp, if (isSelected) accent.copy(alpha = 0.55f) else scheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(accent, CircleShape),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = scheme.onSurface,
            )
        }
    }
}

/** Smaller indented reason row under Not Interested / Invalid. */
@Composable
private fun ReasonRow(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String,
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
            .semantics { testTagsAsResourceId = true }
            .clickable(onClick = onClick)
            .padding(start = 12.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .background(if (isSelected) scheme.primary else Color.Transparent, CircleShape)
                .border(
                    width = 1.dp,
                    color = if (isSelected) scheme.primary else scheme.outlineVariant,
                    shape = CircleShape,
                ),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = scheme.onSurface,
        )
    }
}
