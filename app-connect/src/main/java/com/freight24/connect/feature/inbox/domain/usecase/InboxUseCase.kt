package com.freight24.connect.feature.inbox.domain.usecase

import com.freight24.common.usecase.EmptyUseCaseParams
import com.freight24.common.usecase.UseCase
import com.freight24.connect.repository.InterestInboxRepository
import com.freight24.rest.response.IndentInterestInboxResponse
import javax.inject.Inject

interface InboxUseCase : UseCase<EmptyUseCaseParams, IndentInterestInboxResponse>


class InboxUseCaseImpl @Inject constructor(
    private val repository: InterestInboxRepository
) : InboxUseCase {

    override suspend fun invoke(params: EmptyUseCaseParams): Result<IndentInterestInboxResponse> {
        return repository.getInbox()
    }
}