package com.example

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.ads.AdManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.model.FamilyMember
import com.example.ui.components.HomeFlowBottomBar
import com.example.ui.components.HomeFlowDrawerSheet
import com.example.ui.components.HomeFlowTopBar
import com.example.ui.components.NotificationSheet
import com.example.ui.screens.bills.BillsScreen
import com.example.ui.screens.budgets.BudgetsScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.dialogs.AddBillDialog
import com.example.ui.screens.dialogs.AddCategoryBudgetDialog
import com.example.ui.screens.dialogs.AddEditMemberDialog
import com.example.ui.screens.dialogs.AddGoalDialog
import com.example.ui.screens.dialogs.AddTransactionDialog
import com.example.ui.screens.dialogs.ExportPdfDialog
import com.example.ui.screens.dialogs.ContributeGoalDialog
import com.example.ui.screens.dialogs.EditBudgetDialog
import com.example.ui.screens.dialogs.GiveAllowanceDialog
import com.example.ui.screens.dialogs.SetMonthlyBudgetDialog
import com.example.ui.screens.family.FamilyScreen
import com.example.ui.screens.savings.SavingsGoalsScreen
import com.example.ui.screens.settings.AboutAppDialog
import com.example.ui.screens.settings.RateUsDialog
import com.example.ui.screens.settings.SettingsDialog
import com.example.ui.screens.settings.shareHomeFlowApp
import com.example.ui.screens.transactions.TransactionsScreen
import com.example.ui.theme.HomeFlowTheme
import com.example.ui.viewmodel.CashFlowPeriod
import com.example.ui.viewmodel.HomeFlowViewModel
import com.example.ui.viewmodel.NavigationTab
import com.example.util.NotificationHelper
import com.example.util.PdfReportGenerator
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: HomeFlowViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationHelper.createNotificationChannel(this)
        AdManager.initialize(this)

        setContent {
            val systemDark = isSystemInDarkTheme()
            val manualDark by viewModel.isDarkMode.collectAsStateWithLifecycle()
            val effectiveDark = manualDark ?: systemDark

            HomeFlowTheme(darkTheme = effectiveDark) {
                MainApp(viewModel = viewModel, isDarkMode = effectiveDark)
            }
        }
    }
}

