package com.example.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "savings_goals")
data class SavingsGoal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val deadlineMillis: Long,
    val category: String, // "Vacation", "Emergency", "Gadget", "Education", "Vehicle", "Home"
    val memberId: Long = 0, // 0 for shared family goal
    val memberName: String = "All Family",
    val colorHex: String = "#0D9488",
    val iconName: String = "savings"
)
