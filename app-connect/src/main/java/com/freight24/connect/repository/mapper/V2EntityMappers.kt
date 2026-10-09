package com.freight24.connect.repository.mapper

import com.freight24.connect.domain.model.ActivationEligibility
import com.freight24.connect.domain.model.ClientAddress
import com.freight24.connect.domain.model.ClientBranch
import com.freight24.connect.domain.model.ClientBranchAssignment
import com.freight24.connect.domain.model.ClientCompany
import com.freight24.connect.domain.model.ClientContact
import com.freight24.connect.domain.model.ClientContactRole
import com.freight24.connect.domain.model.ClientCounts
import com.freight24.connect.domain.model.ClientDetails
import com.freight24.connect.domain.model.ClientDocumentSummary
import com.freight24.connect.domain.model.ClientManagementItem
import com.freight24.connect.domain.model.ClientPickerOption
import com.freight24.connect.domain.model.ClientPrimaryContact
import com.freight24.connect.domain.model.DocumentExpiry
import com.freight24.connect.domain.model.EntityPage
import com.freight24.connect.domain.model.EntityActor
import com.freight24.connect.domain.model.MissingRequirement
import com.freight24.connect.domain.model.NamedLocation
import com.freight24.connect.domain.model.TruckBlacklist
import com.freight24.connect.domain.model.TruckAssignmentOption
import com.freight24.connect.domain.model.TruckDetails
import com.freight24.connect.domain.model.TruckDetachment
import com.freight24.connect.domain.model.TruckDocument
import com.freight24.connect.domain.model.TruckDocuments
import com.freight24.connect.domain.model.TruckManagementItem
import com.freight24.connect.domain.model.TruckOwnerBankAccount
import com.freight24.connect.domain.model.TruckOwnerCounts
import com.freight24.connect.domain.model.TruckOwnerDetails
import com.freight24.connect.domain.model.TruckOwnerFieldValidation
import com.freight24.connect.domain.model.TruckOwnerManagementItem
import com.freight24.connect.domain.model.TruckOwnerPickerOption
import com.freight24.connect.domain.model.TruckOwnerPreferredRoute
import com.freight24.connect.domain.model.TruckOwnerReference
import com.freight24.connect.domain.model.TruckOwnerTruckStats
import com.freight24.connect.domain.model.TruckOwnerValidation
import com.freight24.connect.domain.model.TruckCounts
import com.freight24.connect.domain.model.VehicleDetails
import com.freight24.connect.domain.model.VerificationDocument
import com.freight24.connect.domain.model.calculateEndOfList
import com.freight24.rest.response.ClientV2AddressDto
import com.freight24.rest.response.ClientV2DetailBranchDto
import com.freight24.rest.response.ClientV2DetailContactDto
import com.freight24.rest.response.ClientV2DetailDto
import com.freight24.rest.response.ClientV2DocumentDto
import com.freight24.rest.response.ClientV2RoleDto
import com.freight24.rest.response.ClientV2SummaryDto
import com.freight24.rest.response.ClientV2PageResponse
import com.freight24.rest.response.TruckOwnerV2DetailDto
import com.freight24.rest.response.TruckOwnerV2DocumentDto
import com.freight24.rest.response.TruckOwnerV2FieldValidationDto
import com.freight24.rest.response.TruckOwnerV2SummaryDto
import com.freight24.rest.response.TruckOwnerV2PageResponse
import com.freight24.rest.response.TruckOwnerV2ValidationData
import com.freight24.rest.response.TruckV2DetailDto
import com.freight24.rest.response.TruckV2DocumentDto
import com.freight24.rest.response.TruckV2SummaryDto
import com.freight24.rest.response.TruckV2PageResponse
import com.freight24.rest.response.V2ActivationEligibilityDto
import com.freight24.rest.response.V2ActorDto
import com.freight24.rest.response.V2NamedIdDto

