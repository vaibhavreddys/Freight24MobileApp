// TrucksScreen.kt
package com.freight24.trucker.feature.trucks


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun TrucksScreen(
    paddingValues: PaddingValues,
    viewModel: TrucksViewModel = hiltViewModel()
) {
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val trucks by viewModel.trucks.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        Column {

            FilterChips(
                options = TruckFilter.entries,
                selected = selectedFilter,
                onSelected = viewModel::onFilterSelected
            )

            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 96.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(trucks) { truck ->
                    TruckCard(truck)
                }
            }
        }
    }
}


@Composable
fun FilterChips(
    options: List<TruckFilter>,
    selected: TruckFilter,
    onSelected: (TruckFilter) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(options) { option ->
            FilterChip(
                selected = option == selected,
                onClick = { onSelected(option) },
                label = { Text(option.name.replace("_", " ")) }
            )
        }
    }
}


@Composable
fun TruckCard(truck: TruckUiModel) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TruckIcon(truck.status)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(truck.number, style = MaterialTheme.typography.titleMedium)
                    Text(truck.description, style = MaterialTheme.typography.bodySmall)
                }
                StatusChip(truck.status)
            }

            Divider(Modifier.padding(vertical = 12.dp))

            VerificationRow(truck.verification)
        }
    }
}

@Composable
fun TruckIcon(status: TruckStatus) {
    val bg = when (status) {
        TruckStatus.AVAILABLE -> Color(0xFFEAF2FF)
        TruckStatus.ON_TRIP -> Color(0xFFFFF3E0)
        TruckStatus.MAINTENANCE -> Color(0xFFFFEBEE)
    }
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(bg, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.LocalShipping, contentDescription = null)
    }
}

@Composable
fun StatusChip(status: TruckStatus) {
    val (text, bg) = when (status) {
        TruckStatus.AVAILABLE -> "Available" to Color(0xFFE8F5E9)
        TruckStatus.ON_TRIP -> "On Trip" to Color(0xFFE3F2FD)
        TruckStatus.MAINTENANCE -> "Maintenance" to Color(0xFFFFEBEE)
    }

    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun VerificationRow(status: VerificationStatus) {
    val (text, icon, color) = when (status) {
        VerificationStatus.COMPLETED ->
            Triple("Document verification completed", Icons.Default.CheckCircle, Color(0xFF2E7D32))
        VerificationStatus.PENDING ->
            Triple("Document verification pending", Icons.Default.Error, Color(0xFFF9A825))
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = color)
        Spacer(Modifier.width(8.dp))
        Text(text, color = color)
    }
}

@Composable
fun OnboardTruckButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        text = { Text("Onboard a New Truck") },
        icon = { Icon(Icons.Default.Add, contentDescription = null) }
    )
}


// -------- Dummy data --------

enum class TruckStatus { AVAILABLE, ON_TRIP, MAINTENANCE }
enum class VerificationStatus { COMPLETED, PENDING }

data class TruckUiModel(
    val number: String,
    val description: String,
    val status: TruckStatus,
    val verification: VerificationStatus
)

fun dummyTrucks() = listOf(
    TruckUiModel("KA01AP1232", "32ft SXL Container", TruckStatus.AVAILABLE, VerificationStatus.COMPLETED),
    TruckUiModel("RJ14GC9988", "20ft MXL Open Body", TruckStatus.ON_TRIP, VerificationStatus.COMPLETED),
    TruckUiModel("TN09AY4567", "32ft MXL Multi-Axle", TruckStatus.MAINTENANCE, VerificationStatus.PENDING)
)

fun List<TruckUiModel>.filterBy(filter: String): List<TruckUiModel> = when (filter) {
    "Available" -> filter { it.status == TruckStatus.AVAILABLE }
    "On Trip" -> filter { it.status == TruckStatus.ON_TRIP }
    "Maintenance" -> filter { it.status == TruckStatus.MAINTENANCE }
    else -> this
}
