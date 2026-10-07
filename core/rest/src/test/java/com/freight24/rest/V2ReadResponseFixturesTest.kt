package com.freight24.rest

import com.freight24.rest.response.ClientV2DetailResponse
import com.freight24.rest.response.ClientV2PageResponse
import com.freight24.rest.response.ClientV2ValidationResponse
import com.freight24.rest.response.TruckOwnerV2DetailResponse
import com.freight24.rest.response.TruckOwnerV2PageResponse
import com.freight24.rest.response.TruckOwnerV2ValidationResponse
import com.freight24.rest.response.TruckV2DetailResponse
import com.freight24.rest.response.TruckV2PageResponse
import com.freight24.rest.response.TruckV2ValidationResponse
import com.freight24.rest.response.V2ActivationFailureResponse
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class V2ReadResponseFixturesTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `truck list and search fixtures decode the shared page contract`() {
        listOf(truckListFixture, truckSearchFixture).forEach { fixture ->
            val response = json.decodeFromString<TruckV2PageResponse>(fixture)
            assertTrue(response.success)
            assertEquals(1, response.data.size)
            assertEquals("F24-1042", response.data.single().owner?.tagCode)
            assertEquals(16.5, response.data.single().vehicle?.capacityMt)
        }
    }

    @Test
    fun `truck detail update and status fixtures decode the enriched contract`() {
        listOf(truckDetailFixture, truckUpdateFixture, truckStatusFixture).forEach { fixture ->
            val response = json.decodeFromString<TruckV2DetailResponse>(fixture)
            assertEquals("TN01AB1234", response.data?.registrationNumber)
            assertEquals(12, response.data?.truckTypeId)
            assertEquals("verified", response.data?.documents?.rcCard?.status)
        }

        val failure = json.decodeFromString<V2ActivationFailureResponse>(truckActivationFailureFixture)
        assertFalse(failure.success)
        assertEquals("RC_VERIFIED", failure.data?.activationEligibility?.missingRequirements?.single()?.code)
    }

    @Test
    fun `truck validation fixture keeps owner user and profile identifiers distinct`() {
        val response = json.decodeFromString<TruckV2ValidationResponse>(truckValidationFixture)
        assertTrue(response.data.exists)
        assertEquals("owner-user-1", response.data.truck?.owner?.id)
        assertEquals("owner-profile-1", response.data.truck?.owner?.profileId)
    }

    @Test
    fun `truck owner list full search and basic search fixtures decode`() {
        val list = json.decodeFromString<TruckOwnerV2PageResponse>(truckOwnerListFixture)
        val search = json.decodeFromString<TruckOwnerV2PageResponse>(truckOwnerSearchFixture)
        val basic = json.decodeFromString<TruckOwnerV2PageResponse>(truckOwnerBasicSearchFixture)

        assertEquals(2, list.counts?.pendingDocs)
        assertEquals("owner-user-1", search.data.single().id)
        assertEquals("owner-profile-1", basic.data.single().id)
        assertEquals("owner-user-1", basic.data.single().userId)
        assertEquals("owner-profile-1", basic.data.single().profileId)
    }

    @Test
    fun `truck owner detail update and status fixtures decode mandatory document groups`() {
        listOf(truckOwnerDetailFixture, truckOwnerUpdateFixture, truckOwnerStatusFixture).forEach { fixture ->
            val response = json.decodeFromString<TruckOwnerV2DetailResponse>(fixture)
            assertEquals(3, response.data?.documents?.mandatory?.size)
            assertEquals("Cancelled Cheque", response.data?.documents?.mandatory?.last()?.type)
            assertEquals("owner-profile-1", response.data?.profileId)
        }
    }

    @Test
    fun `truck owner validation and activation failure fixtures decode`() {
        val validation = json.decodeFromString<TruckOwnerV2ValidationResponse>(truckOwnerValidationFixture)
        assertTrue(validation.data.phone?.exists == true)
        assertEquals("owner-profile-1", validation.data.phone?.trucker?.profileId)

        val failure = json.decodeFromString<V2ActivationFailureResponse>(truckOwnerActivationFailureFixture)
        assertEquals("inactive", failure.data?.status)
        assertTrue(failure.data?.activationEligibility?.missingRequirements?.isNotEmpty() == true)
    }

    @Test
    fun `client list and search fixtures decode the same paginated contract`() {
        listOf(clientListFixture, clientSearchFixture).forEach { fixture ->
            val response = json.decodeFromString<ClientV2PageResponse>(fixture)
            assertEquals("client-profile-1", response.data.single().id)
            assertEquals("client-user-1", response.data.single().userId)
            assertEquals("Priya", response.data.single().primaryContact?.fullName)
        }
    }

    @Test
    fun `client detail update and status fixtures decode nested resources`() {
        listOf(clientDetailFixture, clientUpdateFixture, clientStatusFixture).forEach { fixture ->
            val response = json.decodeFromString<ClientV2DetailResponse>(fixture)
            assertEquals("client-profile-1", response.data?.clientId)
            assertEquals("F24-2088", response.data?.tagCode)
            assertEquals(1, response.data?.contacts?.size)
            assertEquals(1, response.data?.branches?.size)
            assertEquals("verified", response.data?.documents?.first()?.status)
        }
        val detail = json.decodeFromString<ClientV2DetailResponse>(clientDetailFixture)
        assertEquals("Guindy", detail.data?.branches?.single()?.address?.area?.name)
        assertEquals("33ABCDE1234F1Z5", detail.data?.branches?.single()?.gst?.gstin)
    }

    @Test
    fun `client validation fixture decodes profile and branch conflicts`() {
        val response = json.decodeFromString<ClientV2ValidationResponse>(clientValidationFixture)
        assertFalse(response.data.company_phone?.exists == true)
        assertTrue(response.data.gst_number?.exists == true)
        assertEquals("CLIENT_BRANCH", response.data.gst_number?.foundIn)
        assertEquals("branch-1", response.data.gst_number?.branch?.id)
        assertNull(response.data.company_phone?.client)
    }

    private companion object {
        const val truckListFixture = """
            {"success":true,"page":1,"limit":30,"counts":{"total":1,"active":1,"inactive":0,"blacklisted":0,"pending_docs":0},"data":[{"truckId":"truck-1","registrationNumber":"TN01AB1234","status":"active","isVerified":true,"isBlacklisted":false,"availability":"AVAILABLE","owner":{"name":"ABC Transport","phone":"+919876543210","tagCode":"F24-1042"},"vehicle":{"make":"Tata","model":"LPT","year":2022,"capacityMt":16.5},"truckType":"32 Feet","bodyType":"Closed","displayName":"32 ft MXL","fitnessCertificate":{"expiryDate":"2027-01-01","expiryStatus":"VALID"},"insurance":{"expiryDate":"2027-02-01","expiryStatus":"VALID"}}]}
        """
        const val truckSearchFixture = truckListFixture
        const val truckDetailFixture = """
            {"success":true,"data":{"truckId":"truck-1","truckTypeId":12,"registrationNumber":"TN01AB1234","status":"active","isVerified":true,"isBlacklisted":false,"activationEligibility":{"eligible":true,"missingRequirements":[]},"availability":"AVAILABLE","owner":{"name":"ABC Transport","phone":"+919876543210","tagCode":"F24-1042"},"vehicle":{"make":"Tata","model":"LPT","year":2022,"capacityMt":16.5},"truckType":"32 Feet","bodyType":"Closed","displayName":"32 ft MXL","documents":{"rcCard":{"url":"s3://rc","status":"verified"},"fcCertificate":{"url":null,"status":"pending","expiryDate":null,"expiryStatus":"NOT_AVAILABLE"},"insurance":{"url":null,"status":"pending","expiryDate":null,"expiryStatus":"NOT_AVAILABLE"}},"detached":null,"blacklist":null}}
        """
        const val truckUpdateFixture = """{"success":true,"message":"Truck updated successfully","data":{"truckId":"truck-1","truckTypeId":12,"registrationNumber":"TN01AB1234","status":"active","isVerified":true,"isBlacklisted":false,"documents":{"rcCard":{"status":"verified"}}}}"""
        const val truckStatusFixture = """{"success":true,"message":"Truck active successfully","data":{"truckId":"truck-1","truckTypeId":12,"registrationNumber":"TN01AB1234","status":"active","isVerified":true,"isBlacklisted":false,"documents":{"rcCard":{"status":"verified"}}}}"""
        const val truckValidationFixture = """{"success":true,"data":{"exists":true,"truck":{"id":"truck-1","registrationNumber":"TN01AB1234","owner":{"id":"owner-user-1","profileId":"owner-profile-1","fullName":"ABC Transport","tagCode":"F24-1042"},"make":"Tata","model":"LPT","year":2022,"capacityMt":16.5,"status":"active"}}}"""
        const val truckActivationFailureFixture = """{"success":false,"message":"Complete all mandatory truck details and RC verification before activation","data":{"activationEligibility":{"eligible":false,"missingRequirements":[{"code":"RC_VERIFIED","label":"Verified Registration Certificate"}]}}}"""

        const val truckOwnerListFixture = """
            {"success":true,"page":1,"limit":30,"counts":{"total":3,"active":1,"inactive":2,"pending_docs":2},"data":[{"id":"owner-user-1","tagCode":"F24-1042","fullName":"Arun Kumar","email":"arun@example.com","phone":"+919876543210","businessName":"ABC Transport","gstNumber":"33ABCDE1234F1Z5","panNumber":"ABCDE1234F","status":"active","baseCity":{"id":1,"name":"Chennai"},"baseArea":null,"preferredRoutesCount":2,"trucksCount":1,"createdAt":"2026-10-01T10:00:00Z","updatedAt":"2026-10-02T10:00:00Z"}]}
        """
        const val truckOwnerSearchFixture = """{"success":true,"page":1,"limit":30,"count":1,"data":[{"id":"owner-user-1","tagCode":"F24-1042","fullName":"Arun Kumar","phone":"+919876543210","status":"active","preferredRoutesCount":2,"trucksCount":1}]}"""
        const val truckOwnerBasicSearchFixture = """{"success":true,"page":1,"limit":30,"count":1,"data":[{"id":"owner-profile-1","userId":"owner-user-1","profileId":"owner-profile-1","tagCode":"F24-1042","fullName":"Arun Kumar","phone":"+919876543210","businessName":"ABC Transport","status":"active"}]}"""
        const val truckOwnerDetailFixture = """
            {"success":true,"data":{"id":"owner-user-1","profileId":"owner-profile-1","tagCode":"F24-1042","fullName":"Arun Kumar","email":"arun@example.com","phone":"+919876543210","altPhones":[],"businessName":"ABC Transport","gstNumber":"33ABCDE1234F1Z5","status":"active","baseLocation":"Chennai, Tamil Nadu","panCard":"ABCDE1234F","aadharCard":"123412341234","isTdsApplicable":true,"trucksCount":1,"truckStats":{"total":1,"active":1,"inactive":0,"blacklisted":0},"documents":{"mandatory":[{"id":"doc-aadhaar","type":"Aadhaar Card","status":"verified","url":"s3://aadhaar"},{"id":"doc-pan","type":"PAN Card","status":"verified","url":"s3://pan"},{"id":"bank-1","type":"Cancelled Cheque","status":"verified","url":"s3://cheque"}],"optional":[]},"bank":{"id":"bank-1","accountHolderName":"Arun Kumar","accountNumber":"1234567890","ifscCode":"HDFC0001234","bankName":"HDFC","chequeImageUrl":"s3://cheque","verificationStatus":"verified"},"bankVerified":true,"documentsVerified":true,"activationEligibility":{"eligible":true,"missingRequirements":[]},"preferredRoutes":[],"profileVerificationStatus":"verified"}}
        """
        const val truckOwnerUpdateFixture = """{"success":true,"message":"Personal details updated successfully","data":{"id":"owner-user-1","profileId":"owner-profile-1","fullName":"Arun Kumar","status":"active","documents":{"mandatory":[{"type":"Aadhaar Card","status":"verified"},{"type":"PAN Card","status":"verified"},{"type":"Cancelled Cheque","status":"verified"}],"optional":[]}}}"""
        const val truckOwnerStatusFixture = """{"success":true,"message":"Trucker activated successfully","data":{"id":"owner-user-1","profileId":"owner-profile-1","fullName":"Arun Kumar","status":"active","documents":{"mandatory":[{"type":"Aadhaar Card","status":"verified"},{"type":"PAN Card","status":"verified"},{"type":"Cancelled Cheque","status":"verified"}],"optional":[]}}}"""
        const val truckOwnerValidationFixture = """{"success":true,"data":{"phone":{"exists":true,"trucker":{"id":"owner-user-1","profileId":"owner-profile-1","fullName":"Arun Kumar","status":"active","tagCode":"F24-1042"}},"pan":{"exists":false,"trucker":null}}}"""
        const val truckOwnerActivationFailureFixture = """{"success":false,"message":"Complete all mandatory truck owner details and document verification before activation","data":{"status":"inactive","activationEligibility":{"eligible":false,"missingRequirements":[{"field":"aadhar_document","message":"Aadhaar Card must be verified"}]}}}"""

        const val clientListFixture = """
            {"success":true,"page":1,"limit":30,"counts":{"total":1,"active":1,"inactive":0},"data":[{"id":"client-profile-1","userId":"client-user-1","clientType":"DIRECT_CUSTOMER","companyName":"Acme Industries","displayName":"Acme","companyEmail":"ops@acme.test","companyPhone":"+919900001111","panNumber":"ABCDE1234F","gstNumber":"33ABCDE1234F1Z5","companyType":"PRIVATE_LIMITED","registeredAddress":{"addressLine1":"1 Industrial Road","city":{"id":1,"name":"Chennai"},"state":{"id":1,"name":"Tamil Nadu"},"pinCode":"600001"},"billingSameAsRegistered":true,"billingAddress":null,"podSameAsRegistered":true,"podAddress":null,"primaryContact":{"id":"contact-1","fullName":"Priya","phone":"+919900002222"},"branchCount":1,"activeBranchCount":1,"inactiveBranchCount":0,"contactCount":1,"activeContactCount":1,"inactiveContactCount":0,"status":"active"}]}
        """
        const val clientSearchFixture = clientListFixture
        const val clientDetailFixture = """
            {"success":true,"data":{"id":"client-user-1","clientId":"client-profile-1","tagCode":"F24-2088","clientType":"DIRECT_CUSTOMER","company":{"legalName":"Acme Industries","displayName":"Acme","pan":"ABCDE1234F","gstNumber":"33ABCDE1234F1Z5","companyType":"PRIVATE_LIMITED","email":"ops@acme.test","phone":"+919900001111"},"registeredAddress":{"line1":"1 Industrial Road","city":{"id":1,"name":"Chennai"},"state":{"id":1,"name":"Tamil Nadu"},"pinCode":"600001"},"billingAddress":{"sameAsRegistered":true},"podAddress":{"sameAsRegistered":true},"contacts":[{"id":"contact-1","fullName":"Priya","phone":"+919900002222","isPrimary":true,"isActive":true,"clientRoles":[{"id":"role-1","code":"OPERATIONS","name":"Operations"}],"branchAssignments":[]}],"contactCount":1,"branches":[{"id":"branch-1","branchName":"Chennai","branchCode":"CHE","status":"active","address":{"line1":"2 Industrial Road","area":{"id":2,"name":"Guindy"},"city":{"id":1,"name":"Chennai"},"state":{"id":1,"name":"Tamil Nadu"},"pinCode":"600032"},"gst":{"gstin":"33ABCDE1234F1Z5","addressSameAsBranch":true},"contacts":[],"documents":[]}],"branchCount":1,"documents":[{"id":"client-doc-1","type":"PAN","url":"s3://pan","status":"verified"},{"id":null,"type":"GST_CERTIFICATE","url":null,"status":"not_available"}],"documentSummary":{"total":4,"uploaded":1,"pending":0,"verified":1,"rejected":0},"documentsVerified":true,"profileVerificationStatus":"verified","status":"active"}}
        """
        const val clientUpdateFixture = """{"success":true,"message":"Client updated successfully","data":{"id":"client-user-1","clientId":"client-profile-1","tagCode":"F24-2088","company":{"legalName":"Acme Industries"},"contacts":[{"id":"contact-1","fullName":"Priya"}],"branches":[{"id":"branch-1","branchName":"Chennai"}],"documents":[{"type":"PAN","status":"verified"}]}}"""
        const val clientStatusFixture = """{"success":true,"message":"Client active successfully","data":{"id":"client-user-1","clientId":"client-profile-1","tagCode":"F24-2088","company":{"legalName":"Acme Industries"},"contacts":[{"id":"contact-1","fullName":"Priya"}],"branches":[{"id":"branch-1","branchName":"Chennai"}],"documents":[{"type":"PAN","status":"verified"}],"status":"active"}}"""
        const val clientValidationFixture = """{"success":true,"data":{"company_phone":{"exists":false,"client":null},"gst_number":{"exists":true,"foundIn":"CLIENT_BRANCH","client":{"id":"client-profile-1","companyName":"Acme Industries","displayName":"Acme"},"branch":{"id":"branch-1","clientId":"client-profile-1","branchName":"Chennai","branchCode":"CHE","gstNumber":"33ABCDE1234F1Z5","status":"active"}}}}"""
    }
}
