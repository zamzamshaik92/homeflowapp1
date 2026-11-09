package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.NavigationTab

@Composable
fun HomeFlowDrawerSheet(
    currentTab: NavigationTab,
    selectedCurrencyCode: String,
    onSelectTab: (NavigationTab) -> Unit,
    onExportPdf: () -> Unit,
    onOpenSettings: () -> Unit,
    onRateUs: () -> Unit,
    onShareApp: () -> Unit,
    onAboutApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        modifier = modifier
            .width(310.dp)
            .fillMaxHeight()
            .testTag("homeflow_drawer_sheet"),
        drawerContainerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(vertical = 12.dp)
        ) {
            // Header Banner (Clicking returns to Overview)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onSelectTab(NavigationTab.DASHBOARD) }
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "HomeFlow",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Family Financial Hub",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Divider(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            // Section 1: Core Navigation Items
            DrawerSectionHeader("FINANCIAL MANAGEMENT")

            DrawerMenuItem(
                label = "Family Members",
                icon = Icons.Default.FamilyRestroom,
                selected = currentTab == NavigationTab.FAMILY,
                onClick = { onSelectTab(NavigationTab.FAMILY) }
            )

            DrawerMenuItem(
                label = "Transactions",
                icon = Icons.Default.ReceiptLong,
                selected = currentTab == NavigationTab.TRANSACTIONS,
                onClick = { onSelectTab(NavigationTab.TRANSACTIONS) }
            )

            DrawerMenuItem(
                label = "Savings",
                icon = Icons.Default.Savings,
                selected = currentTab == NavigationTab.GOALS,
                onClick = { onSelectTab(NavigationTab.GOALS) }
            )

            DrawerMenuItem(
                label = "Budget",
                icon = Icons.Default.PieChart,
                selected = currentTab == NavigationTab.BUDGETS,
                onClick = { onSelectTab(NavigationTab.BUDGETS) }
            )

            DrawerMenuItem(
                label = "Expenses",
                icon = Icons.Default.Payment,
                selected = currentTab == NavigationTab.EXPENSES,
                onClick = { onSelectTab(NavigationTab.EXPENSES) }
            )

            DrawerMenuItem(
                label = "Export PDF Report",
                icon = Icons.Default.PictureAsPdf,
                selected = false,
                onClick = onExportPdf
            )

            Divider(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Section 2: Utilities & App Info
            DrawerSectionHeader("APP & PREFERENCES")

            DrawerMenuItem(
                label = "Settings ($selectedCurrencyCode)",
                icon = Icons.Default.Settings,
                selected = false,
                onClick = onOpenSettings
            )

            DrawerMenuItem(
                label = "Rate Us on Google Play",
                icon = Icons.Default.StarRate,
                selected = false,
                onClick = onRateUs
            )

            DrawerMenuItem(
                label = "Share App",
                icon = Icons.Default.Share,
                selected = false,
                onClick = onShareApp
            )

            DrawerMenuItem(
                label = "About App",
                icon = Icons.Default.Info,
                selected = false,
                onClick = onAboutApp
            )

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "HomeFlow v1.2 • Zero-Data-Leak Privacy",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}

@Composable
private fun DrawerSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            letterSpacing = 1.sp
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
    )
}

@Composable
private fun DrawerMenuItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                )
            )
        },
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        },
        selected = selected,
        onClick = onClick,
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .testTag("drawer_item_${label.lowercase().replace(" ", "_").take(15)}")
    )
}
