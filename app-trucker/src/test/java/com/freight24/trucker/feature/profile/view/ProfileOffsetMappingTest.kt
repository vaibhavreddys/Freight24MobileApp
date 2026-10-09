package com.freight24.trucker.feature.profile.view

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.VisualTransformation
import com.freight24.ui.trucker.AadhaarVisualTransformation
import com.freight24.ui.trucker.IndianPhoneVisualTransformation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileOffsetMappingTest {
    @Test
    fun phoneGroupingHandlesLocalAndCountryCodeNumbers() {
        assertEquals("90891 23456", transform("+919089123456"))
        assertEquals("90891 23456", transform("9089123456"))
        assertEquals("90891 23459", transform("[\"+919089123459\""))
    }

    @Test
    fun phoneOffsetsStayInRangeForEveryInputLengthAndMalformedValue() {
        val inputs = (0..25).map { "987654321098765432109876543".take(it) } +
            listOf("+919089123456", "[\"+919089123459\"", "\"+919089123454\"]", "abc", "98 765-43210")
        inputs.forEach { assertValidOffsets(it, IndianPhoneVisualTransformation) }
    }

    @Test
    fun aadhaarOffsetsStayInRangeIncludingShortAndOversizedValues() {
        val inputs = (0..25).map { "456712348907456712348907456".take(it) } + listOf("4567 1234 8907", "abc", "12-34")
        inputs.forEach { assertValidOffsets(it, AadhaarVisualTransformation) }
        assertEquals("4567 1234 8907", AadhaarVisualTransformation.filter(AnnotatedString("456712348907")).text.text)
    }

    @Test
    fun cursorMappingPreservesGroupingBoundariesForBarePhoneDigits() {
        val mapping = IndianPhoneVisualTransformation.filter(AnnotatedString("9089123456")).offsetMapping
        assertEquals(5, mapping.originalToTransformed(5))
        assertEquals(7, mapping.originalToTransformed(6))
        assertEquals(5, mapping.transformedToOriginal(6))
        assertEquals(10, mapping.transformedToOriginal(11))
    }

    private fun transform(value: String) = IndianPhoneVisualTransformation.filter(AnnotatedString(value)).text.text

    private fun assertValidOffsets(input: String, transformation: VisualTransformation) {
        val result = transformation.filter(AnnotatedString(input))
        val forward = (0..input.length).map { result.offsetMapping.originalToTransformed(it) }
        val reverse = (0..result.text.length).map { result.offsetMapping.transformedToOriginal(it) }
        assertTrue(forward.all { it in 0..result.text.length })
        assertTrue(reverse.all { it in 0..input.length })
        assertEquals(forward.sorted(), forward)
        assertEquals(reverse.sorted(), reverse)
    }
}
