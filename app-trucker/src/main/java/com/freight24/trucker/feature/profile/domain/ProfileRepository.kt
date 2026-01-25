package com.freight24.trucker.feature.profile.domain

interface ProfileRepository {
    suspend fun getProfile(): Result<Profile>
}
