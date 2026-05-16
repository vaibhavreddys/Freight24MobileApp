package com.freight24.network.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultNetwork

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthenticatedNetwork
