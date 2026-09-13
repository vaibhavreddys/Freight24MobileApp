package com.freight24.connect.feature.leads.views

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freight24.connect.feature.leads.domain.viewmodel.LeadDetailViewModel
import com.freight24.rest.request.LeadRouteEntryRequest
import com.freight24.rest.response.LeadDetailDto

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

private fun activityColor(actionType: String): Color = when (actionType) {
    "converted" -> Color(0xFF1D7A4C)
    "call_outcome" -> Color(0xFF9C6400)
    "marked_invalid" -> Color(0xFFB3461F)
    "assigned" -> Color(0xFF1D4F7C)
    "imported", "details_updated" -> Color(0xFF467C00)
    "reopened" -> Color(0xFF5B4A9F)
    else -> Color(0xFF5E6D66)
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun LeadDetailScreen(
    onBackClick: () -> Unit,
    viewModel: LeadDetailViewModel = hiltViewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    val context = LocalContext.current
    var showCallOutcomeDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            com.freight24.ui.components.FreightTopAppBar(
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LeadHeaderCard(detail = detail, onCallClick = {
                runCatching {
                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${detail.phone}")))
                }
                showCallOutcomeDialog = true
            })

            state.actionError?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            }
            state.actionSuccess?.let {
                Text(text = it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            }

            if (detail.status in listOf("invalid", "not_interested", "do_not_contact")) {
                Button(
                    onClick = { viewModel.reopenLead() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .semantics { contentDescription = "reopen_lead" },
                    enabled = state.actionBusy == null,
                    shape = RoundedCornerShape(24.dp),
                ) {
                    Text(text = "Reopen Lead", fontWeight = FontWeight.SemiBold)
                }
            }

            if (detail.status in listOf("new", "contact_in_progress", "follow_up", "soft_onboarded")) {
                SoftOnboardingSection(
                    state = state,
                    detail = detail,
                    onSave = { name, cityId, typeIds, routes, notes ->
                        viewModel.saveOnboarding(name, cityId, typeIds, routes, notes)
                    },
                    onCityQuery = { viewModel.searchCities(it) },
                    onCityDismiss = { viewModel.clearCityResults() },
                )
                if (detail.status in listOf("soft_onboarded", "follow_up")) {
                    Button(
                        onClick = { viewModel.convertLead() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .semantics { contentDescription = "convert_lead" },
                        enabled = state.actionBusy == null,
                        shape = RoundedCornerShape(24.dp),
                    ) {
                        if (state.actionBusy == "convert") {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text(text = "Convert to Truck Owner", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            ActivityTimeline(detail = detail)
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showCallOutcomeDialog && state.detail != null) {
        CallOutcomeDialog(
            onDismiss = { showCallOutcomeDialog = false },
            onOutcome = { outcome, reason ->
                showCallOutcomeDialog = false
                viewModel.recordCallOutcome(outcome, reason)
            },
        )
    }
}

@Composable
private fun LeadHeaderCard(detail: LeadDetailDto, onCallClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = detail.name ?: "Unnamed lead",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Text(
                        text = detail.phone,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                StatusPill(status = detail.status)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = detail.baseCity?.name ?: "No base city",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = "Attempts: ${detail.callAttempts}${detail.lastCallOutcome?.let { " · $it" } ?: ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            detail.assignedUser?.let {
                Text(
                    text = "Assigned to ${it.fullName ?: it.tagCode ?: "executive"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onCallClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .semantics(mergeDescendants = true) { contentDescription = "call_lead_button" },
                shape = RoundedCornerShape(26.dp),
            ) {
                Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Call ${detail.phone}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun CallOutcomeDialog(
    onDismiss: () -> Unit,
    onOutcome: (outcome: String, reason: String?) -> Unit,
) {
    var selected by remember { mutableStateOf<String?>(null) }

    val reasonList = when (selected) {
        "not_interested" -> NOT_INTERESTED_REASONS
        "invalid" -> INVALID_REASONS
        else -> emptyList()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "How did the call go?", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                listOf(
                    "no_answer" to "No Answer",
                    "interested" to "Interested",
                    "not_interested" to "Not Interested",
                    "invalid" to "Invalid",
                ).forEach { (value, label) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selected = value },
                    ) {
                        RadioButton(selected = selected == value, onClick = { selected = value })
                        Text(text = label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                if (reasonList.isNotEmpty()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                    Text(text = "Select a reason", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    reasonList.forEach { (value, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selected = "${selected}::$value" },
                        ) {
                            RadioButton(selected = selected?.endsWith("::$value") == true, onClick = { selected = "${selected?.substringBefore("::")}::$value" })
                            Text(text = label, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val raw = selected ?: return@TextButton
                    val outcome = raw.substringBefore("::")
                    val reason = raw.substringAfter("::", "").ifBlank { null }
                    onOutcome(outcome, reason)
                },
                enabled = selected != null,
            ) { Text(text = "Save", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = "Cancel") }
        },
    )
}

@Composable
private fun SoftOnboardingSection(
    state: com.freight24.connect.feature.leads.domain.viewmodel.LeadDetailState,
    detail: LeadDetailDto,
    onSave: (name: String, cityId: Int?, typeIds: List<Int>, routes: List<LeadRouteEntryRequest>, notes: String?) -> Unit,
    onCityQuery: (String) -> Unit,
    onCityDismiss: () -> Unit,
) {
    var name by remember(detail.id) { mutableStateOf(detail.name ?: "") }
    var notes by remember(detail.id) { mutableStateOf(detail.notes ?: "") }
    var selectedCity by remember(detail.id) { mutableStateOf(detail.baseCity) }
    var selectedTypeIds by remember(detail.id) { mutableStateOf(detail.truckTypes.map { it.id }) }
    var routes by remember(detail.id) {
        mutableStateOf(
            detail.routes.map { LeadRouteEntryRequest(it.sourceCity?.id ?: 0, it.destinationCity?.id ?: 0, it.priority) }
                .ifEmpty { listOf(LeadRouteEntryRequest(0, 0, 1)) }
        )
    }
    // Cities seen so far (search results + selections) so selected ids can resolve to names
    val knownCities = remember { mutableStateListOf<com.freight24.rest.response.LeadCityDto>() }
    androidx.compose.runtime.LaunchedEffect(state.cityResults) {
        for (city in state.cityResults) {
            if (knownCities.none { it.id == city.id }) knownCities.add(city)
        }
    }
    fun knowCity(city: com.freight24.rest.response.LeadCityDto?) {
        if (city != null && knownCities.none { it.id == city.id }) knownCities.add(city)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = "Soft Onboarding", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold)

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(text = "Name *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "lead_name_field" },
                singleLine = true,
            )

            // Truck type multi-select chips
            Text(text = "Truck Types *", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            androidx.compose.foundation.layout.FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                state.truckTypes.forEach { type ->
                    val selected = type.id in selectedTypeIds
                    FilterChip(
                        selected = selected,
                        onClick = {
                            selectedTypeIds = if (selected) selectedTypeIds - type.id else selectedTypeIds + type.id
                        },
                        label = { Text(text = type.displayName, style = MaterialTheme.typography.labelSmall) },
                    )
                }
            }

            CitySearchField(
                label = "Base Location",
                selectedCity = selectedCity,
                cityResults = state.cityResults,
                modifier = Modifier.fillMaxWidth(),
                onQueryChange = onCityQuery,
                onDismiss = onCityDismiss,
                onCitySelected = { city ->
                    knowCity(city)
                    selectedCity = city
                    onCityDismiss()
                },
            )

            // Preferred routes
            Text(text = "Preferred Routes", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            routes.forEachIndexed { index, route ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "${index + 1}.", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    CitySearchField(
                        label = "Source",
                        selectedCity = knownCities.firstOrNull { it.id == route.sourceCityId },
                        cityResults = state.cityResults,
                        modifier = Modifier.weight(1f),
                        onQueryChange = onCityQuery,
                        onDismiss = onCityDismiss,
                        onCitySelected = { city ->
                            knowCity(city)
                            routes = routes.mapIndexed { i, r -> if (i == index) r.copy(sourceCityId = city?.id ?: 0) else r }
                            onCityDismiss()
                        },
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    CitySearchField(
                        label = "Destination",
                        selectedCity = knownCities.firstOrNull { it.id == route.destinationCityId },
                        cityResults = state.cityResults,
                        modifier = Modifier.weight(1f),
                        onQueryChange = onCityQuery,
                        onDismiss = onCityDismiss,
                        onCitySelected = { city ->
                            knowCity(city)
                            routes = routes.mapIndexed { i, r -> if (i == index) r.copy(destinationCityId = city?.id ?: 0) else r }
                            onCityDismiss()
                        },
                    )
                    IconButton(onClick = { routes = routes.filterIndexed { i, _ -> i != index } }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Remove route", modifier = Modifier.size(16.dp))
                    }
                }
            }
            OutlinedButton(onClick = { routes = routes + LeadRouteEntryRequest(0, 0, routes.size + 1) }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Add route", style = MaterialTheme.typography.labelSmall)
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(text = "Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
            )

            Button(
                onClick = {
                    onSave(
                        name.trim(),
                        selectedCity?.id,
                        selectedTypeIds,
                        routes.filter { it.sourceCityId != 0 && it.destinationCityId != 0 },
                        notes.takeIf { it.isNotBlank() },
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .semantics { contentDescription = "save_lead_details" },
                enabled = state.actionBusy == null,
                shape = RoundedCornerShape(24.dp),
            ) {
                if (state.actionBusy == "save_onboarding") {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(text = "Save Details", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun CitySearchField(
    label: String,
    selectedCity: com.freight24.rest.response.LeadCityDto?,
    cityResults: List<com.freight24.rest.response.LeadCityDto>,
    onQueryChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onCitySelected: (com.freight24.rest.response.LeadCityDto?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by remember(selectedCity?.id) { mutableStateOf(selectedCity?.name ?: "") }
    var menuOpen by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                menuOpen = true
                onQueryChange(it)
            },
            label = { Text(text = if (selectedCity != null) "$label: ${selectedCity.name}" else label) },
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "city_search_$label" },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodySmall,
        )
        if (menuOpen && cityResults.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column {
                    cityResults.take(6).forEach { city ->
                        DropdownMenuItem(
                            text = { Text(text = city.name, style = MaterialTheme.typography.bodySmall) },
                            onClick = {
                                query = city.name
                                menuOpen = false
                                onCitySelected(city)
                            },
                        )
                    }
                }
            }
        }
        if (menuOpen && cityResults.isEmpty() && query.length >= 2) {
            LaunchedEffect(query) { onDismiss() } // no-op refresh to close when empty results
        }
    }
}

@Composable
private fun ActivityTimeline(detail: LeadDetailDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Activity", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold)
            if (detail.activities.isEmpty()) {
                Text(
                    text = "No activity yet",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            detail.activities.take(20).forEachIndexed { index, activity ->
                Row(verticalAlignment = Alignment.Top) {
                    // Timeline spine: action-colored dot joined by a connector
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(activityColor(activity.actionType), CircleShape),
                        )
                        if (index < minOf(detail.activities.size, 20) - 1) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(36.dp)
                                    .background(
                                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                                        RoundedCornerShape(2.dp),
                                    ),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = activity.actionType.replace('_', ' ').uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = activityColor(activity.actionType),
                        )
                        Text(
                            text = buildString {
                                append(activity.actor?.fullName ?: activity.actor?.tagCode ?: "System")
                                if (activity.statusFrom != null && activity.statusTo != null) {
                                    append(" · ${activity.statusFrom} → ${activity.statusTo}")
                                }
                                activity.notes?.let { append(" · $it") }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}
