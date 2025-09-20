package com.florientmanfo.com.florientmanfo.data.repository

import com.florientmanfo.com.florientmanfo.models.firebase.FirebaseRepository
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.BasePath
import com.google.cloud.storage.StorageOptions
import com.google.cloud.storage.Blob
import com.google.cloud.storage.Bucket
import io.ktor.server.config.*
import java.io.FileInputStream


class FirebaseRepositoryImpl(private val config: ApplicationConfig) : FirebaseRepository {

    private lateinit var bucket: Bucket

    init {
        configureCloudStorage()
    }

    override fun uploadFile(fileData: ByteArray, fileName: String?, path: BucketPath): String {
        val blob: Blob = bucket.create("${path.value}/$fileName", fileData, "image/jpeg")
        return "https://storage.googleapis.com/${blob.bucket}/${blob.name}"
    }

    override fun deleteFile(fileName: String, path: BucketPath) {
        bucket.get("${path.value}/$fileName")?.delete()
    }

    private fun configureCloudStorage() {
        val storage = when (config.property("ktor.environment").getString()) {
            "prod" -> {
                val credentials = GoogleCredentials.getApplicationDefault()
                StorageOptions.newBuilder()
                    .setCredentials(credentials)
                    .build()
                    .service
            }

            else -> {
                val keyFilePath = this::class.java.getResource("/service-account-key.json")
                    ?.path ?: throw Exception("Key file not found")
                val credentials = FileInputStream(keyFilePath)
                StorageOptions.newBuilder()
                    .setCredentials(GoogleCredentials.fromStream(credentials))
                    .build()
                    .service
            }
        }

        val bucketName = config.property("ktor.firebase.bucket").getString()

        bucket = storage.get(bucketName)
    }

    companion object {
        enum class BucketPath(val value: String) {
            RECIPES("Recipes"),
            USERS("Users"),
            IMAGES("Images");
        }
    }
}