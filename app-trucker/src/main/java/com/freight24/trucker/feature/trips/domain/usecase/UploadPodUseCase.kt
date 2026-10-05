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

data class UploadPodUseCaseDataParams(
    val tripId: String,
    val file: File,
    val notes: String?,
    // The trucker app only ever uploads ePOD — hPOD is a finance upload.
    val podType: String = "epod",
    // Set when a POD is already on file (pending/rejected) so the upload goes
    // through the replace endpoint instead of the create one.
    val replaceDocumentId: String? = null,
) : UseCaseParams

internal interface UploadPodUseCase : UseCase<UploadPodUseCaseDataParams, TripDetail>

internal class UploadPodUseCaseImpl
    @Inject
    constructor(
        private val repository: TripRepository,
    ) : UploadPodUseCase {
        override suspend fun invoke(params: UploadPodUseCaseDataParams): Result<TripDetail> {
            val mimeType =
                if (params.file.extension.equals("pdf", ignoreCase = true)) {
                    "application/pdf"
                } else {
                    getMimeType(params.file.absolutePath) ?: "application/octet-stream"
                }
            val requestFile = params.file.asRequestBody(mimeType.toMediaTypeOrNull())

            val filePart =
                MultipartBody.Part.createFormData(
                    "file", // The name expected by the server for the file field
                    params.file.name, // The actual file name
                    requestFile,
                )

            return repository.uploadPod(
                params.tripId,
                filePart,
                params.podType.toRequestBody("text/plain".toMediaType()),
                params.notes?.toRequestBody("text/plain".toMediaType()),
                params.replaceDocumentId,
            )
        }

        private fun getMimeType(url: String): String? {
            val extension = MimeTypeMap.getFileExtensionFromUrl(url)
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
    }
