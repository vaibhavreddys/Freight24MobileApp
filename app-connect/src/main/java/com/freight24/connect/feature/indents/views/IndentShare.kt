package com.freight24.connect.feature.indents.views

import android.content.Context
import android.content.Intent
import com.freight24.common.util.formatIndianCurrency
import com.freight24.common.util.formatLoadWeight
import com.freight24.rest.response.IndentResponse

private const val WHATSAPP_PACKAGE = "com.whatsapp"

/**
 * The city on one end of the lane, or null when the indent has no such location.
 *
 * The backend is inconsistent about the casing of `unloading`, so this matches
 * case-insensitively rather than reproducing the screen's exact-string check and
 * silently sharing a half-empty message.
 */
private fun IndentResponse.IndentGroupDetails.city(type: String): String? =
    indentLocations
        .firstOrNull { it.locationType.equals(type, ignoreCase = true) }
        ?.let { it.areaName ?: it.cityName }
        ?.takeIf { it.isNotBlank() }

/**
 * What an operator sends a trucker when forwarding an indent.
 *
 * Only the four facts a driver decides on — and the rate only when this operator
 * is allowed to see it, since [IndentResponse.Indent.hideCostInfo] governs the
 * screen and must govern the forwarded copy too.
 */
internal fun buildIndentShareMessage(indent: IndentResponse.Indent): String {
    val group = indent.indentGroupDetails
    val lane = listOf("loading", "unloading").mapNotNull { group.city(it) }
    val specs =
        listOfNotNull(
            group.truckType?.displayName,
            formatLoadWeight(group.loadWeight, "MT"),
            group.material,
        ).filter { it.isNotBlank() }

    return buildList {
        // A chip has no plain-text equivalent, so the badge borrows the one piece
        // of decoration WhatsApp does render: a fenced code block, which comes
        // out boxed and monospace rather than as ordinary body text.
        add("*`NEW LOAD`*")
        if (lane.size == 2) {
            add("")
            add("${lane[0]}  ⮕  ${lane[1]}")
        }
        if (specs.isNotEmpty()) {
//            add("")
            add(specs.joinToString(" • "))
        }
        val targetCost = indent.negotiatedTripCost
        if (!indent.hideCostInfo && targetCost != null && targetCost > 0) {
            add("")
            add("Cost: *`₹${formatIndianCurrency(targetCost.toInt().toString())}`*")
        }
        add("")
        add("For more loads download our app *F24 Loads*")
        add("https://play.google.com/store/apps/details?id=com.mobilegame.wordsearch")
    }.joinToString("\n")
}

/**
 * Opens WhatsApp on its contact picker with [message] pre-filled.
 *
 * Package-targeted rather than a chooser so WhatsApp is the one app the operator
 * lands in; the chooser is only reached when WhatsApp is not installed, which
 * would otherwise be an unhandled [android.content.ActivityNotFoundException].
 */
internal fun shareIndentOnWhatsApp(
    context: Context,
    message: String,
) {
    val send =
        Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }

    runCatching { context.startActivity(Intent(send).setPackage(WHATSAPP_PACKAGE)) }
        .recoverCatching { context.startActivity(Intent.createChooser(send, "Share indent")) }
}
