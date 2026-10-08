package com.freight24.connect.feature.indents.views

internal enum class IndentCardOwnership {
    MINE,
    OTHER,
}

internal fun indentCardOwnership(value: String?): IndentCardOwnership? =
    when (value?.trim()?.uppercase()) {
        "CREATED_BY_ME", "CLONED_TO_ME", "ASSIGNED_TO_ME" -> IndentCardOwnership.MINE
        "OTHER" -> IndentCardOwnership.OTHER
        else -> null
    }
