package com.florientmanfo.com.florientmanfo.models.firebase

interface FirebaseRepository {
    fun uploadFile(fileData: ByteArray, fileName: String?): String
    fun deleteFile(fileName: String)
}