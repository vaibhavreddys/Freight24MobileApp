package com.freight24.connect.repository

import com.freight24.rest.request.CreateIndentRequest
import com.freight24.rest.request.UpdateIndentGroupRequest
import com.freight24.rest.request.UpdateIndentRequest
import com.freight24.rest.response.GetIndentsResponse
import com.freight24.rest.response.IndentResponse
import com.freight24.rest.response.IndentsListResponse
import com.freight24.rest.response.TruckCategoryDto

interface IndentRepository {
    suspend fun getTruckTypes(): Result<TruckCategoryDto>

    suspend fun createIndent(request: CreateIndentRequest): Result<IndentResponse>

    suspend fun getMyIndents(
        status: String,
        page: Int? = null,
        limit: Int? = null
    ): Result<GetIndentsResponse>

    suspend fun getIndentById(id: String): Result<IndentResponse>

    suspend fun updateIndent(id: String, request: UpdateIndentRequest): Result<IndentResponse>

    /**
     * Group-level update — the only write path that can change an indent's
     * contact roles. Backend accepts it while the group is `OPEN` and only from
     * the creator or an admin / operations manager.
     */
    suspend fun updateIndentGroup(
        groupId: String,
        request: UpdateIndentGroupRequest,
    ): Result<IndentResponse>

    suspend fun getAllIndents(page: Int, limit: Int): Result<GetIndentsResponse>
    suspend fun getInactiveIndents(page: Int, limit: Int): Result<GetIndentsResponse>
    suspend fun cloneIndent(indentGroupId: String, assignedTo: String?, notes: String?): Result<IndentResponse>
    suspend fun searchIndents(query: String, priority: String?, clientId: String?, assignedToMe: Boolean?): Result<GetIndentsResponse>
}
