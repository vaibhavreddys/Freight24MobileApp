package com.freight24.network.adapter

data class NetworkConfig(
    val logsEnabled: Boolean,
    val logsTag: String,
    val headers: Map<String, String>,
    val timeout: Long,
)
