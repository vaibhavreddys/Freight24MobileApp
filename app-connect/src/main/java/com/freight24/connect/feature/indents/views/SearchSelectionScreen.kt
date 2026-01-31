package com.freight24.connect.feature.indents.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.freight24.connect.feature.indents.domain.viewmodel.CreateIndentEvent
import com.freight24.connect.feature.indents.domain.viewmodel.IndentsViewModel
import com.freight24.connect.feature.indents.domain.viewmodel.IndentsViewModelImpl
import com.freight24.connect.navigation.ConnectDestination
import com.freight24.rest.response.ClientDto
import com.freight24.rest.response.LocationDto

enum class SearchType {
    CLIENT,
    LOCATION_FROM,
    LOCATION_TO,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchSelectionScreen(
    searchType: SearchType,
    onBackClick: () -> Unit,
    navController: NavController,
) {
    // Scope ViewModel to CreateIndent destination to share state with CreateIndentScreen
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val createIndentBackStackEntry = remember(navBackStackEntry) {
        navController.getBackStackEntry(ConnectDestination.CreateIndent.route)
    }
    val viewModel: IndentsViewModel = hiltViewModel<IndentsViewModelImpl>(
        createIndentBackStackEntry
    )
    val uiState by viewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }

    val (title, hint, query, results, isLoading) =
        when (searchType) {
            SearchType.CLIENT ->
                SearchConfig(
                    title = "Select Client",
                    hint = "Search by client name",
                    query = uiState.clientQuery,
                    results = uiState.clientSearchResults,
                    isLoading = false,
                )

            SearchType.LOCATION_FROM ->
                SearchConfig(
                    title = "Select Loading Location",
                    hint = "Search by city or area",
                    query = uiState.fromLocationQuery,
                    results = uiState.fromLocationSearchResults,
                    isLoading = false,
                )

            SearchType.LOCATION_TO ->
                SearchConfig(
                    title = "Select Unloading Location",
                    hint = "Search by city or area",
                    query = uiState.toLocationQuery,
                    results = uiState.toLocationSearchResults,
                    isLoading = false,
                )
        }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { newQuery ->
                    when (searchType) {
                        SearchType.CLIENT -> viewModel.onEvent(CreateIndentEvent.SearchClient(newQuery))
                        SearchType.LOCATION_FROM -> viewModel.onEvent(CreateIndentEvent.SearchFromLocation(newQuery))
                        SearchType.LOCATION_TO -> viewModel.onEvent(CreateIndentEvent.SearchToLocation(newQuery))
                    }
                },
                label = { Text(hint) },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .focusRequester(focusRequester),
                singleLine = true,
            )

            if (isLoading) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    if (query.length < 3) {
                        item {
                            Text(
                                text = "Type at least 3 characters to search",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                            )
                        }
                    } else if (results.isEmpty()) {
                        item {
                            Text(
                                text = "No results found",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                            )
                        }
                    } else {
                        items(results) { item ->
                            when (item) {
                                is LocationDto -> {
                                    LocationListItem(
                                        location = item,
                                        onClick = {
                                            when (searchType) {
                                                SearchType.LOCATION_FROM -> viewModel.onEvent(CreateIndentEvent.SelectFromLocation(item))
                                                SearchType.LOCATION_TO -> viewModel.onEvent(CreateIndentEvent.SelectToLocation(item))
                                                else -> {}
                                            }
                                            onBackClick()
                                        },
                                    )
                                }
                                is ClientDto -> {
                                    ClientListItem(
                                        client = item,
                                        onClick = {
                                            viewModel.onEvent(CreateIndentEvent.SelectClient(item))
                                            onBackClick()
                                        },
                                    )
                                }
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationListItem(
    location: LocationDto,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = {
            Text(location.name, style = MaterialTheme.typography.bodyLarge)
        },
        supportingContent = {
            if (location.type.equals("area", true)) {
                location.city?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            } else {
                location.state?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
        },
    )
}

@Composable
private fun ClientListItem(
    client: ClientDto,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = {
            Column {
                Text(
                    text = client.companyName,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = client.tagCode,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

        },
    )
}

private data class SearchConfig(
    val title: String,
    val hint: String,
    val query: String,
    val results: List<Any>,
    val isLoading: Boolean,
)
