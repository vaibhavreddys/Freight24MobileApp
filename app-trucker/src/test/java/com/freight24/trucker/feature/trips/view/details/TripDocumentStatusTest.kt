package com.freight24.trucker.feature.trips.view.details

import com.freight24.common.model.TripDocument
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TripDocumentStatusTest {
    @Test
    fun epodWithoutHpodShowsNoticeRegardlessOfVerificationStatus() {
        listOf("pending", "verified", "rejected").forEach { status ->
            assertEquals(PodBottomNotice.AWAITING_HARD_COPY, notice(pod("epod", status)))
        }
    }

    @Test
    fun pendingHpodShowsVerificationNoticeWithOrWithoutEpod() {
        assertEquals(PodBottomNotice.VERIFICATION_IN_PROGRESS, notice(pod("hpod", "pending")))
        assertEquals(PodBottomNotice.VERIFICATION_IN_PROGRESS, notice(pod("epod"), pod("hpod", "pending")))
    }

    @Test
    fun verifiedHpodShowsPaymentNotice() {
        assertEquals(PodBottomNotice.PAYMENT_PROCESSING, notice(pod("hpod")))
    }

    @Test
    fun rejectedOrUnknownHpodDoesNotClaimVerificationOrPaymentProcessing() {
        listOf("rejected", "unknown").forEach { status ->
            assertNull(notice(pod("epod"), pod("hpod", status)))
        }
    }

    @Test
    fun noEpodHidesNotice() {
        assertNull(notice())
        assertNull(notice(pod(null)))
        assertNull(notice(pod("epod").copy(documentType = "lr")))
    }

    @Test
    fun typesAreCaseInsensitive() {
        assertEquals(PodBottomNotice.AWAITING_HARD_COPY, notice(pod("EPOD").copy(documentType = "POD")))
        assertEquals(PodBottomNotice.VERIFICATION_IN_PROGRESS, notice(pod("HPOD", "PENDING")))
        assertEquals(PodBottomNotice.PAYMENT_PROCESSING, notice(pod("HPOD", "VERIFIED")))
    }

    @Test
    fun fullyPaidOverpaidAndZeroPayableHideAllPodNotices() {
        listOf(pod("epod"), pod("hpod", "pending"), pod("hpod")).forEach { doc ->
            assertNull(listOf(doc).podBottomNotice(50000.0, 50000.0))
            assertNull(listOf(doc).podBottomNotice(50000.0, 51000.0))
            assertNull(listOf(doc).podBottomNotice(0.0, 0.0))
        }
    }

    @Test
    fun outstandingBalanceIncludingSmallAmountsKeepsPaymentNotice() {
        assertEquals(PodBottomNotice.PAYMENT_PROCESSING, listOf(pod("hpod")).podBottomNotice(50000.0, 49999.99))
    }

    @Test
    fun missingFinancialAmountsDoNotAssumePaymentIsSettled() {
        listOf(null to null, 50000.0 to null, null to 50000.0).forEach { (net, paid) ->
            assertEquals(PodBottomNotice.PAYMENT_PROCESSING, listOf(pod("hpod")).podBottomNotice(net, paid))
        }
    }

    private fun notice(vararg documents: TripDocument) = documents.toList().podBottomNotice(50000.0, 45000.0)

    private fun pod(type: String?, status: String = "verified") =
        TripDocument(id = "document", documentType = "pod", verificationStatus = status, podType = type)
}
