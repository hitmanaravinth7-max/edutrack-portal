package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BloodStockEntity
import com.example.ui.components.BloodGroupBadge
import com.example.ui.theme.BorderGray
import com.example.ui.theme.CleanWhite
import com.example.ui.theme.RedContainer
import com.example.ui.theme.RedDark
import com.example.ui.theme.RedPrimary
import com.example.ui.theme.StatusFulfilled
import com.example.ui.theme.StatusGreenLight
import com.example.ui.theme.Urgent24h
import com.example.ui.theme.UrgentCritical
import com.example.ui.viewmodel.LifeLinkViewModel

@Composable
fun AdminStockScreen(
    viewModel: LifeLinkViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allBanks by viewModel.allBloodBanks.collectAsState()
    val allStock by viewModel.allStock.collectAsState()
    val selectedBankId by viewModel.adminSelectedBankId.collectAsState()

    var selectedComponentFilter by remember { mutableStateOf("All") }
    val componentOptions = listOf("All", "Whole Blood", "Plasma", "Platelets")

    val currentBank = allBanks.find { it.id == selectedBankId } ?: allBanks.firstOrNull()
    val bankStocks = allStock.filter { it.bankId == selectedBankId }

    val filteredStocks = if (selectedComponentFilter == "All") {
        bankStocks
    } else {
        bankStocks.filter { it.component.equals(selectedComponentFilter, ignoreCase = true) }
    }

    val lowStockCount = bankStocks.count { it.units < 5 }
    val nearExpiryCount = bankStocks.count { it.expiryDaysLeft <= 5 }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("admin_stock_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Blood Bank Selector Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_bank_selector_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalHospital,
                            contentDescription = null,
                            tint = RedPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Blood Bank / Hospital Center",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    // Bank Selector Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        allBanks.forEach { bank ->
                            val isSel = bank.id == selectedBankId
                            FilterChip(
                                selected = isSel,
                                onClick = { viewModel.adminSelectedBankId.value = bank.id },
                                label = { Text(bank.name, maxLines = 1) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RedPrimary,
                                    selectedLabelColor = CleanWhite
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.testTag("admin_bank_chip_${bank.id}")
                            )
                        }
                    }

                    currentBank?.let { b ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${b.address} • Contact: ${b.phone}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Summary Alerts: Low Stock & Near Expiry
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Low Stock Alert
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (lowStockCount > 0) RedContainer else StatusGreenLight
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (lowStockCount > 0) UrgentCritical.copy(alpha = 0.5f) else StatusFulfilled.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (lowStockCount > 0) Icons.Default.NotificationImportant else Icons.Default.Inventory,
                                contentDescription = null,
                                tint = if (lowStockCount > 0) UrgentCritical else StatusFulfilled,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Low Stock (<5u)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (lowStockCount > 0) RedDark else StatusFulfilled
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "$lowStockCount items",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                            color = if (lowStockCount > 0) UrgentCritical else StatusFulfilled
                        )
                    }
                }

                // Near Expiry Alert
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (nearExpiryCount > 0) Color(0xFFFFF3E0) else StatusGreenLight
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (nearExpiryCount > 0) Urgent24h.copy(alpha = 0.5f) else StatusFulfilled.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.HourglassBottom,
                                contentDescription = null,
                                tint = if (nearExpiryCount > 0) Urgent24h else StatusFulfilled,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Near Expiry (≤5d)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (nearExpiryCount > 0) Urgent24h else StatusFulfilled
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "$nearExpiryCount items",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                            color = if (nearExpiryCount > 0) Urgent24h else StatusFulfilled
                        )
                    }
                }
            }
        }

        // Component Filter Chips
        item {
            Column {
                Text(
                    text = "Filter by Component",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    componentOptions.forEach { comp ->
                        val isSel = selectedComponentFilter == comp
                        FilterChip(
                            selected = isSel,
                            onClick = { selectedComponentFilter = comp },
                            label = { Text(comp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RedDark,
                                selectedLabelColor = CleanWhite
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
            }
        }

        // Inventory Stock Items Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Live Inventory (${filteredStocks.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Use +/- to update units",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Stock List
        items(filteredStocks, key = { "${it.bankId}_${it.bloodGroup}_${it.component}" }) { stock ->
            AdminStockItemCard(
                stock = stock,
                onIncrement = {
                    viewModel.updateStockUnits(stock.bankId, stock.bloodGroup, stock.component, 1)
                },
                onDecrement = {
                    viewModel.updateStockUnits(stock.bankId, stock.bloodGroup, stock.component, -1)
                }
            )
        }
    }
}

@Composable
fun AdminStockItemCard(
    stock: BloodStockEntity,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCritical = stock.units == 0
    val isLow = stock.units in 1..4
    val isNearExpiry = stock.expiryDaysLeft <= 5

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("stock_item_${stock.bloodGroup}_${stock.component}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = if (isCritical) {
            androidx.compose.foundation.BorderStroke(1.5.dp, UrgentCritical)
        } else if (isLow) {
            androidx.compose.foundation.BorderStroke(1.dp, Urgent24h)
        } else {
            androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Group & Component info
            Row(verticalAlignment = Alignment.CenterVertically) {
                BloodGroupBadge(
                    bloodGroup = stock.bloodGroup,
                    size = 44,
                    isHighlighted = isLow || isCritical
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stock.component,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isCritical) {
                            Text(
                                text = "CRITICAL: Out of Stock",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                color = UrgentCritical
                            )
                        } else if (isLow) {
                            Text(
                                text = "LOW: Replenish Soon",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Urgent24h
                            )
                        } else {
                            Text(
                                text = "Stock Stable",
                                style = MaterialTheme.typography.labelSmall,
                                color = StatusFulfilled
                            )
                        }
                        Text(
                            text = " • Exp: ${stock.expiryDaysLeft}d",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isNearExpiry) Urgent24h else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Quick Counter Stepper: [-] Units [+]
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    IconButton(
                        onClick = onDecrement,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("dec_stock_${stock.bloodGroup}_${stock.component}")
                    ) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease")
                    }
                    Text(
                        text = "${stock.units}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        modifier = Modifier.padding(horizontal = 8.dp),
                        color = if (isCritical) UrgentCritical else MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(
                        onClick = onIncrement,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("inc_stock_${stock.bloodGroup}_${stock.component}")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Increase")
                    }
                }
            }
        }
    }
}
