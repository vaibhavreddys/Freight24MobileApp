package com.freight24.connect.feature.truckowners.views
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freight24.connect.feature.truckowners.domain.viewmodel.ManageTruckOwnersViewModel
import com.freight24.connect.feature.truckowners.domain.viewmodel.ManageTruckOwnersViewModel.OwnerFilter
import com.freight24.connect.domain.model.TruckOwnerManagementItem
import com.freight24.ui.UiState
import com.freight24.ui.components.DocketCard
import com.freight24.ui.components.FilterSelectionChip
import com.freight24.ui.components.FreightEmptyState
import com.freight24.ui.components.FreightTopAppBar
import com.freight24.ui.theme.FreightThemeTokens
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged

private enum class OwnerSection(val title: String) {
    ACTIVE("Active"),
    PENDING("Pending Review"),
    INACTIVE("Inactive"),
}

private data class SectionItem(
    val section: OwnerSection,
    val owner: TruckOwnerManagementItem,
)

private sealed interface OwnerListRow {
    data class Header(val section: OwnerSection) : OwnerListRow

    data class Owner(val item: SectionItem) : OwnerListRow
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageTruckOwnersScreen(
    viewModel: ManageTruckOwnersViewModel = hiltViewModel(),
    onAddTruckOwnerClick: () -> Unit,
    onTruckOwnerClick: (String) -> Unit,
    onBackClick: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val filteredOwners by viewModel.filteredOwners.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val isLoadingMore by viewModel.isLoadingMore.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val animatedOwnerIds = remember { mutableStateListOf<String>() }
    var isNavigatingBack by remember { mutableStateOf(false) }
    val navigationInteractionSource = remember { MutableInteractionSource() }

    LaunchedEffect(listState, filteredOwners.size) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .distinctUntilChanged()
            .collect { lastVisibleIndex ->
                val totalItems = listState.layoutInfo.totalItemsCount
                if (lastVisibleIndex != null && totalItems > 0 && lastVisibleIndex >= totalItems - 4) {
                    viewModel.loadNextPage()
                }
            }
    }

    fun navigateBack() {
        if (isNavigatingBack) return
        isNavigatingBack = true
        onBackClick()
    }

    BackHandler(enabled = !isNavigatingBack) {
        navigateBack()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            FreightTopAppBar(
                title = {
                    Text(
                        text = "Truck Owners",
                        style =
                            MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = (-0.03).sp,
                                color = MaterialTheme.colorScheme.onSurface,
                            ),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navigateBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            AddTruckOwnerFab(onClick = { if (!isNavigatingBack) onAddTruckOwnerClick() })
        },
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize(),
            ) {
                HeaderBlock(
                    searchQuery = searchQuery,
                    selectedFilter = selectedFilter,
                    onSearchQueryChanged = viewModel::onSearchQueryChanged,
                    onFilterSelected = viewModel::onFilterChanged,
                )

                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = viewModel::onRefresh,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    when (val state = uiState) {
                        is UiState.Loading -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }

                        is UiState.Error -> {
                            ErrorState(
                                message = state.message,
                                onRetry = viewModel::retry,
                            )
                        }

                        is UiState.Success -> {
                            val groupedOwners = remember(filteredOwners) { buildSectionItems(filteredOwners) }
                            val listRows = remember(groupedOwners) { buildListRows(groupedOwners) }

                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 104.dp),
                            ) {
                                if (groupedOwners.isEmpty()) {
                                    item {
                                        EmptyState(hasSearch = searchQuery.isNotBlank() || selectedFilter != OwnerFilter.ALL)
                                    }
                                } else {
                                    itemsIndexed(
                                        items = listRows,
                                        key = { _, row ->
                                            when (row) {
                                                is OwnerListRow.Header -> "header_${row.section.name}"
                                                is OwnerListRow.Owner -> row.item.owner.ownerUserId
                                            }
                                        },
                                    ) { index, row ->
                                        when (row) {
                                            is OwnerListRow.Header -> {
                                                SectionHeader(title = row.section.title)
                                            }

                                            is OwnerListRow.Owner -> {
                                                OwnerCard(
                                                    owner = row.item.owner,
                                                    index = index,
                                                    shouldAnimate = row.item.owner.ownerUserId !in animatedOwnerIds,
                                                    onAnimationFinished = {
                                                        if (row.item.owner.ownerUserId !in animatedOwnerIds) {
                                                            animatedOwnerIds.add(row.item.owner.ownerUserId)
                                                        }
                                                    },
                                                    onClick = { onTruckOwnerClick(row.item.owner.ownerUserId) },
                                                )
                                                Spacer(modifier = Modifier.height(12.dp))
                                            }
                                        }
                                    }
                                    if (isLoadingMore) {
                                        item {
                                            Box(
                                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                CircularProgressIndicator()
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        else -> Unit
                    }
                }
            }
            // Topmost scrim: once BACK is pressed this screen is animating out,
            // but its list items would still win hit-tests against the page
            // underneath (More). This overlay consumes those taps instead.
            if (isNavigatingBack) {
                Box(
                    modifier =
                        Modifier
                            .matchParentSize()
                            .clickable(
                                interactionSource = navigationInteractionSource,
                                indication = null,
                                onClick = {},
                            ),
                )
            }
        }
    }
}

@Composable
private fun HeaderBlock(
    searchQuery: String,
    selectedFilter: OwnerFilter,
    onSearchQueryChanged: (String) -> Unit,
    onFilterSelected: (OwnerFilter) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(top = 8.dp, bottom = 12.dp),
    ) {
        SearchField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(OwnerFilter.entries) { filter ->
                FilterSelectionChip(
                    label = filter.label,
                    isSelected = selectedFilter == filter,
                    onClick = { onFilterSelected(filter) },
                )
            }
        }
    }
}

