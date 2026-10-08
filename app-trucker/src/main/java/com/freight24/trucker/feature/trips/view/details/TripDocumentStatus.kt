package com.freight24.trucker.feature.trips.view.details

import com.freight24.common.model.TripDocument

internal enum class PodBottomNotice {
    AWAITING_HARD_COPY,
    VERIFICATION_IN_PROGRESS,
    PAYMENT_PROCESSING,
}

internal fun List<TripDocument>.podBottomNotice(
    netPayable: Double?,
    totalPaidAmount: Double?,
): PodBottomNotice? {
    if (netPayable != null && totalPaidAmount != null && totalPaidAmount >= netPayable) return null

    val pods = filter { it.documentType.equals("pod", ignoreCase = true) }
    val hpod = pods.firstOrNull { it.podType.equals("hpod", ignoreCase = true) }
    return when {
        hpod?.verificationStatus.equals("pending", ignoreCase = true) -> PodBottomNotice.VERIFICATION_IN_PROGRESS
        hpod?.verificationStatus.equals("verified", ignoreCase = true) -> PodBottomNotice.PAYMENT_PROCESSING
        hpod == null && pods.any { it.podType.equals("epod", ignoreCase = true) } -> PodBottomNotice.AWAITING_HARD_COPY
        else -> null
    }
}
