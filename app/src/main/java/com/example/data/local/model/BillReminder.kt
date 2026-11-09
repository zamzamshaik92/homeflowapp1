package com.example.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bill_reminders")
data class BillReminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val dueDateMillis: Long,
    val category: String, // "Electricity", "Water", "Internet", "Rent/Mortgage", "School", "Subscription", "Insurance"
    val frequency: String = "Monthly", // "Monthly", "Quarterly", "Annual", "One-time"
    val isPaid: Boolean = false,
    val paidDateMillis: Long? = null,
    val autoPay: Boolean = false,
    val memberId: Long = 0, // 0 for shared family bill
    val assigneeName: String = "Mark & Sarah"
)
