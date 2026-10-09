package com.freight24.trucker.feature.profile.view

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
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.rest.repository.LocationRepository
import com.freight24.rest.response.CityResult
import com.freight24.ui.components.FreightTopAppBar
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

internal data class BaseCitySearchUiState(
    val query: String = "",
    val results: List<CityResult> = emptyList(),
    val isLoading: Boolean = false,
    val hasSearched: Boolean = false,
    val errorMessage: String? = null,
)

@OptIn(FlowPreview::class)
@HiltViewModel
internal class BaseCitySearchViewModel
    @Inject
    constructor(
        private val locationRepository: LocationRepository,
    ) : ViewModel() {
        private val query = MutableStateFlow("")
        private val _uiState = MutableStateFlow(BaseCitySearchUiState())
        val uiState: StateFlow<BaseCitySearchUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                query
                    .debounce(400L)
                    .map { it.trim() }
                    .distinctUntilChanged()
                    .collectLatest(::search)
            }
        }

        fun onQueryChanged(value: String) {
            query.value = value
            _uiState.update {
                it.copy(
                    query = value,
                    results = if (value.trim().length < 2) emptyList() else it.results,
                    isLoading = false,
                    hasSearched = if (value.trim().length < 2) false else it.hasSearched,
                    errorMessage = null,
                )
            }
        }

        fun retry() {
            viewModelScope.launch { search(query.value.trim()) }
        }

        private suspend fun search(searchQuery: String) {
            if (searchQuery.length < 2) {
                _uiState.update {
                    it.copy(results = emptyList(), isLoading = false, hasSearched = false, errorMessage = null)
                }
                return
            }

            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            locationRepository.searchCities(searchQuery, "CITY")
                .onSuccess { results ->
                    _uiState.update {
                        it.copy(results = results, isLoading = false, hasSearched = true, errorMessage = null)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            results = emptyList(),
                            isLoading = false,
                            hasSearched = true,
                            errorMessage = error.message ?: "Unable to search cities",
                        )
                    }
                }
        }
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BaseCitySearchScreen(
    onBackClick: () -> Unit,
    onCitySelected: (CityResult) -> Unit,
    viewModel: BaseCitySearchViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val focusRequester = androidx.compose.runtime.remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            FreightTopAppBar(
                title = { Text("Select base location", fontWeight = FontWeight.SemiBold) },
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
                value = state.query,
                onValueChange = viewModel::onQueryChanged,
                placeholder = { Text("Search city") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .focusRequester(focusRequester),
            )

            when {
                state.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                state.errorMessage != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = state.errorMessage.orEmpty(),
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(horizontal = 24.dp),
                            )
                            Button(onClick = viewModel::retry, modifier = Modifier.padding(top = 12.dp)) {
                                Text("Retry")
                            }
                        }
                    }
                }

                state.query.trim().length < 2 -> {
                    SearchMessage("Enter at least 2 characters to search cities")
                }

                state.hasSearched && state.results.isEmpty() -> {
                    SearchMessage("No cities found")
                }

                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(state.results, key = CityResult::id) { city ->
                            ListItem(
                                modifier = Modifier.clickable { onCitySelected(city) },
                                leadingContent = {
                                    Icon(Icons.Default.LocationCity, contentDescription = null)
                                },
                                headlineContent = {
                                    Text(city.name, fontWeight = FontWeight.SemiBold)
                                },
                                supportingContent = {
                                    city.state.takeIf(String::isNotBlank)?.let { Text(it) }
                                },
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchMessage(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
    }
}
