package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.HomeFlowDatabase
import com.example.data.local.PreferencesManager
import com.example.data.local.model.BillReminder
import com.example.data.local.model.Budget
import com.example.data.local.model.FamilyMember
import com.example.data.local.model.NotificationItem
import com.example.data.local.model.SavingsGoal
import com.example.data.local.model.Transaction
import com.example.data.model.CurrencyOption
import com.example.data.model.SupportedCurrencies
import com.example.data.repository.HomeFlowRepository
import com.example.util.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class NavigationTab(val title: String, val shortTitle: String) {
    DASHBOARD("Overview", "Overview"),
    TRANSACTIONS("Transactions", "Transactions"),
    BUDGETS("Budgets", "Budget"),
    GOALS("Savings", "Savings"),
    BILLS("Bills", "Bills"),
    FAMILY("Family Members", "Family"),
    EXPENSES("Expenses", "Expenses")
}

enum class CashFlowPeriod(val label: String) {
    DAY("Day"),
    WEEK("Week"),
    MONTH("Month"),
    YEAR("Year")
}

data class DashboardSummary(
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val netBalance: Double = 0.0,
    val totalBudget: Double = 0.0,
    val budgetRemaining: Double = 0.0,
    val budgetSpentPercentage: Float = 0f,
    val categorySpending: Map<String, Double> = emptyMap(),
    val memberSpending: Map<Long, Double> = emptyMap()
)

class HomeFlowViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HomeFlowRepository
    private val preferencesManager = PreferencesManager(application)

    init {
        val db = HomeFlowDatabase.getDatabase(application, viewModelScope)
        repository = HomeFlowRepository(db)
    }

    // Currency and Privacy Preferences
    val currencyCode: StateFlow<String> = preferencesManager.currencyCode
    val isAmountsHidden: StateFlow<Boolean> = preferencesManager.isAmountsHidden

    val currentCurrency: StateFlow<CurrencyOption> = currencyCode.map { code ->
        SupportedCurrencies.getByCode(code)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SupportedCurrencies.getByCode("USD"))

    fun setCurrency(code: String) {
        preferencesManager.setCurrency(code)
    }

    fun toggleAmountsHidden() {
        preferencesManager.toggleAmountsHidden()
    }

    fun setAmountsHidden(hidden: Boolean) {
        preferencesManager.setAmountsHidden(hidden)
    }

    // Navigation and filter states
    private val _currentTab = MutableStateFlow(NavigationTab.DASHBOARD)
    val currentTab: StateFlow<NavigationTab> = _currentTab.asStateFlow()

    private val _cashFlowPeriod = MutableStateFlow(CashFlowPeriod.MONTH)
    val cashFlowPeriod: StateFlow<CashFlowPeriod> = _cashFlowPeriod.asStateFlow()

    private val _selectedMemberId = MutableStateFlow<Long>(0) // 0 = All Family
    val selectedMemberId: StateFlow<Long> = _selectedMemberId.asStateFlow()

    private val _isDarkMode = MutableStateFlow<Boolean?>(null) // null = system, true = dark, false = light
    val isDarkMode: StateFlow<Boolean?> = _isDarkMode.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _transactionTypeFilter = MutableStateFlow("ALL") // "ALL", "EXPENSE", "INCOME", "ALLOWANCE"
    val transactionTypeFilter: StateFlow<String> = _transactionTypeFilter.asStateFlow()

    // Dialog state holders
    val showAddTransactionDialog = MutableStateFlow(false)
    val showAddBillDialog = MutableStateFlow(false)
    val showAddGoalDialog = MutableStateFlow(false)
    val showGiveAllowanceDialog = MutableStateFlow(false)
    val showEditBudgetDialog = MutableStateFlow<Budget?>(null)
    val showSetMonthlyBudgetDialog = MutableStateFlow(false)
    val showAddCategoryBudgetDialog = MutableStateFlow(false)
    val showContributeGoalDialog = MutableStateFlow<SavingsGoal?>(null)
    val showNotificationSheet = MutableStateFlow(false)
    val showAddEditMemberDialog = MutableStateFlow<FamilyMember?>(null)
    val isCreatingNewMember = MutableStateFlow(false)
    val showSettingsDialog = MutableStateFlow(false)
    val showAboutDialog = MutableStateFlow(false)
    val showRateDialog = MutableStateFlow(false)
    val showExportPdfDialog = MutableStateFlow(false)

    val monthlyBudget: StateFlow<Double> = preferencesManager.monthlyBudget

    // Data streams from repository
    val members: StateFlow<List<FamilyMember>> = repository.allMembers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<Transaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBudgets: StateFlow<List<Budget>> = repository.allBudgets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allGoals: StateFlow<List<SavingsGoal>> = repository.allGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBills: StateFlow<List<BillReminder>> = repository.allBills
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotifications: StateFlow<List<NotificationItem>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationsCount: StateFlow<Int> = repository.unreadNotificationsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Filtered transactions stream
    val filteredTransactions: StateFlow<List<Transaction>> = combine(
        allTransactions,
        selectedMemberId,
        searchQuery,
        transactionTypeFilter
    ) { txList, memberId, query, typeFilter ->
        txList.filter { tx ->
            val matchesMember = if (memberId == 0L) true else tx.memberId == memberId
            val matchesQuery = query.isBlank() ||
                    tx.title.contains(query, ignoreCase = true) ||
                    tx.category.contains(query, ignoreCase = true) ||
                    tx.memberName.contains(query, ignoreCase = true)
            val matchesType = when (typeFilter) {
                "EXPENSE" -> tx.type == "EXPENSE"
                "INCOME" -> tx.type == "INCOME"
                "ALLOWANCE" -> tx.type == "ALLOWANCE"
                else -> true
            }
            matchesMember && matchesQuery && matchesType
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Financial Analytics Summary with Cash Flow Period Filtering
    val summary: StateFlow<DashboardSummary> = combine(
        allTransactions,
        allBudgets,
        selectedMemberId,
        cashFlowPeriod,
        preferencesManager.monthlyBudget
    ) { txList, budgetList, memberId, period, customBudget ->
        val now = System.currentTimeMillis()
        val periodStartMillis = when (period) {
            CashFlowPeriod.DAY -> now - (24 * 60 * 60 * 1000L)
            CashFlowPeriod.WEEK -> now - (7 * 24 * 60 * 60 * 1000L)
            CashFlowPeriod.MONTH -> now - (30 * 24 * 60 * 60 * 1000L)
            CashFlowPeriod.YEAR -> now - (365 * 24 * 60 * 60 * 1000L)
        }

        val periodTransactions = txList.filter { it.dateMillis >= periodStartMillis }
        val relevantTransactions = if (memberId == 0L) periodTransactions else periodTransactions.filter { it.memberId == memberId }

        val income = relevantTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }
        val expenses = relevantTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
        val net = income - expenses

        val categoryBudgetSum = budgetList.sumOf { it.monthlyLimit }
        val totalBudget = if (customBudget > 0.0) customBudget else categoryBudgetSum
        val remaining = (totalBudget - expenses).coerceAtLeast(0.0)
        val percentage = if (totalBudget > 0) ((expenses / totalBudget).toFloat()).coerceIn(0f, 1f) else 0f

        val categoryMap = mutableMapOf<String, Double>()
        for (tx in relevantTransactions.filter { it.type == "EXPENSE" }) {
            categoryMap[tx.category] = (categoryMap[tx.category] ?: 0.0) + tx.amount
        }

        val memberMap = mutableMapOf<Long, Double>()
        for (tx in periodTransactions.filter { it.type == "EXPENSE" }) {
            memberMap[tx.memberId] = (memberMap[tx.memberId] ?: 0.0) + tx.amount
        }

        DashboardSummary(
            totalIncome = income,
            totalExpenses = expenses,
            netBalance = net,
            totalBudget = totalBudget,
            budgetRemaining = remaining,
            budgetSpentPercentage = percentage,
            categorySpending = categoryMap,
            memberSpending = memberMap
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardSummary())

    fun setTab(tab: NavigationTab) {
        _currentTab.value = tab
    }

    fun setCashFlowPeriod(period: CashFlowPeriod) {
        _cashFlowPeriod.value = period
    }

    fun selectMember(memberId: Long) {
        _selectedMemberId.value = memberId
    }

    fun toggleDarkMode() {
        _isDarkMode.value = when (_isDarkMode.value) {
            null -> true
            true -> false
            false -> true
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setTransactionTypeFilter(type: String) {
        _transactionTypeFilter.value = type
    }

    // Family Member Management CRUD
    fun addFamilyMember(
        name: String,
        relation: String,
        role: String,
        age: Int,
        colorHex: String,
        iconName: String,
        monthlyAllowance: Double,
        monthlySpendingLimit: Double
    ) {
        viewModelScope.launch {
            val member = FamilyMember(
                name = name.trim(),
                relation = relation.trim(),
                role = role,
                age = age,
                avatarColorHex = colorHex,
                avatarIcon = iconName,
                monthlyAllowance = monthlyAllowance,
                allowanceBalance = 0.0,
                monthlySpendingLimit = monthlySpendingLimit
            )
            repository.addMember(member)
        }
    }

    fun updateFamilyMember(member: FamilyMember) {
        viewModelScope.launch {
            repository.updateMember(member)
        }
    }

    fun deleteFamilyMember(memberId: Long) {
        viewModelScope.launch {
            if (_selectedMemberId.value == memberId) {
                _selectedMemberId.value = 0L
            }
            repository.deleteMember(memberId)
        }
    }

    // Data Actions
    fun addTransaction(
        title: String,
        amount: Double,
        type: String,
        category: String,
        memberId: Long,
        memberName: String,
        paymentMethod: String
    ) {
        viewModelScope.launch {
            val tx = Transaction(
                title = title.trim(),
                amount = amount,
                type = type,
                category = category,
                dateMillis = System.currentTimeMillis(),
                memberId = memberId,
                memberName = memberName,
                paymentMethod = paymentMethod,
                notes = "" // Notes removed as per user request
            )
            repository.addTransaction(tx)
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
        }
    }

    fun addBudget(category: String, limit: Double) {
        viewModelScope.launch {
            repository.addBudget(Budget(category = category, monthlyLimit = limit))
        }
    }

    fun updateBudget(budget: Budget) {
        viewModelScope.launch {
            repository.updateBudget(budget)
        }
    }

    fun setMonthlyBudget(amount: Double) {
        preferencesManager.setMonthlyBudget(amount)
        viewModelScope.launch {
            if (amount > 0.0) {
                val curr = currentCurrency.value
                val formatted = "%.2f".format(amount)
                repository.addNotification(
                    NotificationItem(
                        title = "Monthly Budget Set",
                        message = "Family monthly budget set to ${curr.symbol}$formatted.",
                        timestampMillis = System.currentTimeMillis(),
                        type = "BUDGET_ALERT"
                    )
                )
            }
        }
    }

    fun deleteBudget(budget: Budget) {
        viewModelScope.launch {
            repository.deleteBudget(budget)
        }
    }

    fun addSavingsGoal(
        title: String,
        targetAmount: Double,
        initialAmount: Double,
        category: String,
        memberId: Long,
        memberName: String,
        colorHex: String,
        iconName: String,
        targetDays: Int
    ) {
        viewModelScope.launch {
            val deadline = System.currentTimeMillis() + (targetDays * 24 * 60 * 60 * 1000L)
            val goal = SavingsGoal(
                title = title.trim(),
                targetAmount = targetAmount,
                currentAmount = initialAmount,
                deadlineMillis = deadline,
                category = category,
                memberId = memberId,
                memberName = memberName,
                colorHex = colorHex,
                iconName = iconName
            )
            repository.addGoal(goal)
        }
    }

    fun contributeToGoal(goal: SavingsGoal, amount: Double, memberId: Long, memberName: String) {
        viewModelScope.launch {
            repository.contributeToGoal(
                goal.id, amount, memberId, memberName, goal.title, currentCurrency.value.symbol
            )
        }
    }

    fun deleteGoal(goalId: Long) {
        viewModelScope.launch {
            repository.deleteGoal(goalId)
        }
    }

    fun addBill(
        title: String,
        amount: Double,
        daysUntilDue: Int,
        category: String,
        frequency: String,
        autoPay: Boolean = false,
        memberId: Long,
        assigneeName: String
    ) {
        viewModelScope.launch {
            val dueDate = System.currentTimeMillis() + (daysUntilDue * 24 * 60 * 60 * 1000L)
            val bill = BillReminder(
                title = title.trim(),
                amount = amount,
                dueDateMillis = dueDate,
                category = category,
                frequency = frequency,
                isPaid = false,
                autoPay = autoPay,
                memberId = memberId,
                assigneeName = assigneeName
            )
            repository.addBill(bill, currentCurrency.value.symbol)
        }
    }

    fun toggleBillPaid(bill: BillReminder) {
        viewModelScope.launch {
            repository.toggleBillPaid(bill, currentCurrency.value.symbol)
        }
    }

    fun deleteBill(billId: Long) {
        viewModelScope.launch {
            repository.deleteBill(billId)
        }
    }

    fun triggerBillNotification(bill: BillReminder) {
        val context = getApplication<Application>()
        val curr = currentCurrency.value
        val title = "HomeFlow Reminder: ${bill.title}"
        val message = "${bill.title} of ${curr.symbol}${bill.amount} is due soon! (Assigned to: ${bill.assigneeName})"
        NotificationHelper.showNotification(
            context = context,
            notificationId = bill.id.toInt(),
            title = title,
            message = message
        )
    }

    fun distributeAllowance(childId: Long, amount: Double, fromParentName: String, childName: String) {
        viewModelScope.launch {
            repository.distributeAllowance(childId, amount, fromParentName, childName, currentCurrency.value.symbol)
        }
    }

    fun updateMemberLimits(member: FamilyMember, newAllowance: Double, newLimit: Double) {
        viewModelScope.launch {
            repository.updateMember(
                member.copy(
                    monthlyAllowance = newAllowance,
                    monthlySpendingLimit = newLimit
                )
            )
        }
    }

    fun markNotificationRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationRead(id)
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsRead()
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            repository.clearAllNotifications()
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            repository.resetAllData()
            preferencesManager.resetPreferences()
            _selectedMemberId.value = 0L
        }
    }
}
