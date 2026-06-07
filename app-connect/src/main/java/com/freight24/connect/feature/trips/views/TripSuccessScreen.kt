package com.freight24.connect.feature.trips.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freight24.ui.theme.FreightThemeTokens

@Composable
fun TripSuccessScreen(
    tripId: String,
    truckNumber: String,
    origin: String,
    destination: String,
    driverName: String,
    driverPhone: String,
    onGoToTripsClick: () -> Unit,
    onBackToDashboardClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val extendedColors = FreightThemeTokens.colors

    Surface(
        color = colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Spacer(
                modifier = Modifier.size(48.dp)
            )

            // Success Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(extendedColors.success.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(extendedColors.success, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Success",
                        tint = colorScheme.onPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Trip Created Successfully!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Your trip has been initiated and assigned.",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Trip Details Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Trip ID and Truck Number Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("TRIP ID", style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "#$tripId",
                                style = MaterialTheme.typography.titleMedium,
                                color = extendedColors.success,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("TRUCK NUMBER", style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(
                                truckNumber,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = colorScheme.outlineVariant
                    )

                    // Route
                    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                         Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(16.dp)
                        ) {
                            // Origin Dot
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(extendedColors.success, CircleShape)
                            )
                            // Line
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .weight(1f)
                                    .background(colorScheme.outlineVariant)
                            )
                            // Destination Dot
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(colorScheme.error, CircleShape)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("ROUTE", style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "$origin to $destination",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Driver
                    if(driverName.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = "Driver", tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("DRIVER", style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    driverName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Call, contentDescription = "DriverPhone", tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("DRIVER PH0NE", style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(
                                driverPhone,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Buttons
            Button(
                onClick = onGoToTripsClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = extendedColors.success,
                    contentColor = colorScheme.onPrimary
                )
            ) {
                Text("GO TO TRIPS", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onBackToDashboardClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = colorScheme.onSurfaceVariant
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outlineVariant)
            ) {
                Text("BACK TO DASHBOARD", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview
@Composable
private fun TripSuccessScreenPreview() {
    TripSuccessScreen(
        tripId = "T-260209-BB-002",
        truckNumber = "KA01MZ9275",
        origin = "Bhiwandi",
        destination = "Hoskote",
        driverName = "Raja",
        driverPhone = "8734030434",
        onGoToTripsClick = {},
        onBackToDashboardClick = {}
    )
}
