package com.freight24.connect.feature.findTrucks.views

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.freight24.connect.R
import com.freight24.connect.feature.findTrucks.domain.viewmodel.FindTrucksEvent
import com.freight24.connect.feature.findTrucks.domain.viewmodel.FindTrucksViewModel
import com.freight24.connect.feature.findTrucks.domain.viewmodel.FindTrucksViewModelImpl
import com.freight24.connect.feature.indents.views.SearchType
import com.freight24.rest.response.LocationDto
import com.freight24.rest.response.SearchTruckerResponse
import com.freight24.rest.response.TruckCategoryDto
import com.freight24.rest.response.TruckTypeDto
import com.freight24.ui.components.FilterSelectionChip
import com.freight24.ui.components.FreightEmptyState
import kotlinx.serialization.json.Json
import androidx.core.net.toUri
import com.freight24.ui.components.rememberCallAction

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FindTrucksScreen(
    paddingValues: PaddingValues,
    onNavigateToSearch: (String) -> Unit,
    navController: NavController,
    viewModel: FindTrucksViewModel = hiltViewModel<FindTrucksViewModelImpl>()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var swapRotationTarget by remember { mutableFloatStateOf(0f) }
    val swapRotation by animateFloatAsState(
        targetValue = swapRotationTarget,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "Location swap rotation"
    )

    // Observe SavedStateHandle for location selections
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    
    LaunchedEffect(savedStateHandle) {
        savedStateHandle?.getStateFlow<String?>("selected_from_location", null)
            ?.collect { json ->
                json?.let {
                    val location = Json.decodeFromString<LocationDto>(it)
                    viewModel.onEvent(FindTrucksEvent.SelectLoadingLocation(location))
                    savedStateHandle.remove<String>("selected_from_location")
                }
            }
    }

    LaunchedEffect(savedStateHandle) {
        savedStateHandle?.getStateFlow<String?>("selected_to_location", null)
            ?.collect { json ->
                json?.let {
                    val location = Json.decodeFromString<LocationDto>(it)
                    viewModel.onEvent(FindTrucksEvent.SelectUnloadingLocation(location))
                    savedStateHandle.remove<String>("selected_to_location")
                }
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(paddingValues)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Location Selection Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    LocationItem(
                        label = "Loading Point",
                        location = uiState.loadingLocation,
                        iconColor = colorScheme.primary,
                        onClick = { onNavigateToSearch(SearchType.LOCATION_FROM.name) }
                    )
                    
                    Box(
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        HorizontalDivider(color = colorScheme.outlineVariant)
                        TooltipBox(
                            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                            tooltip = { PlainTooltip { Text(stringResource(R.string.find_trucks_swap_locations)) } },
                            state = rememberTooltipState()
                        ) {
                            FilledIconButton(
                                onClick = {
                                    swapRotationTarget += 180f
                                    viewModel.onEvent(FindTrucksEvent.SwapLocations)
                                },
                                enabled = !uiState.isSearching &&
                                    (uiState.loadingLocation != null || uiState.unloadingLocation != null),
                                modifier = Modifier.size(48.dp),
                                shape = CircleShape,
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = colorScheme.onSurfaceVariant,
                                    contentColor = colorScheme.surface
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwapVert,
                                    contentDescription = stringResource(R.string.find_trucks_swap_locations),
                                    modifier = Modifier.size(24.dp).rotate(swapRotation)
                                )
                            }
                        }
                    }
                    
                    LocationItem(
                        label = "Unloading Point",
                        location = uiState.unloadingLocation,
                        iconColor = colorScheme.secondary,
                        isOptional = true,
                        onClick = { onNavigateToSearch(SearchType.LOCATION_TO.name) }
                    )
                }
            }

            // Truck Type Section
            var isTruckSectionExpanded by remember { mutableStateOf(true) }
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isTruckSectionExpanded = !isTruckSectionExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Truck Type",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (uiState.selectedTruckTypes.isNotEmpty()) {
                                val names = uiState.selectedTruckTypes.joinToString(", ") { it.displayName }
                                val label = if (uiState.selectedTruckTypes.size > 2) {
                                    "${uiState.selectedTruckTypes.take(2).joinToString(", ") { it.displayName }} (+${uiState.selectedTruckTypes.size - 2})"
                                } else {
                                    names
                                }
                                Text(
                                    label,
                                    color = colorScheme.primary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            } else {
                                Text(
                                    "Select truck types",
                                    color = colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (uiState.selectedTruckTypes.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onEvent(FindTrucksEvent.ClearTruckTypes) }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Icon(
                                if (isTruckSectionExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (isTruckSectionExpanded && uiState.truckTypes != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        TruckTypeChips(
                            truckTypes = uiState.truckTypes!!,
                            selectedIds = uiState.selectedTruckTypes.map { it.id }.toSet(),
                            onToggle = { viewModel.onEvent(FindTrucksEvent.ToggleTruckType(it)) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.onEvent(FindTrucksEvent.SearchTrucks) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.primary,
                    contentColor = colorScheme.onPrimary,
                    disabledContainerColor = colorScheme.surfaceContainerHighest,
                    disabledContentColor = colorScheme.onSurfaceVariant,
                ),
                enabled = uiState.loadingLocation != null && uiState.selectedTruckTypes.isNotEmpty() && !uiState.isSearching
            ) {
                if (uiState.isSearching) {
                    CircularProgressIndicator(color = colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text("Search Trucks", style = MaterialTheme.typography.titleMedium)
                }
            }
        }

        // Search Results Bottom Sheet
        if (uiState.showResults) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.onEvent(FindTrucksEvent.DismissResults) },
                sheetState = sheetState,
                containerColor = colorScheme.surface,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                SearchResultsContent(uiState.searchResults)
            }
        }
    }

    if (uiState.error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(FindTrucksEvent.ClearError) },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(FindTrucksEvent.ClearError) }) {
                    Text("OK")
                }
            },
            title = { Text("Error") },
            text = { Text(uiState.error!!) }
        )
    }
}

