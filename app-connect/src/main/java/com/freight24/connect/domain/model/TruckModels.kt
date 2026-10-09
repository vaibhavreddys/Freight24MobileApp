package com.freight24.connect.domain.model

data class TruckManagementItem(
    val truckId: String,
    val registrationNumber: String,
    val status: String,
    val isVerified: Boolean,
    val isBlacklisted: Boolean,
    val availability: String?,
    val owner: TruckOwnerReference?,
    val vehicle: VehicleDetails?,
    val truckType: String?,
    val bodyType: String?,
    val displayName: String?,
    val fitnessCertificate: DocumentExpiry?,
    val insurance: DocumentExpiry?,
)

data class TruckAssignmentOption(
    val truckId: String,
    val registrationNumber: String,
    val status: String,
    val isVerified: Boolean,
    val isBlacklisted: Boolean,
    val availability: String?,
    val ownerName: String?,
    val ownerPhone: String?,
    val ownerTagCode: String?,
    val truckType: String?,
    val bodyType: String?,
    val displayName: String?,
    val capacityMt: Double?,
    val ownerStatus: String? = null,
)

data class TruckDetails(
    val truckId: String,
    val truckTypeId: Int?,
    val registrationNumber: String,
    val status: String,
    val isVerified: Boolean,
    val isBlacklisted: Boolean,
    val activationEligibility: ActivationEligibility?,
    val availability: String?,
    val owner: TruckOwnerReference?,
    val vehicle: VehicleDetails?,
    val truckType: String?,
    val bodyType: String?,
    val displayName: String?,
    val documents: TruckDocuments?,
    val detached: TruckDetachment?,
    val blacklist: TruckBlacklist?,
)

data class TruckOwnerReference(
    val name: String?,
    val phone: String?,
    val tagCode: String?,
    /** 'active' | 'inactive' — the owner's account state on the partner book. */
    val status: String? = null,
)

data class VehicleDetails(
    val make: String?,
    val model: String?,
    val manufacturingYear: Int?,
    val capacityMt: Double?,
)

data class DocumentExpiry(
    val expiryDate: String?,
    val expiryStatus: String?,
)

data class TruckDocuments(
    val registrationCertificate: TruckDocument?,
    val fitnessCertificate: TruckDocument?,
    val insurance: TruckDocument?,
)

data class TruckDocument(
    val url: String?,
    val status: String?,
    val expiryDate: String?,
    val expiryStatus: String?,
)

data class TruckDetachment(
    val isDetached: Boolean,
    val reason: String?,
    val detachedAt: String?,
    val detachedBy: EntityActor?,
)

data class TruckBlacklist(
    val reason: String?,
    val blacklistedAt: String?,
    val blacklistedBy: EntityActor?,
)

data class EntityActor(
    val userId: String?,
    val tagCode: String?,
    val name: String?,
    val phone: String?,
)

data class TruckRegistrationValidation(
    val exists: Boolean,
    val truckId: String?,
)
