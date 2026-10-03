package com.freight24.rest.response

import kotlinx.serialization.Serializable

/**
 * `GET /api/v1/indents/trucker/load-board-summary` — the collapsed board: one
 * row per operational city with its most recent loads inlined.
 *
 * `data` is an object, not an array, and carries the city groups alongside the
 * board totals and the paging cursor.
 */
@Serializable
data class LoadBoardResponse(
    val success: Boolean,
    val data: LoadBoardData? = null,
) {

    @Serializable
    data class LoadBoardData (
        val groups: List<CityLoadSummary>?,
    )

    @Serializable
    data class LoadBoardData(
        val groups: List<CityGroup> = emptyList(),
        val summary: BoardSummary? = null,
        val pagination: BoardPagination? = null,
    )

    /** One operational city on the board, with its most recent loads inlined. */
    @Serializable
    data class CityGroup(
        val groupId: Int? = null,
        val groupName: String? = null,
        val groupType: String? = null,
        val totalLoads: Int? = null,
        val latestLoads: List<LoadBoardLoad> = emptyList(),
        val operationalCityId: Int? = null,
    )

    @Serializable
    data class BoardSummary(
        val totalLoads: Int = 0,
        val totalGroups: Int = 0,
        val fallbackCityGroups: Int = 0,
        val operationalCityGroups: Int = 0,
    )

    @Serializable
    data class BoardPagination(
        val page: Int = 0,
        val loadLimit: Int = 0,
        val groupLimit: Int = 0,
        val hasNextPage: Boolean = false,
        val totalGroups: Int = 0,
    )

    @Serializable
    data class Tat(
        val hours: Int?,
        val enabled: Boolean?,
    )

    @Serializable
    data class TruckType(
        val id: Int,
        val name: String,
        val bodyType: String,
        val displayName: String,
    )


    @Serializable
    data class Loading(
        val area: String?,
        val city: String?,
        val address: String?,
        val contactPhone: String?,
        val contactPerson: String?,
    )

    @Serializable
    data class Employee(
        val name: String?,
        val phone: String?
    )

    @Serializable
    data class UnLoading(
        val area: String?,
        val city: String?,
    )


}
