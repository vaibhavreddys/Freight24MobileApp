package com.freight24.connect.feature.truckowners.views

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.freight24.connect.feature.truckowners.domain.viewmodel.ManageTruckOwnersViewModel
import com.freight24.connect.feature.truckowners.domain.viewmodel.ManageTruckOwnersViewModel.OwnerFilter
import com.freight24.rest.response.TruckOwnerDto
import com.freight24.ui.R
import com.freight24.ui.UiState
import com.freight24.ui.theme.FreightThemeTokens
import kotlinx.coroutines.delay
import kotlin.math.abs

private enum class OwnerSection(val title: String) {
    ACTIVE("Active"),
    PENDING("Pending Review"),
    INACTIVE("Inactive")
}

private data class SectionItem(
    val section: OwnerSection,
    val owner: TruckOwnerDto
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
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val filteredOwners by viewModel.filteredOwners.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val successColor = FreightThemeTokens.colors.successColor
    val listState = rememberLazyListState()
    val animatedOwnerIds = remember { mutableStateListOf<String>() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Truck Owners",
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.03).sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                },
                navigationIcon = {
                    Surface(
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .size(36.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable(onClick = onBackClick),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            AddTruckOwnerFab(
                successColor = successColor,
                onClick = onAddTruckOwnerClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            HeaderBlock(
                searchQuery = searchQuery,
                selectedFilter = selectedFilter,
                onSearchQueryChanged = viewModel::onSearchQueryChanged,
                onFilterSelected = viewModel::onFilterChanged
            )

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = viewModel::onRefresh,
                modifier = Modifier.fillMaxSize()
            ) {
                when (val state = uiState) {
                    is UiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = successColor)
                        }
                    }

                    is UiState.Error -> {
                        ErrorState(
                            message = state.message,
                            onRetry = viewModel::retry
                        )
                    }

                    is UiState.Success -> {
                        val groupedOwners = remember(filteredOwners) { buildSectionItems(filteredOwners) }
                        val listRows = remember(groupedOwners) { buildListRows(groupedOwners) }

                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 104.dp)
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
                                            is OwnerListRow.Owner -> row.item.owner.id
                                        }
                                    }
                                ) { index, row ->
                                    when (row) {
                                        is OwnerListRow.Header -> {
                                            SectionHeader(title = row.section.title)
                                        }

                                        is OwnerListRow.Owner -> {
                                            OwnerCard(
                                                owner = row.item.owner,
                                                index = index,
                                                shouldAnimate = row.item.owner.id !in animatedOwnerIds,
                                                onAnimationFinished = {
                                                    if (row.item.owner.id !in animatedOwnerIds) {
                                                        animatedOwnerIds.add(row.item.owner.id)
                                                    }
                                                },
                                                onClick = { onTruckOwnerClick(row.item.owner.user_id ?: row.item.owner.id) }
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))
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
    }
}

@Composable
private fun HeaderBlock(
    searchQuery: String,
    selectedFilter: OwnerFilter,
    onSearchQueryChanged: (String) -> Unit,
    onFilterSelected: (OwnerFilter) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 8.dp, bottom = 12.dp)
    ) {
        SearchField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(OwnerFilter.entries) { filter ->
                FilterChip(
                    title = filter.label,
                    isSelected = selectedFilter == filter,
                    onClick = { onFilterSelected(filter) }
                )
            }
        }
    }
}

@Composable
private fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        singleLine = true,
        placeholder = {
            Text(
                text = "Search by name or TAG ID",
                style = TextStyle(
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            cursorColor = MaterialTheme.colorScheme.primary
        ),
        textStyle = TextStyle(fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
    )
}

@Composable
private fun FilterChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val successColor = FreightThemeTokens.colors.successColor
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
            color = if (isSelected) successColor else MaterialTheme.colorScheme.surface,
            border = BorderStroke(
                width = 0.5.dp,
            color = if (isSelected) MaterialTheme.colorScheme.surface.copy(alpha = 0f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            ),
        modifier = Modifier.height(32.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color =
                        if (isSelected) {
                            FreightThemeTokens.colors.onSuccessColor
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                        }
                )
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = TextStyle(
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
        ),
        modifier = Modifier.padding(start = 2.dp, top = 4.dp, bottom = 8.dp)
    )
}

@Composable
private fun OwnerCard(
    owner: TruckOwnerDto,
    index: Int,
    shouldAnimate: Boolean,
    onAnimationFinished: () -> Unit,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()
    var visible by remember(owner.id, shouldAnimate) { mutableStateOf(!shouldAnimate) }
    val density = LocalDensity.current
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.99f else 1f,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "owner_card_scale"
    )
    val hoverOffset by animateDpAsState(
        targetValue = if (isHovered) (-1).dp else 0.dp,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "owner_card_hover_offset"
    )
    val alpha by animateFloatAsState(
        targetValue = when {
            !visible -> 0f
            owner.ownerSection == OwnerSection.INACTIVE -> 0.6f
            else -> 1f
        },
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "owner_card_alpha"
    )
    val translateY by animateFloatAsState(
        targetValue = with(density) { if (visible) hoverOffset.toPx() else 10.dp.toPx() },
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "owner_card_translation"
    )

    LaunchedEffect(owner.id, shouldAnimate) {
        if (shouldAnimate) {
            delay(index * 50L)
            visible = true
            onAnimationFinished()
        } else {
            visible = true
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
                translationY = translateY
            },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OwnerAvatar(owner = owner)
            Spacer(modifier = Modifier.width(13.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = owner.full_name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.01).sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Spacer(modifier = Modifier.height(3.dp))
                TagText(owner = owner)
            }

            Spacer(modifier = Modifier.width(12.dp))
            StatusBadge(owner.ownerSection)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}

