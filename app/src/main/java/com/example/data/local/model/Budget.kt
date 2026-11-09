package com.example.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String,
    val monthlyLimit: Double,
    val period: String = "2026-11", // YYYY-MM
    val memberId: Long = 0 // 0 means Family-wide
)
