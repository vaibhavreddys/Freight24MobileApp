package com.freight24.connect.feature.leads.views

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.PersonOutline
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
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
import com.freight24.rest.response.CityResult
import com.freight24.rest.response.LeadCityDto
import com.freight24.rest.response.LeadDetailDto
import com.freight24.rest.response.LeadTruckTypeDto
import com.freight24.rest.response.LeadUserDto
import com.freight24.ui.components.CitySearchField
import com.freight24.ui.components.DocketCard
import com.freight24.ui.components.FreightTopAppBar
import com.freight24.ui.components.HatchMark
import com.freight24.ui.components.PanelShape
import com.freight24.ui.components.PerforationRule
import com.freight24.ui.components.TagShape
import com.freight24.ui.theme.FreightThemeTokens
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

// ===========================================================================
// Lead Details — the docket itself.
//
// The list shows the tray; this is one docket pulled out. The header splits into
// two registers: the FACTS an executive acts on (base location, truck types) and
// the QUIET meta line (attempts made, last outcome) beneath a perforation. Status
// spine and eyebrow carry the verdict, the soft-onboarding form sits in its own
// docket, and the decision buttons are pinned so they are never below the fold.
// ===========================================================================

private val ACTIVE_STATUSES = listOf("new", "contact_in_progress", "follow_up", "soft_onboarded")
private val CLOSED_STATUSES = listOf("invalid", "not_interested", "do_not_contact")
private val CONVERT_AVAILABLE_STATUSES = listOf("soft_onboarded", "follow_up")

/** Every input in the soft-onboarding form shares one height and one silhouette. */
private val FieldHeight = 56.dp

/**
 * Notes is a fixed four-line box rather than a `minLines`-driven grower.
 *
 * The form lives in a scrolling column above a pinned action bar, so a field that grows
 * with every keystroke shifts everything below it — including the buttons the user is
 * reaching for. A stable height with internal scrolling keeps the form still. It also
 * sidesteps `BasicTextField`'s minimum intrinsic height ignoring `minLines`, which had
 * collapsed this field to a sliver that rendered no text at all.
 */
private val NotesFieldHeight = 112.dp

/** Stable keys for the single city-search owner (see `LeadDetailState.cityResultsOwner`). */
private const val CityKeyBaseLocation = "lead_city_base"

/**
 * Breathing room left above a city field when it is scrolled to the top of the form.
 *
 * Enough that the field does not sit flush against the app bar and the suggestion list
 * underneath it still reads as attached to the field, small enough that it does not eat
 * a meaningful slice of a phone-sized viewport.
 */
private val FieldTopMargin = 12.dp

private fun cityKeyRouteSource(rowId: Long) = "lead_city_route_${rowId}_source"
private fun cityKeyRouteDestination(rowId: Long) = "lead_city_route_${rowId}_destination"

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

private fun String.pluralize(count: Int): String = if (count == 1) this else "${this}s"

/** A lead's own city (id + name only) promoted to the richer search-result shape. */
private fun LeadCityDto.toCityResult() =
    CityResult(type = "CITY", id = id, name = name, city = name, state = "")

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

// ===========================================================================
// Form state
// ===========================================================================

/**
 * One preferred-route entry being edited.
 *
 * Carries a stable [id] because the row is rendered from a `forEach` — keying state by list
 * position meant deleting the first route moved the second route's text into it. The query
 * strings live here rather than inside the field so the parent owns them, which is what the
 * shared city field expects.
 */
private class RouteRow(val id: Long) {
    var sourceQuery by mutableStateOf("")
    var sourceCity by mutableStateOf<CityResult?>(null)
    var destinationQuery by mutableStateOf("")
    var destinationCity by mutableStateOf<CityResult?>(null)

    /** Only a row with both ends picked is a real lane worth counting or saving. */
    val isComplete: Boolean get() = sourceCity != null && destinationCity != null
}

/** Form fields for the soft-onboarding docket, hoisted above the pinned bar. */
private class LeadOnboardingForm(detail: LeadDetailDto) {
    var name by mutableStateOf(detail.name ?: "")
    var notes by mutableStateOf(detail.notes ?: "")
    var baseQuery by mutableStateOf(detail.baseCity?.name ?: "")
    var selectedCity by mutableStateOf(detail.baseCity?.toCityResult())
    var selectedTypeIds by mutableStateOf(detail.truckTypes.map { it.id })

    private var nextRowId = 0L

