package com.freight24.connect.feature.indents.views

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.freight24.rest.response.ClientDto
import com.freight24.rest.response.LocationDto
import com.freight24.rest.response.TruckTypeDto

@Preview(showBackground = true, name = "Full Screen Content")
@Composable
fun CreateIndentContentPreview() {
    CreateIndentContent(
        uiState = CreateIndentUiState(
            truckTypes = mockTruckTypes(),
            selectedFromLocation = LocationDto("1", "Mumbai", "Mumbai", "Maharashtra"),
            fromLocationQuery = "Mumbai"
        ),
        snackbarHostState = SnackbarHostState(),
        onBackClick = {},
        onEvent = {}
    )
}

@Preview(showBackground = true, name = "Location AutoComplete")
@Composable
fun LocationAutoCompletePreview() {
    LocationAutoComplete(
        label = "From Location",
        query = "Mum",
        onQueryChange = {},
        searchResults = listOf(
            LocationDto("1", "Mumbai", "Mumbai", "Maharashtra"),
            LocationDto("2", "Mumbai Port", "Mumbai", "Maharashtra")
        ),
        onSelect = {}
    )
}

@Preview(showBackground = true, name = "Client AutoComplete")
@Composable
fun ClientAutoCompletePreview() {
    ClientAutoComplete(
        label = "Client Name",
        query = "Tata",
        onQueryChange = {},
        searchResults = listOf(
            ClientDto("1", "Tata Steel"),
            ClientDto("2", "Tata Motors")
        ),
        onSelect = {}
    )
}

@Preview(showBackground = true, name = "Truck Type Selection")
@Composable
fun TruckTypeSelectionPreview() {
    TruckTypeSelection(
        truckTypes = mockTruckTypes(),
        selectedTruckType = mockTruckTypes().first(),
        onSelect = {}
    )
}

@Preview(showBackground = true, name = "Optional Fields Expanded")
@Composable
fun OptionalFieldsSectionPreview() {
    OptionalFieldsSection(
        uiState = CreateIndentUiState(
            clientCost = "25000",
            negotiatedCost = "24000",
            remarks = "Urgent delivery"
        ),
        onEvent = {}
    )
}

private fun mockTruckTypes() = listOf(
    TruckTypeDto("1", "19 ft Open", "Open"),
    TruckTypeDto("2", "20 ft Open", "Open"),
    TruckTypeDto("3", "32 ft SXL", "Closed"),
    TruckTypeDto("4", "32 ft MXL", "Closed"),
)
