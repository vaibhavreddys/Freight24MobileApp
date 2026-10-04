package com.freight24.connect.feature.trips.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.freight24.connect.R
import com.freight24.rest.response.TripDataResponse
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

@Composable
internal fun ClientPaymentBreakdownContent(
    trip: TripDataResponse,
    onViewTransactions: () -> Unit
) {
    val payment = trip.clientPaymentBreakdown
    if (payment == null) {
        Text(
            text = stringResource(R.string.trip_client_payments_unavailable),
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    val financials = trip.financials
    val loading = financials?.halting?.loading
    val unloading = financials?.halting?.unloading
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        ClientPaymentRow(stringResource(R.string.trip_client_rate), trip.clientTripCost)
        ClientPaymentRow(
            label = stringResource(R.string.trip_client_loading_halting),
            amount = loading?.totalAmount,
            sign = "+",
            detail = loading?.let {
                stringResource(R.string.trip_halting_detail, it.days, formatClientMoney(it.perDayAmount))
            }
        )
        ClientPaymentRow(
            stringResource(R.string.trip_client_loading_charges),
            financials?.luCharges?.loading?.reimbursable,
            sign = "+"
        )
        ClientPaymentRow(
            label = stringResource(R.string.trip_client_unloading_halting),
            amount = unloading?.totalAmount,
            sign = "+",
            detail = unloading?.let {
                stringResource(R.string.trip_halting_detail, it.days, formatClientMoney(it.perDayAmount))
            }
        )
        ClientPaymentRow(
            stringResource(R.string.trip_client_unloading_charges),
            financials?.luCharges?.unloading?.reimbursable,
            sign = "+"
        )
        ClientPaymentRow(
            stringResource(R.string.trip_client_other_deductions),
            financials?.otherDeductions,
            sign = "-"
        )
        if (financials?.disputeResolved == true &&
            (financials.disputeResolutionAmount?.signum() ?: 0) != 0
        ) {
            ClientPaymentRow(
                stringResource(R.string.trip_client_resolved_dispute),
                financials.disputeResolutionAmount,
                sign = "-"
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        ClientPaymentRow(
            stringResource(R.string.trip_client_total_bill),
            payment.clientNetAmount,
            emphasized = true
        )
        ClientPaymentRow(
            stringResource(R.string.trip_client_advance_received),
            payment.advanceReceivedAmount,
            sign = "-"
        )
        ClientPaymentRow(
            stringResource(R.string.trip_client_balance_received),
            payment.balanceReceivedAmount,
            sign = "-"
        )
        ClientPaymentRow(stringResource(R.string.trip_client_total_received), payment.clientPaidAmount)
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        ClientPaymentRow(
            stringResource(R.string.trip_client_pending_receivable),
            payment.clientPendingAmount,
            emphasized = true
        )
        if ((payment.clientCreditAmount?.signum() ?: 0) != 0) {
            ClientPaymentRow(
                stringResource(R.string.trip_client_credit),
                payment.clientCreditAmount,
                emphasized = true
            )
        }
        if (payment.isPaymentCompleted) {
            Text(
                text = stringResource(R.string.trip_client_payment_completed),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
        TextButton(onClick = onViewTransactions, modifier = Modifier.align(Alignment.End)) {
            Text(stringResource(R.string.trip_view_transactions))
        }
    }
}

@Composable
private fun ClientPaymentRow(
    label: String,
    amount: BigDecimal?,
    sign: String = "",
    detail: String? = null,
    emphasized: Boolean = false
) {
    val colorScheme = MaterialTheme.colorScheme
    val hasAmount = (amount?.signum() ?: 0) != 0
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(0.6f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (emphasized) FontWeight.Bold else FontWeight.Normal,
                color = if (emphasized) colorScheme.onSurface else colorScheme.onSurfaceVariant
            )
            if (detail != null) {
                Text(
                    text = detail,
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }
        Text(
            text = (if ((amount?.signum() ?: 0) > 0 && sign.isNotEmpty()) "$sign " else "") +
                formatClientMoney(amount),
            modifier = Modifier.weight(0.4f),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (emphasized || hasAmount) FontWeight.Bold else FontWeight.Normal,
            color = when {
                emphasized -> colorScheme.primary
                !hasAmount -> colorScheme.onSurfaceVariant
                sign == "+" -> colorScheme.primary
                else -> colorScheme.onSurface
            }
        )
    }
}

internal fun formatClientMoney(amount: BigDecimal?): String {
    val formatter = DecimalFormat("#,##0.##", DecimalFormatSymbols(Locale.forLanguageTag("en-IN")))
    return "\u20B9${formatter.format(amount ?: BigDecimal.ZERO)}"
}