    val routes = mutableStateListOf<RouteRow>().apply {
        detail.routes.forEach { route ->
            add(
                RouteRow(id = nextRowId++).apply {
                    sourceCity = route.sourceCity?.toCityResult()
                    sourceQuery = route.sourceCity?.name.orEmpty()
                    destinationCity = route.destinationCity?.toCityResult()
                    destinationQuery = route.destinationCity?.name.orEmpty()
                }
            )
        }
        // A lone blank row is how you record the first lane; it also keeps the section
        // from ever being a dead end after the last route is removed.
        if (isEmpty()) add(RouteRow(id = nextRowId++))
    }

    /** Counted for the section header — incomplete drafts are not lanes yet. */
    val completeRouteCount: Int get() = routes.count { it.isComplete }

    fun addRoute() {
        routes.add(RouteRow(id = nextRowId++))
    }

    fun removeRoute(rowId: Long) {
        routes.removeAll { it.id == rowId }
        if (routes.isEmpty()) routes.add(RouteRow(id = nextRowId++))
    }

    /** Only complete rows are sent; a half-filled draft is not a lane. */
    fun routeRequests(): List<LeadRouteEntryRequest> =
        routes.filter { it.isComplete }.map { row ->
            LeadRouteEntryRequest(
                sourceCityId = row.sourceCity!!.id,
                destinationCityId = row.destinationCity!!.id,
                priority = 1,
            )
        }

    val isSavable: Boolean
        get() = name.isNotBlank() && selectedTypeIds.isNotEmpty()
}

// ===========================================================================
// Keeping the field you are typing in above the keyboard
// ===========================================================================

/**
 * Scrolls the form so the city field being typed into sits at the top of the viewport.
 *
 * Why this exists at all: the app is edge-to-edge, so the IME is an overlay the layout
 * knows nothing about. Compose's own bring-into-view only guarantees "some part of this
 * field is inside the *layout* viewport" — which, with a keyboard on top of it, meant the
 * field the user was typing into sat completely behind the keyboard (measured on device:
 * the Base Location field landed at y=1623 with the keyboard starting at y≈1544).
 * `Modifier.imePadding()` on the scroll container fixes the "knows nothing about it" half;
 * this class fixes the "barely visible is not good enough" half. A city field opens a
 * suggestion list directly underneath it, so the field needs to be at the *top* of the
 * viewport for the dropdown to have anywhere to go.
 *
 * Offsets are measured, never hard-coded: the suggestion list growing, a route row being
 * added, or the truck-type chips wrapping onto another line all move the fields below
 * them, and a stale offset would scroll to the wrong place.
 */
