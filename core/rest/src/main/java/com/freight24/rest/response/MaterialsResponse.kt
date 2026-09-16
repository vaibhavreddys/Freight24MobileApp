package com.freight24.rest.response

import kotlinx.serialization.Serializable

/**
 * One cargo category an indent can be raised against.
 *
 * [id] is opaque to the app — it is only ever used as a list key. Today the
 * backend serves 1..47 from a constant; when these move into a `materials`
 * table it becomes the row id and nothing here changes.
 */
@Serializable
data class MaterialDto(
    val id: Int,
    val name: String,
)

@Serializable
data class MaterialsResponse(
    val total: Int = 0,
    val data: List<MaterialDto> = emptyList(),
) : BaseResponse()
