package com.example.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.model.BillReminder
import com.example.data.local.model.FamilyMember
import com.example.data.local.model.SavingsGoal
import com.example.data.local.model.Transaction
import com.example.data.model.CurrencyOption
import com.example.ui.components.CategoryDonutChart
import com.example.ui.components.FamilyFilterBar
import com.example.ui.components.MemberAvatar
import com.example.ui.components.getCategoryColor
import com.example.ui.components.parseColor
import com.example.ui.viewmodel.CashFlowPeriod
import com.example.ui.viewmodel.DashboardSummary
import com.example.ui.viewmodel.NavigationTab
import com.example.ads.BannerAdCard
import com.example.util.Formatters

@Composable
fun DashboardScreen(
    summary: DashboardSummary,
    members: List<FamilyMember>,
    selectedMemberId: Long,
    cashFlowPeriod: CashFlowPeriod,
    currency: CurrencyOption,
    isAmountsHidden: Boolean,
    recentTransactions: List<Transaction>,
    upcomingBills: List<BillReminder>,
    goals: List<SavingsGoal>,
    onSelectMember: (Long) -> Unit,
    onSetCashFlowPeriod: (CashFlowPeriod) -> Unit,
    onNavigateTab: (NavigationTab) -> Unit,
    onAddTransaction: () -> Unit,
    onAddMember: () -> Unit,
    onGiveAllowance: () -> Unit,
    onToggleBillPaid: (BillReminder) -> Unit,
    onTriggerBillNotification: (BillReminder) -> Unit,
    onContributeGoal: (SavingsGoal) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(top = 8.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Family Member switcher bar (if members exist)
        if (members.isNotEmpty()) {
            item {
                FamilyFilterBar(
                    members = members,
                    selectedMemberId = selectedMemberId,
                    onSelectMember = onSelectMember
                )
            }
        }

        // Hero Cash Flow Balance Card with Day/Week/Month/Year filters
        item {
            HeroBalanceCard(
                summary = summary,
                selectedMemberId = selectedMemberId,
                members = members,
                cashFlowPeriod = cashFlowPeriod,
                currency = currency,
                isAmountsHidden = isAmountsHidden,
                onSetCashFlowPeriod = onSetCashFlowPeriod,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Quick Actions Row
        item {
            QuickActionsRow(
                onAddExpense = onAddTransaction,
                onGiveAllowance = onGiveAllowance,
                onViewBills = { onNavigateTab(NavigationTab.BILLS) },
                onViewBudgets = { onNavigateTab(NavigationTab.BUDGETS) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Welcome / Add Member card if no family members yet
        if (members.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FamilyRestroom,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Add Your Family Members",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Add parents, children, or household members to manage shared cash flow, allowances, and individual spending limits.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onAddMember,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("welcome_add_member_button")
                        ) {
                            Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add First Family Member")
                        }
                    }
                }
            }
        } else {
            // Family Members Status Carousel
            item {
                FamilyMembersCarousel(
                    members = members,
                    memberSpending = summary.memberSpending,
                    currency = currency,
                    isAmountsHidden = isAmountsHidden,
                    onMemberClick = onSelectMember,
                    onManageFamily = { onNavigateTab(NavigationTab.FAMILY) },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }

        // Upcoming Bills Alert Carousel
        val pendingBills = upcomingBills.filter { !it.isPaid }.take(3)
        if (pendingBills.isNotEmpty()) {
            item {
                BillsAlertSection(
                    bills = pendingBills,
                    currency = currency,
                    isAmountsHidden = isAmountsHidden,
                    onSeeAll = { onNavigateTab(NavigationTab.BILLS) },
                    onTogglePaid = onToggleBillPaid,
                    onNotify = onTriggerBillNotification,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        // Category Spending Donut Chart Card
        if (summary.categorySpending.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Category Breakdown (${cashFlowPeriod.label})",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            TextButton(onClick = { onNavigateTab(NavigationTab.BUDGETS) }) {
                                Text("View Budgets", fontSize = 12.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        CategoryDonutChart(categorySpending = summary.categorySpending)
                    }
                }
            }
        }

        // Savings Goals Carousel
        if (goals.isNotEmpty()) {
            item {
                SavingsGoalsSection(
                    goals = goals.take(3),
                    currency = currency,
                    isAmountsHidden = isAmountsHidden,
                    onSeeAll = { onNavigateTab(NavigationTab.GOALS) },
                    onContribute = onContributeGoal,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        // Google AdMob Test Banner
        item {
            BannerAdCard(modifier = Modifier.padding(horizontal = 16.dp))
        }

        // Recent Transactions Section
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = { onNavigateTab(NavigationTab.TRANSACTIONS) }) {
                    Text("See All", fontSize = 12.sp)
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        if (recentTransactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No transactions yet. Tap \"Add Entry\" to record your first income or expense.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(recentTransactions.take(5), key = { it.id }) { tx ->
                TransactionRowItem(
                    transaction = tx,
                    currency = currency,
                    isAmountsHidden = isAmountsHidden,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun HeroBalanceCard(
    summary: DashboardSummary,
    selectedMemberId: Long,
    members: List<FamilyMember>,
    cashFlowPeriod: CashFlowPeriod,
    currency: CurrencyOption,
    isAmountsHidden: Boolean,
    onSetCashFlowPeriod: (CashFlowPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    val member = members.find { it.id == selectedMemberId }
    val title = if (member != null) "${member.name}'s Cash Flow" else "Total Family Cash Flow"

    val gradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF0F766E), // Deep Teal
            Color(0xFF0D9488), // Emerald
            Color(0xFF14B8A6)  // Mint Teal
        )
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("hero_balance_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .padding(18.dp)
        ) {
            Column {
                // Title & Subtitle Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp
                        ),
                        color = Color.White
                    )
                    Text(
                        text = "Net Cash Flow",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Cash Flow Period Filter Segmented Bar: Day, Week, Month, Year
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CashFlowPeriod.values().forEach { period ->
                        val isSelected = cashFlowPeriod == period
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(9.dp))
                                .background(if (isSelected) Color.White else Color.Transparent)
                                .clickable { onSetCashFlowPeriod(period) }
                                .padding(vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = period.label,
                                maxLines = 1,
                                softWrap = false,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color(0xFF0F766E) else Color.White
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = Formatters.formatCurrency(summary.netBalance, currency, isAmountsHidden),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-1).sp
                    ),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Inflow (Income) & Outflow (Expenses) Sub-Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Inflow (Income)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Inflow",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = Formatters.formatCurrency(summary.totalIncome, currency, isAmountsHidden),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // Outflow (Expenses)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF43F5E)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Outflow",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = Formatters.formatCurrency(summary.totalExpenses, currency, isAmountsHidden),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                if (summary.totalBudget > 0) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Budget: ${Formatters.formatCurrency(summary.budgetRemaining, currency, isAmountsHidden)} remaining",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                            Text(
                                text = "${(summary.budgetSpentPercentage * 100).toInt()}% used",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { summary.budgetSpentPercentage },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFFFBBF24),
                            trackColor = Color.White.copy(alpha = 0.25f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionsRow(
    onAddExpense: () -> Unit,
    onGiveAllowance: () -> Unit,
    onViewBills: () -> Unit,
    onViewBudgets: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        QuickActionButton(
            label = "Add Entry",
            icon = Icons.Default.Add,
            color = MaterialTheme.colorScheme.primary,
            onClick = onAddExpense,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        QuickActionButton(
            label = "Allowance",
            icon = Icons.Default.MonetizationOn,
            color = Color(0xFF8B5CF6),
            onClick = onGiveAllowance,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        QuickActionButton(
            label = "Bills",
            icon = Icons.Default.Payment,
            color = Color(0xFFF59E0B),
            onClick = onViewBills,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        QuickActionButton(
            label = "Budgets",
            icon = Icons.Default.TrendingUp,
            color = Color(0xFF3B82F6),
            onClick = onViewBudgets,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun FamilyMembersCarousel(
    members: List<FamilyMember>,
    memberSpending: Map<Long, Double>,
    currency: CurrencyOption,
    isAmountsHidden: Boolean,
    onMemberClick: (Long) -> Unit,
    onManageFamily: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Family Members",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            TextButton(onClick = onManageFamily) {
                Text("Manage", fontSize = 12.sp)
            }
        }

        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            members.forEach { m ->
                val spent = memberSpending[m.id] ?: 0.0
                Card(
                    modifier = Modifier
                        .width(150.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { onMemberClick(m.id) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MemberAvatar(
                                name = m.name,
                                colorHex = m.avatarColorHex,
                                iconName = m.avatarIcon,
                                relation = m.relation,
                                size = 36.dp
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = m.relation,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = m.name,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "Spent: ${Formatters.formatCurrency(spent, currency, isAmountsHidden)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (m.role == "Child") {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Allowance: ${Formatters.formatCurrency(m.allowanceBalance, currency, isAmountsHidden)}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF10B981),
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BillsAlertSection(
    bills: List<BillReminder>,
    currency: CurrencyOption,
    isAmountsHidden: Boolean,
    onSeeAll: () -> Unit,
    onTogglePaid: (BillReminder) -> Unit,
    onNotify: (BillReminder) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF59E0B))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Upcoming Bills",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                TextButton(onClick = onSeeAll) {
                    Text("Manage", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            bills.forEach { bill ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = bill.title,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${Formatters.formatDaysRemaining(bill.dueDateMillis)} • Assigned to ${bill.assigneeName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFF59E0B)
                        )
                    }

                    Text(
                        text = Formatters.formatCurrency(bill.amount, currency, isAmountsHidden),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { onNotify(bill) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Send notification reminder",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Button(
                        onClick = { onTogglePaid(bill) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Pay", fontSize = 11.sp)
                    }
                }
                Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
private fun SavingsGoalsSection(
    goals: List<SavingsGoal>,
    currency: CurrencyOption,
    isAmountsHidden: Boolean,
    onSeeAll: () -> Unit,
    onContribute: (SavingsGoal) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Savings Goals",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = onSeeAll) {
                    Text("All Goals", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            goals.forEach { goal ->
                val progress = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f) else 0f
                val color = parseColor(goal.colorHex)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(46.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.size(46.dp),
                            color = color,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeWidth = 4.dp
                        )
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = goal.title,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${Formatters.formatCurrency(goal.currentAmount, currency, isAmountsHidden)} of ${Formatters.formatCurrency(goal.targetAmount, currency, isAmountsHidden)} (${goal.memberName})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    OutlinedButton(
                        onClick = { onContribute(goal) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("+ Add", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionRowItem(
    transaction: Transaction,
    currency: CurrencyOption,
    isAmountsHidden: Boolean,
    modifier: Modifier = Modifier
) {
    val isExpense = transaction.type == "EXPENSE"
    val isIncome = transaction.type == "INCOME"
    val amountColor = when {
        isExpense -> Color(0xFFF43F5E)
        isIncome -> Color(0xFF10B981)
        else -> Color(0xFF8B5CF6)
    }
    val sign = when {
        isExpense -> "-"
        isIncome -> "+"
        else -> ""
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("tx_${transaction.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(getCategoryColor(transaction.category).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = transaction.category.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = getCategoryColor(transaction.category)
                    )
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = transaction.memberName,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = " • ${Formatters.formatShortDate(transaction.dateMillis)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (isAmountsHidden) "••••" else "$sign${Formatters.formatCurrency(transaction.amount, currency, false)}",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = amountColor
                    )
                )
                Text(
                    text = transaction.paymentMethod,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
