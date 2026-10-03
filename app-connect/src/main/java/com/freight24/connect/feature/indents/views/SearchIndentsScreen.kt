package com.freight24.connect.feature.indents.views
import com.freight24.ui.components.FreightTopAppBar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import androidx.hilt.navigation.compose.hiltViewModel
import com.freight24.common.util.DateTimeUtils.formatRelativeTime
import com.freight24.common.util.formatLoadWeight
import com.freight24.connect.feature.indents.domain.viewmodel.SearchIndentsEvent
import com.freight24.rest.response.IndentResponse
import com.freight24.connect.feature.indents.domain.viewmodel.SearchIndentsUiState
import com.freight24.connect.feature.indents.domain.viewmodel.SearchIndentsViewModel
import com.freight24.connect.feature.indents.domain.viewmodel.SearchIndentsViewModelImpl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchIndentsScreen(
    onBackClick: () -> Unit,
    onIndentClick: (String) -> Unit,
    onCloneClick: (String, String) -> Unit,
    viewModel: SearchIndentsViewModel = hiltViewModel<SearchIndentsViewModelImpl>()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            FreightTopAppBar(
                title = {
                    TextField(
                        value = uiState.query,
                        onValueChange = { viewModel.onEvent(SearchIndentsEvent.UpdateQuery(it)) },
                        placeholder = { Text("Search by Client or Indent ID") },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = colorScheme.surface,
                            unfocusedContainerColor = colorScheme.surface,
                            disabledContainerColor = colorScheme.surface,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.surface.copy(alpha = 0f),
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = { viewModel.onEvent(SearchIndentsEvent.PerformSearch) }
                        ),
                        leadingIcon = {
                             Icon(Icons.Default.Search, contentDescription = "Search", tint = colorScheme.onSurfaceVariant)
                        },
                        trailingIcon = {
                            if (uiState.query.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onEvent(SearchIndentsEvent.UpdateQuery("")) }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        SearchIndentsContent(
            uiState = uiState,
            onIndentClick = onIndentClick,
            onCloneClick = onCloneClick,
            onFilterChange = { event -> viewModel.onEvent(event) },
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SearchIndentsContent(
    uiState: SearchIndentsUiState,
    onIndentClick: (String) -> Unit,
    onCloneClick: (String, String) -> Unit,
    onFilterChange: (SearchIndentsEvent.UpdateFilters) -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(modifier = modifier.fillMaxSize()) {
        // Filters
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = uiState.assignedToMe,
                    onClick = {
                        onFilterChange(SearchIndentsEvent.UpdateFilters(assignedToMe = !uiState.assignedToMe))
                    },
                    label = { Text("Assigned to Me") },
                    leadingIcon = if (uiState.assignedToMe) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null
                )
            }
            
            item {
                var expanded by remember { mutableStateOf(false) }
                Box {
                    FilterChip(
                        selected = uiState.priority != null,
                        onClick = { expanded = true },
                        label = { Text(uiState.priority ?: "Priority") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All") },
                            onClick = {
                                onFilterChange(SearchIndentsEvent.UpdateFilters(priority = null))
                                expanded = false
                            }
                        )
                        listOf("HIGH", "MEDIUM", "LOW").forEach { priority ->
                            DropdownMenuItem(
                                text = { Text(priority) },
                                onClick = {
                                    onFilterChange(SearchIndentsEvent.UpdateFilters(priority = priority))
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (uiState.indents.isEmpty() && uiState.query.isNotEmpty()) {
                Text(
                    text = "No indents found",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge,
                    color = colorScheme.onSurfaceVariant
                )
            } else {
                val context = LocalContext.current

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.indents) { indent ->
                        val loadingLocation = indent.indentGroupDetails.indentLocations.find { it.locationType == "loading" }
                        val unloadingLocation = indent.indentGroupDetails.indentLocations.find { it.locationType == "unloading" }
                        val clientProfile = indent.indentGroupDetails.clientProfile
                        val fleetExecutive = indent.fleetExecutive
                        val showAdmin = indent.indentOwnership == "OTHER"

                        ConnectIndentCard(
                            companyName = clientProfile?.companyName ?: clientProfile?.fullName ?: "Unknown",
                            indentCode = indent.indentCode,
                            postedTime = formatRelativeTime(indent.createdAt),
                            status = indent.status,
                            fromCity = loadingLocation?.areaName ?: loadingLocation?.cityName ?: "Unknown",
                            toCity = unloadingLocation?.areaName ?: unloadingLocation?.cityName ?: "Unknown",
                            clientCostFormatted = formatCurrency(indent.indentGroupDetails.clientTripCost),
                            negotiatedCostFormatted = formatCurrency(indent.negotiatedTripCost),
                            truckDisplay = indent.indentGroupDetails.truckType?.displayName ?: "N/A",
                            weightDisplay = formatLoadWeight(indent.indentGroupDetails.loadWeight, "MT"),
                            materialDisplay = indent.indentGroupDetails.material.replaceFirstChar { it.uppercase() },
                            showAdmin = showAdmin,
                            adminName = fleetExecutive?.name,
                            onCallClick = clientProfile?.phoneNumber?.takeIf { it.isNotBlank() }?.let { phone ->
                                {
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:$phone")
                                    }
                                    context.startActivity(intent)
                                }
                            },
                            onClick = { onIndentClick(indent.id) },
                            onCloneClick = { onCloneClick(indent.id, indent.indentGroupDetails.id) },
                        )
                    }
                }
            }
        }
    }
}
