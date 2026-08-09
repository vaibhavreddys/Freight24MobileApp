package com.freight24.connect.feature.indents.views

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
import com.freight24.ui.theme.FreightThemeTokens
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun IndentSuccessScreen(
    navController: NavController,
    indentId: String,
    indentCode: String? = null,
    onViewIndentClick: () -> Unit,
    onBackToHomeClick: () -> Unit,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    val createIndentBackStackEntry = remember(navBackStackEntry) {
        try {
            navController.getBackStackEntry(ConnectDestination.CreateIndent.route)
        } catch (e: Exception) {
            null
        }
    }

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
                indentCode = indentCode ?: uiState.createdIndentCode ?: uiState.createdIndentId ?: indentId,
                weight = uiState.weight,
                material = uiState.materialName,
                fromLocation = uiState.selectedFromLocation?.name ?: "",
                toLocation = uiState.selectedToLocation?.name ?: "",
                truckType = uiState.selectedTruckType?.displayName ?: "",
                createdDate = uiState.createdDate,
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
                indentCode = indentCode ?: uiState.createdIndentCode ?: uiState.createdIndentId ?: indentId,
                weight = groupDetails?.loadWeight?.toString() ?: "",
                material = groupDetails?.material ?: "",
                fromLocation = loadingLocation?.areaName ?: loadingLocation?.cityName ?: "",
                toLocation = unloadingLocation?.areaName ?: unloadingLocation?.cityName ?: "",
                truckType = groupDetails?.truckType?.displayName ?: "",
                createdDate = uiState.createdDate,
                onViewIndentClick = onViewIndentClick,
                onBackToHomeClick = onBackToHomeClick
            )
        }
        else -> {
            IndentSuccessComponent(
                indentCode = indentCode ?: indentId,
                weight = "--",
                material = "--",
                fromLocation = "--",
                toLocation = "--",
                truckType = "--",
                createdDate = null,
                onViewIndentClick = onViewIndentClick,
                onBackToHomeClick = onBackToHomeClick
            )
        }
    }
}

@Composable
fun IndentSuccessComponent(
    indentCode: String,
    weight: String,
    material: String,
    fromLocation: String,
    toLocation: String,
    truckType: String,
    createdDate: String?,
    onViewIndentClick: () -> Unit,
    onBackToHomeClick: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val extendedColors = FreightThemeTokens.colors
    val clipboardManager = LocalClipboardManager.current
    val hapticFeedback = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var animateCheck by remember { mutableStateOf(false) }
    val checkScale by animateFloatAsState(
        targetValue = if (animateCheck) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "checkAnimation"
    )

    LaunchedEffect(Unit) {
        delay(150)
        animateCheck = true
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .scale(checkScale)
                    .background(
                        color = extendedColors.success.copy(alpha = 0.12f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(
                            color = extendedColors.success,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Success",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Indent Created!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Indent Code",
                                style = MaterialTheme.typography.labelMedium,
                                color = colorScheme.onSurface
                            )
                            Text(
                                text = "#$indentCode",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = colorScheme.onSurface
                            )
                            if (createdDate != null) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Created ${formatCreatedAt(createdDate)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colorScheme.onSurface
                                )
                            }
                        }
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(indentCode))
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "Indent code copied",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            },
                            modifier = Modifier.size(48.dp),
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = colorScheme.onSurfaceVariant
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy indent code",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    HorizontalDivider(thickness = 0.5.dp, color = colorScheme.outlineVariant)

                    RouteCard(
                        fromLocation = fromLocation,
                        toLocation = toLocation,
                        accentColor = extendedColors.success
                    )

                    HorizontalDivider(thickness = 0.5.dp, color = colorScheme.outlineVariant)

                    SpecRow(
                        icon = Icons.Default.LocalShipping,
                        label = "Truck Type",
                        value = truckType.lowercase()
                            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                        iconTint = extendedColors.success
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SpecRow(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Scale,
                            label = "Weight",
                            value = "$weight MT",
                            iconTint = extendedColors.success
                        )
                        SpecRow(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Inventory2,
                            label = "Material",
                            value = material.lowercase()
                                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                            iconTint = extendedColors.success
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Transporters will start quoting soon — you'll be notified.",
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onViewIndentClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
            ) {
                Text(
                    text = "View Indent Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onBackToHomeClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Back to Dashboard",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun RouteCard(
    fromLocation: String,
    toLocation: String,
    accentColor: Color
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = fromLocation,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onSurface,
                maxLines = 1
            )
        }
        Row(
            modifier = Modifier.width(100.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                thickness = 1.5.dp,
                color = accentColor.copy(alpha = 0.3f)
            )
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalShipping,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                thickness = 1.5.dp,
                color = accentColor.copy(alpha = 0.3f)
            )
        }
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = toLocation,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SpecRow(
    icon: ImageVector,
    label: String,
    value: String,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurface
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface,
                    maxLines = 1
                )
            }
        }
    }
}

private fun formatCreatedAt(isoTimestamp: String): String {
    return try {
        val parsed = LocalDateTime.parse(isoTimestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        parsed.format(DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a", Locale.getDefault()))
    } catch (e: Exception) {
        isoTimestamp
    }
}

@Preview(showBackground = true, name = "Indent Success")
@Composable
fun IndentSuccessComponentPreview() {
    MaterialTheme {
        IndentSuccessComponent(
            indentCode = "000093",
            weight = "17",
            material = "fruits",
            fromLocation = "Mumbai",
            toLocation = "Ahmedabad",
            truckType = "24 Ft Open Body",
            createdDate = "2026-08-09T15:45:00",
            onViewIndentClick = {},
            onBackToHomeClick = {}
        )
    }
}
