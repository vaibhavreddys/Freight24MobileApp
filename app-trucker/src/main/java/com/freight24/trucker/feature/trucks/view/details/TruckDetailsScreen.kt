package com.freight24.trucker.feature.trucks.view.details

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.freight24.common.model.TruckDetail
import com.freight24.common.model.TruckDocument
import com.freight24.ui.UiState
import com.freight24.ui.components.FilterSelectionChip
import com.freight24.ui.components.TruckHeaderCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TruckDetailsScreen(
    onBackClick: () -> Unit,
    viewModel: TruckDetailsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = "Truck Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
//                },
//                actions = {
//                    IconButton(onClick = { /* More options */ }) {
//                        Icon(Icons.Default.MoreVert, contentDescription = "More")
//                    }
                },
            )
        },
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.onRefresh() },
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background),
        ) {
            when (val state = uiState) {
                is UiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is UiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
                is UiState.Success -> {
                    TruckDetailsContent(
                        truck = state.data,
                        onDocumentClick = { url ->
                            if (url != null) {
                                val intent =
                                    Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(Uri.parse(url), "application/pdf")
                                        flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                                    }
                                context.startActivity(Intent.createChooser(intent, "Open PDF"))
                            }
                        },
                    )
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun TruckDetailsContent(
    truck: TruckDetail,
    onDocumentClick: (String?) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            TruckHeaderCard(
                registrationNumber = truck.registrationNumber,
                truckType = truck.truckType,
                availability = "AVAILABLE", // Defaulting as detail API doesn't have it yet, or use a field if added
                isVerified = truck.isVerified,
                isBlacklisted = truck.isBlacklisted,
            )
        }

        item {
            SpecificationsSection(truck)
        }

        item {
            DocumentsSection(truck, onDocumentClick)
        }
    }
}

@Composable
private fun SpecificationsSection(truck: TruckDetail) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Specifications",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SpecItem(Modifier.weight(1f), "BODY TYPE", truck.bodyType ?: "NA")
                    SpecItem(Modifier.weight(1f), "MAKE", truck.make ?: "NA")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SpecItem(Modifier.weight(1f), "MODEL", truck.model ?: "NA")
                    SpecItem(Modifier.weight(1f), "TRUCK TYPE", truck.truckType ?: "NA")
                }
            }
        }
    }
}

@Composable
private fun SpecItem(
    modifier: Modifier,
    label: String,
    value: String,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant, // Using theme aware surface variant
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                fontSize = 10.sp,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun DocumentsSection(
    truck: TruckDetail,
    onDocumentClick: (String?) -> Unit,
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Documents",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.height(16.dp))

            DocumentItem("RC Book", truck.rcCard, onDocumentClick)
            DocumentItem("Insurance Policy", truck.insurance, onDocumentClick)
            DocumentItem("Fitness Certificate", truck.fcCertificate, onDocumentClick)
        }
    }
}

@Composable
private fun DocumentItem(
    title: String,
    doc: TruckDocument?,
    onDocumentClick: (String?) -> Unit,
) {
    val hasUrl = !doc?.url.isNullOrEmpty()

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable {
                    if (hasUrl) {
                        onDocumentClick(doc.url)
                    } else {
                        // Trigger upload flow
                    }
                }
                .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val iconColor =
            when (doc?.status?.uppercase()) {
                "COMPLETED", "VERIFIED" -> MaterialTheme.colorScheme.primary
                "PENDING" -> Color(0xFFF9A825)
                else -> if (hasUrl) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            }

        Surface(
            color = iconColor.copy(alpha = 0.1f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.size(40.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (title.contains("Insurance")) Icons.Default.Shield else Icons.Default.Description,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = if (hasUrl) doc.status else ("Pending Upload"),
                style = MaterialTheme.typography.bodySmall,
                color = if (doc?.status?.uppercase() == "VERIFIED") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (hasUrl) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(20.dp),
            )
        } else {
            FilterSelectionChip(
                label = "Upload",
                isSelected = false,
                onClick = { /* Trigger upload flow */ },
                leadingIcon = Icons.Default.FileUpload,
            )
        }
    }
}
