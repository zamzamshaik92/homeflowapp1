package com.example.ui.screens.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.model.FamilyMember
import com.example.data.local.model.Transaction
import com.example.data.model.CurrencyOption
import com.example.util.Formatters
import java.util.Calendar

enum class ExportDateRange(val label: String) {
    TODAY("Today"),
    LAST_7_DAYS("Last 7 Days"),
    LAST_30_DAYS("Last 30 Days"),
    THIS_MONTH("This Month"),
    THIS_YEAR("This Year");

    fun filter(transactions: List<Transaction>): List<Transaction> {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        val minTime: Long = when (this) {
            TODAY -> {
                calendar.timeInMillis = now
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            }
            LAST_7_DAYS -> now - (7L * 24 * 60 * 60 * 1000L)
            LAST_30_DAYS -> now - (30L * 24 * 60 * 60 * 1000L)
            THIS_MONTH -> {
                calendar.timeInMillis = now
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            }
            THIS_YEAR -> {
                calendar.timeInMillis = now
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            }
        }
        return transactions.filter { it.dateMillis >= minTime }
    }
}

/**
 * Filter modal dialog displayed before exporting and downloading the PDF financial report.
 */
@Composable
fun ExportPdfDialog(
    allTransactions: List<Transaction>,
    members: List<FamilyMember>,
    currency: CurrencyOption,
    isAmountsHidden: Boolean,
    initialMemberId: Long = 0L,
    onDismiss: () -> Unit,
    onExport: (
        filteredTransactions: List<Transaction>,
        dateRangeLabel: String,
        memberName: String,
        typeFilter: String
    ) -> Unit
) {
    var selectedDateRange by remember { mutableStateOf(ExportDateRange.LAST_30_DAYS) }
    var selectedMemberId by remember { mutableStateOf(initialMemberId) }
    var selectedType by remember { mutableStateOf("ALL") } // ALL, INCOME, EXPENSE

    // Filter computation
    val dateFiltered = remember(selectedDateRange, allTransactions) {
        selectedDateRange.filter(allTransactions)
    }

    val userFiltered = remember(selectedMemberId, dateFiltered) {
        if (selectedMemberId == 0L) {
            dateFiltered
        } else {
            dateFiltered.filter { it.memberId == selectedMemberId }
        }
    }

    val finalFiltered = remember(selectedType, userFiltered) {
        when (selectedType) {
            "INCOME" -> userFiltered.filter { it.type == "INCOME" }
            "EXPENSE" -> userFiltered.filter { it.type == "EXPENSE" }
            else -> userFiltered
        }
    }

    val totalIncome = remember(finalFiltered) {
        finalFiltered.filter { it.type == "INCOME" }.sumOf { it.amount }
    }
    val totalExpense = remember(finalFiltered) {
        finalFiltered.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    }
    val netBalance = totalIncome - totalExpense

    val selectedMemberName = if (selectedMemberId == 0L) {
        "All Family"
    } else {
        members.find { it.id == selectedMemberId }?.name ?: "Family Member"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("export_pdf_filter_dialog"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Export PDF Report",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Select filters for your report",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Section 1: Date Range Filter
                Text(
                    text = "Date Range",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ExportDateRange.values().forEach { range ->
                        val isSelected = selectedDateRange == range
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedDateRange = range },
                            label = { Text(range.label, fontSize = 11.5.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Section 2: User / Family Member Filter
                Text(
                    text = "User / Family Member",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedMemberId == 0L,
                        onClick = { selectedMemberId = 0L },
                        label = { Text("All Family", fontSize = 11.5.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )

                    members.forEach { member ->
                        FilterChip(
                            selected = selectedMemberId == member.id,
                            onClick = { selectedMemberId = member.id },
                            label = { Text(member.name, fontSize = 11.5.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Section 3: Transaction Type Filter
                Text(
                    text = "Transaction Type",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("ALL" to "All", "INCOME" to "Income", "EXPENSE" to "Expense").forEach { (typeVal, label) ->
                        val isSelected = selectedType == typeVal
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedType = typeVal },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Section 4: Live Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Report Matches:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${finalFiltered.size} Transactions",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Inflow: +${Formatters.formatCurrency(totalIncome, currency, isAmountsHidden)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF10B981)
                            )
                            Text(
                                text = "Outflow: -${Formatters.formatCurrency(totalExpense, currency, isAmountsHidden)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFF43F5E)
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "Net Cash Flow: ${Formatters.formatCurrency(netBalance, currency, isAmountsHidden)}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val typeLabel = when (selectedType) {
                        "INCOME" -> "Income Only"
                        "EXPENSE" -> "Expense Only"
                        else -> "All Types"
                    }
                    onExport(
                        finalFiltered,
                        selectedDateRange.label,
                        selectedMemberName,
                        typeLabel
                    )
                },
                modifier = Modifier.testTag("confirm_export_pdf_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Download PDF")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
