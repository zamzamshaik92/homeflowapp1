package com.example.ui.screens.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Toys
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.model.Budget
import com.example.data.local.model.FamilyMember
import com.example.data.local.model.SavingsGoal
import com.example.data.model.CurrencyOption
import com.example.ui.components.getCategoryColor
import com.example.ui.components.parseColor
import com.example.util.Formatters

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddTransactionDialog(
    members: List<FamilyMember>,
    currency: CurrencyOption,
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        amount: Double,
        type: String,
        category: String,
        memberId: Long,
        memberName: String,
        paymentMethod: String
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("EXPENSE") } // EXPENSE, INCOME, ALLOWANCE
    var selectedCategory by remember { mutableStateOf("Groceries") }
    var selectedMemberId by remember { mutableStateOf(members.firstOrNull()?.id ?: 0L) }
    var paymentMethod by remember { mutableStateOf("Card") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val expenseCategories = listOf(
        "Groceries", "Dining", "Utilities", "Housing", "Entertainment",
        "Education", "Shopping", "Transport", "Healthcare", "Other"
    )
    val incomeCategories = listOf("Salary", "Freelance", "Investment", "Gift", "Other")
    val categories = if (type == "INCOME") incomeCategories else expenseCategories

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Add Transaction",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Type selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("EXPENSE" to "Expense", "INCOME" to "Income").forEach { (valType, label) ->
                        FilterChip(
                            selected = type == valType,
                            onClick = {
                                type = valType
                                selectedCategory = if (valType == "INCOME") "Salary" else "Groceries"
                            },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (valType == "EXPENSE") Color(0xFFF43F5E).copy(alpha = 0.2f)
                                else Color(0xFF10B981).copy(alpha = 0.2f)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title / Merchant") },
                    placeholder = { Text("e.g. Grocery Store") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transaction_title_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (${currency.symbol})") },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transaction_amount_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Member selector
                if (members.isNotEmpty()) {
                    Text(
                        text = "Family Member",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // All Family / Shared option
                        val isShared = selectedMemberId == 0L
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isShared) Color(0xFF10B981).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .border(
                                    width = if (isShared) 1.5.dp else 0.dp,
                                    color = if (isShared) Color(0xFF10B981) else Color.Transparent,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { selectedMemberId = 0L }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Shared Family",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isShared) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }

                        members.forEach { m ->
                            val isSelected = selectedMemberId == m.id
                            val color = parseColor(m.avatarColorHex)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .border(
                                        width = if (isSelected) 1.5.dp else 0.dp,
                                        color = if (isSelected) color else Color.Transparent,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable { selectedMemberId = m.id }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = m.name,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Category selector
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        val catColor = getCategoryColor(cat)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) catColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.dp,
                                    color = if (isSelected) catColor else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = cat,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp
                                ),
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Payment Method
                Text(
                    text = "Payment Method",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Card", "Cash", "Bank Transfer").forEach { method ->
                        FilterChip(
                            selected = paymentMethod == method,
                            onClick = { paymentMethod = method },
                            label = { Text(method, fontSize = 11.sp) }
                        )
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (title.isBlank()) {
                        errorMessage = "Please enter a transaction title."
                        return@Button
                    }
                    if (amount == null || amount <= 0.0) {
                        errorMessage = "Please enter a valid positive amount."
                        return@Button
                    }
                    val memberName = if (selectedMemberId == 0L) "All Family"
                    else members.find { it.id == selectedMemberId }?.name ?: "Family"
                    onConfirm(
                        title,
                        amount,
                        type,
                        selectedCategory,
                        selectedMemberId,
                        memberName,
                        paymentMethod
                    )
                },
                modifier = Modifier.testTag("save_transaction_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditMemberDialog(
    initialMember: FamilyMember?,
    currency: CurrencyOption,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        relation: String,
        role: String,
        age: Int,
        colorHex: String,
        iconName: String,
        monthlyAllowance: Double,
        monthlySpendingLimit: Double
    ) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var name by remember { mutableStateOf(initialMember?.name ?: "") }
    var relation by remember { mutableStateOf(initialMember?.relation ?: "Dad") }
    var role by remember { mutableStateOf(initialMember?.role ?: "Parent") }
    var ageText by remember { mutableStateOf(if (initialMember != null && initialMember.age > 0) initialMember.age.toString() else "35") }
    var allowanceText by remember { mutableStateOf(if (initialMember != null && initialMember.monthlyAllowance > 0) initialMember.monthlyAllowance.toInt().toString() else "0") }
    var limitText by remember { mutableStateOf(if (initialMember != null && initialMember.monthlySpendingLimit > 0) initialMember.monthlySpendingLimit.toInt().toString() else "1000") }
    var selectedColor by remember { mutableStateOf(initialMember?.avatarColorHex ?: "#3B82F6") }
    var selectedIcon by remember { mutableStateOf(initialMember?.avatarIcon ?: "face_man") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val presetColors = listOf("#3B82F6", "#EC4899", "#8B5CF6", "#F59E0B", "#10B981", "#06B6D4", "#F43F5E", "#6366F1")
    val presetRelations = listOf("Dad", "Mom", "Son", "Daughter", "Spouse", "Grandparent", "Other")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (initialMember == null) "Add Family Member" else "Edit Member Profile",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                if (initialMember != null && onDelete != null) {
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Member",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Member Name") },
                    placeholder = { Text("e.g. Sarah") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("member_name_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Relation / Role",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetRelations.forEach { rel ->
                        FilterChip(
                            selected = relation == rel,
                            onClick = {
                                relation = rel
                                if (rel in listOf("Son", "Daughter")) {
                                    role = "Child"
                                    if (selectedColor == "#3B82F6") selectedColor = "#8B5CF6"
                                } else {
                                    role = "Parent"
                                }
                            },
                            label = { Text(rel, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = ageText,
                        onValueChange = { ageText = it },
                        label = { Text("Age") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = limitText,
                        onValueChange = { limitText = it },
                        label = { Text("Spending Limit (${currency.symbol})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1.5f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = allowanceText,
                    onValueChange = { allowanceText = it },
                    label = { Text("Monthly Pocket Money/Allowance (${currency.symbol})") },
                    placeholder = { Text("0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Theme Color",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presetColors.forEach { colHex ->
                        val col = parseColor(colHex)
                        val isSelected = selectedColor == colHex
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(col)
                                .border(
                                    width = if (isSelected) 2.5.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = colHex }
                        )
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = "Please enter member name."
                        return@Button
                    }
                    val age = ageText.toIntOrNull() ?: 0
                    val allowance = allowanceText.toDoubleOrNull() ?: 0.0
                    val limit = limitText.toDoubleOrNull() ?: 1000.0

                    onSave(
                        name.trim(),
                        relation,
                        role,
                        age,
                        selectedColor,
                        selectedIcon,
                        allowance,
                        limit
                    )
                },
                modifier = Modifier.testTag("save_member_button")
            ) {
                Text(if (initialMember == null) "Add Member" else "Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    if (showDeleteConfirm && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete ${initialMember?.name ?: "Member"}?") },
            text = { Text("Are you sure you want to remove this family member?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AddBillDialog(
    members: List<FamilyMember>,
    currency: CurrencyOption,
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        amount: Double,
        daysUntilDue: Int,
        category: String,
        frequency: String,
        memberId: Long,
        assigneeName: String
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var daysText by remember { mutableStateOf("7") }
    var category by remember { mutableStateOf("Utilities") }
    var frequency by remember { mutableStateOf("Monthly") }
    var selectedMemberId by remember { mutableStateOf(members.firstOrNull()?.id ?: 0L) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Bill Reminder",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Bill Title") },
                    placeholder = { Text("e.g. Electric & Gas") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("bill_title_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (${currency.symbol})") },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("bill_amount_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = daysText,
                    onValueChange = { daysText = it },
                    label = { Text("Due In (Days)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    val days = daysText.toIntOrNull() ?: 7
                    if (title.isBlank()) {
                        errorMessage = "Please enter a bill title."
                        return@Button
                    }
                    if (amount == null || amount <= 0.0) {
                        errorMessage = "Please enter a valid amount."
                        return@Button
                    }
                    val memberName = members.find { it.id == selectedMemberId }?.name ?: "Family"
                    onConfirm(title, amount, days, category, frequency, selectedMemberId, memberName)
                },
                modifier = Modifier.testTag("save_bill_button")
            ) {
                Text("Schedule")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddGoalDialog(
    members: List<FamilyMember>,
    currency: CurrencyOption,
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        targetAmount: Double,
        initialAmount: Double,
        category: String,
        memberId: Long,
        memberName: String,
        colorHex: String,
        iconName: String,
        targetDays: Int
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var targetText by remember { mutableStateOf("") }
    var initialText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Vacation") }
    var targetDaysText by remember { mutableStateOf("90") }
    var selectedMemberId by remember { mutableStateOf(0L) } // 0 = All Family
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "New Savings Goal",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Goal Title") },
                    placeholder = { Text("e.g. Summer Vacation") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("goal_title_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = targetText,
                    onValueChange = { targetText = it },
                    label = { Text("Target Amount (${currency.symbol})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("goal_target_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = initialText,
                    onValueChange = { initialText = it },
                    label = { Text("Starting Deposit (${currency.symbol})") },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = targetDaysText,
                    onValueChange = { targetDaysText = it },
                    label = { Text("Target Timeframe (Days)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val target = targetText.toDoubleOrNull()
                    val initial = initialText.toDoubleOrNull() ?: 0.0
                    val days = targetDaysText.toIntOrNull() ?: 90
                    if (title.isBlank()) {
                        errorMessage = "Please enter goal name."
                        return@Button
                    }
                    if (target == null || target <= 0.0) {
                        errorMessage = "Target amount must be greater than zero."
                        return@Button
                    }
                    val memberName = if (selectedMemberId == 0L) "All Family"
                    else members.find { it.id == selectedMemberId }?.name ?: "Family"
                    onConfirm(
                        title,
                        target,
                        initial,
                        category,
                        selectedMemberId,
                        memberName,
                        "#0D9488",
                        "savings",
                        days
                    )
                },
                modifier = Modifier.testTag("save_goal_button")
            ) {
                Text("Create Goal")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ContributeGoalDialog(
    goal: SavingsGoal,
    members: List<FamilyMember>,
    currency: CurrencyOption,
    isAmountsHidden: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, memberId: Long, memberName: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var selectedMemberId by remember { mutableStateOf(members.firstOrNull()?.id ?: 0L) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add to \"${goal.title}\"",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Current: ${Formatters.formatCurrency(goal.currentAmount, currency, isAmountsHidden)} / ${Formatters.formatCurrency(goal.targetAmount, currency, isAmountsHidden)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Contribution Amount (${currency.symbol})") },
                    placeholder = { Text("50.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("contribute_amount_input")
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (amount == null || amount <= 0.0) {
                        errorMessage = "Please enter a valid amount."
                        return@Button
                    }
                    val member = members.find { it.id == selectedMemberId } ?: members.firstOrNull()
                    onConfirm(amount, member?.id ?: 0L, member?.name ?: "Family")
                },
                modifier = Modifier.testTag("confirm_contribute_button")
            ) {
                Text("Contribute")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun GiveAllowanceDialog(
    children: List<FamilyMember>,
    parents: List<FamilyMember>,
    currency: CurrencyOption,
    onDismiss: () -> Unit,
    onConfirm: (childId: Long, amount: Double, parentName: String, childName: String) -> Unit
) {
    var selectedChildId by remember { mutableStateOf(children.firstOrNull()?.id ?: 0L) }
    var selectedParentId by remember { mutableStateOf(parents.firstOrNull()?.id ?: 0L) }
    val currentChild = children.find { it.id == selectedChildId } ?: children.firstOrNull()
    var amountText by remember { mutableStateOf(currentChild?.monthlyAllowance?.toInt()?.toString() ?: "50") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Distribute Pocket Money",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (children.isEmpty()) {
                    Text(
                        text = "No children profiles found. Please add a child profile first in Family Members.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    Text(
                        text = "Transfer pocket money / allowance to child's balance.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Select Child",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        children.forEach { child ->
                            val isSelected = selectedChildId == child.id
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedChildId = child.id
                                    amountText = child.monthlyAllowance.toInt().toString()
                                },
                                label = { Text("${child.name} (${currency.symbol}${child.monthlyAllowance.toInt()})") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Transfer Amount (${currency.symbol})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("allowance_amount_input")
                    )
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            if (children.isNotEmpty()) {
                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull()
                        if (amount == null || amount <= 0.0) {
                            errorMessage = "Please enter valid allowance amount."
                            return@Button
                        }
                        val child = children.find { it.id == selectedChildId } ?: children.first()
                        val parent = parents.find { it.id == selectedParentId } ?: parents.firstOrNull()
                        val parentName = parent?.name ?: "Parent"
                        onConfirm(child.id, amount, parentName, child.name)
                    },
                    modifier = Modifier.testTag("transfer_allowance_button")
                ) {
                    Text("Transfer Allowance")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditBudgetDialog(
    budget: Budget,
    currency: CurrencyOption,
    onDismiss: () -> Unit,
    onConfirm: (Budget) -> Unit
) {
    var limitText by remember { mutableStateOf(budget.monthlyLimit.toInt().toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit ${budget.category} Budget",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Set the maximum family spending limit per month for ${budget.category}.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = limitText,
                    onValueChange = { limitText = it },
                    label = { Text("Monthly Limit (${currency.symbol})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_budget_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val limit = limitText.toDoubleOrNull() ?: budget.monthlyLimit
                    onConfirm(budget.copy(monthlyLimit = limit))
                }
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SetMonthlyBudgetDialog(
    currentBudget: Double,
    currency: CurrencyOption,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var budgetText by remember {
        mutableStateOf(if (currentBudget > 0) currentBudget.toInt().toString() else "")
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val presets = listOf(1000, 2000, 3000, 5000, 10000)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Set Monthly Budget",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Establish an overall monthly spending budget for the entire family. HomeFlow will track all monthly expenses against this target.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = budgetText,
                    onValueChange = {
                        budgetText = it
                        errorMessage = null
                    },
                    label = { Text("Total Monthly Budget (${currency.symbol})") },
                    placeholder = { Text("e.g. 3500") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("monthly_budget_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Quick Presets",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presets.forEach { preset ->
                        FilterChip(
                            selected = budgetText == preset.toString(),
                            onClick = { budgetText = preset.toString() },
                            label = { Text("${currency.symbol}$preset", fontSize = 11.sp) }
                        )
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = budgetText.toDoubleOrNull()
                    if (amount == null || amount < 0) {
                        errorMessage = "Please enter a valid budget amount."
                        return@Button
                    }
                    onConfirm(amount)
                },
                modifier = Modifier.testTag("save_monthly_budget_button")
            ) {
                Text("Save Budget")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddCategoryBudgetDialog(
    existingCategories: List<String>,
    currency: CurrencyOption,
    onDismiss: () -> Unit,
    onConfirm: (category: String, limit: Double) -> Unit
) {
    val defaultCategories = listOf(
        "Groceries",
        "Housing",
        "Dining",
        "Utilities",
        "Transportation",
        "Entertainment",
        "Healthcare",
        "Shopping",
        "Education",
        "Personal Care",
        "Other"
    )
    val availableCategories = defaultCategories.filter { it !in existingCategories }.ifEmpty { defaultCategories }

    var selectedCategory by remember { mutableStateOf(availableCategories.first()) }
    var limitText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Category Budget",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Select a category and assign a monthly spending limit.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    availableCategories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = limitText,
                    onValueChange = {
                        limitText = it
                        errorMessage = null
                    },
                    label = { Text("Monthly Limit (${currency.symbol})") },
                    placeholder = { Text("e.g. 500") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("category_budget_limit_input")
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val limit = limitText.toDoubleOrNull()
                    if (limit == null || limit <= 0.0) {
                        errorMessage = "Please enter a valid monthly limit."
                        return@Button
                    }
                    onConfirm(selectedCategory, limit)
                },
                modifier = Modifier.testTag("confirm_category_budget_button")
            ) {
                Text("Add Budget")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
