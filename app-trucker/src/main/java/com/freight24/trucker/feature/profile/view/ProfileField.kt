package com.freight24.trucker.feature.profile.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.freight24.trucker.feature.profile.util.ProfileFieldValidators
import com.freight24.trucker.feature.profile.util.ValidationResult

/**
 * Enum representing the type of validation for profile fields.
 * Used to apply Indian-specific validation rules.
 */
internal enum class ValidationType {
    NONE,
    AADHAAR,
    PAN,
    GST,
    IFSC,
    UPI,
    PHONE,
    EMAIL,
    ACCOUNT_NUMBER,
}

/**
 * One label/value row, optionally editable.
 *
 * The per-field leading icons are gone. They added 44dp of indent per row to
 * restate the label ("Branch name" carried a clipboard), five fields shared one
 * `Icons.Default.Badge` briefcase glyph, and four different icon sizes coexisted
 * inside a single card.
 *
 * [lockedReason] is shown instead of an input when a verified section makes the
 * field read-only, so the user can tell *why* rather than guessing from a
 * missing text box.
 */
@Composable
internal fun ProfileField(
    label: String,
    value: String,
    isEditing: Boolean,
    isEditable: Boolean = true,
    isLocked: Boolean = false,
    lockedReason: String? = null,
    /**
     * Replaces the default value display. Used by composite rows (city search,
     * route chips) whose body is richer than a single line of text.
     */
    valueContent: (@Composable () -> Unit)? = null,
    onValueChange: (String) -> Unit = {},
    isSensitive: Boolean = false,
    validationType: ValidationType = ValidationType.NONE,
    showAsLabelOnly: Boolean = false,
    onFocusLost: (() -> Unit)? = null,
    duplicateError: String? = null,
    /**
     * Read-only verification hint for the document backing this value. Lets the
     * field answer "what is my number and is it accepted?" without sending the
     * user to the Documents section. Never an upload control — the Documents
     * section owns that.
     */
    documentTone: DocumentTone? = null,
) {
    // State for validation error
    var validationError by rememberSaveable { mutableStateOf<String?>(null) }

    // Validate on value change
    val handleValueChange: (String) -> Unit = { newValue ->
        // Apply input filtering based on validation type
        val filteredValue =
            when (validationType) {
                ValidationType.AADHAAR -> newValue.filter { it.isDigit() }.take(12)
                ValidationType.PHONE -> newValue.filter { it.isDigit() }.take(10)
                ValidationType.ACCOUNT_NUMBER -> newValue.filter { it.isDigit() }.take(18)
                ValidationType.IFSC -> newValue.uppercase().filter { it.isLetterOrDigit() }.take(11)
                ValidationType.PAN -> newValue.uppercase().filter { it.isLetterOrDigit() }.take(10)
                ValidationType.GST -> newValue.uppercase().filter { it.isLetterOrDigit() }.take(15)
                else -> newValue
            }

        onValueChange(filteredValue)

        // Validate and show error
        validationError =
            when (validationType) {
                ValidationType.AADHAAR -> {
                    val result = ProfileFieldValidators.validateAadhaar(filteredValue)
                    if (result is ValidationResult.Error) result.message else null
                }
                ValidationType.PAN -> {
                    val result = ProfileFieldValidators.validatePan(filteredValue)
                    if (result is ValidationResult.Error) result.message else null
                }
                ValidationType.GST -> {
                    val result = ProfileFieldValidators.validateGst(filteredValue)
                    if (result is ValidationResult.Error) result.message else null
                }
                ValidationType.IFSC -> {
                    val result = ProfileFieldValidators.validateIfsc(filteredValue)
                    if (result is ValidationResult.Error) result.message else null
                }
                ValidationType.UPI -> {
                    val result = ProfileFieldValidators.validateUpiId(filteredValue)
                    if (result is ValidationResult.Error) result.message else null
                }
                ValidationType.EMAIL -> {
                    val result = ProfileFieldValidators.validateEmail(filteredValue)
                    if (result is ValidationResult.Error) result.message else null
                }
                ValidationType.ACCOUNT_NUMBER -> {
                    val result = ProfileFieldValidators.validateAccountNumber(filteredValue)
                    if (result is ValidationResult.Error) result.message else null
                }
                else -> null
            }
    }

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            if (documentTone != null) {
                Spacer(modifier = Modifier.width(8.dp))
                StatusChip(text = documentTone.label(), tone = documentTone.statusTone())
            } else if (isLocked) {
                Spacer(modifier = Modifier.width(8.dp))
                LockedTag(reason = lockedReason)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))

        if ((isEditing && isEditable) || showAsLabelOnly) {
            if (showAsLabelOnly) {
                // Display as read-only label (for phone number)
                Text(
                    text = value.ifEmpty { "Not added" },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (value.isEmpty()) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.onSurface,
                )
            } else {
                val keyboardOptions =
                    androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType =
                            when (validationType) {
                                ValidationType.AADHAAR, ValidationType.PHONE, ValidationType.ACCOUNT_NUMBER -> KeyboardType.Number
                                ValidationType.EMAIL -> KeyboardType.Email
                                else -> KeyboardType.Text
                            },
                        capitalization =
                            when (validationType) {
                                ValidationType.PAN, ValidationType.IFSC, ValidationType.GST -> KeyboardCapitalization.Characters
                                else -> KeyboardCapitalization.Words
                            },
                        imeAction = ImeAction.Next,
                    )

                var isFocused by remember { mutableStateOf(false) }

                val visualTransformation =
                    when (validationType) {
                        ValidationType.AADHAAR ->
                            VisualTransformation { text ->
                                val transformed = ProfileFieldValidators.formatAadhaar(text.text)
                                TransformedText(
                                    AnnotatedString(transformed),
                                    object : OffsetMapping {
                                        override fun originalToTransformed(offset: Int): Int {
                                            if (offset <= 4) return offset
                                            if (offset <= 8) return offset + 1
                                            return offset + 2
                                        }

                                        override fun transformedToOriginal(offset: Int): Int {
                                            if (offset <= 4) return offset
                                            if (offset <= 9) return offset - 1
                                            return offset - 2
                                        }
                                    },
                                )
                            }
                        ValidationType.PHONE ->
                            VisualTransformation { text ->
                                val transformed = ProfileFieldValidators.formatPhone(text.text)
                                TransformedText(
                                    AnnotatedString(transformed),
                                    object : OffsetMapping {
                                        override fun originalToTransformed(offset: Int): Int {
                                            if (offset <= 5) return offset
                                            return offset + 1
                                        }

                                        override fun transformedToOriginal(offset: Int): Int {
                                            if (offset <= 5) return offset
                                            return offset - 1
                                        }
                                    },
                                )
                            }
                        else -> VisualTransformation.None
                    }

                OutlinedTextField(
                    value = value,
                    onValueChange = handleValueChange,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .onFocusChanged { state ->
                                if (isFocused && !state.isFocused) {
                                    onFocusLost?.invoke()
                                }
                                isFocused = state.isFocused
                            },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    isError = validationError != null,
                    keyboardOptions = keyboardOptions,
                    visualTransformation = visualTransformation,
                    supportingText = {
                        val errorText = validationError ?: duplicateError
                        if (errorText != null) {
                            Text(
                                text = errorText,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    },
                    placeholder =
                        when (validationType) {
                            ValidationType.AADHAAR -> {
                                { Text("1234 5678 9012") }
                            }
                            ValidationType.PAN -> {
                                { Text("ABCDE1234F") }
                            }
                            ValidationType.GST -> {
                                { Text("27ABCDE1234F1Z5") }
                            }
                            ValidationType.IFSC -> {
                                { Text("HDFC0001234") }
                            }
                            ValidationType.UPI -> {
                                { Text("name@okhdfcbank") }
                            }
                            ValidationType.EMAIL -> {
                                { Text("email@example.com") }
                            }
                            else -> null
                        },
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (validationError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = if (validationError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant,
                            errorBorderColor = MaterialTheme.colorScheme.error,
                            errorLabelColor = MaterialTheme.colorScheme.error,
                            errorSupportingTextColor = MaterialTheme.colorScheme.error,
                        ),
                )
            }
        } else if (valueContent != null) {
            valueContent()
        } else {
            val displayValue =
                if (isSensitive && value.isNotEmpty()) {
                    "•••• •••• ${value.takeLast(4)}"
                } else {
                    value.ifEmpty { "Not added" }
                }
            Text(
                text = displayValue,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (value.isEmpty()) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
