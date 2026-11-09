package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.model.BillReminder
import kotlinx.coroutines.flow.Flow

@Dao
interface BillReminderDao {
    @Query("SELECT * FROM bill_reminders ORDER BY isPaid ASC, dueDateMillis ASC")
    fun getAllBills(): Flow<List<BillReminder>>

    @Query("SELECT * FROM bill_reminders WHERE isPaid = 0 ORDER BY dueDateMillis ASC")
    fun getPendingBills(): Flow<List<BillReminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bill: BillReminder): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(bills: List<BillReminder>)

    @Update
    suspend fun update(bill: BillReminder)

    @Query("UPDATE bill_reminders SET isPaid = 1, paidDateMillis = :paidDate WHERE id = :id")
    suspend fun markAsPaid(id: Long, paidDate: Long)

    @Query("UPDATE bill_reminders SET isPaid = 0, paidDateMillis = NULL WHERE id = :id")
    suspend fun markAsUnpaid(id: Long)

    @Delete
    suspend fun delete(bill: BillReminder)

    @Query("DELETE FROM bill_reminders WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM bill_reminders")
    suspend fun getCount(): Int
}
