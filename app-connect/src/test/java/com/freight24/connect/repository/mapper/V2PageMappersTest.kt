package com.freight24.connect.repository.mapper

import com.freight24.rest.response.ClientV2Counts
import com.freight24.rest.response.ClientV2PageResponse
import com.freight24.rest.response.TruckOwnerV2PageResponse
import com.freight24.rest.response.TruckOwnerV2SummaryDto
import com.freight24.rest.response.TruckV2Counts
import com.freight24.rest.response.TruckV2OwnerDto
import com.freight24.rest.response.TruckV2PageResponse
import com.freight24.rest.response.TruckV2SummaryDto
import com.freight24.rest.response.TruckV2VehicleDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class V2PageMappersTest {
    @Test
    fun `truck page exposes counts and next page when more results exist`() {
        val page =
            TruckV2PageResponse(
                success = true,
                page = 1,
                limit = 2,
                counts =
                    TruckV2Counts(
                        total = 3,
                        active = 2,
                        inactive = 1,
                        pendingDocs = 1,
                    ),
                data = listOf(truck("truck-1"), truck("truck-2")),
            ).toManagementPage()

        assertEquals(3, page.counts.total)
        assertEquals(1, page.counts.pendingDocuments)
        assertFalse(page.endOfList)
        assertTrue(page.hasNextPage)
        assertEquals(2, page.nextPage)
    }

    @Test
    fun `short final truck page marks end of list`() {
        val page =
            TruckV2PageResponse(
                success = true,
                page = 2,
                limit = 2,
                counts = TruckV2Counts(total = 3),
                data = listOf(truck("truck-3")),
            ).toManagementPage()

        assertTrue(page.endOfList)
        assertFalse(page.hasNextPage)
        assertNull(page.nextPage)
    }

    @Test
    fun `truck assignment page exposes only selection data`() {
        val page =
            TruckV2PageResponse(
                success = true,
                page = 1,
                limit = 30,
                counts = TruckV2Counts(total = 1),
                data =
                    listOf(
                        TruckV2SummaryDto(
                            truckId = "truck-1",
                            registrationNumber = "TN01AB1234",
                            status = "active",
                            availability = "AVAILABLE",
                            owner = TruckV2OwnerDto(name = "ABC Transport", tagCode = "F24-1042"),
                            vehicle = TruckV2VehicleDto(capacityMt = 16.5),
                        ),
                    ),
            ).toAssignmentPage()

        assertEquals("truck-1", page.items.single().truckId)
        assertEquals("F24-1042", page.items.single().ownerTagCode)
        assertEquals(16.5, page.items.single().capacityMt)
    }

    @Test
    fun `owner search count maps without status counts`() {
        val page =
            TruckOwnerV2PageResponse(
                success = true,
                page = 1,
                limit = 30,
                count = 1,
                data =
                    listOf(
                        TruckOwnerV2SummaryDto(
                            id = "owner-profile-1",
                            userId = "owner-user-1",
                            profileId = "owner-profile-1",
                            fullName = "Arun Kumar",
                        ),
                    ),
            ).toPickerPage()

        assertEquals(1, page.counts.total)
        assertNull(page.counts.active)
        assertTrue(page.endOfList)
        assertEquals("owner-user-1", page.items.single().ownerUserId)
        assertEquals("owner-profile-1", page.items.single().ownerProfileId)
    }

    @Test
    fun `empty client page is terminal`() {
        val page =
            ClientV2PageResponse(
                success = true,
                page = 1,
                limit = 30,
                counts = ClientV2Counts(),
                data = emptyList(),
            ).toManagementPage()

        assertTrue(page.endOfList)
        assertEquals(0, page.counts.total)
        assertTrue(page.items.isEmpty())
    }

    private fun truck(id: String) =
        TruckV2SummaryDto(
            truckId = id,
            registrationNumber = "TN01AB1234",
            status = "active",
        )
}
