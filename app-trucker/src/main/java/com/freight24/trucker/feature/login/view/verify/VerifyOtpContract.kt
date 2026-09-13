package com.freight24.trucker.feature.login.view.verify

import com.freight24.common.util.formatCountdownClock
import com.freight24.common.util.formatCountdownClockPadded
import com.freight24.common.util.formatIndianMobileNumber

/**
 * The contract between the OTP feature's logic and its UI.
 *
 * All derived labels (resend text, expiry text, formatted phone) are
 * precomputed here so the composable never has to do string work or
 * conditional logic — it just renders what it is handed.
 *
 * Consequence: a visual redesign only touches `VerifyOtpContent.kt`. The
 * ViewModel and this contract stay put, keeping design churn away from the
 * `otp_input` / `verify_btn` test-tag contract the Maestro flows use.
 */

/**
 * The full validity window for a code, in seconds.
 *
 * Drives the expiry meter's fill, so it has to agree with the ViewModel's
 * countdown — the ViewModel reads this constant rather than declaring its own,
 * which is what stops the meter and the clock from disagreeing.
 */
internal const val OTP_VERIFY_WINDOW_SECONDS = 300

internal data class VerifyOtpUiState(
    val phoneNumber: String = "",
    val otp: String = "",
    val otpLength: Int = 6,
    val isVerifying: Boolean = false,
    val isResending: Boolean = false,
    val errorMessage: String? = null,
    val attemptsRemaining: Int? = null,
    /** Countdown before "Resend code" becomes available. */
    val resendSeconds: Int = 0,
    /** Seconds left before the current code expires. 0 means expired. */
    val expirySeconds: Int = 0,
    /** Set briefly on a wrong code; drives the shake animation. */
    val shake: Boolean = false,
    /** Six digits entered AND the code is still live AND nothing in flight.
     *  Supplied by the screen, which is the only place that can see both
     *  the ViewModel's length flag and the expiry countdown. */
    val canVerify: Boolean = false,
) {
    val isExpired: Boolean get() = expirySeconds <= 0
    val canResend: Boolean get() = resendSeconds <= 0 && !isResending

    val formattedPhone: String
        get() = formatIndianMobileNumber(phoneNumber, withCountryCode = true)

    /** Label for the secondary action. */
    val resendLabel: String
        get() = when {
            isResending -> "Sending a new code…"
            canResend -> "Resend code"
            else -> "Resend in ${formatCountdownClock(resendSeconds)}"
        }

    /**
     * The bare countdown readout for the expiry meter, e.g. "04:44".
     *
     * Padded, unlike the inline [resendLabel], so the value holds the same
     * width while it ticks and matches the Connect auth screen's readout
     * character for character.
     */
    val expiryClock: String
        get() = formatCountdownClockPadded(expirySeconds)

    /** How much of the validity window is left, 0..1 — the meter's fill. */
    val expiryFraction: Float
        get() = (expirySeconds.toFloat() / OTP_VERIFY_WINDOW_SECONDS).coerceIn(0f, 1f)

    /** True in the final 30 seconds — the meter and its readout switch to the
     *  warning colour so the deadline is not missed. */
    val isExpiringSoon: Boolean get() = expirySeconds in 1..30

    /** Label for the primary action. */
    val primaryLabel: String
        get() = if (isExpired) "Send a new code" else "Verify & continue"

    /** Secondary "attempts remaining" line under the error, if any. */
    val attemptsLabel: String?
        get() = attemptsRemaining?.let { "$it attempt${if (it == 1) "" else "s"} remaining" }
}

internal interface VerifyOtpActions {
    fun onOtpChange(value: String)
    fun onVerify()
    fun onResend()
    fun onChangeNumber()
    fun onBack()
    fun onCallHelpline()
}

/** No-op implementation, handy for previews. */
internal object VerifyOtpActionsNoop : VerifyOtpActions {
    override fun onOtpChange(value: String) = Unit
    override fun onVerify() = Unit
    override fun onResend() = Unit
    override fun onChangeNumber() = Unit
    override fun onBack() = Unit
    override fun onCallHelpline() = Unit
}
