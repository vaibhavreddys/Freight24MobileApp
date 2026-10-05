package com.freight24.common.util

fun formatIndianCurrency(input: String): String {
    val cleaned = input.replace(",", "").filter { it.isDigit() || it == '.' }
    if (cleaned.isEmpty()) return ""

    val parts = cleaned.split(".")
    val integerPart = parts[0]
    val decimalPart = if (parts.size > 1) ".${parts[1].take(2)}" else ""

    if (integerPart.length <= 3) return "$integerPart$decimalPart"

    val lastThree = integerPart.takeLast(3)
    val remaining = integerPart.dropLast(3)

    // Correct Indian grouping: reverse -> chunk by 2 -> reverse chunks back -> join
    val groupedRemaining =
        remaining.reversed()
            .chunked(2)
            .map { it.reversed() }
            .reversed()
            .joinToString(",")

    return "$groupedRemaining,$lastThree$decimalPart"
}