private class FieldScrollTarget(
    private val scrollState: ScrollState,
    private val scope: CoroutineScope,
) {
    /** Field key -> its top edge, in the scroll content's own coordinate space. */
    private val offsets = mutableMapOf<String, Float>()

    /** Top edge of the scroll viewport, in root coordinates. Does not move when scrolling. */
    private var viewportTop = Float.NaN

    private var animation: Job? = null

    fun onViewportPositioned(top: Float) {
        viewportTop = top
    }

    fun onFieldPositioned(key: String, top: Float) {
        if (viewportTop.isNaN()) return
        // Position in root moves with the scroll, so add it back to recover the field's
        // position in the content. The result is constant across scroll positions, which
        // is what makes it safe to refresh on every placement pass.
        offsets[key] = top - viewportTop + scrollState.value
    }

    /**
     * Brings [key] to the top of the viewport, leaving [topMarginPx] of breathing room so
     * the field does not sit flush against the app bar.
     *
     * The target is clamped to the available scroll range. The form is short enough that
     * the lower route rows cannot physically reach the very top — there is less content
     * below them than one viewport's worth — so they rise as far as the content allows and
     * settle fully above the keyboard, which is the outcome that actually matters.
     */
    fun scrollToTop(key: String, topMarginPx: Float) {
        val offset = offsets[key] ?: return
        val target = (offset - topMarginPx).toInt().coerceIn(0, scrollState.maxValue)
        animation?.cancel()
        animation = scope.launch {
            scrollState.animateScrollTo(
                value = target,
                animationSpec = tween(
                    durationMillis = 250,
                    easing = FastOutSlowInEasing,
                ),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeadDetailScreen(
    onBackClick: () -> Unit,
    viewModel: LeadDetailViewModel = hiltViewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    val context = LocalContext.current
    val canViewAssignee = viewModel.canViewAssignee
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

        val scrollState = rememberScrollState()
        val scope = rememberCoroutineScope()
        val fieldScroll = remember(scrollState, scope) { FieldScrollTarget(scrollState, scope) }
        val fieldTopMarginPx = with(LocalDensity.current) { FieldTopMargin.toPx() }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                // The scaffold's insets are already spent above; without this the IME
                // inset below would be counted a second time and the pinned bar would
                // float a nav-bar's height too high.
                .consumeWindowInsets(innerPadding)
                // Edge-to-edge window: the IME does not resize us, it just covers us. This
                // is what shrinks the viewport to the part of the screen the user can
                // actually see, so the scroll maths below has a truthful bottom edge and
                // the pinned bar rides up with the keyboard.
                .imePadding()
                .onGloballyPositioned { fieldScroll.onViewportPositioned(it.positionInRoot().y) },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    // keep content clear of the pinned action bar
                    .padding(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                LeadHeaderDocket(
                    detail = detail,
                    canViewAssignee = canViewAssignee,
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
                    LeadOnboardingDocket(
                        state = state,
                        form = form,
                        fieldScroll = fieldScroll,
                        fieldTopMarginPx = fieldTopMarginPx,
                        onCityQuery = viewModel::searchCities,
                        onCityResultsCleared = viewModel::clearCityResults,
                    )
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
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    val onSave = {
                        viewModel.saveOnboarding(
                            form.name.trim(),
                            form.selectedCity?.id,
                            form.selectedTypeIds,
                            form.routeRequests(),
                            form.notes.takeIf { it.isNotBlank() },
                        )
                        Unit
                    }
                    when {
                        isActive && canConvert -> {
                            ActionButton(
                                text = "Save Details",
                                testTag = "lead_save_button",
                                busy = state.actionBusy == "save_onboarding",
                                enabled = state.actionBusy == null && form.isSavable,
                                filled = false,
                                onClick = onSave,
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
                                onClick = onSave,
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

/**
 * The lead's identity, then two registers of information.
 *
 * A lead that has been worked shows its FACTS first — base location and the truck types
 * recorded — because that is what the next call is about. Call history is bookkeeping, so
 * it sits below a perforation in a quieter voice. A brand-new lead has neither, and is not
 * padded out with zeroes to look complete.
 */
@Composable
private fun LeadHeaderDocket(
    detail: LeadDetailDto,
    canViewAssignee: Boolean,
    onCallClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val accent = LeadStatusAccent(detail.status)
    val isFresh = isFreshLead(detail.status)

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
        // The phone number is the one field that is always meaningful: it is why the
        // lead exists, whatever state it is in and whoever is looking at it.
        Text(
            text = detail.phone,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = scheme.primary,
        )

        if (!isFresh) {
            Spacer(Modifier.height(14.dp))
            LeadFactsPanel(detail = detail)

            Spacer(Modifier.height(12.dp))
            PerforationRule()
            Spacer(Modifier.height(10.dp))
            LeadMetaStatus(detail = detail)
        } else if (detail.truckTypes.isNotEmpty()) {
            // Truck types can already be captured while soft-onboarding a new lead, so
            // they are worth showing even before the lead has a base location or history.
            Spacer(Modifier.height(12.dp))
            TruckTypeLine(truckTypes = detail.truckTypes)
        }

        if (canViewAssignee) {
            Spacer(Modifier.height(10.dp))
            PerforationRule()
            Spacer(Modifier.height(10.dp))
            AssigneeLine(assignee = detail.assignedUser)
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
            Text(text = "Call ${detail.phone}", fontWeight = FontWeight.SemiBold, maxLines = 1)
        }
    }
}

/**
 * The two facts that decide the next call, side by side and at reading size.
 *
 * `truckTypes` is a list of truck *types* the owner runs — it was labelled "trucks
 * recorded", which read as a fleet count and was wrong.
 */
@Composable
private fun LeadFactsPanel(detail: LeadDetailDto) {
    val scheme = MaterialTheme.colorScheme
    val truckTypeNames = detail.truckTypes.joinToString(", ") { it.displayName }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = PanelShape,
        // One tonal step above the card's own surface so the block reads as a distinct
        // register rather than more body text. `surfaceContainerLow` was too close to
        // `surface` in dark mode to carry the weight this information needs.
        color = scheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.5f)),
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 11.dp)
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FactColumn(
                label = "Base location",
                value = detail.baseCity?.name,
                icon = Icons.Default.LocationOn,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(scheme.outlineVariant.copy(alpha = 0.7f)),
            )
            FactColumn(
                label = "Truck types",
                value = truckTypeNames.ifBlank { null },
                icon = Icons.Default.LocalShipping,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun FactColumn(
    label: String,
    value: String?,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(13.dp),
                tint = scheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(5.dp))
            Text(
                text = label.uppercase(),
                style = ledgerLabel(),
                color = scheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = value ?: "Not recorded",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (value == null) FontWeight.Normal else FontWeight.SemiBold,
            color = if (value == null) scheme.onSurfaceVariant.copy(alpha = 0.7f) else scheme.onSurface,
        )
    }
}

/** Truck types as a plain reading line — used for leads that are still fresh. */
@Composable
private fun TruckTypeLine(truckTypes: List<LeadTruckTypeDto>) {
    val scheme = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.LocalShipping,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = scheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = truckTypes.joinToString(", ") { it.displayName },
            style = MaterialTheme.typography.bodySmall,
            color = scheme.onSurfaceVariant,
        )
    }
}

/**
 * Call bookkeeping — deliberately quieter than the facts above it.
 *
 * Kept in one line and one register so it never competes with base location and truck
 * types for attention. "attempts made" is the term used everywhere: a bare count leaves
 * the reader guessing whether it means calls made or calls left.
 */
@Composable
private fun LeadMetaStatus(detail: LeadDetailDto) {
    val scheme = MaterialTheme.colorScheme
    val outcome = callOutcomeLabel(detail.lastCallOutcome)

    Text(
        text = buildString {
            if (detail.callAttempts == 0) {
                append("No attempts made yet")
            } else {
                append("${detail.callAttempts} ${"attempt".pluralize(detail.callAttempts)} made")
                if (outcome.isNotBlank()) append(" · last ${outcome.lowercase()}")
            }
        },
        style = MaterialTheme.typography.bodySmall,
        color = scheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

/** Who owns this lead — hidden entirely from fleet executives, who own their own. */
@Composable
private fun AssigneeLine(assignee: LeadUserDto?) {
    val scheme = MaterialTheme.colorScheme
    val name = assignee?.fullName ?: assignee?.tagCode

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Outlined.PersonOutline,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = scheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = name?.let { "Assigned to $it" } ?: "Unassigned",
            style = MaterialTheme.typography.bodySmall,
            color = scheme.onSurfaceVariant,
        )
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
    fieldScroll: FieldScrollTarget,
    fieldTopMarginPx: Float,
    onCityQuery: (owner: String, query: String) -> Unit,
    onCityResultsCleared: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme

    DocketCard(
        accent = scheme.tertiary,
        testTag = "lead_onboarding_card",
        onClick = null,
    ) {
        DocketSectionLabel(text = "Soft Onboarding", hatch = true)
        Spacer(Modifier.height(14.dp))

        OutlinedTextField(
            value = form.name,
            onValueChange = { form.name = it },
            label = { Text(text = "Name *") },
            modifier = Modifier
                .fillMaxWidth()
                .height(FieldHeight)
                .testTag("lead_name_field")
                .semantics { testTagsAsResourceId = true },
            shape = PanelShape,
            textStyle = MaterialTheme.typography.bodyLarge,
            singleLine = true,
        )

        Spacer(Modifier.height(14.dp))
        Text(
            text = "TRUCK TYPES *",
            style = ledgerLabel(),
            color = scheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
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

        Spacer(Modifier.height(14.dp))
        CitySearchField(
            query = form.baseQuery,
            onQueryChange = { text ->
                form.baseQuery = text
                // Typing means the previously picked city no longer matches what is on
                // screen; keeping it selected would silently save the old city.
                if (form.selectedCity?.name != text) form.selectedCity = null
                onCityQuery(CityKeyBaseLocation, text)
                // Only on a real edit — the field's own callback is not fired for focus
                // changes or cursor moves, so this cannot scroll the form out from under
                // the user while they are reading it.
                fieldScroll.scrollToTop(CityKeyBaseLocation, fieldTopMarginPx)
            },
            suggestions = if (state.cityResultsOwner == CityKeyBaseLocation) state.cityResults else emptyList(),
            onCitySelected = { city ->
                form.selectedCity = city
                form.baseQuery = city.name
                onCityResultsCleared()
            },
            isLoading = state.cityResultsOwner == CityKeyBaseLocation && state.citySearchLoading,
            label = "Base Location",
            selectAllOnFocus = true,
            testTag = "city_search_base_location",
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned {
                    fieldScroll.onFieldPositioned(CityKeyBaseLocation, it.positionInRoot().y)
                },
        )

        Spacer(Modifier.height(16.dp))
        Text(
            text = if (form.completeRouteCount > 0) {
                "PREFERRED ROUTES (${form.completeRouteCount})"
            } else {
                "PREFERRED ROUTES"
            },
            style = ledgerLabel(),
            color = scheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))

        form.routes.forEach { row ->
            RouteEntryRow(
                row = row,
                resultsOwner = state.cityResultsOwner,
                results = state.cityResults,
                isLoading = state.citySearchLoading,
                fieldScroll = fieldScroll,
                fieldTopMarginPx = fieldTopMarginPx,
                onQuery = onCityQuery,
                onResultsCleared = onCityResultsCleared,
                onRemove = { form.removeRoute(row.id) },
                // A lone blank row is the way in, not something to delete.
                canRemove = form.routes.size > 1 || row.isComplete,
            )
            Spacer(Modifier.height(8.dp))
        }

        // Once a lane exists the blank row is no longer the only way to add one, so the
        // button appears. With nothing recorded yet, the fields above already are the
        // "add route" affordance and a second button would just be a second way to say it.
        if (form.completeRouteCount > 0 || form.routes.size > 1) {
            OutlinedButton(
                onClick = { form.addRoute() },
                modifier = Modifier.testTag("lead_add_route"),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, scheme.primary.copy(alpha = 0.4f)),
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Add route",
                    style = MaterialTheme.typography.labelMedium,
                    color = scheme.primary,
                )
            }
            Spacer(Modifier.height(14.dp))
        } else {
            Spacer(Modifier.height(6.dp))
        }

        OutlinedTextField(
            value = form.notes,
            onValueChange = { form.notes = it },
            label = { Text(text = "Notes") },
            modifier = Modifier
                .fillMaxWidth()
                .height(NotesFieldHeight)
                .testTag("lead_notes_field")
                .semantics { testTagsAsResourceId = true },
            shape = PanelShape,
            textStyle = MaterialTheme.typography.bodyLarge,
        )
    }
}

/**
 * One preferred-route entry: source, a direction marker, destination.
 *
 * No serial number — the lane's direction is already carried by the arrow, and numbering
 * entries made a three-route owner look like a list of steps rather than a set of lanes.
 * The count lives in the section header instead.
 */
@Composable
private fun RouteEntryRow(
    row: RouteRow,
    resultsOwner: String?,
    results: List<CityResult>,
    isLoading: Boolean,
    fieldScroll: FieldScrollTarget,
    fieldTopMarginPx: Float,
    onQuery: (owner: String, query: String) -> Unit,
    onResultsCleared: () -> Unit,
    onRemove: () -> Unit,
    canRemove: Boolean,
) {
    val sourceKey = cityKeyRouteSource(row.id)
    val destinationKey = cityKeyRouteDestination(row.id)

    Row(
        modifier = Modifier.fillMaxWidth(),
        // Top-aligned: a city dropdown expands the field's column, and centring would
        // drag the arrow and the remove button down with it.
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        CitySearchField(
            query = row.sourceQuery,
            onQueryChange = { text ->
                row.sourceQuery = text
                if (row.sourceCity?.name != text) row.sourceCity = null
                onQuery(sourceKey, text)
                fieldScroll.scrollToTop(sourceKey, fieldTopMarginPx)
            },
            suggestions = if (resultsOwner == sourceKey) results else emptyList(),
            onCitySelected = { city ->
                row.sourceCity = city
                row.sourceQuery = city.name
                onResultsCleared()
            },
            isLoading = resultsOwner == sourceKey && isLoading,
            label = "Source",
            selectAllOnFocus = true,
            testTag = "city_search_route_${row.id}_source",
            modifier = Modifier
                .weight(1f)
                .onGloballyPositioned { fieldScroll.onFieldPositioned(sourceKey, it.positionInRoot().y) },
        )

        Box(modifier = Modifier.height(FieldHeight), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = FreightThemeTokens.colors.routeDestination,
            )
        }

        CitySearchField(
            query = row.destinationQuery,
            onQueryChange = { text ->
                row.destinationQuery = text
                if (row.destinationCity?.name != text) row.destinationCity = null
                onQuery(destinationKey, text)
                fieldScroll.scrollToTop(destinationKey, fieldTopMarginPx)
            },
            suggestions = if (resultsOwner == destinationKey) results else emptyList(),
            onCitySelected = { city ->
                row.destinationCity = city
                row.destinationQuery = city.name
                onResultsCleared()
            },
            isLoading = resultsOwner == destinationKey && isLoading,
            label = "Destination",
            selectAllOnFocus = true,
            testTag = "city_search_route_${row.id}_destination",
            modifier = Modifier
                .weight(1f)
                .onGloballyPositioned { fieldScroll.onFieldPositioned(destinationKey, it.positionInRoot().y) },
        )

        Box(modifier = Modifier.height(FieldHeight), contentAlignment = Alignment.Center) {
            if (canRemove) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("lead_remove_route_${row.id}")
                        .semantics { testTagsAsResourceId = true },
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove route",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
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
