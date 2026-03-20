package com.freight24.trucker.feature.profile.util

import com.freight24.trucker.feature.profile.domain.DocumentStatus
import com.freight24.trucker.feature.profile.domain.DocumentType

object ProfileDocumentTypeIds {
    const val AADHAAR = 1
    const val PAN = 2
    const val DRIVING_LICENSE = 3
    const val RC_BOOK = 4
    const val INSURANCE_CERTIFICATE = 5
    const val GST_CERTIFICATE = 6
    const val COMPANY_REGISTRATION = 7
    const val TDS_CERTIFICATE = 8
    const val CANCELLED_CHEQUE = 9

    val editableDocumentTypes: List<DocumentType> = listOf(
        DocumentType.AADHAAR,
        DocumentType.PAN,
        DocumentType.GST_CERTIFICATE,
        DocumentType.COMPANY_REGISTRATION,
        DocumentType.TDS_CERTIFICATE,
        DocumentType.CANCELLED_CHEQUE,
    )

    fun documentTypeId(type: DocumentType): Int? = when (type) {
        DocumentType.AADHAAR -> AADHAAR
        DocumentType.PAN -> PAN
        DocumentType.DRIVING_LICENSE -> DRIVING_LICENSE
        DocumentType.RC_BOOK -> RC_BOOK
        DocumentType.INSURANCE_CERTIFICATE -> INSURANCE_CERTIFICATE
        DocumentType.GST_CERTIFICATE -> GST_CERTIFICATE
        DocumentType.COMPANY_REGISTRATION -> COMPANY_REGISTRATION
        DocumentType.TDS_CERTIFICATE -> TDS_CERTIFICATE
        DocumentType.CANCELLED_CHEQUE -> CANCELLED_CHEQUE
        DocumentType.UNKNOWN -> null
    }

    fun fieldKey(type: DocumentType): String = when (type) {
        DocumentType.AADHAAR -> "aadhaar"
        DocumentType.PAN -> "pan"
        DocumentType.DRIVING_LICENSE -> "driving_license"
        DocumentType.RC_BOOK -> "rc_book"
        DocumentType.INSURANCE_CERTIFICATE -> "insurance_certificate"
        DocumentType.GST_CERTIFICATE -> "gst"
        DocumentType.COMPANY_REGISTRATION -> "company_registration"
        DocumentType.TDS_CERTIFICATE -> "tds"
        DocumentType.CANCELLED_CHEQUE -> "cancelled_cheque"
        DocumentType.UNKNOWN -> "unknown"
    }

    fun verifyStatus(status: DocumentStatus): String = when (status) {
        DocumentStatus.VERIFIED -> "verified"
        DocumentStatus.REJECTED -> "rejected"
        DocumentStatus.UPLOADED -> "pending"
        DocumentStatus.PENDING -> "pending"
        DocumentStatus.UNKNOWN -> "unknown"
    }
}
