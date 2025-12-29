import com.freight24.core.network.model.AuthData

/**
 * This is debug class extracting OTP from server response message
 */
object OtpExtractor {

    private val otpRegex = Regex("""\b(\d{4,6})\b""")

    fun extract(message: AuthData?): String? {
        return otpRegex.find(message?.otp_code.toString())?.value
    }
}