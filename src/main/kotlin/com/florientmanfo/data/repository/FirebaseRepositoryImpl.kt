package com.florientmanfo.com.florientmanfo.data.repository

import com.florientmanfo.com.florientmanfo.models.firebase.FirebaseRepository
import com.google.auth.oauth2.GoogleCredentials
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

    override fun uploadFile(fileData: ByteArray, fileName: String?): String {
        val blob: Blob = bucket.create("Recipes/$fileName", fileData, "image/jpeg")
        return "https://storage.googleapis.com/${blob.bucket}/${blob.name}"
    }

    override fun deleteFile(fileName: String) {
        bucket.get("Recipes/$fileName")?.delete()
    }

    private fun configureCloudStorage() {
        val keyFilePath = this::class.java.getResource("/service-account-key.json")
            ?.path ?: throw Exception("Key file not found")
        val bucketName = config.property("ktor.firebase.bucket").getString()
        val storage = StorageOptions.newBuilder()
            .setCredentials(GoogleCredentials.fromStream(FileInputStream(keyFilePath)))
            .build()
            .service
        bucket = storage.get(bucketName)
    }
}