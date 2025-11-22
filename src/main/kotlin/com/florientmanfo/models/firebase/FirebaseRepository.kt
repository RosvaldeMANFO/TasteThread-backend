package com.florientmanfo.models.firebase

import com.florientmanfo.data.repository.FirebaseRepositoryImpl.Companion.BucketPath

interface FirebaseRepository {
    fun uploadFile(fileData: ByteArray, fileName: String?, path: BucketPath): String
    fun deleteFile(fileName: String, path: BucketPath)
}