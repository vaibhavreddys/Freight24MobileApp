package com.freight24.common.usecase

import kotlinx.coroutines.flow.Flow

interface UseCase<P : UseCaseParams, R> {
    suspend operator fun invoke(params: P): Result<R>
}

interface FlowUseCase<P : UseCaseParams, R> {
    suspend operator fun invoke(params: P): Flow<R>
}

suspend operator fun <R> UseCase<EmptyUseCaseParams, R>.invoke() = this.invoke(EmptyUseCaseParams)

suspend operator fun <R> FlowUseCase<EmptyUseCaseParams, R>.invoke() = this.invoke(EmptyUseCaseParams)
