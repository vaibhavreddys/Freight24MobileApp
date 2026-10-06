package com.freight24.connect.domain.model

interface EntityCounts {
    val total: Int
}

data class EntityPage<T, C : EntityCounts>(
    val page: Int,
    val limit: Int,
    val counts: C,
    val items: List<T>,
    val endOfList: Boolean,
) {
    val hasNextPage: Boolean
        get() = !endOfList

    val nextPage: Int?
        get() = if (hasNextPage) page + 1 else null
}

data class TruckCounts(
    override val total: Int,
    val active: Int,
    val inactive: Int,
    val blacklisted: Int,
    val pendingDocuments: Int,
) : EntityCounts

data class TruckOwnerCounts(
    override val total: Int,
    val active: Int?,
    val inactive: Int?,
    val pendingDocuments: Int?,
) : EntityCounts

data class ClientCounts(
    override val total: Int,
    val active: Int,
    val inactive: Int,
) : EntityCounts

internal fun calculateEndOfList(
    page: Int,
    limit: Int,
    total: Int,
    returnedItems: Int,
): Boolean {
    if (total <= 0 || returnedItems <= 0) return true
    if (limit <= 0) return true

    return returnedItems < limit || page.toLong() * limit.toLong() >= total.toLong()
}
