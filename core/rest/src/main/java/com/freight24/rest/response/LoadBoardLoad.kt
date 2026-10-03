package com.freight24.rest.response

import kotlinx.serialization.Serializable

/**
 * A single load on the trucker load board.
 *
 * `load-board-summary` (inlined under each city group) and `load-board-paginated`
 * (the expanded-city feed) return the identical camelCase projection, so both
 * responses share this type. Earlier revisions of these DTOs were snake_case and
 * flat; keep the two responses pointed at this class rather than forking it.
 */
@Serializable
data class LoadBoardLoad(
    val indentId: String? = null,
    val indentCode: String? = null,
    val indentGroupId: String? = null,
    val masterIndentId: String? = null,
    val isMaster: Boolean? = null,
    val material: String? = null,
    val priority: String? = null,
    val weight: Double? = null,
    /** Null when either indent hides cost info — see [isPriceHidden]. */
    val price: Double? = null,
    val isPriceHidden: Boolean? = null,
    val groupType: String? = null,
    /** Only present on the paginated feed; summary rows sit under their group. */
    val operationalCity: String? = null,
    val operationalCityId: Int? = null,
    val truckType: TruckType? = null,
    val tat: Tat? = null,
    val loading: Stop? = null,
    val unloading: Stop? = null,
    val employee: Employee? = null,
    val createdAt: String,
    val updatedAt: String? = null,
    val expiresAt: String? = null,
    val isInterested: Boolean? = null,
    val interestStatus: String? = null,
) {
    @Serializable
    data class TruckType(
        val id: Int? = null,
        val name: String? = null,
        val bodyType: String? = null,
        val displayName: String? = null,
    )

    @Serializable
    data class Tat(
        val hours: Int? = null,
        val enabled: Boolean? = null,
    )

    /**
     * A loading or unloading stop. The RPC only populates `address`,
     * `contactPhone` and `contactPerson` on the loading side, so every field
     * beyond `area` / `city` is optional.
     */
    @Serializable
    data class Stop(
        val area: String? = null,
        val city: String? = null,
        val address: String? = null,
        val contactPhone: String? = null,
        val contactPerson: String? = null,
    )

    /** The fleet executive assigned to the indent. */
    @Serializable
    data class Employee(
        val name: String? = null,
        val phone: String? = null,
    )
}
