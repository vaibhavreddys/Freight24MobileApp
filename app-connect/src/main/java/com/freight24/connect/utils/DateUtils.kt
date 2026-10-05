package com.freight24.connect.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun String.toDate(): String =
    runCatching {
        LocalDateTime
            .parse(this)
            .format(
                DateTimeFormatter.ofPattern("dd MMM yyyy"),
            )
    }.getOrNull() ?: ""

fun String.toDateTime(): String =
    runCatching {
        LocalDateTime
            .parse(this)
            .format(
                DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"),
            )
    }.getOrNull() ?: ""
