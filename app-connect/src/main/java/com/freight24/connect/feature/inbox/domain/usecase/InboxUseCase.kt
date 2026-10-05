package com.freight24.connect.feature.inbox.domain.usecase

import com.freight24.common.usecase.UseCase
import com.freight24.common.usecase.UseCaseParams
import com.freight24.connect.repository.InterestInboxRepository
import com.freight24.rest.response.IndentInterestInboxResponse
import javax.inject.Inject

data class InboxUseCaseParams(
    val indentId: String? = null,
) : UseCaseParams

interface InboxUseCase : UseCase<InboxUseCaseParams, IndentInterestInboxResponse>

class InboxUseCaseImpl
    @Inject
    constructor(
        private val repository: InterestInboxRepository,
    ) : InboxUseCase {
        override suspend fun invoke(params: InboxUseCaseParams): Result<IndentInterestInboxResponse> {
            return repository.getInbox(params.indentId)
        }
    }
