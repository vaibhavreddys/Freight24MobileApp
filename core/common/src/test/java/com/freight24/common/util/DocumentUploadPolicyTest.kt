package com.freight24.common.util

import java.io.RandomAccessFile
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DocumentUploadPolicyTest {
    @get:Rule val files = TemporaryFolder()

    @Test fun allowsOneToFiveImages() {
        (1..5).forEach(DocumentUploadPolicy::validateImageCount)
    }

    @Test fun rejectsEmptyOrExcessSelections() {
        listOf(0, 6, -1).forEach { count ->
            assertThrows(DocumentUploadException::class.java) { DocumentUploadPolicy.validateImageCount(count) }
        }
    }

    @Test fun acceptsExactThreeMegabytes() {
        DocumentUploadPolicy.validateSize(3_000_000)
        val file = files.newFile()
        RandomAccessFile(file, "rw").use { it.setLength(3_000_000) }
        DocumentUploadPolicy.validateFile(file)
    }

    @Test fun rejectsOneByteOverLimit() {
        assertThrows(DocumentUploadException::class.java) { DocumentUploadPolicy.validateSize(3_000_001) }
        val file = files.newFile()
        RandomAccessFile(file, "rw").use { it.setLength(3_000_001) }
        assertThrows(DocumentUploadException::class.java) { DocumentUploadPolicy.validateFile(file) }
    }

    @Test fun rejectsEmptyOrMissingFiles() {
        val file = files.newFile()
        assertThrows(DocumentUploadException::class.java) { DocumentUploadPolicy.validateFile(file) }
        file.delete()
        assertThrows(DocumentUploadException::class.java) { DocumentUploadPolicy.validateFile(file) }
    }
}
