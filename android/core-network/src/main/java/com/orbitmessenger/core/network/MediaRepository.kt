package com.orbitmessenger.core.network

import java.io.File

interface MediaRepository {
    /**
     * Uploads a file securely to the backend.
     * Returns the remote URL of the uploaded file on success.
     */
    suspend fun uploadFile(file: File, mimeType: String, onProgress: (Float) -> Unit): String?
}
