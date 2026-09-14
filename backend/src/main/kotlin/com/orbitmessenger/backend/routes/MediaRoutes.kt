package com.orbitmessenger.backend.routes

import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.HttpStatusCode
import java.io.File
import java.util.UUID

fun Route.mediaRoutes() {
    // Note: In a production environment, this would upload directly to S3 or Google Cloud Storage.
    // We are simulating a local file-based object store for demonstration.
    val uploadDir = File("uploads")
    if (!uploadDir.exists()) {
        uploadDir.mkdirs()
    }

    authenticate("auth-jwt") {
        route("/api/v1/media") {
            post("/upload") {
                var fileUrl: String? = null
                val multipartData = call.receiveMultipart()

                multipartData.forEachPart { part ->
                    if (part is PartData.FileItem) {
                        val fileName = part.originalFileName ?: UUID.randomUUID().toString()
                        val ext = File(fileName).extension
                        val uniqueName = "${UUID.randomUUID()}.$ext"
                        val file = File(uploadDir, uniqueName)
                        
                        part.streamProvider().use { its ->
                            file.outputStream().buffered().use {
                                its.copyTo(it)
                            }
                        }
                        
                        // Assuming the server is hosting the /uploads directory statically
                        fileUrl = "/uploads/$uniqueName"
                    }
                    part.dispose()
                }

                if (fileUrl != null) {
                    call.respond(HttpStatusCode.Created, mapOf("url" to fileUrl))
                } else {
                    call.respond(HttpStatusCode.BadRequest, "No file provided")
                }
            }
        }
    }
}