fun TruckV2PageResponse.toManagementPage(): EntityPage<TruckManagementItem, TruckCounts> {
    val domainItems = data.map { it.toManagementItem() }
    val domainCounts =
        TruckCounts(
            total = counts.total,
            active = counts.active,
            inactive = counts.inactive,
            blacklisted = counts.blacklisted,
            pendingDocuments = counts.pendingDocs,
        )

    return EntityPage(
        page = page,
        limit = limit,
        counts = domainCounts,
        items = domainItems,
        endOfList = calculateEndOfList(page, limit, domainCounts.total, domainItems.size),
    )
}

fun TruckV2PageResponse.toAssignmentPage(): EntityPage<TruckAssignmentOption, TruckCounts> {
    val domainItems = data.map { it.toAssignmentOption() }
    val domainCounts =
        TruckCounts(
            total = counts.total,
            active = counts.active,
            inactive = counts.inactive,
            blacklisted = counts.blacklisted,
            pendingDocuments = counts.pendingDocs,
        )

    return EntityPage(
        page = page,
        limit = limit,
        counts = domainCounts,
        items = domainItems,
        endOfList = calculateEndOfList(page, limit, domainCounts.total, domainItems.size),
    )
}

fun TruckOwnerV2PageResponse.toManagementPage(): EntityPage<TruckOwnerManagementItem, TruckOwnerCounts> {
    val domainItems = data.map { it.toManagementItem() }
    val domainCounts =
        TruckOwnerCounts(
            total = counts?.total ?: count ?: domainItems.size,
            active = counts?.active,
            inactive = counts?.inactive,
            pendingDocuments = counts?.pendingDocs,
        )

    return EntityPage(
        page = page,
        limit = limit,
        counts = domainCounts,
        items = domainItems,
        endOfList = calculateEndOfList(page, limit, domainCounts.total, domainItems.size),
    )
}

fun TruckOwnerV2PageResponse.toPickerPage(): EntityPage<TruckOwnerPickerOption, TruckOwnerCounts> {
    val domainItems = data.map { it.toPickerOption() }
    val domainCounts =
        TruckOwnerCounts(
            total = counts?.total ?: count ?: domainItems.size,
            active = counts?.active,
            inactive = counts?.inactive,
            pendingDocuments = counts?.pendingDocs,
        )

    return EntityPage(
        page = page,
        limit = limit,
        counts = domainCounts,
        items = domainItems,
        endOfList = calculateEndOfList(page, limit, domainCounts.total, domainItems.size),
    )
}

fun ClientV2PageResponse.toManagementPage(): EntityPage<ClientManagementItem, ClientCounts> {
    val domainItems = data.map { it.toManagementItem() }
    val domainCounts =
        ClientCounts(
            total = counts.total,
            active = counts.active,
            inactive = counts.inactive,
        )

    return EntityPage(
        page = page,
        limit = limit,
        counts = domainCounts,
        items = domainItems,
        endOfList = calculateEndOfList(page, limit, domainCounts.total, domainItems.size),
    )
}

fun ClientV2PageResponse.toPickerPage(): EntityPage<ClientPickerOption, ClientCounts> {
    val domainItems = data.map { it.toPickerOption() }
    val domainCounts =
        ClientCounts(
            total = counts.total,
            active = counts.active,
            inactive = counts.inactive,
        )

    return EntityPage(
        page = page,
        limit = limit,
        counts = domainCounts,
        items = domainItems,
        endOfList = calculateEndOfList(page, limit, domainCounts.total, domainItems.size),
    )
}

fun TruckV2SummaryDto.toManagementItem(): TruckManagementItem =
    TruckManagementItem(
        truckId = truckId,
        registrationNumber = registrationNumber,
        status = status,
        isVerified = isVerified,
        isBlacklisted = isBlacklisted,
        availability = availability,
        owner = owner?.let {
            TruckOwnerReference(name = it.name, phone = it.phone, tagCode = it.tagCode, status = it.status)
        },
        vehicle = vehicle?.let {
            VehicleDetails(
                make = it.make,
                model = it.model,
                manufacturingYear = it.year,
                capacityMt = it.capacityMt,
            )
        },
        truckType = truckType,
        bodyType = bodyType,
        displayName = displayName,
        fitnessCertificate = fitnessCertificate?.let {
            DocumentExpiry(expiryDate = it.expiryDate, expiryStatus = it.expiryStatus)
        },
        insurance = insurance?.let {
            DocumentExpiry(expiryDate = it.expiryDate, expiryStatus = it.expiryStatus)
        },
    )

