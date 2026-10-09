package com.freight24.trucker.feature.profile.mapper

import com.freight24.rest.response.BankAccountDetailsDto
import com.freight24.rest.response.PreferredRouteDto
import com.freight24.rest.response.ProfileDocumentDto
import com.freight24.rest.response.ProfileDto
import com.freight24.trucker.feature.profile.domain.BankAccountDetails
import com.freight24.trucker.feature.profile.domain.DocumentStatus
import com.freight24.trucker.feature.profile.domain.DocumentType
import com.freight24.trucker.feature.profile.domain.PreferredRoute
import com.freight24.trucker.feature.profile.domain.Profile
import com.freight24.trucker.feature.profile.domain.ProfileDocument
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive

fun ProfileDto.toDomain(): Profile =
    Profile(
        id = id,
        userId = userId,
        baseCityId = baseCityId,
        baseCityName = baseCityName,
        baseCityState = baseCityState,
        baseLocation = baseLocation,
        fullName = fullName,
        emailId = emailId,
        phoneNo = phoneNo,
        alternativePhoneNo = normalizeAlternativePhoneNumbers(alternativePhoneNo.orEmpty()),
        // Map both panCard and panNumber to domain's panNumber
        panNumber = panCard ?: panNumber,
        // Map both aadharCard and aadhaarNumber to domain's aadhaarNumber
        aadhaarNumber = aadharCard ?: aadhaarNumber,
        isTdsApplicable = isTdsApplicable ?: false,
        tagCode = tagCode,
        documents = documents?.map { it.toDomain() }.orEmpty(),
        bankAccountDetails = bankAccountDetails?.toDomain(),
        businessName = businessName,
        gstNumber = gstNumber,
        bankAccountVerificationStatus = bankAccountVerificationStatus ?: false,
        documentsVerificationStatus = documentsVerificationStatus ?: false,
        profileVerificationStatus = profileVerificationStatus,
        status = status,
        preferredRoutes = preferredRoutes?.mapNotNull { it.toDomain() }.orEmpty(),
    )

internal fun normalizeAlternativePhoneNumbers(values: List<String>): List<String> {
    fun decode(value: String): List<String>? = runCatching {
        when (val parsed = Json.parseToJsonElement(value)) {
            is JsonArray -> parsed.mapNotNull { (it as? JsonPrimitive)?.takeIf { phone -> phone.isString }?.content }
            is JsonPrimitive -> if (parsed.isString) listOf(parsed.content) else null
            else -> null
        }
    }.getOrNull()

    // Some responses split a JSON-encoded phone array at its commas. Joining
    // those fragments restores the array so it can be decoded structurally.
    val joined = values.joinToString(",").trim()
    val decoded = if (joined.startsWith("[")) decode(joined) else null
    return (decoded ?: values.flatMap { decode(it) ?: listOf(it) })
        .map { it.trim() }
        .filter { it.isNotEmpty() }
}

fun BankAccountDetailsDto.toDomain(): BankAccountDetails =
    BankAccountDetails(
        id = id,
        accountHolderName = accountHolderName,
        accountNumber = accountNumber,
        ifscCode = ifscCode,
        upiId = upiId,
        bankName = bankName,
        branchName = branchName,
        chequeImageUrl = chequeImageUrl,
        verificationStatus = verificationStatus,
    )

fun ProfileDocumentDto.toDomain(): ProfileDocument =
    ProfileDocument(
        docId = documentId,
        type = documentType?.toDocumentType() ?: DocumentType.UNKNOWN,
        url = url,
        status =
            when (status?.uppercase()) {
                "VERIFIED" -> DocumentStatus.VERIFIED
                "REJECTED" -> DocumentStatus.REJECTED
                "UPLOADED" -> DocumentStatus.UPLOADED
                else -> if (!url.isNullOrEmpty()) DocumentStatus.UPLOADED else DocumentStatus.PENDING
            },
    )

private fun String.toDocumentType(): DocumentType =
    when {
        this.contains("AADHAAR", ignoreCase = true) -> DocumentType.AADHAAR
        this.contains("PAN", ignoreCase = true) -> DocumentType.PAN
        this.contains("DL", ignoreCase = true) || this.contains("DRIVING", ignoreCase = true) -> DocumentType.DRIVING_LICENSE
        this.contains("RC", ignoreCase = true) -> DocumentType.RC_BOOK
        this.contains("INSURANCE", ignoreCase = true) -> DocumentType.INSURANCE_CERTIFICATE
        this.contains("GST", ignoreCase = true) -> DocumentType.GST_CERTIFICATE
        this.contains("COMPANY", ignoreCase = true) -> DocumentType.COMPANY_REGISTRATION
        this.contains("TDS", ignoreCase = true) -> DocumentType.TDS_CERTIFICATE
        this.contains("CANCELLED", ignoreCase = true) || this.contains("CHEQUE", ignoreCase = true) -> DocumentType.CANCELLED_CHEQUE
        else -> DocumentType.UNKNOWN
    }

private fun PreferredRouteDto.toDomain(): PreferredRoute? {
    val fromCity = sourceCity ?: return null
    val toCity = destinationCity ?: return null
    return PreferredRoute(
        id = id,
        priority = priority,
        sourceCityId = fromCity.id,
        sourceCityName = fromCity.name,
        destCityId = toCity.id,
        destCityName = toCity.name,
    )
}
