package com.freight24.ui.trucker

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.freight24.ui.auth.AuthColors
import com.freight24.ui.auth.AuthOutlinedRow
import com.freight24.ui.auth.AuthTypeScale

// ---------------------------------------------------------------------------
// app-trucker — the driver's copy of the auth document.
//
// These are the pieces of the auth screens that Connect does not have, or
// does not have in this form. They are built from the same shared primitives
// in com.freight24.ui.auth and drawn entirely from AuthColors, so they sit on
// the sheet as part of the document rather than as bolted-on widgets.
//
// Kept out of the two screen files because both the login and the OTP screen
// render them, and the helpline in particular must never drift between the
// two — a driver who is stuck on the code screen has to be able to read the
// same number they saw a minute earlier.
// ---------------------------------------------------------------------------

/**
 * The always-visible helpline, as an outlined row on the sheet.
 *
 * Connect shows a support row here too, but as a soft "Call support" affordance
 * with a reassurance line under it. This one prints the actual number at
 * weight, because for this audience the number *is* the trust signal and the
 * real fallback when the flow goes wrong — a driver who cannot get a code
 * needs to read a phone number off the screen, not tap a link that might open
 * a dialer they then have to work out how to use.
 */
@Composable
fun TruckerAuthSupportRow(
    caption: String,
    number: String,
    colors: AuthColors,
    type: AuthTypeScale,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Filled.Call,
) {
    AuthOutlinedRow(
        onClick = onClick,
        colors = colors,
        modifier = modifier.heightIn(min = 64.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier =
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colors.field.copy(alpha = 0.10f))
                        .border(1.dp, colors.field.copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.field,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = caption,
                    style = type.sheetBody,
                    color = colors.sheetInkMuted,
                )
                Text(
                    text = number,
                    style =
                        type.sheetBody.copy(
                            fontWeight = FontWeight.Bold,
                            fontFeatureSettings = "tnum",
                        ),
                    color = colors.sheetInk,
                )
            }
        }
    }
}

/**
 * Inline failure on the sheet.
 *
 * Same shape and behaviour as the error presentation on the trucker app's
 * post-login screens (tinted panel, leading glyph, message at body weight)
 * but drawn from the document palette so it belongs on the paper. [detail]
 * carries the secondary line — the remaining-attempts count on the code
 * screen — so the recovery information is attached to the failure instead of
 * being a separate line the user has to connect to it.
 */
@Composable
fun TruckerAuthErrorBlock(
    message: String,
    colors: AuthColors,
    type: AuthTypeScale,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    detail: String? = null,
) {
    AnimatedVisibility(
        visible = visible && message.isNotBlank(),
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.sheetDanger.copy(alpha = 0.10f))
                    .border(
                        width = 1.dp,
                        color = colors.sheetDanger.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(16.dp),
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                imageVector = Icons.Filled.ErrorOutline,
                contentDescription = null,
                tint = colors.sheetDanger,
                modifier = Modifier.size(22.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = message,
                    style =
                        type.sheetBody.copy(
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                        ),
                    color = colors.sheetDanger,
                )
                if (detail != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = detail,
                        style = type.sheetBody,
                        color = colors.sheetInkMuted,
                    )
                }
            }
        }
    }
}
