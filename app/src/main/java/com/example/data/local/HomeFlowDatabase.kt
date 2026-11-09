package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.BillReminderDao
import com.example.data.local.dao.BudgetDao
import com.example.data.local.dao.FamilyMemberDao
import com.example.data.local.dao.NotificationDao
import com.example.data.local.dao.SavingsGoalDao
import com.example.data.local.dao.TransactionDao
import com.example.data.local.model.BillReminder
import com.example.data.local.model.Budget
import com.example.data.local.model.FamilyMember
import com.example.data.local.model.NotificationItem
import com.example.data.local.model.SavingsGoal
import com.example.data.local.model.Transaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        FamilyMember::class,
        Transaction::class,
        Budget::class,
        SavingsGoal::class,
        BillReminder::class,
        NotificationItem::class
    ],
    version = 2,
    exportSchema = false
)
abstract class HomeFlowDatabase : RoomDatabase() {

    abstract fun familyMemberDao(): FamilyMemberDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun billReminderDao(): BillReminderDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: HomeFlowDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): HomeFlowDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HomeFlowDatabase::class.java,
                    "homeflow_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun clearAllData(database: HomeFlowDatabase) {
            database.transactionDao().getCount() // trigger init if needed
            database.clearAllTables()
        }
    }
}
