package com.freight24.notifications

sealed class AppNotification(
    val id: Int,
    val title: String,
    val message: String,
) {
    class Critical(
        message: String,
    ) : AppNotification(
            id = 1001,
            title = "\uD83D\uDE9A F24 Alerts",
            message = message,
        )

    class TripUpdate(
        message: String,
    ) : AppNotification(
            id = 2001,
            title = "Trip Update",
            message = message,
        )

    class InterestAlert(
        title: String,
        message: String,
        val indentId: String? = null,
    ) : AppNotification(
            id = indentId?.let { 3000 + (it.hashCode() and 0x7FFF) % 1000 } ?: 3001,
            title = title,
            message = message,
        )
}
