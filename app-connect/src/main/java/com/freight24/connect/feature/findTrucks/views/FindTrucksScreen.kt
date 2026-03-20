package com.freight24.connect.feature.findTrucks.views

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.freight24.connect.feature.findTrucks.domain.viewmodel.FindTrucksEvent
import com.freight24.connect.feature.findTrucks.domain.viewmodel.FindTrucksViewModel
import com.freight24.connect.feature.findTrucks.domain.viewmodel.FindTrucksViewModelImpl
import com.freight24.connect.feature.indents.views.SearchType
import com.freight24.rest.response.LocationDto
import com.freight24.rest.response.SearchTruckerResponse
import com.freight24.rest.response.TruckCategoryDto
import com.freight24.rest.response.TruckTypeDto
import com.freight24.ui.components.FilterSelectionChip
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
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

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
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    LocationItem(
                        label = "Loading Point",
                        location = uiState.loadingLocation,
                        iconColor = Color(0xFF22C55E),
                        onClick = { onNavigateToSearch(SearchType.LOCATION_FROM.name) }
                    )
                    
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    
                    LocationItem(
                        label = "Unloading Point",
                        location = uiState.unloadingLocation,
                        iconColor = Color(0xFFEF4444),
                        isOptional = true,
                        onClick = { onNavigateToSearch(SearchType.LOCATION_TO.name) }
                    )
                }
            }

            // Truck Type Section
            var isTruckSectionExpanded by remember { mutableStateOf(true) }
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
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
                            if (uiState.selectedTruckType != null) {
                                Text(
                                    uiState.selectedTruckType?.displayName ?: "",
                                    color = Color(0xFF22C55E),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            } else {
                                Text(
                                    "Select truck type",
                                    color = Color(0xFF64748B),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (uiState.selectedTruckType != null) {
                                IconButton(onClick = { /* Handle clear if needed, but in this UI its just selection */ }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Icon(
                                if (isTruckSectionExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8)
                            )
                        }
                    }

                    if (isTruckSectionExpanded && uiState.truckTypes != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        TruckTypeChips(
                            truckTypes = uiState.truckTypes!!,
                            selectedId = uiState.selectedTruckType?.id,
                            onSelect = { viewModel.onEvent(FindTrucksEvent.SelectTruckType(it)) }
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
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF22C55E),
                    disabledContainerColor = Color(0xFFCBD5E1)
                ),
                enabled = uiState.loadingLocation != null && uiState.selectedTruckType != null && !uiState.isSearching
            ) {
                if (uiState.isSearching) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Search Trucks", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Search Results Bottom Sheet
        if (uiState.showResults) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.onEvent(FindTrucksEvent.DismissResults) },
                sheetState = sheetState,
                containerColor = Color.White,
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
            color = iconColor.copy(alpha = 0.1f)
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
                    color = Color(0xFF64748B)
                )
                if (isOptional) {
                    Text(
                        " • Optional",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
            Text(
                text = location?.name ?: "Tap to select",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (location != null) FontWeight.Bold else FontWeight.Medium,
                color = if (location != null) Color(0xFF0F172A) else Color(0xFF94A3B8)
            )

            if (location != null) {
                val locationName = if(location.type.lowercase() == "city")  location.state ?: "" else location.city ?: ""
                Text(
                    locationName,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF64748B)
                )
            }
        }
        
        Icon(
            Icons.Default.Search,
            contentDescription = null,
            tint = Color(0xFFCBD5E1),
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun TruckTypeChips(
    truckTypes: TruckCategoryDto,
    selectedId: String?,
    onSelect: (TruckTypeDto) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (truckTypes.open.isNotEmpty()) {
            TruckCategorySection("Open", truckTypes.open, selectedId, onSelect)
        }
        if (truckTypes.closed.isNotEmpty()) {
            TruckCategorySection("Closed", truckTypes.closed, selectedId, onSelect)
        }
        if (truckTypes.trailer.isNotEmpty()) {
            TruckCategorySection("Trailer", truckTypes.trailer, selectedId, onSelect)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TruckCategorySection(
    title: String,
    types: List<TruckTypeDto>,
    selectedId: String?,
    onSelect: (TruckTypeDto) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF64748B),
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
                    isSelected = selectedId == truck.id,
                    onClick = { onSelect(truck) }
                )
            }
        }
    }
}

@Composable
fun SearchResultsContent(data: SearchTruckerResponse.TruckerData?) {
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
                color = Color(0xFF0F172A)
            )
            Surface(
                color = Color(0xFFDCFCE7),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    "${data?.totalOwners ?: 0} TRUCKERS FOUND",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF15803D),
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        if (data == null || data.truckers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color(0xFFCBD5E1)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No trucks found for this criteria", color = Color(0xFF64748B))
                }
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
    var isExpanded by remember { mutableStateOf(false) }
    val makeCall = rememberCallAction(trucker.phone)

    Card(
        modifier = Modifier.fillMaxWidth().clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
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
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = Color(0xFFFFF7ED),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Outlined.WorkspacePremium,
                                    contentDescription = null,
                                    tint = Color(0xFFC2410C),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    trucker.score,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC2410C)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${trucker.trucks.size} ${if (trucker.trucks.size == 1) "truck" else "trucks"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF64748B)
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
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = CircleShape,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                if (isExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                                contentDescription = "Expand",
                                tint = Color(0xFF1E293B),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            if (isExpanded) {
                HorizontalDivider(color = Color(0xFFE2E8F0))
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
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                color = Color.White,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Outlined.LocalShipping,
                                        contentDescription = null,
                                        tint = Color(0xFF475569),
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
                                        color = Color(0xFF0F172A)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = Color(0xFFDCFCE7),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Outlined.WorkspacePremium,
                                                contentDescription = null,
                                                tint = Color(0xFF16A34A),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(
                                                truck.score,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF16A34A)
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
                                                color = Color(0xFFDCFCE7),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(
                                                    reason,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = Color(0xFF16A34A),
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
                                    tint = Color(0xFF16A34A)
                                )
                            }
                        }
                        if (index < trucker.trucks.size - 1) {
                            HorizontalDivider(
                                color = Color(0xFFE2E8F0), 
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
