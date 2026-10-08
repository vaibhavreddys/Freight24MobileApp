package com.freight24.connect.feature.indents.views

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class IndentCardOwnershipTest {
    @Test
    fun ownedAndAssignedIndentsAreMine() {
        listOf("CREATED_BY_ME", "CLONED_TO_ME", "ASSIGNED_TO_ME").forEach {
            assertEquals(IndentCardOwnership.MINE, indentCardOwnership(it))
        }
    }

    @Test
    fun otherEmployeesIndentsAreOther() {
        assertEquals(IndentCardOwnership.OTHER, indentCardOwnership("OTHER"))
    }

    @Test
    fun ownershipIgnoresCaseAndSurroundingWhitespace() {
        assertEquals(IndentCardOwnership.MINE, indentCardOwnership(" created_by_me "))
        assertEquals(IndentCardOwnership.OTHER, indentCardOwnership("other"))
    }

    @Test
    fun absentOrUnknownOwnershipIsNotGuessed() {
        listOf(null, "", " ", "unknown").forEach { assertNull(indentCardOwnership(it)) }
    }
}
