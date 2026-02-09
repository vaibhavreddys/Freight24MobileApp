package com.freight24.connect.feature.indents.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.freight24.connect.feature.indents.domain.viewmodel.CloneIndentViewModel
import com.freight24.connect.feature.indents.domain.viewmodel.CloneIndentViewModelImpl
import com.freight24.connect.feature.indents.domain.viewmodel.IndentsViewModel
import com.freight24.connect.feature.indents.domain.viewmodel.IndentsViewModelImpl
import com.freight24.connect.navigation.ConnectDestination
import com.freight24.connect.enum.ConnectTab

@Composable
fun IndentSuccessScreen(
    navController: NavController,
    indentId: String,
    onViewIndentClick: () -> Unit,
    onBackToHomeClick: () -> Unit,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    
    // Check for CreateIndent backstack entry
    val createIndentBackStackEntry = remember(navBackStackEntry) {
        try {
            navController.getBackStackEntry(ConnectDestination.CreateIndent.route)
        } catch (e: Exception) {
            null
        }
    }

    // Check for CloneIndent backstack entry
    val cloneIndentBackStackEntry = remember(navBackStackEntry) {
        try {
            navController.getBackStackEntry(ConnectDestination.CloneIndent.route)
        } catch (e: Exception) {
            null
        }
    }

    when {
        createIndentBackStackEntry != null -> {
            val viewModel: IndentsViewModel = hiltViewModel<IndentsViewModelImpl>(createIndentBackStackEntry)
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            IndentSuccessComponent(
                indentId = uiState.createdIndentId ?: indentId,
                weight = uiState.weight,
                material = uiState.materialName,
                fromLocation = uiState.selectedFromLocation?.name ?: "",
                toLocation = uiState.selectedToLocation?.name ?: "",
                truckType = uiState.selectedTruckType?.displayName ?: "",
                onViewIndentClick = onViewIndentClick,
                onBackToHomeClick = onBackToHomeClick
            )
        }
        cloneIndentBackStackEntry != null -> {
            val viewModel: CloneIndentViewModel = hiltViewModel<CloneIndentViewModelImpl>(cloneIndentBackStackEntry)
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val indent = uiState.indent
            val groupDetails = indent?.indentGroupDetails
            val loadingLocation = groupDetails?.indentLocations?.find { it.locationType == "loading" }
            val unloadingLocation = groupDetails?.indentLocations?.find { it.locationType == "unloading" }
            
            IndentSuccessComponent(
                indentId = uiState.createdIndentId ?: indentId,
                weight = groupDetails?.loadWeight?.toString() ?: "",
                material = groupDetails?.material ?: "",
                fromLocation = loadingLocation?.areaName ?: loadingLocation?.cityName ?: "",
                toLocation = unloadingLocation?.areaName ?: unloadingLocation?.cityName ?: "",
                truckType = groupDetails?.truckType?.displayName ?: "",
                onViewIndentClick = onViewIndentClick,
                onBackToHomeClick = onBackToHomeClick
            )
        }
        else -> {
            // Fallback if no VM is found, just show the ID
            IndentSuccessComponent(
                indentId = indentId,
                weight = "--",
                material = "--",
                fromLocation = "--",
                toLocation = "--",
                truckType = "--",
                onViewIndentClick = onViewIndentClick,
                onBackToHomeClick = onBackToHomeClick
            )
        }
    }
}

@Composable
fun IndentSuccessComponent(
    indentId: String,
    weight: String,
    material: String,
    fromLocation: String,
    toLocation: String,
    truckType: String,
    onViewIndentClick: () -> Unit,
    onBackToHomeClick: () -> Unit,
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Spacer(
            modifier = Modifier.size(48.dp)
        )

        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = Color(0xFF4CAF50),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Success",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Success Title
        Text(
            text = "Indent Created Successfully!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = Color(0xFF1a1a1a)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Success Subtitle
        Text(
            text = "Your indent has been registered and is now\nactive for sourcing.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = Color.Gray,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.5
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Indent Details Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF8F9FA)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // Indent Id
                DetailRow(
                    label = "Indent Id",
                    value = "#$indentId",
                    valueColor = Color(0xFF1a1a1a)
                )

                // Route
                DetailRow(
                    label = "ROUTE",
                    value = "$fromLocation → $toLocation",
                    valueColor = Color(0xFF1a1a1a)
                )

                // Truck Type
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TRUCK TYPE",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = Color(0xFF1976D2),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = truckType,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1a1a1a)
                        )
                    }
                }

                DetailRow(
                    label = "WEIGHT",
                    value = "$weight MT",
                    valueColor = Color(0xFF1a1a1a)
                )

                DetailRow(
                    label = "MATERIAL",
                    value = material,
                    valueColor = Color(0xFF1a1a1a)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // View Indent Button
        Button(
            onClick = onViewIndentClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "View Indent",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.size(8.dp))

            Icon(
                imageVector = Icons.Outlined.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Back to Home Link
        TextButton(
            onClick = onBackToHomeClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Back to Home",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = Color.Black
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}

@Preview(showBackground = true)
@Composable
fun IndentSuccessComponentPreview() {
    IndentSuccessComponent(
        indentId = "IND-002328",
        weight = "17",
        material = "Cotton",
        fromLocation = "Mumbai",
        toLocation = "Ahmedabad",
        truckType = "24 FT Open Body",
        onViewIndentClick = {},
        onBackToHomeClick = {}
    )
}
