package com.freight24.trucker.feature.profile.mapper

import com.freight24.rest.response.BankAccountDetailsDto
import com.freight24.rest.response.ProfileDocumentDto
import com.freight24.rest.response.ProfileDto
import com.freight24.trucker.feature.profile.domain.BankAccountDetails
import com.freight24.trucker.feature.profile.domain.DocumentStatus
import com.freight24.trucker.feature.profile.domain.DocumentType
import com.freight24.trucker.feature.profile.domain.Profile
import com.freight24.trucker.feature.profile.domain.ProfileDocument

fun ProfileDto.toDomain(): Profile =
    Profile(
        id = id,
        userId = userId,
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
        type = type?.toDocumentType() ?: DocumentType.UNKNOWN,
        url = url,
        status = if (!url.isNullOrEmpty()) DocumentStatus.UPLOADED else DocumentStatus.PENDING,
    )

private fun String.toDocumentType(): DocumentType =
    when (this.uppercase()) {
        "PAN" -> DocumentType.PAN
        "AADHAAR" -> DocumentType.AADHAAR
        "GST_CERTIFICATE", "GST" -> DocumentType.GST_CERTIFICATE
        "TDS_CERTIFICATE", "TDS" -> DocumentType.TDS_CERTIFICATE
        "CANCELLED_CHEQUE" -> DocumentType.CANCELLED_CHEQUE
        else -> DocumentType.UNKNOWN
    }
