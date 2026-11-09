package com.example.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "family_members")
data class FamilyMember(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val relation: String, // "Dad", "Mom", "Son", "Daughter"
    val role: String, // "Parent", "Child"
    val age: Int,
    val avatarColorHex: String,
    val avatarIcon: String,
    val monthlyAllowance: Double = 0.0,
    val allowanceBalance: Double = 0.0,
    val monthlySpendingLimit: Double = 0.0
)
