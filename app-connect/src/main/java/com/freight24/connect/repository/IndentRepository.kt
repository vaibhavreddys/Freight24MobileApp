package com.freight24.connect.repository

import com.freight24.rest.request.CreateIndentRequest
import com.freight24.rest.request.UpdateIndentGroupRequest
import com.freight24.rest.request.UpdateIndentRequest
import com.freight24.rest.request.IndentLifecycleRequest
import com.freight24.rest.response.GetIndentsResponse
import com.freight24.rest.response.IndentResponse
import com.freight24.rest.response.MaterialDto
import com.freight24.rest.response.TruckCategoryDto

interface IndentRepository {
    suspend fun getTruckTypes(): Result<TruckCategoryDto>

    suspend fun createIndent(request: CreateIndentRequest): Result<IndentResponse>

    suspend fun getMyIndents(
        status: String,
        page: Int? = null,
        limit: Int? = null,
    ): Result<GetIndentsResponse>

    suspend fun getIndentById(id: String): Result<IndentResponse>

    suspend fun updateIndent(
        id: String,
        request: UpdateIndentRequest,
    ): Result<IndentResponse>

    suspend fun cancelIndent(id: String, request: IndentLifecycleRequest): Result<Unit>

    suspend fun reopenIndent(id: String, request: IndentLifecycleRequest): Result<Unit>

    /**
     * Group-level update — the only write path that can change an indent's
     * contact roles. Backend accepts it while the group is `OPEN` and only from
     * the creator or an admin / operations manager.
     *
     * Returns success/failure only. The route's body is the raw `indent_groups`
     * row, not an indent projection, and no caller needs it — the group PUT
     * already committed by the time it responds, so a body we cannot decode must
     * not be reported as a failed write. Re-read `GET indents/child/{id}` when
     * the refreshed state is needed.
     */
    suspend fun updateIndentGroup(
        groupId: String,
        request: UpdateIndentGroupRequest,
    ): Result<Unit>

    /** Cargo categories for the material field. Fixed list, no paging. */
    suspend fun getMaterials(): Result<List<MaterialDto>>

    suspend fun getAllIndents(
        page: Int,
        limit: Int,
    ): Result<GetIndentsResponse>

    suspend fun getInactiveIndents(
        page: Int,
        limit: Int,
    ): Result<GetIndentsResponse>

    suspend fun cloneIndent(
        indentGroupId: String,
        assignedTo: String?,
        notes: String?,
    ): Result<IndentResponse>

    suspend fun searchIndents(
        query: String,
        priority: String?,
        clientId: String?,
        assignedToMe: Boolean?,
    ): Result<GetIndentsResponse>
}
