package com.freight24.rest.response

import kotlinx.serialization.Serializable

@Serializable
data class DashboardDataResponse(
    val data : DashboardData
): BaseResponse() {
    @Serializable
    data class DashboardData(
        val activeTrips: ActiveTrips,
        val indentPulse: IndentPulse,
        val pendingActions: PendingActions,
        val attentionNeeded: AttentionNeeded
    )
    @Serializable
    data class ActiveTrips(
        val total: Int,
        val loading: Int,
        val assigned: Int,
        val inTransit: Int,
    )
    @Serializable
    data class IndentPulse(
        val open: Int,
        val total: Int,
        val closed: Int,
        val readyForAssignment: Int,
    )
    @Serializable
    data class PendingActions(
        val lrReviews: Int,
        val podFollowUp: Int,
    )

    @Serializable
    data class AttentionNeeded(
        val waitingForLR: Int,
        val truckerInterests: Int,
    )
}