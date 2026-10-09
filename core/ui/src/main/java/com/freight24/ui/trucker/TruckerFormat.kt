package com.freight24.ui.trucker

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

// ---------------------------------------------------------------------------
// Indian mobile-number input transformation.
//
// Editable grouping tracks every source boundary, including formatted and
// oversized values returned by the backend. Plain display formatting and
// countdown formatters live in core:common.
// ---------------------------------------------------------------------------

/** "9876543210" -> "98765 43210" as the user types. */
val IndianPhoneVisualTransformation: VisualTransformation =
    GroupedDigitsVisualTransformation(maxDigits = 10, groupSize = 5, stripIndianCountryCode = true)

val AadhaarVisualTransformation: VisualTransformation =
    GroupedDigitsVisualTransformation(maxDigits = 12, groupSize = 4)

private class GroupedDigitsVisualTransformation(
    private val maxDigits: Int,
    private val groupSize: Int,
    private val stripIndianCountryCode: Boolean = false,
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digitIndices = text.text.indices.filter { text.text[it].isDigit() }
        val hasCountryCode = stripIndianCountryCode && digitIndices.size > 10 &&
            digitIndices.take(2).map { text.text[it] }.joinToString("") == "91"
        val keptIndices = digitIndices.drop(if (hasCountryCode) 2 else 0).take(maxDigits).toSet()
        val forward = IntArray(text.length + 1)
        val reverse = mutableListOf(0)
        val output = StringBuilder()
        var digitCount = 0

        // Track source boundaries as characters are removed and grouping spaces
        // are inserted, rather than assuming the source is already bare digits.
        text.text.forEachIndexed { index, character ->
            if (index in keptIndices) {
                if (digitCount > 0 && digitCount % groupSize == 0) {
                    output.append(' ')
                    reverse.add(index)
                }
                output.append(character)
                reverse.add(index + 1)
                digitCount++
            }
            forward[index + 1] = output.length
        }

        return TransformedText(
            AnnotatedString(output.toString()),
            object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int = forward[offset.coerceIn(0, text.length)]
                override fun transformedToOriginal(offset: Int): Int = reverse[offset.coerceIn(0, output.length)]
            },
        )
    }
}
