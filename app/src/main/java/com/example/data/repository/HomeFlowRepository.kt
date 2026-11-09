package com.example.data.repository

import com.example.data.local.HomeFlowDatabase
import com.example.data.local.model.BillReminder
import com.example.data.local.model.Budget
import com.example.data.local.model.FamilyMember
import com.example.data.local.model.NotificationItem
import com.example.data.local.model.SavingsGoal
import com.example.data.local.model.Transaction
import kotlinx.coroutines.flow.Flow

class HomeFlowRepository(private val database: HomeFlowDatabase) {

    private val memberDao = database.familyMemberDao()
    private val transactionDao = database.transactionDao()
    private val budgetDao = database.budgetDao()
    private val savingsGoalDao = database.savingsGoalDao()
    private val billReminderDao = database.billReminderDao()
    private val notificationDao = database.notificationDao()

    val allMembers: Flow<List<FamilyMember>> = memberDao.getAllMembers()
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
    val allBudgets: Flow<List<Budget>> = budgetDao.getAllBudgets()
    val allGoals: Flow<List<SavingsGoal>> = savingsGoalDao.getAllGoals()
    val allBills: Flow<List<BillReminder>> = billReminderDao.getAllBills()
    val allNotifications: Flow<List<NotificationItem>> = notificationDao.getAllNotifications()
    val unreadNotificationsCount: Flow<Int> = notificationDao.getUnreadCount()

    suspend fun addMember(member: FamilyMember): Long {
        return memberDao.insert(member)
    }

    suspend fun updateMember(member: FamilyMember) {
        memberDao.update(member)
    }

    suspend fun deleteMember(id: Long) {
        memberDao.deleteById(id)
    }

    suspend fun addTransaction(transaction: Transaction) {
        transactionDao.insert(transaction)
        // If it's an allowance deduction for kid
        if (transaction.paymentMethod == "Allowance" && transaction.memberId > 0) {
            memberDao.deductAllowance(transaction.memberId, transaction.amount)
        }
    }

    suspend fun deleteTransaction(id: Long) {
        transactionDao.deleteById(id)
    }

    suspend fun addBudget(budget: Budget) {
        budgetDao.insert(budget)
    }

    suspend fun updateBudget(budget: Budget) {
        budgetDao.update(budget)
    }

    suspend fun deleteBudget(budget: Budget) {
        budgetDao.delete(budget)
    }

    suspend fun addGoal(goal: SavingsGoal) {
        savingsGoalDao.insert(goal)
    }

    suspend fun contributeToGoal(
        goalId: Long,
        amount: Double,
        memberId: Long,
        memberName: String,
        goalTitle: String,
        currencySymbol: String = "$"
    ) {
        savingsGoalDao.contributeToGoal(goalId, amount)
        val transaction = Transaction(
            title = "Contribution: $goalTitle",
            amount = amount,
            type = "EXPENSE",
            category = "Savings",
            dateMillis = System.currentTimeMillis(),
            memberId = memberId,
            memberName = memberName,
            paymentMethod = "Bank Transfer",
            notes = "Allocated to savings goal"
        )
        transactionDao.insert(transaction)

        notificationDao.insert(
            NotificationItem(
                title = "Goal Contribution",
                message = "$currencySymbol$amount contributed to \"$goalTitle\" by $memberName.",
                timestampMillis = System.currentTimeMillis(),
                type = "GOAL_PROGRESS"
            )
        )
    }

    suspend fun deleteGoal(goalId: Long) {
        savingsGoalDao.deleteById(goalId)
    }

    suspend fun addBill(bill: BillReminder, currencySymbol: String = "$") {
        billReminderDao.insert(bill)
        notificationDao.insert(
            NotificationItem(
                title = "New Bill Scheduled",
                message = "${bill.title} ($currencySymbol${bill.amount}) added to reminders.",
                timestampMillis = System.currentTimeMillis(),
                type = "BILL_DUE"
            )
        )
    }

    suspend fun toggleBillPaid(bill: BillReminder, currencySymbol: String = "$") {
        if (!bill.isPaid) {
            val now = System.currentTimeMillis()
            billReminderDao.markAsPaid(bill.id, now)
            val tx = Transaction(
                title = "Paid: ${bill.title}",
                amount = bill.amount,
                type = "EXPENSE",
                category = bill.category,
                dateMillis = now,
                memberId = bill.memberId,
                memberName = bill.assigneeName,
                paymentMethod = if (bill.autoPay) "Auto-Debit" else "Bank Transfer",
                notes = "Bill reminder marked paid"
            )
            transactionDao.insert(tx)

            notificationDao.insert(
                NotificationItem(
                    title = "Bill Paid",
                    message = "${bill.title} ($currencySymbol${bill.amount}) marked as paid.",
                    timestampMillis = now,
                    type = "BILL_DUE"
                )
            )
        } else {
            billReminderDao.markAsUnpaid(bill.id)
        }
    }

    suspend fun deleteBill(billId: Long) {
        billReminderDao.deleteById(billId)
    }

    suspend fun distributeAllowance(
        childId: Long,
        amount: Double,
        fromParentName: String,
        childName: String,
        currencySymbol: String = "$"
    ) {
        memberDao.addAllowance(childId, amount)
        val now = System.currentTimeMillis()
        val tx = Transaction(
            title = "Allowance to $childName",
            amount = amount,
            type = "ALLOWANCE",
            category = "Allowance",
            dateMillis = now,
            memberId = childId,
            memberName = childName,
            paymentMethod = "Bank Transfer",
            notes = "Sent by $fromParentName"
        )
        transactionDao.insert(tx)

        notificationDao.insert(
            NotificationItem(
                title = "Allowance Transferred",
                message = "$currencySymbol${amount} pocket money transferred to $childName by $fromParentName.",
                timestampMillis = now,
                type = "ALLOWANCE"
            )
        )
    }

    suspend fun markNotificationRead(id: Long) {
        notificationDao.markAsRead(id)
    }

    suspend fun markAllNotificationsRead() {
        notificationDao.markAllAsRead()
    }

    suspend fun clearAllNotifications() {
        notificationDao.clearAll()
    }

    suspend fun addNotification(notification: NotificationItem) {
        notificationDao.insert(notification)
    }

    suspend fun resetAllData() {
        HomeFlowDatabase.clearAllData(database)
    }
}
