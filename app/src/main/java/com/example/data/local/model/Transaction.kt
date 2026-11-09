package com.example.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: String, // "EXPENSE", "INCOME", "ALLOWANCE"
    val category: String, // "Groceries", "Utilities", "Housing", "Education", "Entertainment", "Healthcare", "Shopping", "Dining", "Allowance", "Transport", "Salary", "Other"
    val dateMillis: Long,
    val memberId: Long, // 0 for shared family, or specific member id
    val memberName: String,
    val paymentMethod: String = "Card", // "Card", "Cash", "Bank Transfer", "Allowance"
    val notes: String = ""
)
