package com.freight24.connect.feature.truckowners.profile.mapper

import com.freight24.connect.feature.truckowners.profile.domain.BankAccountDetails
import com.freight24.connect.feature.truckowners.profile.domain.DocumentStatus
import com.freight24.connect.feature.truckowners.profile.domain.DocumentType
import com.freight24.connect.feature.truckowners.profile.domain.PreferredRoute
import com.freight24.connect.feature.truckowners.profile.domain.Profile
import com.freight24.connect.feature.truckowners.profile.domain.ProfileDocument
import com.freight24.connect.domain.model.TruckOwnerDetails
import com.freight24.connect.domain.model.VerificationDocument
import com.freight24.rest.response.BankAccountDetailsDto
import com.freight24.rest.response.PreferredRouteDto
import com.freight24.rest.response.ProfileDocumentDto
import com.freight24.rest.response.ProfileDto

fun ProfileDto.toDomain(): Profile =
    Profile(
        id = id,
        userId = userId,
        status = status,
        baseCityId = baseCityId,
        baseCityName = baseCityName,
        baseCityState = baseCityState,
        baseAreaId = null,
        baseAreaName = null,
        baseLocation = baseLocation,
        fullName = fullName,
        emailId = emailId,
        phoneNo = phoneNo,
        alternativePhoneNo = alternativePhoneNo.orEmpty(),
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
        preferredRoutes = preferredRoutes?.mapNotNull { it.toDomain() }.orEmpty(),
    )

fun TruckOwnerDetails.toProfile(): Profile =
    Profile(
        id = ownerProfileId,
        userId = ownerUserId,
        status = status,
        baseCityId = baseCity?.id,
        baseCityName = baseCity?.name,
        baseCityState = baseCityState,
        baseAreaId = baseArea?.id,
        baseAreaName = baseArea?.name,
        baseLocation = baseLocation,
        fullName = fullName,
        emailId = email,
        phoneNo = phone,
        alternativePhoneNo = alternatePhones,
        panNumber = panNumber,
        aadhaarNumber = aadhaarNumber,
        isTdsApplicable = isTdsApplicable ?: false,
        tagCode = tagCode,
        documents = (mandatoryDocuments + optionalDocuments).map { it.toProfileDocument() },
        bankAccountDetails =
            bankAccount?.let {
                BankAccountDetails(
                    accountHolderName = it.accountHolderName,
                    accountNumber = it.accountNumber,
                    ifscCode = it.ifscCode,
                    upiId = it.upiId,
                    bankName = it.bankName,
                    branchName = it.branchName,
                )
            },
        businessName = businessName,
        gstNumber = gstNumber,
        bankAccountVerificationStatus = bankVerified,
        documentsVerificationStatus = documentsVerified,
        profileVerificationStatus = profileVerificationStatus,
        preferredRoutes =
            preferredRoutes.mapNotNull { route ->
                val source = route.sourceCity ?: return@mapNotNull null
                val destination = route.destinationCity ?: return@mapNotNull null
                PreferredRoute(
                    id = route.routeId,
                    priority = route.priority ?: 3,
                    sourceCityId = source.id,
                    sourceCityName = source.name,
                    destCityId = destination.id,
                    destCityName = destination.name,
                )
            },
    )

private fun VerificationDocument.toProfileDocument(): ProfileDocument =
    ProfileDocument(
        docId = documentId,
        type = type.toDocumentType(),
        url = url,
        status =
            when (status.uppercase()) {
                "VERIFIED" -> DocumentStatus.VERIFIED
                "REJECTED" -> DocumentStatus.REJECTED
                "UPLOADED", "PENDING" -> if (url.isNullOrBlank()) DocumentStatus.PENDING else DocumentStatus.UPLOADED
                else -> if (url.isNullOrBlank()) DocumentStatus.PENDING else DocumentStatus.UPLOADED
            },
    )

fun BankAccountDetailsDto.toDomain(): BankAccountDetails =
    BankAccountDetails(
        accountHolderName = accountHolderName,
        accountNumber = accountNumber,
        ifscCode = ifscCode,
        upiId = upiId,
        bankName = bankName,
        branchName = branchName,
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