@Composable
private fun OwnerAvatar(owner: TruckOwnerDto) {
    val inactiveBackground = MaterialTheme.colorScheme.surfaceVariant
    val inactiveText = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    val palette =
        remember(owner.full_name, owner.ownerSection, inactiveBackground, inactiveText) {
            avatarPaletteFor(
                owner = owner,
                inactiveBackground = inactiveBackground,
                inactiveText = inactiveText
            )
        }

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(palette.first),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = owner.full_name.initials(),
            style = TextStyle(
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.03.sp,
                color = palette.second
            )
        )
    }
}

@Composable
private fun TagText(owner: TruckOwnerDto) {
    val warningColor = FreightThemeTokens.colors.warningColor

    if (owner.ownerSection == OwnerSection.PENDING) {
        Text(
            text = "Pending assignment",
            style = TextStyle(
                fontSize = 11.5.sp,
                color = warningColor.copy(alpha = 0.6f)
            )
        )
        return
    }

    val tag = owner.tag_code.orEmpty().ifBlank { "Not assigned" }
    Text(
        text = buildAnnotatedString {
            withStyle(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                append("#")
            }
            withStyle(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                append(tag)
            }
        },
        style = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = 11.5.sp
        )
    )
}

@Composable
private fun StatusBadge(section: OwnerSection) {
    val successColor = FreightThemeTokens.colors.successColor
    val warningColor = FreightThemeTokens.colors.warningColor
    val background: Color
    val content: Color
    val border: Color
    val label: String

    when (section) {
        OwnerSection.ACTIVE -> {
            background = successColor.copy(alpha = 0.10f)
            content = successColor
            border = successColor.copy(alpha = 0.20f)
            label = "Active"
        }

        OwnerSection.PENDING -> {
            background = warningColor.copy(alpha = 0.10f)
            content = warningColor
            border = warningColor.copy(alpha = 0.20f)
            label = "Pending"
        }

        OwnerSection.INACTIVE -> {
            background = MaterialTheme.colorScheme.surfaceVariant
            content = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
            border = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            label = "Inactive"
        }
    }

    Box(
        modifier = Modifier
            .border(BorderStroke(0.5.dp, border), RoundedCornerShape(20.dp))
            .background(background, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.02.sp,
                color = content
            )
        )
    }
}

@Composable
private fun AddTruckOwnerFab(
    successColor: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()
    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.96f
            isHovered -> 1.05f
            else -> 1f
        },
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "fab_scale"
    )

    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier
            .padding(end = 4.dp, bottom = 4.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(16.dp),
        containerColor = FreightThemeTokens.colors.successColor,
        contentColor = FreightThemeTokens.colors.onSuccessColor,
        interactionSource = interactionSource
    ) {
        Box(
            modifier = Modifier.size(52.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = com.freight24.connect.R.drawable.ic_add_truck_owner),
                contentDescription = "Add truck owner"
            )
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = FreightThemeTokens.colors.successColor
        ) {
            Text(
                text = "Retry",
                modifier = Modifier
                    .clickable(onClick = onRetry)
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                color = FreightThemeTokens.colors.onSuccessColor
            )
        }
    }
}

@Composable
private fun EmptyState(hasSearch: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 96.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (hasSearch) "No matching truck owners found" else "No truck owners available",
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

private fun buildSectionItems(owners: List<TruckOwnerDto>): List<SectionItem> {
    val grouped = owners.groupBy { it.ownerSection }
    return buildList {
        OwnerSection.entries.forEach { section ->
            grouped[section]
                ?.sortedBy { it.full_name.lowercase() }
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

private val TruckOwnerDto.ownerSection: OwnerSection
    get() =
        when (status?.trim()?.lowercase()) {
            "active" -> OwnerSection.ACTIVE
            "inactive" -> OwnerSection.INACTIVE
            else -> OwnerSection.PENDING
        }

private fun avatarPaletteFor(
    owner: TruckOwnerDto,
    inactiveBackground: Color,
    inactiveText: Color
): Pair<Color, Color> {
    if (owner.ownerSection == OwnerSection.INACTIVE) {
        return inactiveBackground to inactiveText
    }

    val palettes =
        listOf(
            Color(0xFFEEEDFE) to Color(0xFF534AB7),
            Color(0xFFE1F5EE) to Color(0xFF0F6E56),
            Color(0xFFFAEEDA) to Color(0xFF854F0B),
            Color(0xFFE6F1FB) to Color(0xFF185FA5),
            Color(0xFFFAECE7) to Color(0xFF993C1D)
        )
    val hash = owner.full_name.trim().ifBlank { owner.id }.hashCode()
    return palettes[abs(hash) % palettes.size]
}

private fun String.initials(): String =
    trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.take(1).uppercase() }
        .ifBlank { "TO" }
