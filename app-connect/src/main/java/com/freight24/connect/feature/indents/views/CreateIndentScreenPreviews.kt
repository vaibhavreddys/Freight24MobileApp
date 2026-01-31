package com.freight24.connect.feature.indents.views

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.freight24.connect.feature.indents.domain.viewmodel.CreateIndentUiState
import com.freight24.rest.response.ClientDto
import com.freight24.rest.response.LocationDto
import com.freight24.rest.response.TruckCategoryDto
import com.freight24.rest.response.TruckTypeDto

@Preview(showBackground = true, name = "Full Screen Content")
@Composable
fun CreateIndentContentPreview() {
    CreateIndentContent(
        uiState =
            CreateIndentUiState(
                truckTypes = mockTruckTypes(),
                selectedFromLocation = LocationDto("1", "Mumbai", "Mumbai", "Maharashtra"),
                fromLocationQuery = "Mumbai",
            ),
        snackbarHostState = SnackbarHostState(),
        onBackClick = {},
        onEvent = {},
        onNavigateToSearch = {},
        onNavigateToReview = {}
    )
}

@Preview(showBackground = true, name = "Location AutoComplete")
@Composable
fun LocationAutoCompletePreview() {
    LocationAutoComplete(
        label = "From Location",
        query = "Mum",
        onQueryChange = {},
        searchResults =
            listOf(
                LocationDto("1", "Mumbai", "Mumbai", "Maharashtra"),
                LocationDto("2", "Mumbai Port", "Mumbai", "Maharashtra"),
            ),
        onSelect = {},
    )
}

@Preview(showBackground = true, name = "Client AutoComplete")
@Composable
fun ClientAutoCompletePreview() {
    ClientAutoComplete(
        label = "Client Name",
        query = "Tata",
        onQueryChange = {},
        searchResults =
            listOf(
                ClientDto(
                    "1",
                    "12234",
                    "Tata Steel",
                    "F23-1233",
                    "9035892114",
                    "Active",
                ),
                ClientDto(
                    "2",
                    "12234",
                    "Tata Motors",
                    "F23-1233",
                    "9035892114",
                    "Active",
                ),
            ),
        onSelect = {},
    )
}

@Preview(showBackground = true, name = "Truck Type Selection")
@Composable
fun TruckTypeSelectionPreview() {
    TruckTypeSelection(
        truckTypes = mockTruckTypes(),
        selectedTruckType = mockTruckTypes().open.first(),
        onSelect = {},
    )
}

@Preview(showBackground = true, name = "Optional Fields Expanded")
@Composable
fun OptionalFieldsSectionPreview() {
    OptionalFieldsSection(
        uiState =
            CreateIndentUiState(
                clientCost = "25000",
                negotiatedCost = "24000",
                remarks = "Urgent delivery",
            ),
        onEvent = {},
    )
}

private fun mockTruckTypes() =
    TruckCategoryDto(
        open =
            listOf(
                TruckTypeDto("1", "19 ft Open", "19 ft Open"),
                TruckTypeDto("2", "20 ft Open", "20 ft Open"),
            ),
        closed =
            listOf(
                TruckTypeDto("3", "32 ft SXL", "32 ft SXL"),
                TruckTypeDto("4", "32 ft MXL", "32 ft MXL"),
            ),
        trailer = listOf(),
    )
