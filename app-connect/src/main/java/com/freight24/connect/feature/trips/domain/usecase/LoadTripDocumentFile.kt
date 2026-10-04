package com.freight24.connect.feature.trips.domain.usecase

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class LoadTripDocumentFile @Inject constructor(
    private val signedDocumentUseCase: SignedDocumentUseCase,
    @param:ApplicationContext private val context: Context
) {
    suspend operator fun invoke(tripId: String, documentId: String): Result<File> {
        return try {
            val signedDocument = signedDocumentUseCase(
                SignedDocumentUseCaseDataParams(tripId, documentId)
            ).getOrThrow()
            val file = withContext(Dispatchers.IO) {
                val directory = File(context.cacheDir, "shared_trip_documents")
                check(directory.isDirectory || directory.mkdirs()) { "Unable to prepare document folder" }
                val file = File.createTempFile("Trip_document_", ".pdf", directory)
                val connection = URL(signedDocument.data.signedUrl).openConnection() as HttpURLConnection
                try {
                    connection.connectTimeout = 15_000
                    connection.readTimeout = 30_000
                    check(connection.responseCode == HttpURLConnection.HTTP_OK) { "Unable to download document" }
                    connection.inputStream.use { input ->
                        file.outputStream().use { output -> input.copyTo(output) }
                    }
                    check(file.length() > 0) { "The document is empty" }
                    file
                } catch (error: Exception) {
                    file.delete()
                    throw error
                } finally {
                    connection.disconnect()
                }
            }
            Result.success(file)
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Result.failure(error)
        }
    }
}
