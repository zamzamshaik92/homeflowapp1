package com.example.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val message: String,
    val timestampMillis: Long,
    val type: String, // "BILL_DUE", "BUDGET_ALERT", "GOAL_PROGRESS", "ALLOWANCE"
    val isRead: Boolean = false
)
