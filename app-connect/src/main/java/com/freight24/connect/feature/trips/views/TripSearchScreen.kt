package com.freight24.connect.feature.trips.views

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TripSearchScreen(
    onTripClick: (String) -> Unit,
    onBackClick: () -> Unit,
    viewModel: TripSearchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    var textFieldValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue("T-", TextRange(2, 2)))
    }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search Trips", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
        ) {
            OutlinedTextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    val newText = newValue.text
                    val cursorPos = newValue.selection.start

                    val rawText =
                        newText.filter { it.isLetterOrDigit() }.take(12).uppercase()
                    val newRaw =
                        if (rawText.startsWith("T")) rawText else "T${rawText.take(11)}"

                    val formatted = newRaw.toFormattedTripCode()

                    val alnumBefore = newText.take(cursorPos).count { it.isLetterOrDigit() }

                    var newCursor = formatted.length
                    var alnumSeen = 0
                    for ((i, c) in formatted.withIndex()) {
                        if (c.isLetterOrDigit()) {
                            alnumSeen++
                            if (alnumSeen > alnumBefore) {
                                newCursor = i
                                break
                            }
                        }
                    }

                    while (newCursor < formatted.length && formatted[newCursor] == '-') {
                        newCursor++
                    }

                    if (newCursor < 2) newCursor = formatted.length

                    textFieldValue =
                        TextFieldValue(
                            text = formatted,
                            selection = TextRange(newCursor.coerceIn(0, formatted.length)),
                        )

                    viewModel.onQueryChanged(if (formatted == "T-") "" else formatted)
                },
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                placeholder = { Text("Search by trip code...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                keyboardOptions =
                    KeyboardOptions(
                        imeAction = ImeAction.Search,
                        keyboardType = textFieldValue.text.toKeyboardType(),
                    ),
                keyboardActions =
                    KeyboardActions(onSearch = { focusManager.clearFocus() }),
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                FilterChip(
                    selected = uiState.scope == "my",
                    onClick = { viewModel.onScopeChanged("my") },
                    label = { Text("My Trips") },
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = uiState.scope == "all",
                    onClick = { viewModel.onScopeChanged("all") },
                    label = { Text("All Trips") },
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = uiState.error ?: "Unknown error",
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
                uiState.query.isBlank() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Enter a trip code to search",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                uiState.results.isEmpty() && uiState.query.count { it.isDigit() } < 4 -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Type at least 4 digits to search",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                uiState.results.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No trips found",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(uiState.results) { result ->
                            TripSearchResultItem(
                                tripCode = result.tripCode,
                                registrationNumber = result.registrationNumber,
                                origin = result.origin?.city ?: "",
                                destination = result.destination?.city ?: "",
                                onClick = { onTripClick(result.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TripSearchResultItem(
    tripCode: String,
    registrationNumber: String,
    origin: String,
    destination: String,
    onClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 12.dp, horizontal = 4.dp),
    ) {
        Text(
            text = tripCode,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "$registrationNumber · $origin → $destination",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun String.toFormattedTripCode(): String {
    val clean = filter { it.isLetterOrDigit() }.take(12).uppercase()
    if (clean.isEmpty()) return ""

    val sb = StringBuilder()
    sb.append('T')

    var idx = 1

    sb.append('-')
    var digitCount = 0
    while (idx < clean.length && digitCount < 6) {
        if (clean[idx].isDigit()) {
            sb.append(clean[idx])
            digitCount++
        }
        idx++
    }

    val completedDigits = digitCount == 6
    val hasMoreChars = idx < clean.length

    if (completedDigits || hasMoreChars) {
        sb.append('-')

        var letterCount = 0
        while (idx < clean.length && letterCount < 2) {
            if (clean[idx].isLetter()) {
                sb.append(clean[idx].uppercaseChar())
                letterCount++
            }
            idx++
        }

        val completedLetters = letterCount == 2
        if (completedLetters || idx < clean.length) {
            sb.append('-')

            var finalDigitCount = 0
            while (idx < clean.length && finalDigitCount < 3) {
                if (clean[idx].isDigit()) {
                    sb.append(clean[idx])
                    finalDigitCount++
                }
                idx++
            }
        }
    }

    return sb.toString()
}

private fun String.toKeyboardType(): KeyboardType {
    val rawLen = count { it.isLetterOrDigit() }
    val hasProactiveHyphen = endsWith("-") && length > 2 && this[length - 2] != 'T'
    return when {
        rawLen <= 7 -> if (hasProactiveHyphen) KeyboardType.Ascii else KeyboardType.Number
        rawLen <= 9 -> if (hasProactiveHyphen) KeyboardType.Number else KeyboardType.Ascii
        else -> KeyboardType.Number
    }
}
