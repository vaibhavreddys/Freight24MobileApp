package com.freight24.ui.trucker

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.freight24.common.util.groupIndianMobileDigits

// ---------------------------------------------------------------------------
// Indian mobile-number input transformation.
//
// The grouping itself lives in core:common ([groupIndianMobileDigits]) next
// to the other phone/clock formatters; this file only holds the Compose
// [VisualTransformation] wrapper, which core:common cannot host (no Compose
// dependency there). Display formatting ([formatIndianMobileNumber]) and the
// OTP clock ([formatCountdownClock]) are also in core:common.
// ---------------------------------------------------------------------------

/** "9876543210" -> "98765 43210" as the user types. */
val IndianPhoneVisualTransformation: VisualTransformation =
    object : VisualTransformation {
        override fun filter(text: AnnotatedString): TransformedText {
            val raw = text.text.filter(Char::isDigit).take(10)
            val transformed = groupIndianMobileDigits(raw)

            val offsetMapping =
                object : OffsetMapping {
                    override fun originalToTransformed(offset: Int): Int =
                        if (raw.length <= 5) offset else if (offset <= 5) offset else offset + 1

                    override fun transformedToOriginal(offset: Int): Int =
                        if (raw.length <= 5) offset else if (offset <= 5) offset else offset - 1
                }

            return TransformedText(AnnotatedString(transformed), offsetMapping)
        }
    }
