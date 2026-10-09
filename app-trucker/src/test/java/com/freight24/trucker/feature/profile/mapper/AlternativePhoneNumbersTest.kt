package com.freight24.trucker.feature.profile.mapper

import org.junit.Assert.assertEquals
import org.junit.Test

class AlternativePhoneNumbersTest {
    @Test
    fun fragmentedEncodedArrayIsDecodedIntoIndividualNumbers() {
        assertEquals(
            listOf("+919089123459", "+919089123454"),
            normalizeAlternativePhoneNumbers(listOf("[\"+919089123459\"", "\"+919089123454\"]")),
        )
    }

    @Test
    fun encodedArrayAndNormalListAreBothSupported() {
        val expected = listOf("+919089123459", "+919089123454")
        assertEquals(expected, normalizeAlternativePhoneNumbers(expected))
        assertEquals(expected, normalizeAlternativePhoneNumbers(listOf("[\"+919089123459\",\"+919089123454\"]")))
    }

    @Test
    fun emptyAndUnparseableValuesAreHandledWithoutThrowing() {
        assertEquals(emptyList<String>(), normalizeAlternativePhoneNumbers(emptyList()))
        assertEquals(emptyList<String>(), normalizeAlternativePhoneNumbers(listOf(" ")))
        assertEquals(listOf("[broken"), normalizeAlternativePhoneNumbers(listOf("[broken")))
    }
}
