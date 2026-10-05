package com.freight24.connect.feature.trips.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.freight24.rest.response.PaymentTransaction
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

private val transactionDateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PaymentTransactionsSheet(
    userType: String,
    transactions: List<PaymentTransaction>,
    isLoading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = if (userType == "client") "Client transactions" else "Trucker transactions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(16.dp))
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                errorMessage != null ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                        )
                        TextButton(onClick = onRetry) { Text("Retry") }
                    }
                transactions.isEmpty() ->
                    Text(
                        text =
                            if (userType == "client") {
                                "No client collections recorded."
                            } else {
                                "No trucker payouts recorded."
                            },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                else ->
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 560.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(transactions, key = { it.id }) { transaction ->
                            PaymentTransactionItem(transaction)
                        }
                    }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PaymentTransactionItem(transaction: PaymentTransaction) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(formatClientMoney(transaction.amount), fontWeight = FontWeight.Bold)
                    Text(
                        transaction.paymentDate.displayTransactionDate(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text(
                        transaction.paymentMethod.paymentLabel(),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        transaction.paymentReference.orEmpty().ifBlank { "-" },
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "Updated by ${transaction.verifiedBy?.name ?: "-"}",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    transaction.createdAt.displayTransactionDate(),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun String?.paymentLabel(): String =
    when (this) {
        "bank_transfer" -> "Bank transfer"
        "upi" -> "UPI"
        "cheque" -> "Cheque"
        "cash" -> "Cash"
        else -> this?.replace('_', ' ')?.replaceFirstChar { it.uppercase() } ?: "-"
    }

private fun String?.displayTransactionDate(): String {
    if (this.isNullOrBlank()) return "-"
    return runCatching {
        OffsetDateTime.parse(this).format(transactionDateFormatter)
    }.getOrDefault(this)
}
