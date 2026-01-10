package com.freight24.common.util

object StringUtils {
    private val otpRegex = Regex("""\b(\d{4,6})\b""")

    fun String.extract(text: String): String? {
        return otpRegex.find(text)?.value
    }
}