@Composable
fun MainApp(
    viewModel: HomeFlowViewModel,
    isDarkMode: Boolean
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // Notification Permission for Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val cashFlowPeriod by viewModel.cashFlowPeriod.collectAsStateWithLifecycle()
    val currentCurrency by viewModel.currentCurrency.collectAsStateWithLifecycle()
    val isAmountsHidden by viewModel.isAmountsHidden.collectAsStateWithLifecycle()
    val selectedMemberId by viewModel.selectedMemberId.collectAsStateWithLifecycle()
    val members by viewModel.members.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val filteredTransactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
    val allBudgets by viewModel.allBudgets.collectAsStateWithLifecycle()
    val allGoals by viewModel.allGoals.collectAsStateWithLifecycle()
    val allBills by viewModel.allBills.collectAsStateWithLifecycle()
    val allNotifications by viewModel.allNotifications.collectAsStateWithLifecycle()
    val unreadNotificationsCount by viewModel.unreadNotificationsCount.collectAsStateWithLifecycle()
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val typeFilter by viewModel.transactionTypeFilter.collectAsStateWithLifecycle()

    val showAddTx by viewModel.showAddTransactionDialog.collectAsStateWithLifecycle()
    val showAddBill by viewModel.showAddBillDialog.collectAsStateWithLifecycle()
    val showAddGoal by viewModel.showAddGoalDialog.collectAsStateWithLifecycle()
    val showGiveAllowance by viewModel.showGiveAllowanceDialog.collectAsStateWithLifecycle()
    val showEditBudget by viewModel.showEditBudgetDialog.collectAsStateWithLifecycle()
    val showSetMonthlyBudget by viewModel.showSetMonthlyBudgetDialog.collectAsStateWithLifecycle()
    val showAddCategoryBudget by viewModel.showAddCategoryBudgetDialog.collectAsStateWithLifecycle()
    val monthlyBudget by viewModel.monthlyBudget.collectAsStateWithLifecycle()
    val showContributeGoal by viewModel.showContributeGoalDialog.collectAsStateWithLifecycle()
    val showNotifications by viewModel.showNotificationSheet.collectAsStateWithLifecycle()
    val showAddEditMember by viewModel.showAddEditMemberDialog.collectAsStateWithLifecycle()
    val isCreatingMember by viewModel.isCreatingNewMember.collectAsStateWithLifecycle()
    val showSettings by viewModel.showSettingsDialog.collectAsStateWithLifecycle()
    val showAbout by viewModel.showAboutDialog.collectAsStateWithLifecycle()
    val showRate by viewModel.showRateDialog.collectAsStateWithLifecycle()
    val showExportPdf by viewModel.showExportPdfDialog.collectAsStateWithLifecycle()

    val selectedMember = members.find { it.id == selectedMemberId }

    // Handle back button: first close drawer, else return to Dashboard
    BackHandler(enabled = drawerState.isOpen || currentTab != NavigationTab.DASHBOARD) {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else {
            viewModel.setTab(NavigationTab.DASHBOARD)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            HomeFlowDrawerSheet(
                currentTab = currentTab,
                selectedCurrencyCode = currentCurrency.code,
                onSelectTab = { tab ->
                    scope.launch { drawerState.close() }
                    if (tab == NavigationTab.EXPENSES) {
                        viewModel.setTransactionTypeFilter("EXPENSE")
                        viewModel.setTab(NavigationTab.TRANSACTIONS)
                    } else {
                        if (tab == NavigationTab.TRANSACTIONS) {
                            viewModel.setTransactionTypeFilter("ALL")
                        }
                        viewModel.setTab(tab)
                    }
                },
                onExportPdf = {
                    scope.launch { drawerState.close() }
                    viewModel.showExportPdfDialog.value = true
                },
                onOpenSettings = {
                    scope.launch { drawerState.close() }
                    viewModel.showSettingsDialog.value = true
                },
                onRateUs = {
                    scope.launch { drawerState.close() }
                    viewModel.showRateDialog.value = true
                },
                onShareApp = {
                    scope.launch { drawerState.close() }
                    shareHomeFlowApp(context)
                },
                onAboutApp = {
                    scope.launch { drawerState.close() }
                    viewModel.showAboutDialog.value = true
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                HomeFlowTopBar(
                    isDarkMode = isDarkMode,
                    isAmountsHidden = isAmountsHidden,
                    currencySymbol = currentCurrency.symbol,
                    unreadNotifications = unreadNotificationsCount,
                    selectedMember = selectedMember,
                    onOpenDrawer = {
                        scope.launch { drawerState.open() }
                    },
                    onNavigateHome = {
                        viewModel.setTab(NavigationTab.DASHBOARD)
                    },
                    onToggleAmountsHidden = { viewModel.toggleAmountsHidden() },
                    onToggleDarkMode = { viewModel.toggleDarkMode() },
                    onOpenNotifications = { viewModel.showNotificationSheet.value = true }
                )
            },
            bottomBar = {
                HomeFlowBottomBar(
                    currentTab = currentTab,
                    onTabSelected = {
                        if (it == NavigationTab.TRANSACTIONS) {
                            viewModel.setTransactionTypeFilter("ALL")
                        }
                        viewModel.setTab(it)
                    }
                )
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Crossfade(
                targetState = currentTab,
                label = "tab_crossfade",
                modifier = Modifier.padding(innerPadding)
            ) { tab ->
                when (tab) {
                    NavigationTab.DASHBOARD -> DashboardScreen(
                        summary = summary,
                        members = members,
                        selectedMemberId = selectedMemberId,
                        cashFlowPeriod = cashFlowPeriod,
                        currency = currentCurrency,
                        isAmountsHidden = isAmountsHidden,
                        recentTransactions = allTransactions,
                        upcomingBills = allBills,
                        goals = allGoals,
                        onSelectMember = { viewModel.selectMember(it) },
                        onSetCashFlowPeriod = { viewModel.setCashFlowPeriod(it) },
                        onNavigateTab = { viewModel.setTab(it) },
                        onAddTransaction = { viewModel.showAddTransactionDialog.value = true },
                        onAddMember = {
                            viewModel.isCreatingNewMember.value = true
                            viewModel.showAddEditMemberDialog.value = null
                        },
                        onGiveAllowance = { viewModel.showGiveAllowanceDialog.value = true },
                        onToggleBillPaid = { viewModel.toggleBillPaid(it) },
                        onTriggerBillNotification = { viewModel.triggerBillNotification(it) },
                        onContributeGoal = { viewModel.showContributeGoalDialog.value = it }
                    )
                    NavigationTab.TRANSACTIONS, NavigationTab.EXPENSES -> TransactionsScreen(
                        transactions = filteredTransactions,
                        members = members,
                        selectedMemberId = selectedMemberId,
                        searchQuery = searchQuery,
                        typeFilter = typeFilter,
                        currency = currentCurrency,
                        isAmountsHidden = isAmountsHidden,
                        onSelectMember = { viewModel.selectMember(it) },
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        onTypeFilterChange = { viewModel.setTransactionTypeFilter(it) },
                        onAddTransaction = { viewModel.showAddTransactionDialog.value = true },
                        onDeleteTransaction = { viewModel.deleteTransaction(it) },
                        onExportPdf = {
                            viewModel.showExportPdfDialog.value = true
                        }
                    )
                    NavigationTab.BUDGETS -> BudgetsScreen(
                        budgets = allBudgets,
                        summary = summary,
                        monthlyBudget = monthlyBudget,
                        currency = currentCurrency,
                        isAmountsHidden = isAmountsHidden,
                        onSetMonthlyBudget = { viewModel.showSetMonthlyBudgetDialog.value = true },
                        onAddCategoryBudget = { viewModel.showAddCategoryBudgetDialog.value = true },
                        onEditBudget = { viewModel.showEditBudgetDialog.value = it },
                        onDeleteBudget = { viewModel.deleteBudget(it) }
                    )
                    NavigationTab.GOALS -> SavingsGoalsScreen(
                        goals = allGoals,
                        currency = currentCurrency,
                        isAmountsHidden = isAmountsHidden,
                        onAddGoal = { viewModel.showAddGoalDialog.value = true },
                        onContribute = { viewModel.showContributeGoalDialog.value = it },
                        onDeleteGoal = { viewModel.deleteGoal(it) }
                    )
                    NavigationTab.BILLS -> BillsScreen(
                        bills = allBills,
                        currency = currentCurrency,
                        isAmountsHidden = isAmountsHidden,
                        onAddBill = { viewModel.showAddBillDialog.value = true },
                        onTogglePaid = { viewModel.toggleBillPaid(it) },
                        onTriggerNotification = { viewModel.triggerBillNotification(it) },
                        onDeleteBill = { viewModel.deleteBill(it) }
                    )
                    NavigationTab.FAMILY -> FamilyScreen(
                        members = members,
                        memberSpending = summary.memberSpending,
                        currency = currentCurrency,
                        isAmountsHidden = isAmountsHidden,
                        onAddMember = {
                            viewModel.isCreatingNewMember.value = true
                            viewModel.showAddEditMemberDialog.value = null
                        },
                        onEditMember = { member ->
                            viewModel.isCreatingNewMember.value = false
                            viewModel.showAddEditMemberDialog.value = member
                        },
                        onDeleteMember = { memberId ->
                            viewModel.deleteFamilyMember(memberId)
                        },
                        onGiveAllowance = { viewModel.showGiveAllowanceDialog.value = true }
                    )
                }
            }
        }
    }

    // Interactive Dialogs
    if (showAddTx) {
        AddTransactionDialog(
            members = members,
            currency = currentCurrency,
            onDismiss = { viewModel.showAddTransactionDialog.value = false },
            onConfirm = { title, amount, type, category, memberId, memberName, paymentMethod ->
                viewModel.addTransaction(
                    title, amount, type, category, memberId, memberName, paymentMethod
                )
                viewModel.showAddTransactionDialog.value = false
            }
        )
    }

    if (showAddBill) {
        AddBillDialog(
            members = members,
            currency = currentCurrency,
            onDismiss = { viewModel.showAddBillDialog.value = false },
            onConfirm = { title, amount, daysUntilDue, category, frequency, memberId, assigneeName ->
                viewModel.addBill(
                    title = title,
                    amount = amount,
                    daysUntilDue = daysUntilDue,
                    category = category,
                    frequency = frequency,
                    autoPay = false,
                    memberId = memberId,
                    assigneeName = assigneeName
                )
                viewModel.showAddBillDialog.value = false
            }
        )
    }

    if (showAddGoal) {
        AddGoalDialog(
            members = members,
            currency = currentCurrency,
            onDismiss = { viewModel.showAddGoalDialog.value = false },
            onConfirm = { title, targetAmount, initialAmount, category, memberId, memberName, colorHex, iconName, targetDays ->
                viewModel.addSavingsGoal(
                    title, targetAmount, initialAmount, category, memberId, memberName, colorHex, iconName, targetDays
                )
                viewModel.showAddGoalDialog.value = false
            }
        )
    }

    if (showContributeGoal != null) {
        ContributeGoalDialog(
            goal = showContributeGoal!!,
            members = members,
            currency = currentCurrency,
            isAmountsHidden = isAmountsHidden,
            onDismiss = { viewModel.showContributeGoalDialog.value = null },
            onConfirm = { amount, memberId, memberName ->
                viewModel.contributeToGoal(showContributeGoal!!, amount, memberId, memberName)
                viewModel.showContributeGoalDialog.value = null
            }
        )
    }

    if (showGiveAllowance) {
        val children = members.filter { it.role == "Child" }
        val parents = members.filter { it.role == "Parent" }
        GiveAllowanceDialog(
            children = children,
            parents = parents,
            currency = currentCurrency,
            onDismiss = { viewModel.showGiveAllowanceDialog.value = false },
            onConfirm = { childId, amount, parentName, childName ->
                viewModel.distributeAllowance(childId, amount, parentName, childName)
                viewModel.showGiveAllowanceDialog.value = false
            }
        )
    }

    if (showEditBudget != null) {
        EditBudgetDialog(
            budget = showEditBudget!!,
            currency = currentCurrency,
            onDismiss = { viewModel.showEditBudgetDialog.value = null },
            onConfirm = { updated ->
                viewModel.updateBudget(updated)
                viewModel.showEditBudgetDialog.value = null
            }
        )
    }

    if (showSetMonthlyBudget) {
        SetMonthlyBudgetDialog(
            currentBudget = monthlyBudget,
            currency = currentCurrency,
            onDismiss = { viewModel.showSetMonthlyBudgetDialog.value = false },
            onConfirm = { amount ->
                viewModel.setMonthlyBudget(amount)
                viewModel.showSetMonthlyBudgetDialog.value = false
            }
        )
    }

    if (showAddCategoryBudget) {
        AddCategoryBudgetDialog(
            existingCategories = allBudgets.map { it.category },
            currency = currentCurrency,
            onDismiss = { viewModel.showAddCategoryBudgetDialog.value = false },
            onConfirm = { category, limit ->
                viewModel.addBudget(category, limit)
                viewModel.showAddCategoryBudgetDialog.value = false
            }
        )
    }

    if (isCreatingMember || showAddEditMember != null) {
        val memberToEdit = if (isCreatingMember) null else showAddEditMember
        AddEditMemberDialog(
            initialMember = memberToEdit,
            currency = currentCurrency,
            onDismiss = {
                viewModel.isCreatingNewMember.value = false
                viewModel.showAddEditMemberDialog.value = null
            },
            onSave = { name, relation, role, age, colorHex, iconName, allowance, limit ->
                if (memberToEdit == null) {
                    viewModel.addFamilyMember(
                        name, relation, role, age, colorHex, iconName, allowance, limit
                    )
                } else {
                    viewModel.updateFamilyMember(
                        memberToEdit.copy(
                            name = name,
                            relation = relation,
                            role = role,
                            age = age,
                            avatarColorHex = colorHex,
                            avatarIcon = iconName,
                            monthlyAllowance = allowance,
                            monthlySpendingLimit = limit
                        )
                    )
                }
                viewModel.isCreatingNewMember.value = false
                viewModel.showAddEditMemberDialog.value = null
            },
            onDelete = if (memberToEdit != null) {
                {
                    viewModel.deleteFamilyMember(memberToEdit.id)
                    viewModel.isCreatingNewMember.value = false
                    viewModel.showAddEditMemberDialog.value = null
                }
            } else null
        )
    }

    if (showNotifications) {
        NotificationSheet(
            notifications = allNotifications,
            currency = currentCurrency,
            onDismiss = { viewModel.showNotificationSheet.value = false },
            onMarkAllRead = { viewModel.markAllNotificationsRead() },
            onClearAll = { viewModel.clearAllNotifications() },
            onMarkRead = { viewModel.markNotificationRead(it) }
        )
    }

    if (showSettings) {
        SettingsDialog(
            selectedCurrencyCode = currentCurrency.code,
            isAmountsHidden = isAmountsHidden,
            isDarkMode = isDarkMode,
            onCurrencySelected = { viewModel.setCurrency(it) },
            onToggleAmountsHidden = { viewModel.toggleAmountsHidden() },
            onToggleDarkMode = { viewModel.toggleDarkMode() },
            onResetAllData = { viewModel.resetAllData() },
            onDismiss = { viewModel.showSettingsDialog.value = false }
        )
    }

    if (showAbout) {
        AboutAppDialog(onDismiss = { viewModel.showAboutDialog.value = false })
    }

    if (showRate) {
        RateUsDialog(onDismiss = { viewModel.showRateDialog.value = false })
    }

    if (showExportPdf) {
        ExportPdfDialog(
            allTransactions = allTransactions,
            members = members,
            currency = currentCurrency,
            isAmountsHidden = isAmountsHidden,
            initialMemberId = selectedMemberId,
            onDismiss = { viewModel.showExportPdfDialog.value = false },
            onExport = { filteredTxs, dateRangeLabel, memberName, typeFilterName ->
                viewModel.showExportPdfDialog.value = false
                val activity = context as? Activity
                val doExport = {
                    PdfReportGenerator.generateAndDownloadReport(
                        context = context,
                        transactions = filteredTxs,
                        dateRangeLabel = dateRangeLabel,
                        selectedMemberName = memberName,
                        typeFilter = typeFilterName,
                        currency = currentCurrency,
                        isAmountsHidden = isAmountsHidden
                    )
                }
                if (activity != null) {
                    AdManager.showInterstitial(activity) {
                        doExport()
                    }
                } else {
                    doExport()
                }
            }
        )
    }
}