fun TruckV2SummaryDto.toAssignmentOption(): TruckAssignmentOption =
    TruckAssignmentOption(
        truckId = truckId,
        registrationNumber = registrationNumber,
        status = status,
        isVerified = isVerified,
        isBlacklisted = isBlacklisted,
        availability = availability,
        ownerName = owner?.name,
        ownerPhone = owner?.phone,
        ownerTagCode = owner?.tagCode,
        ownerStatus = owner?.status,
        truckType = truckType,
        bodyType = bodyType,
        displayName = displayName,
        capacityMt = vehicle?.capacityMt,
    )

fun TruckV2DetailDto.toDomain(): TruckDetails =
    TruckDetails(
        truckId = truckId,
        truckTypeId = truckTypeId,
        registrationNumber = registrationNumber,
        status = status,
        isVerified = isVerified,
        isBlacklisted = isBlacklisted,
        activationEligibility = activationEligibility?.toDomain(),
        availability = availability,
        owner = owner?.let {
            TruckOwnerReference(name = it.name, phone = it.phone, tagCode = it.tagCode, status = it.status)
        },
        vehicle = vehicle?.let {
            VehicleDetails(
                make = it.make,
                model = it.model,
                manufacturingYear = it.year,
                capacityMt = it.capacityMt,
            )
        },
        truckType = truckType,
        bodyType = bodyType,
        displayName = displayName,
        documents = documents?.let {
            TruckDocuments(
                registrationCertificate = it.rcCard?.toDomain(),
                fitnessCertificate = it.fcCertificate?.toDomain(),
                insurance = it.insurance?.toDomain(),
            )
        },
        detached = detached?.let {
            TruckDetachment(
                isDetached = it.isDetached,
                reason = it.reason,
                detachedAt = it.detachedAt,
                detachedBy = it.detachedBy?.toDomain(),
            )
        },
        blacklist = blacklist?.let {
            TruckBlacklist(
                reason = it.reason,
                blacklistedAt = it.blacklistedAt,
                blacklistedBy = it.blacklistedBy?.toDomain(),
            )
        },
    )

