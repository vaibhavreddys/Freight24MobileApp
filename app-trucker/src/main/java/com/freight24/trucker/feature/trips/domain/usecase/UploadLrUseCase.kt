package com.freight24.trucker.feature.trips.domain.usecase

import android.webkit.MimeTypeMap
import com.freight24.common.model.TripDetail
import com.freight24.common.usecase.UseCase
import com.freight24.common.usecase.UseCaseParams
import com.freight24.trucker.feature.trips.data.repository.TripRepository
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

data class UploadLrUseCaseDataParams(
    val tripId: String,
    val file : File,
    val notes: String?,
): UseCaseParams

internal interface UploadLrUseCase : UseCase<UploadLrUseCaseDataParams, TripDetail>

internal class UploadLrUseCaseImpl @Inject constructor(
    private val repository: TripRepository
) : UploadLrUseCase {

    override suspend fun invoke(params: UploadLrUseCaseDataParams): Result<TripDetail> {

        val mimeType = getMimeType(params.file.absolutePath) ?: "application/octet-stream"
        val requestFile = params.file.asRequestBody(mimeType.toMediaTypeOrNull())

        val filePart = MultipartBody.Part.createFormData(
            "file", // The name expected by the server for the file field
            params.file.name, // The actual file name
            requestFile
        )

        return repository.uploadLr(
            params.tripId,
            filePart,
            params.notes?.toRequestBody("text/plain".toMediaType())
        )
    }

    private fun getMimeType(url: String): String? {
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }
}
