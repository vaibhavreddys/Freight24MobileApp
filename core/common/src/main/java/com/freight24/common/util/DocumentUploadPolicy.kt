package com.freight24.common.util

import java.io.File
import java.io.IOException

class DocumentUploadException(message: String) : IOException(message)

object DocumentUploadPolicy {
    const val MAX_IMAGES = 5
    const val MAX_FILE_SIZE_BYTES = 3_000_000L
    const val IMAGE_MAX_DIMENSION = 1600

    fun validateImageCount(count: Int) {
        if (count !in 1..MAX_IMAGES) {
            throw DocumentUploadException("Select between 1 and $MAX_IMAGES images per document.")
        }
    }

    fun validateSize(sizeBytes: Long) {
        if (sizeBytes > MAX_FILE_SIZE_BYTES) {
            throw DocumentUploadException("Document must be 3 MB or smaller. Please choose a smaller file.")
        }
    }

    fun validateFile(file: File) {
        if (!file.isFile || file.length() == 0L) {
            throw DocumentUploadException("The document is empty or unavailable. Please select it again.")
        }
        validateSize(file.length())
    }
}