@Composable
fun LocationItem(
    label: String,
    location: LocationDto?,
    iconColor: Color,
    isOptional: Boolean = false,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = iconColor.copy(alpha = 0.12f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelMedium,
                    color = colorScheme.onSurfaceVariant
                )
                if (isOptional) {
                    Text(
                        " • Optional",
                        style = MaterialTheme.typography.labelMedium,
                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                    )
                }
            }
            Text(
                text = location?.name ?: "Tap to select",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (location != null) FontWeight.Bold else FontWeight.Medium,
                color = if (location != null) colorScheme.onSurface else colorScheme.onSurfaceVariant
            )

            if (location != null) {
                val locationName = if(location.type.lowercase() == "city")  location.state ?: "" else location.city ?: ""
                Text(
                    locationName,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }
        
        Icon(
            Icons.Default.Search,
            contentDescription = null,
            tint = colorScheme.outlineVariant,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun TruckTypeChips(
    truckTypes: TruckCategoryDto,
    selectedIds: Set<String>,
    onToggle: (TruckTypeDto) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (truckTypes.open.isNotEmpty()) {
            TruckCategorySection("Open", truckTypes.open, selectedIds, onToggle)
        }
        if (truckTypes.closed.isNotEmpty()) {
            TruckCategorySection("Closed", truckTypes.closed, selectedIds, onToggle)
        }
        if (truckTypes.trailer.isNotEmpty()) {
            TruckCategorySection("Trailer", truckTypes.trailer, selectedIds, onToggle)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TruckCategorySection(
    title: String,
    types: List<TruckTypeDto>,
    selectedIds: Set<String>,
    onToggle: (TruckTypeDto) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.labelMedium,
            color = colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            types.forEach { truck ->
                FilterSelectionChip(
                    label = truck.displayName,
                    isSelected = selectedIds.contains(truck.id),
                    onClick = { onToggle(truck) }
                )
            }
        }
    }
}

@Composable
fun SearchResultsContent(data: SearchTruckerResponse.TruckerData?) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Search Results",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )
            Surface(
                color = colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    "${data?.totalOwners ?: 0} TRUCKERS FOUND",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        if (data == null || data.truckers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                FreightEmptyState(
                    icon = Icons.Outlined.LocalShipping,
                    title = "No trucks found",
                    subtitle = "Try a different route or add more truck types",
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(data.truckers) { trucker ->
                    TruckerResultItem(trucker)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TruckerResultItem(trucker: SearchTruckerResponse.Trucker) {
    val colorScheme = MaterialTheme.colorScheme
    var isExpanded by remember { mutableStateOf(false) }
    val makeCall = rememberCallAction(trucker.phone)

    Card(
        modifier = Modifier.fillMaxWidth().clickable { isExpanded = !isExpanded },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        border = BorderStroke(1.dp, colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            trucker.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = colorScheme.tertiaryContainer,
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Outlined.WorkspacePremium,
                                    contentDescription = null,
                                    tint = colorScheme.tertiary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    trucker.score,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.tertiary
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${trucker.trucks.size} ${if (trucker.trucks.size == 1) "truck" else "trucks"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { makeCall() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Call,
                            contentDescription = "Call",
                            tint = colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = colorScheme.surfaceVariant,
                        shape = CircleShape,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                if (isExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                                contentDescription = "Expand",
                                tint = colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            if (isExpanded) {
                HorizontalDivider(color = colorScheme.outlineVariant)
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    trucker.trucks.forEachIndexed { index, truck ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outlineVariant),
                                color = colorScheme.surface,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Outlined.LocalShipping,
                                        contentDescription = null,
                                        tint = colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        truck.registrationNumber,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = colorScheme.primaryContainer,
                                        shape = MaterialTheme.shapes.medium
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Outlined.WorkspacePremium,
                                                contentDescription = null,
                                                tint = colorScheme.primary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(
                                                truck.score,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = colorScheme.primary
                                            )
                                        }
                                    }
                                }
                                
                                if (truck.matchReasons.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        truck.matchReasons.forEach { reason ->
                                            Surface(
                                                color = colorScheme.primaryContainer,
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(
                                                    reason,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = colorScheme.primary,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Box(modifier = Modifier.align(Alignment.CenterVertically)) {
                                Icon(
                                    Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = colorScheme.primary
                                )
                            }
                        }
                        if (index < trucker.trucks.size - 1) {
                            HorizontalDivider(
                                color = colorScheme.outlineVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
