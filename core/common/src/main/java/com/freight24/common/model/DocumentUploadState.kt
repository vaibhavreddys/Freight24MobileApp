package com.freight24.common.model

enum class UploadStatus {
    IDLE,
    UPLOADING,
    DONE,
    ERROR,
}

data class DocumentUploadState(
    val fieldKey: String,
    val documentTypeId: Int,
    val existingDocId: String?,
    val existingFileUrl: String?,
    val existingVerifyStatus: String?,
    val temporaryFileKey: String?,
    val uploadStatus: UploadStatus = UploadStatus.IDLE,
)

sealed class UploadMode {
    data object ADD : UploadMode()

    data class UPDATE(
        val existingDocId: String,
    ) : UploadMode()
}
