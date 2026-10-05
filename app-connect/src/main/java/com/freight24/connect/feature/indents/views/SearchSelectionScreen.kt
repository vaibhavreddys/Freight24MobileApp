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
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.freight24.connect.feature.indents.domain.viewmodel.SearchSelectionEvent
import com.freight24.connect.feature.indents.domain.viewmodel.SearchSelectionViewModel
import com.freight24.connect.feature.indents.domain.viewmodel.SearchSelectionViewModelImpl
import com.freight24.rest.response.ClientDto
import com.freight24.rest.response.LocationDto
import com.freight24.ui.components.FreightTopAppBar
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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
    viewModel: SearchSelectionViewModel = hiltViewModel<SearchSelectionViewModelImpl>(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val colorScheme = MaterialTheme.colorScheme

    val (title, hint) =
        when (searchType) {
            SearchType.CLIENT -> "Select Client" to "Search by client name"
            SearchType.LOCATION_FROM -> "Select Loading Location" to "Search by city or area"
            SearchType.LOCATION_TO -> "Select Unloading Location" to "Search by city or area"
        }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            FreightTopAppBar(
                title = { Text(title, fontWeight = FontWeight.SemiBold) },
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
            Text(
                text = hint,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.primary,
            )

            OutlinedTextField(
                value = uiState.query,
                onValueChange = { newQuery ->
                    viewModel.onEvent(SearchSelectionEvent.OnQueryChange(newQuery, searchType))
                },
                placeholder = { Text("Type to search...", color = colorScheme.onSurfaceVariant) },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .focusRequester(focusRequester),
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                trailingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = colorScheme.onSurfaceVariant)
                },
                colors =
                    androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorScheme.primary,
                        unfocusedBorderColor = colorScheme.outlineVariant,
                        focusedContainerColor = colorScheme.surface,
                        unfocusedContainerColor = colorScheme.surface,
                    ),
            )

            if (uiState.isLoading) {
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
                    if (uiState.query.length < 3) {
                        item {
                            Text(
                                text = "Type at least 3 characters to search",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.onSurfaceVariant,
                            )
                        }
                    } else if (uiState.results.isEmpty()) {
                        item {
                            Text(
                                text = "No results found",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        items(uiState.results) { item ->
                            when (item) {
                                is LocationDto -> {
                                    LocationListItem(
                                        location = item,
                                        onClick = {
                                            val key =
                                                when (searchType) {
                                                    SearchType.LOCATION_FROM -> "selected_from_location"
                                                    SearchType.LOCATION_TO -> "selected_to_location"
                                                    else -> ""
                                                }
                                            navController.previousBackStackEntry?.savedStateHandle?.set(key, Json.encodeToString(item))
                                            onBackClick()
                                        },
                                    )
                                }
                                is ClientDto -> {
                                    ClientListItem(
                                        client = item,
                                        onClick = {
                                            navController.previousBackStackEntry?.savedStateHandle?.set(
                                                "selected_client",
                                                Json.encodeToString(item),
                                            )
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
            Text(location.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        },
        supportingContent = {
            if (location.type.equals("area", true)) {
                location.city?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                location.state?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = client.tagCode,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
