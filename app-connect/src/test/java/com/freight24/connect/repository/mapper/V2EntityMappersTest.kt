package com.freight24.connect.repository.mapper

import com.freight24.rest.response.ClientV2CompanyDto
import com.freight24.rest.response.ClientV2DetailDto
import com.freight24.rest.response.ClientV2DocumentDto
import com.freight24.rest.response.ClientV2DocumentSummaryDto
import com.freight24.rest.response.ClientV2SummaryDto
import com.freight24.rest.response.TruckOwnerV2DetailDto
import com.freight24.rest.response.TruckOwnerV2DocumentDto
import com.freight24.rest.response.TruckOwnerV2DocumentsDto
import com.freight24.rest.response.TruckOwnerV2SummaryDto
import com.freight24.rest.response.TruckOwnerV2TruckStatsDto
import com.freight24.rest.response.TruckV2DetailDto
import com.freight24.rest.response.TruckV2DocumentDto
import com.freight24.rest.response.TruckV2DocumentsDto
import com.freight24.rest.response.TruckV2OwnerDto
import com.freight24.rest.response.TruckV2SummaryDto
import com.freight24.rest.response.TruckV2VehicleDto
import com.freight24.rest.response.V2ActivationEligibilityDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class V2EntityMappersTest {
    @Test
    fun `maps truck summaries without exposing transport DTOs`() {
        val domain =
            TruckV2SummaryDto(
                truckId = "truck-1",
                registrationNumber = "TN01AB1234",
                status = "active",
                isVerified = true,
                availability = "AVAILABLE",
                owner = TruckV2OwnerDto(name = "ABC Transport", tagCode = "F24-1042"),
                vehicle = TruckV2VehicleDto(year = 2022, capacityMt = 16.5),
            ).toManagementItem()

        assertEquals("truck-1", domain.truckId)
        assertEquals("F24-1042", domain.owner?.tagCode)
        assertEquals(2022, domain.vehicle?.manufacturingYear)
        assertEquals(16.5, domain.vehicle?.capacityMt)

        val assignment =
            TruckV2SummaryDto(
                truckId = "truck-1",
                registrationNumber = "TN01AB1234",
                status = "active",
                availability = "AVAILABLE",
                owner = TruckV2OwnerDto(name = "ABC Transport", tagCode = "F24-1042"),
                vehicle = TruckV2VehicleDto(capacityMt = 16.5),
            ).toAssignmentOption()

        assertEquals("ABC Transport", assignment.ownerName)
        assertEquals("F24-1042", assignment.ownerTagCode)
        assertEquals(16.5, assignment.capacityMt)
    }

    @Test
    fun `maps truck detail activation and mandatory RC state`() {
        val domain =
            TruckV2DetailDto(
                truckId = "truck-1",
                registrationNumber = "TN01AB1234",
                status = "inactive",
                activationEligibility =
                    V2ActivationEligibilityDto(
                        eligible = false,
                        missingRequirements = emptyList(),
                    ),
                documents =
                    TruckV2DocumentsDto(
                        rcCard = TruckV2DocumentDto(url = "s3://rc", status = "verified"),
                    ),
            ).toDomain()

        assertFalse(domain.activationEligibility?.eligible == true)
        assertEquals("verified", domain.documents?.registrationCertificate?.status)
        assertNull(domain.detached)
    }

    @Test
    fun `maps full and basic owner search identifiers correctly`() {
        val fullSearch =
            TruckOwnerV2SummaryDto(
                id = "owner-user-1",
                fullName = "Arun Kumar",
            ).toManagementItem()
        val basicSearch =
            TruckOwnerV2SummaryDto(
                id = "owner-profile-1",
                userId = "owner-user-1",
                profileId = "owner-profile-1",
                fullName = "Arun Kumar",
            ).toPickerOption()

        assertEquals("owner-user-1", fullSearch.ownerUserId)
        assertNull(fullSearch.ownerProfileId)
        assertEquals("owner-user-1", basicSearch.ownerUserId)
        assertEquals("owner-profile-1", basicSearch.ownerProfileId)
    }

    @Test
    fun `maps owner detail document groups and profile identifiers`() {
        val domain =
            TruckOwnerV2DetailDto(
                id = "owner-user-1",
                profileId = "owner-profile-1",
                fullName = "Arun Kumar",
                status = "active",
                trucksCount = 2,
                truckStats = TruckOwnerV2TruckStatsDto(total = 2, active = 1, inactive = 1),
                documents =
                    TruckOwnerV2DocumentsDto(
                        mandatory =
                            listOf(
                                TruckOwnerV2DocumentDto(
                                    id = "doc-pan",
                                    type = "PAN Card",
                                    status = "verified",
                                ),
                            ),
                    ),
            ).toDomain()

        assertEquals("owner-user-1", domain.ownerUserId)
        assertEquals("owner-profile-1", domain.ownerProfileId)
        assertEquals("PAN Card", domain.mandatoryDocuments.single().type)
        assertEquals(2, domain.truckStats.total)
    }

    @Test
    fun `maps client summary profile and user identifiers explicitly`() {
        val domain =
            ClientV2SummaryDto(
                id = "client-profile-1",
                userId = "client-user-1",
                companyName = "Acme Industries",
            ).toManagementItem()

        val picker =
            ClientV2SummaryDto(
                id = "client-profile-1",
                userId = "client-user-1",
                companyName = "Acme Industries",
            ).toPickerOption()

        assertEquals("client-profile-1", domain.clientId)
        assertEquals("client-user-1", domain.clientUserId)
        assertEquals("client-profile-1", picker.clientId)
        assertEquals("Acme Industries", picker.companyName)
    }

    @Test
    fun `maps client detail company and document verification state`() {
        val domain =
            ClientV2DetailDto(
                id = "client-user-1",
                clientId = "client-profile-1",
                tagCode = "F24-2088",
                company = ClientV2CompanyDto(legalName = "Acme Industries", pan = "ABCDE1234F"),
                documents =
                    listOf(
                        ClientV2DocumentDto(
                            id = "doc-pan",
                            type = "PAN",
                            status = "verified",
                        ),
                    ),
                documentSummary =
                    ClientV2DocumentSummaryDto(
                        total = 4,
                        uploaded = 1,
                        verified = 1,
                    ),
                documentsVerified = true,
            ).toDomain()

        assertEquals("client-profile-1", domain.clientId)
        assertEquals("client-user-1", domain.clientUserId)
        assertEquals("ABCDE1234F", domain.company.panNumber)
        assertEquals("verified", domain.documents.single().status)
        assertTrue(domain.documentsVerified)
    }
}
