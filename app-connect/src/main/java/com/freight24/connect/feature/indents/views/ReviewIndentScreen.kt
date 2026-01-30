package com.freight24.connect.feature.indents.views

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.freight24.connect.navigation.ConnectDestination
import com.freight24.ui.components.VisibilityChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewIndentScreen(
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    navController: NavController,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val createIndentBackStackEntry = remember(navBackStackEntry) {
        navController.getBackStackEntry(ConnectDestination.CreateIndent.route)
    }
    val viewModel: CreateIndentViewModel = hiltViewModel(createIndentBackStackEntry)
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(CreateIndentEvent.ClearSuccess)
            // Navigate back to home or indents list
            onBackClick()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(CreateIndentEvent.ClearError)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Review Indent") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = onEditClick) {
                        Text("Edit", color = MaterialTheme.colorScheme.primary)
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Button(
                onClick = { viewModel.onEvent(CreateIndentEvent.SubmitIndent) },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                enabled = !uiState.isSubmitting,
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp).padding(end = 8.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
                    )
                }
                Text("Confirm & Create Indent")
            }
        },
    ) { paddingValues ->

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Please review the indent details before finalizing",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
            )

            // General Info Section
            ReviewSection(
                image = Icons.Default.Info,
                title = "GENERAL INFO",
                onEditClick = onEditClick,
            ) {
                ReviewItem(label = "INDENT TYPE", value = "New Indent")

                Spacer(modifier = Modifier.size(8.dp))

                ReviewItem(
                    label = "CLIENT NAME",
                    value = uiState.selectedClient?.run {
                        companyName
                    } ?: "Not selected",
                )

                Spacer(modifier = Modifier.size(8.dp))

                ReviewItem(
                    label = "TAG CODE",
                    value = uiState.selectedClient?.run {
                        tagCode
                    } ?: "Not specified"
                )
            }

            // Route Details Section
            ReviewSection(
                image = Icons.Default.Route,
                title = "ROUTE DETAILS",
                onEditClick = onEditClick,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(20.dp),
                    )
                    Column {
                        Text(
                            text = "ORIGIN CITY",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                        )
                        Text(
                            text = uiState.selectedFromLocation?.let {
                                "${it.name}${it.state?.let { state -> ", $state" } ?: ""}"
                            } ?: "Not selected",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFFF44336),
                        modifier = Modifier.size(20.dp),
                    )
                    Column {
                        Text(
                            text = "DESTINATION CITY",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                        )
                        Text(
                            text = uiState.selectedToLocation?.let {
                                "${it.name}${it.state?.let { state -> ", $state" } ?: ""}"
                            } ?: "Not selected",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            if(uiState.clientCost.isNotEmpty()) {

                ReviewSection(
                    Icons.Outlined.Payments,
                    "FINANCIAL DETAILS",
                    onEditClick = {}
                ) {

                    Row(
                        modifier = Modifier.padding(vertical = 16.dp)
                    ) {
                        ReviewItemColumn(
                            label = "CLIENT COST",
                            value = "₹ ${uiState.clientCost}"
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        VerticalDivider(
                            thickness = 0.5.dp,
                            color = Color(0xFFe2e8f0),
                            modifier = Modifier.height(32.dp)
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        ReviewItemColumn(
                            label = "NEGOTIATED COST",
                            value = "₹ ${uiState.negotiatedCost}"
                        )
                    }

                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = Color(0xFFe2e8f0),
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = 16.dp,
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {

                        Text(
                            text = "COST VISIBILITY",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                        )

                        Spacer(
                            modifier = Modifier.weight(1f)
                        )

                        VisibilityChip(
                            isVisible = !uiState.hideCost
                        )
                    }
                }
            }

            // Specifications Section
            ReviewSection(
                image = Icons.Outlined.Tune,
                title = "SPECIFICATIONS",
                onEditClick = onEditClick,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "WEIGHT (TONS)",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                        )
                        Text(
                            text = "${uiState.weight.ifBlank { "0" }} Tons",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f)
                    )  {
                        Text(
                            text = "MATERIAL TYPE",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                        )
                        Text(
                            text = uiState.materialName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = Color(0xFFe2e8f0)
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = "VEHICLE TYPE",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                    )

                    Spacer(Modifier.weight(1f))

                    uiState.selectedTruckType?.let { truck ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF4CAF50),
                        ) {
                            Text(
                                text = truck.displayName,
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                modifier = Modifier.padding(
                                    horizontal = 12.dp,
                                    vertical = 8.dp
                                ),
                            )
                        }
                    } ?: Text(
                        text = "Not selected",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                Spacer(Modifier.height(8.dp))

                ReviewItem(
                    label = "TAT",
                    value = if (uiState.tatRequired) "${uiState.tatHours} hrs" else "No",
                    valueColor = if (uiState.tatRequired) Color.Black else Color.Gray,
                )

                Spacer(Modifier.height(8.dp))

                ReviewItem(
                    label = "TARPAULIN",
                    value = if (uiState.tarpaulinRequired) "Yes" else "No",
                    valueColor = if (uiState.tarpaulinRequired) Color(0xFF4CAF50) else Color.Gray,
                )

                if(uiState.remarks.isNotEmpty()) {

                    Column {
                        Spacer(Modifier.size(8.dp))

                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = Color(0xFFe2e8f0)
                        )

                        Spacer(Modifier.size(8.dp))

                        Text(
                            text = "REMARKS",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                        )

                        Spacer(Modifier.size(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = Color(0xFFF3F3F3),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(16.dp)
                        ) {

                            Text(
                                text = uiState.remarks,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Gray,
                                textAlign = TextAlign.Start
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewSection(
    image: ImageVector? = null,
    title: String,
    onEditClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {

                if(image != null){
                    Image(
                        image,
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(
                            color = Color.Gray,
                            blendMode = BlendMode.SrcIn
                        ),
                        modifier = Modifier.size(18.dp)

                    )
                }

                Spacer(modifier = Modifier.size(8.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = onEditClick, modifier = Modifier.size(24.dp)) {

                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            content()
        }
    }
}

@Composable
fun ReviewItem(
    label: String,
    value: String,
    valueColor: Color = Color.Black,
) {
    Row {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
            modifier = Modifier
                .weight(1f),
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun ReviewItemColumn(
    label: String,
    value: String,
    valueColor: Color = Color.Black,
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
            textAlign = TextAlign.End
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ReviewSectionPreview() {
    Column {
        Spacer(Modifier.size(8.dp))

        HorizontalDivider(
            thickness = 0.5.dp,
            color = Color(0xFFe2e8f0)
        )

        Spacer(Modifier.size(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color(0xFFF3F3F3),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(16.dp)
        ) {

            Text(
                text = "Randowm tect akoadbs hsdbsnfbjfshf sfshf sfhshjsfjh sfhsjhjhjhsfhjjjhsf sfsfjhjjhjjsfisfii",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.Gray,
                textAlign = TextAlign.Start
            )
        }
    }
}
