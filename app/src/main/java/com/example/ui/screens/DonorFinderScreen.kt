package com.example.ui.screens

import android.app.AlertDialog
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.model.BloodGroup
import com.example.data.model.DonorEntity
import com.example.ui.components.BloodGroupBadge
import com.example.ui.theme.BorderGray
import com.example.ui.theme.CleanWhite
import com.example.ui.theme.RedContainer
import com.example.ui.theme.RedDark
import com.example.ui.theme.RedPrimary
import com.example.ui.theme.StatusFulfilled
import com.example.ui.theme.StatusGreenLight
import com.example.ui.theme.StatusOpen
import com.example.ui.theme.Urgent24h
import com.example.ui.theme.UrgentCritical
import com.example.ui.util.GeoUtil
import com.example.ui.util.IntentHelper
import com.example.ui.util.StringsHelper
import com.example.ui.viewmodel.DonorMatch
import com.example.ui.viewmodel.LifeLinkViewModel

@Composable
fun DonorFinderScreen(
    viewModel: LifeLinkViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val language by viewModel.language.collectAsState()
    val matchingDonors by viewModel.matchingDonors.collectAsState()
    val targetGroup by viewModel.donorSearchBloodGroup.collectAsState()
    val maxDist by viewModel.donorMaxDistanceKm.collectAsState()
    val showCompatibleOnly by viewModel.donorShowCompatibleOnly.collectAsState()
    val onlyAvailable by viewModel.donorOnlyAvailable.collectAsState()
    val revealedDonorIds by viewModel.revealedDonorIds.collectAsState()

    var donorToReveal by remember { mutableStateOf<DonorEntity?>(null) }
    var consentAgree by remember { mutableStateOf(false) }

    val bloodGroups = listOf("O-", "O+", "A-", "A+", "B-", "B+", "AB-", "AB+")
    val distanceOptions = listOf(5f, 15f, 30f, 50f, 100f)

    val currentTargetGroupEnum = BloodGroup.fromLabel(targetGroup) ?: BloodGroup.O_NEG
    val compatibleDonorGroups = BloodGroup.getCompatibleDonors(currentTargetGroupEnum).map { it.label }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("donor_finder_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Filter Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("donor_filter_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Recipient Needs Blood Group",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Recipient Blood Group Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        bloodGroups.forEach { group ->
                            val isSel = targetGroup == group
                            FilterChip(
                                selected = isSel,
                                onClick = { viewModel.donorSearchBloodGroup.value = group },
                                label = { Text(group, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RedPrimary,
                                    selectedLabelColor = CleanWhite
                                ),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.testTag("donor_search_group_$group")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Compatibility Explanation Callout
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = RedContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = RedDark,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Compatibility Rule for $targetGroup recipient:",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = RedDark
                                )
                                Text(
                                    text = if (targetGroup == "AB+") {
                                        "AB+ is Universal Recipient: can receive blood from ALL groups!"
                                    } else {
                                        "Can safely receive from: ${compatibleDonorGroups.joinToString(", ")}"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Distance Filter Chips
                    Text(
                        text = "Search Radius: ${maxDist.toInt()} km",
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
                        distanceOptions.forEach { d ->
                            val isSel = maxDist == d
                            FilterChip(
                                selected = isSel,
                                onClick = { viewModel.donorMaxDistanceKm.value = d },
                                label = { Text("${d.toInt()} km") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RedDark,
                                    selectedLabelColor = CleanWhite
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Toggles: Compatibility logic & Available only
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Include compatible blood groups",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                        Switch(
                            checked = showCompatibleOnly,
                            onCheckedChange = { viewModel.donorShowCompatibleOnly.value = it }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Only currently available donors",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                        Switch(
                            checked = onlyAvailable,
                            onCheckedChange = { viewModel.donorOnlyAvailable.value = it }
                        )
                    }
                }
            }
        }

        // Results Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Matching Donors (${matchingDonors.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = StatusFulfilled,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Privacy Protected",
                        style = MaterialTheme.typography.labelSmall,
                        color = StatusFulfilled
                    )
                }
            }
        }

        // Empty state
        if (matchingDonors.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No donors found in this radius",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Try increasing the distance slider or toggle 'Include compatible groups'",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Donor Match Cards
        items(matchingDonors, key = { it.donor.id }) { match ->
            val donor = match.donor
            val isRevealed = revealedDonorIds.contains(donor.id) || !donor.consentMaskPhone

            DonorCard(
                match = match,
                isContactRevealed = isRevealed,
                onRequestReveal = {
                    donorToReveal = donor
                    consentAgree = false
                },
                onCall = { IntentHelper.callPhone(context, donor.phone) },
                onWhatsApp = {
                    val msg = "Hello ${donor.fullName}, I found your contact on LifeLink. We have an urgent need for ${match.donor.bloodGroup} blood donation. Are you available to help?"
                    IntentHelper.openWhatsApp(context, donor.phone, msg)
                }
            )
        }
    }

    // Privacy Consent Request Dialog
    donorToReveal?.let { donor ->
        AlertDialog(
            onDismissRequest = { donorToReveal = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.LockOpen,
                    contentDescription = null,
                    tint = RedPrimary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Request Emergency Contact",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column {
                    Text(
                        text = "To protect donor privacy, phone numbers are masked. By requesting contact with ${donor.fullName} (${donor.bloodGroup}), you agree to use this information strictly for genuine medical blood requirements.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { consentAgree = !consentAgree }
                    ) {
                        Checkbox(
                            checked = consentAgree,
                            onCheckedChange = { consentAgree = it }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "I confirm this is for a medical emergency requirement.",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.revealDonorContact(donor.id)
                        donorToReveal = null
                    },
                    enabled = consentAgree,
                    colors = ButtonDefaults.buttonColors(containerColor = RedPrimary)
                ) {
                    Text("Reveal & Contact")
                }
            },
            dismissButton = {
                TextButton(onClick = { donorToReveal = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DonorCard(
    match: DonorMatch,
    isContactRevealed: Boolean,
    onRequestReveal: () -> Unit,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val donor = match.donor
    val isEligible = donor.isEligibleToDonate()
    val daysUntil = donor.daysUntilEligible()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("donor_card_${donor.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Blood Group, Name, Distance & Availability
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BloodGroupBadge(
                        bloodGroup = donor.bloodGroup,
                        isHighlighted = match.isDirectMatch
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = donor.fullName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified Donor",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Text(
                            text = "${donor.age} yrs • ${donor.city} • ${GeoUtil.formatDistance(match.distanceKm)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Availability Pill
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (donor.isAvailable) StatusGreenLight else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = if (donor.isAvailable) "Available" else "Busy",
                        color = if (donor.isAvailable) StatusFulfilled else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Compatibility Note Pill
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (match.isDirectMatch) RedContainer else Color(0xFFE3F2FD)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (match.isDirectMatch) RedDark else MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = match.compatibilityNote,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = if (match.isDirectMatch) RedDark else MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Eligibility Status (90 days cooldown)
            if (isEligible) {
                Text(
                    text = "✓ Eligible to donate (Cooldown satisfied)",
                    style = MaterialTheme.typography.labelSmall,
                    color = StatusFulfilled
                )
            } else {
                Text(
                    text = "⏳ Ineligible - $daysUntil days cooldown remaining (last donated recently)",
                    style = MaterialTheme.typography.labelSmall,
                    color = Urgent24h
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = BorderGray)
            Spacer(modifier = Modifier.height(10.dp))

            // Contact Area: Masked vs Revealed
            if (isContactRevealed) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Phone Number:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = donor.phone,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = RedDark
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onCall,
                            colors = ButtonDefaults.buttonColors(containerColor = RedPrimary),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Call, contentDescription = "Call", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Call", style = MaterialTheme.typography.labelMedium)
                        }

                        Button(
                            onClick = onWhatsApp,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Message, contentDescription = "WhatsApp", modifier = Modifier.size(16.dp), tint = CleanWhite)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "WhatsApp", style = MaterialTheme.typography.labelMedium, color = CleanWhite)
                        }
                    }
                }
            } else {
                // Masked View with Privacy Notice & Consent Trigger
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Masked",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = donor.maskedPhone(),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    OutlinedButton(
                        onClick = onRequestReveal,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("reveal_donor_${donor.id}")
                    ) {
                        Text(
                            text = "Reveal Contact",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = RedPrimary
                        )
                    }
                }
            }
        }
    }
}