@Composable
private fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        singleLine = true,
        placeholder = {
            Text(
                text = "Search by name, phone or TAG ID",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        colors =
            OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                cursorColor = MaterialTheme.colorScheme.primary,
            ),
        textStyle = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
private fun FilterChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    // Navigation filter — brand primary, not the success/status semantic.
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        border =
            BorderStroke(
                width = 0.5.dp,
                color =
                    if (isSelected) {
                        MaterialTheme.colorScheme.surface.copy(
                            alpha = 0f,
                        )
                    } else {
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    },
            ),
        modifier = Modifier.height(32.dp),
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = title,
                style =
                    MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color =
                            if (isSelected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            },
                    ),
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style =
            MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
    )
}

@Composable
private fun OwnerCard(
    owner: TruckOwnerManagementItem,
    index: Int,
    shouldAnimate: Boolean,
    onAnimationFinished: () -> Unit,
    onClick: () -> Unit,
) {
    var visible by remember(owner.ownerUserId, shouldAnimate) { mutableStateOf(!shouldAnimate) }
    val density = LocalDensity.current
    val alpha by animateFloatAsState(
        targetValue =
            when {
                !visible -> 0f
                owner.ownerSection == OwnerSection.INACTIVE -> 0.75f
                else -> 1f
            },
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "owner_card_alpha",
    )
    val translateY by animateFloatAsState(
        targetValue = with(density) { if (visible) 0f else 10.dp.toPx() },
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "owner_card_translation",
    )

    LaunchedEffect(owner.ownerUserId, shouldAnimate) {
        if (shouldAnimate) {
            delay(index * 50L)
            visible = true
            onAnimationFinished()
        } else {
            visible = true
        }
    }

    // Spine keyed to the owner's state in the partner book: green = active,
    // amber = onboarding pending, neutral = inactive.
    DocketCard(
        accent =
            when (owner.ownerSection) {
                OwnerSection.ACTIVE -> MaterialTheme.colorScheme.primary
                OwnerSection.PENDING -> FreightThemeTokens.colors.warning
                OwnerSection.INACTIVE -> MaterialTheme.colorScheme.outlineVariant
            },
        testTag = "owners_card_${owner.ownerUserId}",
        onClick = onClick,
        modifier =
            Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    this.alpha = alpha
                    translationY = translateY
                },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OwnerAvatar(owner = owner)
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = owner.fullName,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style =
                        MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                )
                Spacer(modifier = Modifier.height(2.dp))
                TagText(owner = owner)
            }

            Spacer(modifier = Modifier.width(10.dp))
            // No per-row status stamp: the section header and the spine already
            // state the owner's state for every row in the group (D9 rule).
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            )
        }
    }
}

@Composable
private fun OwnerAvatar(owner: TruckOwnerManagementItem) {
    val inactive = owner.ownerSection == OwnerSection.INACTIVE
    Box(
        modifier =
            Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    if (inactive) {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    },
                ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = owner.fullName.initials(),
            style =
                MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.03.sp,
                    color =
                        if (inactive) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        },
                ),
        )
    }
}

@Composable
private fun TagText(owner: TruckOwnerManagementItem) {
    val warningColor = FreightThemeTokens.colors.warning

    if (owner.ownerSection == OwnerSection.PENDING) {
        Text(
            text = "Pending assignment",
            style =
                MaterialTheme.typography.bodySmall.copy(
                    color = warningColor,
                ),
        )
        return
    }

    val tag = owner.tagCode.orEmpty().ifBlank { "Not assigned" }
    Text(
        text =
            buildAnnotatedString {
                withStyle(
                    style =
                        SpanStyle(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                ) {
                    append("#")
                }
                withStyle(
                    style =
                        SpanStyle(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                ) {
                    append(tag)
                }
            },
        style =
            MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
            ),
    )
}

@Composable
private fun AddTruckOwnerFab(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ) {
        Icon(
            Icons.Outlined.Add,
            contentDescription = "Add truck owner",
        )
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(40.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(16.dp))
        FilledTonalButton(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
private fun EmptyState(hasSearch: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        FreightEmptyState(
            icon = Icons.Default.Person,
            title = if (hasSearch) "No matching truck owners" else "No truck owners yet",
            subtitle =
                if (hasSearch) {
                    "Try a different name or TAG ID"
                } else {
                    "Onboard your first truck owner to get started"
                },
        )
    }
}

private fun buildSectionItems(owners: List<TruckOwnerManagementItem>): List<SectionItem> {
    val grouped = owners.groupBy { it.ownerSection }
    return buildList {
        OwnerSection.entries.forEach { section ->
            grouped[section]
                ?.sortedBy { it.fullName.lowercase() }
                ?.forEach { owner -> add(SectionItem(section = section, owner = owner)) }
        }
    }
}

private fun buildListRows(items: List<SectionItem>): List<OwnerListRow> {
    val rows = mutableListOf<OwnerListRow>()
    var previousSection: OwnerSection? = null
    items.forEach { item ->
        if (item.section != previousSection) {
            rows.add(OwnerListRow.Header(item.section))
            previousSection = item.section
        }
        rows.add(OwnerListRow.Owner(item))
    }
    return rows
}

private val TruckOwnerManagementItem.ownerSection: OwnerSection
    get() =
        when (status?.trim()?.lowercase()) {
            "active" -> OwnerSection.ACTIVE
            "inactive" -> OwnerSection.INACTIVE
            else -> OwnerSection.PENDING
        }

private fun String.initials(): String =
    trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.take(1).uppercase() }
        .ifBlank { "TO" }