fun TruckOwnerV2SummaryDto.toManagementItem(): TruckOwnerManagementItem =
    TruckOwnerManagementItem(
        ownerUserId = userId ?: id,
        ownerProfileId = profileId,
        tagCode = tagCode,
        fullName = fullName,
        email = email,
        phone = phone,
        businessName = businessName,
        gstNumber = gstNumber,
        panNumber = panNumber,
        status = status,
        baseCity = baseCity?.toDomain(),
        baseArea = baseArea?.toDomain(),
        preferredRoutesCount = preferredRoutesCount,
        trucksCount = trucksCount,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

fun TruckOwnerV2SummaryDto.toPickerOption(): TruckOwnerPickerOption =
    TruckOwnerPickerOption(
        ownerUserId = userId ?: id,
        ownerProfileId = profileId ?: id,
        tagCode = tagCode,
        fullName = fullName,
        phone = phone,
        businessName = businessName,
        status = status,
    )

fun TruckOwnerV2DetailDto.toDomain(): TruckOwnerDetails {
    val stats = truckStats

    return TruckOwnerDetails(
        ownerUserId = id,
        ownerProfileId = profileId,
        tagCode = tagCode,
        fullName = fullName,
        email = email,
        phone = phone,
        alternatePhones = altPhones,
        businessName = businessName,
        gstNumber = gstNumber,
        status = status,
        baseLocation = baseLocation,
        baseCity = baseCity?.toDomain(),
        baseArea = baseArea?.toDomain(),
        baseCityState = baseCityState,
        panNumber = panCard,
        aadhaarNumber = aadharCard,
        isTdsApplicable = isTdsApplicable,
        truckStats = TruckOwnerTruckStats(
            total = stats?.total ?: trucksCount,
            active = stats?.active ?: 0,
            inactive = stats?.inactive ?: 0,
            blacklisted = stats?.blacklisted ?: 0,
        ),
        mandatoryDocuments = documents?.mandatory.orEmpty().map { it.toDomain() },
        optionalDocuments = documents?.optional.orEmpty().map { it.toDomain() },
        bankAccount = bank?.let {
            TruckOwnerBankAccount(
                bankAccountId = it.id,
                accountHolderName = it.accountHolderName,
                accountNumber = it.accountNumber,
                ifscCode = it.ifscCode,
                upiId = it.upiId,
                bankName = it.bankName,
                branchName = it.branchName,
                cancelledChequeUrl = it.chequeImageUrl,
                verificationStatus = it.verificationStatus,
            )
        },
        bankVerified = bankVerified,
        documentsVerified = documentsVerified,
        activationEligibility = activationEligibility?.toDomain(),
        preferredRoutes = preferredRoutes.map {
            TruckOwnerPreferredRoute(
                routeId = it.id,
                priority = it.priority,
                source = it.source,
                sourceCity = it.sourceCity?.toDomain(),
                destinationCity = it.destinationCity?.toDomain(),
            )
        },
        profileVerificationStatus = profileVerificationStatus,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

fun TruckOwnerV2ValidationData.toDomain(): TruckOwnerValidation =
    TruckOwnerValidation(
        phone = phone?.toDomain(),
        pan = pan?.toDomain(),
        aadhaar = aadhar?.toDomain(),
        gst = gst?.toDomain(),
    )

private fun TruckOwnerV2FieldValidationDto.toDomain(): TruckOwnerFieldValidation =
    TruckOwnerFieldValidation(
        exists = exists,
        ownerUserId = trucker?.id,
        ownerProfileId = trucker?.profileId,
        fullName = trucker?.fullName,
        status = trucker?.status,
        tagCode = trucker?.tagCode,
    )

fun ClientV2SummaryDto.toManagementItem(): ClientManagementItem =
    ClientManagementItem(
        clientId = id,
        clientUserId = userId,
        clientType = clientType,
        companyName = companyName,
        displayName = displayName,
        companyEmail = companyEmail,
        companyPhone = companyPhone,
        companyAlternatePhone = companyAltPhone,
        panNumber = panNumber,
        gstNumber = gstNumber,
        companyType = companyType,
        website = website,
        registeredAddress = registeredAddress?.toDomain(),
        primaryContact = primaryContact?.let {
            ClientPrimaryContact(
                contactId = it.id,
                fullName = it.fullName,
                designation = it.designation,
                phone = it.phone,
                alternatePhone = it.alternatePhone,
                email = it.email,
            )
        },
        branchCount = branchCount,
        activeBranchCount = activeBranchCount,
        inactiveBranchCount = inactiveBranchCount,
        contactCount = contactCount,
        activeContactCount = activeContactCount,
        inactiveContactCount = inactiveContactCount,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

fun ClientV2SummaryDto.toPickerOption(): ClientPickerOption =
    ClientPickerOption(
        clientId = id,
        clientUserId = userId,
        tagCode = tagCode,
        companyName = companyName,
        displayName = displayName,
        companyPhone = companyPhone,
        status = status,
        primaryContactName = primaryContact?.fullName,
        primaryContactPhone = primaryContact?.phone,
    )

fun ClientV2DetailDto.toDomain(): ClientDetails =
    ClientDetails(
        clientId = clientId,
        clientUserId = id,
        tagCode = tagCode,
        clientType = clientType,
        company = ClientCompany(
            legalName = company.legalName,
            displayName = company.displayName,
            panNumber = company.pan,
            gstNumber = company.gstNumber,
            companyType = company.companyType,
            website = company.website,
            email = company.email,
            phone = company.phone,
            alternatePhone = company.alternatePhone,
        ),
        registeredAddress = registeredAddress?.toDomain(),
        billingAddress = billingAddress?.toDomain(),
        podAddress = podAddress?.toDomain(),
        contacts = contacts.map { it.toDomain() },
        branches = branches.map { it.toDomain() },
        documents = documents.map { it.toDomain() },
        documentSummary = documentSummary?.let {
            ClientDocumentSummary(
                total = it.total,
                uploaded = it.uploaded,
                pending = it.pending,
                verified = it.verified,
                rejected = it.rejected,
            )
        },
        documentsVerified = documentsVerified,
        profileVerificationStatus = profileVerificationStatus,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

private fun TruckV2DocumentDto.toDomain() =
    TruckDocument(
        url = url,
        status = status,
        expiryDate = expiryDate,
        expiryStatus = expiryStatus,
    )

private fun V2ActorDto.toDomain() =
    EntityActor(userId = id, tagCode = tagCode, name = name, phone = phone)

private fun V2ActivationEligibilityDto.toDomain() =
    ActivationEligibility(
        eligible = eligible,
        missingRequirements = missingRequirements.map {
            MissingRequirement(
                field = it.field,
                code = it.code,
                label = it.label,
                message = it.message,
            )
        },
    )

private fun V2NamedIdDto.toDomain() = NamedLocation(id = id, name = name)

private fun TruckOwnerV2DocumentDto.toDomain() =
    VerificationDocument(documentId = id, type = type, status = status, url = url)

private fun ClientV2AddressDto.toDomain() =
    ClientAddress(
        line1 = line1 ?: addressLine1,
        line2 = line2 ?: addressLine2,
        area = area?.toDomain(),
        city = city?.toDomain(),
        state = state?.toDomain(),
        pinCode = pinCode,
        sameAsRegistered = sameAsRegistered,
    )

private fun ClientV2RoleDto.toDomain() =
    ClientContactRole(
        roleId = id,
        code = code,
        name = name,
        description = description,
        applicableTo = applicableTo,
    )

private fun ClientV2DetailContactDto.toDomain() =
    ClientContact(
        contactId = id,
        fullName = fullName,
        designation = designation,
        phone = phone,
        alternatePhone = alternatePhone,
        email = email,
        isPrimary = isPrimary,
        isActive = isActive,
        clientRoles = clientRoles.map { it.toDomain() },
        branchAssignments = branchAssignments.map {
            ClientBranchAssignment(
                branchId = it.branchId,
                role = it.role?.toDomain(),
            )
        },
    )

private fun ClientV2DetailBranchDto.toDomain() =
    ClientBranch(
        branchId = id,
        branchName = branchName,
        branchCode = branchCode,
        branchTypeId = branchType?.id,
        branchTypeCode = branchType?.code,
        branchTypeName = branchType?.name,
        status = status,
        address = address?.toDomain(),
        billingAddress = billingAddress?.toDomain(),
        gst = gst?.let {
            com.freight24.connect.domain.model.ClientBranchGst(
                gstin = it.gstin,
                legalName = it.legalName,
                registrationStatus = it.registrationStatus,
                address = it.address?.toDomain(),
            )
        },
        podAddress = pod?.address?.toDomain(),
        contacts = contacts.map { it.toDomain() },
        documents = documents.map { it.toDomain() },
    )

private fun com.freight24.rest.response.ClientV2BranchAddressDto.toDomain() =
    ClientAddress(
        line1 = line1,
        line2 = line2,
        area = area?.toDomain(),
        city = city?.toDomain(),
        state = state?.toDomain(),
        pinCode = pinCode,
        sameAsRegistered = null,
    )

private fun com.freight24.rest.response.ClientV2BranchBillingAddressDto.toDomain() =
    ClientAddress(
        line1 = line1,
        line2 = line2,
        area = area?.toDomain(),
        city = city?.toDomain(),
        state = state?.toDomain(),
        pinCode = pinCode,
        sameAsRegistered = sameAsBranch,
    )

private fun ClientV2DocumentDto.toDomain() =
    VerificationDocument(
        documentId = id,
        type = type,
        status = status,
        url = url,
        verifiedBy = verifiedBy,
        verifiedAt = verifiedAt,
        rejectionReason = rejectionReason,
    )
