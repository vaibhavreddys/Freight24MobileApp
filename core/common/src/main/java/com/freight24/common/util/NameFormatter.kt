package com.freight24.common.util

object NameFormatter {
    fun shortenTruckName(name: String?): String {
        if (name.isNullOrBlank()) return "--"

        var formatted = name

        // Replace "Feet" or "Ft" (normalized)
        formatted = formatted.replace(Regex("(?i)feet"), "Ft")

        // Replace Axle Types
        formatted = formatted.replace(Regex("(?i)multi\\s*axle"), "MXL")
        formatted = formatted.replace(Regex("(?i)single\\s*axle"), "SXL")
        formatted = formatted.replace(Regex("(?i)double\\s*axle"), "DXL") // Common in some contexts

        // Replace Container types
        formatted = formatted.replace(Regex("(?i)container"), "Cont")
        formatted = formatted.replace(Regex("(?i)high\\s*cube"), "HQ") // High Cube Container

        // Replace Body Types
        formatted = formatted.replace(Regex("(?i)open\\s*body"), "Open")
        formatted = formatted.replace(Regex("(?i)closed\\s*body"), "Closed")
        formatted = formatted.replace(Regex("(?i)half\\s*body"), "Half")

        // Replace Wheeler to W
        formatted = formatted.replace(Regex("(?i)wheeler"), "W")
        formatted = formatted.replace(Regex("(?i)tyre"), "T")

        // Replace Trailer
        formatted = formatted.replace(Regex("(?i)trailer"), "Trlr")

        // Replace LCV/HCV (Light/Heavy Commercial Vehicle) if spelled out
        formatted = formatted.replace(Regex("(?i)light\\s*commercial\\s*vehicle"), "LCV")
        formatted = formatted.replace(Regex("(?i)heavy\\s*commercial\\s*vehicle"), "HCV")

        // Cleanup extra spaces
        formatted = formatted.replace(Regex("\\s+"), " ").trim()

        return formatted
    }

    fun formatTripStatusMessage(statusMessage: String?): String {
        if (statusMessage.isNullOrBlank()) return "STATUS ??"
        return when {
            statusMessage.contains("IN_TRANSIT", ignoreCase = true)
                    || statusMessage.contains("IN-TRANSIT", ignoreCase = true) -> "IN TRANSIT"
            else -> statusMessage
        }
    }

}
