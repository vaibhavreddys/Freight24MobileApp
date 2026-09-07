package com.freight24.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.freight24.common.model.Trip
import com.freight24.ui.trucker.FleetCard
import com.freight24.ui.trucker.FleetRule
import com.freight24.ui.trucker.FleetTone
import com.freight24.ui.trucker.PlateBadge
import com.freight24.ui.trucker.RoadSpine
import com.freight24.ui.trucker.StageRail
import com.freight24.ui.trucker.TruckerColors
import com.freight24.ui.trucker.TruckerType
import com.freight24.ui.trucker.color
import com.freight24.ui.trucker.fleetPress
import com.freight24.ui.trucker.inr
import com.freight24.ui.trucker.rememberTruckerColors
import com.freight24.ui.trucker.tripStageCount
import com.freight24.ui.trucker.tripStageIndex
import com.freight24.ui.trucker.tripStageLabel

// ---------------------------------------------------------------------------
// Trip card — a consignment slip, kept quiet.
//
// The reads a driver makes on this list, in order: which truck, which lane,
// where it is, what it pays. The card is built in exactly that order and
// nothing else is drawn:
//
//   1. the registration, letter-spaced inside a plate outline, with the
//      body type beneath it;
//   2. the lane — two stops and a dashed centre line filled to the trip's
//      current progress;
//   3. a single hairline;
//   4. the stage, its rail, and the money.
//
// The only mark of colour is the band down the left edge: the siding. Its
// hue carries the stage, so a list can be swept before it is read, and the
// rest of the card stays in ink.
// ---------------------------------------------------------------------------

/** Human stage name: "IN_TRANSIT" -> "In transit". */
fun formatStageName(status: String): String =
    status
        .lowercase()
        .replace("_", " ")
        .replaceFirstChar { it.uppercase() }

/**
 * Siding hue for a stage. Saffron while the driver still owes an action,
 * forest once the load is moving or settling, muted once it is paid out.
 */
fun tripStageTone(stageIndex: Int): FleetTone =
    when (stageIndex) {
        0, 1 -> FleetTone.ACCENT
        2, 3, 4 -> FleetTone.BRAND
        else -> FleetTone.MUTED
    }

@Composable
fun TripCard(
    trip: Trip,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    colors: TruckerColors = rememberTruckerColors(),
) {
    val interactionSource = MutableInteractionSource()
    val stageIndex = tripStageIndex(trip.status)
    val stageTone = tripStageTone(stageIndex)

    FleetCard(
        colors = colors,
        spine = stageTone.color(colors),
        modifier =
            modifier
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .fleetPress(interactionSource)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                )
                .testTag("trip_card")
                .semantics(mergeDescendants = true) {
                    testTagsAsResourceId = true
                    contentDescription = "trip_row_${trip.id}"
                },
    ) {
        // 1 — which truck. Plate hugs the left, date pins to the right.
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlateBadge(
                registration = trip.registrationNumber,
                colors = colors,
                caption = trip.truckType,
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = trip.createdAt,
                style = TruckerType.caption,
                color = colors.inkMuted,
                maxLines = 1,
                textAlign = androidx.compose.ui.text.style.TextAlign.End,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2 — which lane, and how far along it
        RoadSpine(
            from = trip.origin,
            to = trip.destination,
            colors = colors,
            progress = if (stageIndex < 0) 0f else (stageIndex + 1).toFloat() / tripStageCount(),
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3 — one hairline
        FleetRule(colors = colors)

        Spacer(modifier = Modifier.height(13.dp))

        // 4 — where it is, over what it pays
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (stageIndex in 0 until tripStageCount()) tripStageLabel(stageIndex) else formatStageName(trip.status),
                    style = TruckerType.captionStrong,
                    color = stageTone.color(colors),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(8.dp))
                StageRail(
                    currentIndex = stageIndex,
                    colors = colors,
                    modifier = Modifier.width(104.dp),
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = inr(trip.tripCost),
                    style = TruckerType.title,
                    color = colors.ink,
                    maxLines = 1,
                )
                Text(
                    text = "Paid ${inr(trip.totalPaidAmount)}",
                    style = TruckerType.caption,
                    color = colors.inkMuted,
                    maxLines = 1,
                )
            }
        }
    }
}
