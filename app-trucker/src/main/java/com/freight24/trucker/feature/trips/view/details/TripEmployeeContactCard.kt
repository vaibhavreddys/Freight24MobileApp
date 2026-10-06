package com.freight24.trucker.feature.trips.view.details

import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.freight24.common.model.TripEmployee
import com.freight24.trucker.R
import com.freight24.trucker.ui.components.rememberCallAction
import com.freight24.ui.trucker.FleetCardShape
import com.freight24.ui.trucker.FleetCellShape
import com.freight24.ui.trucker.TruckerColors
import com.freight24.ui.trucker.TruckerType

@Composable
internal fun TripEmployeeContactCard(employee: TripEmployee, colors: TruckerColors) {
    val phone = employee.phone?.trim().orEmpty()
    val makeCall = rememberCallAction(phone)
    val name = employee.name?.trim()?.takeIf { it.isNotEmpty() }
        ?: stringResource(R.string.trip_employee_contact_fallback)
    Surface(
        shape = FleetCardShape,
        color = colors.surface,
        border = BorderStroke(1.5.dp, colors.line),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            .clickable(enabled = phone.isNotBlank(), onClick = makeCall)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.trip_employee_contact_label),
                    style = TruckerType.caption,
                    color = colors.inkMuted
                )
                Text(
                    text = name,
                    style = TruckerType.body,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.ink
                )
            }
            OutlinedButton(
                onClick = makeCall,
                enabled = phone.isNotBlank(),
                shape = FleetCellShape,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Outlined.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.trip_employee_call_us), style = TruckerType.captionStrong)
            }
        }
    }
}
