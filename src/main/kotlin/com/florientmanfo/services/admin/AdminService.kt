package com.florientmanfo.com.florientmanfo.services.admin

import com.florientmanfo.com.florientmanfo.models.amdin.AdminRepository
import com.florientmanfo.com.florientmanfo.models.amdin.StatsModel

class AdminService(
    private val repository: AdminRepository,
) {
    suspend fun getStats(): Result<StatsModel>{
        return repository.getStats()
    }
}