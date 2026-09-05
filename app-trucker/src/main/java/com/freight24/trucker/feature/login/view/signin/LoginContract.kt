package com.freight24.trucker.feature.login.view.signin

/**
 * The contract between the login feature's logic and its UI.
 *
 * Everything the screen needs in order to *render* lives in [LoginUiState];
 * everything the user can *do* lives in [LoginActions]. The screen owns the
 * ViewModel, side effects and animation, maps ViewModel flows into this
 * state, and hands it to a stateless `LoginContent` composable.
 *
 * Consequence: a visual redesign only touches `LoginContent.kt`. The
 * ViewModel and this contract stay put, which keeps design churn away from
 * behavioural code (and away from the `phone_input` / `send_otp_btn`
 * test-tag contract the Maestro flows depend on).
 */

internal data class LoginUiState(
    val phoneNumber: String = "",
    /** A send request is in flight — the CTA shows a spinner. */
    val isSending: Boolean = false,
    /** Failure to show inline. Cleared as soon as the number is edited. */
    val errorMessage: String? = null,
    /** True once 10 digits are entered. */
    val isPhoneComplete: Boolean = false,
    /** 0..1 wipe across the CTA during the auto-send countdown. Zero when
     *  the number is incomplete, so the CTA reads as "not yet". */
    val autoSendProgress: Float = 0f,
) {
    /** Whether the primary action should be live. */
    val canSend: Boolean get() = isPhoneComplete && !isSending
}

internal interface LoginActions {
    fun onPhoneChange(value: String)
    fun onSendCode()
    fun onCallHelpline()
}

/** No-op implementation, handy for previews and for states where an action
 *  has nothing to fire yet. */
internal object LoginActionsNoop : LoginActions {
    override fun onPhoneChange(value: String) = Unit
    override fun onSendCode() = Unit
    override fun onCallHelpline() = Unit
}
