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
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BloodGroupBadge
import com.example.ui.theme.BorderGray
import com.example.ui.theme.CleanWhite
import com.example.ui.theme.RedContainer
import com.example.ui.theme.RedDark
import com.example.ui.theme.RedPrimary
import com.example.ui.theme.RedVibrant
import com.example.ui.theme.StatusFulfilled
import com.example.ui.theme.StatusGreenLight
import com.example.ui.theme.Urgent24h
import com.example.ui.theme.UrgentCritical
import com.example.ui.util.StringsHelper
import com.example.ui.viewmodel.LifeLinkViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun DonorProfileScreen(
    viewModel: LifeLinkViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val language by viewModel.language.collectAsState()
    val donor by viewModel.currentDonor.collectAsState()
    val history by viewModel.currentDonorHistory.collectAsState()

    var isEditing by remember { mutableStateOf(false) }

    // Edit fields
    var editName by remember(donor) { mutableStateOf(donor?.fullName ?: "Alex Rivera") }
    var editAge by remember(donor) { mutableIntStateOf(donor?.age ?: 28) }
    var editGroup by remember(donor) { mutableStateOf(donor?.bloodGroup ?: "O-") }
    var editCity by remember(donor) { mutableStateOf(donor?.city ?: "Downtown Metro") }
    var editPhone by remember(donor) { mutableStateOf(donor?.phone ?: "+1 (555) 789-0123") }
    var editEmail by remember(donor) { mutableStateOf(donor?.email ?: "alex.rivera@lifelink.org") }

    val currentDay = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())
    val daysSinceDonation = donor?.let { currentDay - it.lastDonationEpochDay } ?: 110L
    var editDaysAgo by remember(daysSinceDonation) { mutableIntStateOf(daysSinceDonation.toInt().coerceAtLeast(0)) }

    val bloodGroups = listOf("O-", "O+", "A-", "A+", "B-", "B+", "AB-", "AB+")
    val cities = listOf("Downtown Metro", "West Hills", "Northside", "South Bay", "East Valley")

    val isEligible = donor?.isEligibleToDonate() ?: (editDaysAgo >= 90 && editAge in 18..65)
    val daysRemaining = if (editDaysAgo >= 90) 0 else (90 - editDaysAgo)
    val totalDonations = donor?.totalDonationsCount ?: 6
    val livesSaved = totalDonations * 3

    val rankTitle = when {
        totalDonations >= 10 -> "Platinum Champion"
        totalDonations >= 5 -> "Gold Lifesaver"
        totalDonations >= 3 -> "Silver Guardian"
        else -> "Bronze Hero"
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("donor_profile_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Profile Header Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("donor_hero_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(listOf(RedDark, RedVibrant))
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                BloodGroupBadge(
                                    bloodGroup = donor?.bloodGroup ?: editGroup,
                                    size = 54,
                                    isHighlighted = true
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = donor?.fullName ?: editName,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Black
                                        ),
                                        color = CleanWhite
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.EmojiEvents,
                                            contentDescription = null,
                                            tint = Color(0xFFFFD700),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = rankTitle,
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color(0xFFFFD700)
                                        )
                                    }
                                }
                            }

                            IconButton(
                                onClick = { isEditing = !isEditing },
                                modifier = Modifier
                                    .background(CleanWhite.copy(alpha = 0.2f), CircleShape)
                                    .testTag("edit_profile_toggle_button")
                            ) {
                                Icon(
                                    imageVector = if (isEditing) Icons.Default.CheckCircle else Icons.Default.Edit,
                                    contentDescription = "Edit Profile",
                                    tint = CleanWhite
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stats Counters (Donations & Lives Saved)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = CleanWhite.copy(alpha = 0.15f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "$totalDonations",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                                        color = CleanWhite
                                    )
                                    Text(
                                        text = "Donations",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CleanWhite.copy(alpha = 0.9f)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = CleanWhite.copy(alpha = 0.15f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "$livesSaved",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                                        color = CleanWhite
                                    )
                                    Text(
                                        text = "Lives Saved",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CleanWhite.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Auto-Eligibility Calculation Card (90-day cooldown logic)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("donor_eligibility_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isEligible) StatusGreenLight else Color(0xFFFFF3E0)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    if (isEligible) StatusFulfilled.copy(alpha = 0.6f) else Urgent24h.copy(alpha = 0.6f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isEligible) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isEligible) StatusFulfilled else Urgent24h,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isEligible) "You are Eligible to Donate! 🩸" else "Donation Cooldown Active",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                                color = if (isEligible) StatusFulfilled else Urgent24h
                            )
                            Text(
                                text = if (isEligible) {
                                    "Your last donation was $daysSinceDonation days ago (safety cooldown >= 90 days satisfied)."
                                } else {
                                    "Safety protocol: 90 days cooldown required. $daysRemaining days left."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Progress bar for cooldown
                    val progress = (editDaysAgo.toFloat() / 90f).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (isEligible) StatusFulfilled else Urgent24h,
                        trackColor = BorderGray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Last donated: $editDaysAgo days ago",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (isEligible) "Ready" else "$daysRemaining days remaining",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isEligible) StatusFulfilled else Urgent24h
                        )
                    }
                }
            }
        }

        // Availability & Privacy Toggles
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Availability Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Available for Emergency Donations",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Allows patients and blood banks to broadcast alerts to you",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = donor?.isAvailable ?: true,
                            onCheckedChange = { viewModel.toggleCurrentDonorAvailability(it) },
                            modifier = Modifier.testTag("availability_toggle_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = BorderGray)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Privacy Consent Checkbox
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = donor?.consentMaskPhone ?: true,
                            onCheckedChange = { viewModel.toggleCurrentDonorConsent(it) },
                            modifier = Modifier.testTag("privacy_consent_checkbox")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Privacy Shield (Mask Phone Number)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = StringsHelper.t("privacy_consent_checkbox", language),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Edit Profile Form (Visible when isEditing)
        if (isEditing) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, RedPrimary)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Edit Donor Profile",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = RedDark
                        )

                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Full Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = editAge.toString(),
                                onValueChange = { editAge = it.toIntOrNull() ?: 18 },
                                label = { Text("Age (18-65)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = editDaysAgo.toString(),
                                onValueChange = { editDaysAgo = it.toIntOrNull() ?: 0 },
                                label = { Text("Days Ago Last Donated") },
                                singleLine = true,
                                modifier = Modifier.weight(1.3f)
                            )
                        }

                        // Blood Group Selection
                        Column {
                            Text(text = "Blood Group", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                bloodGroups.forEach { g ->
                                    val isSel = editGroup == g
                                    FilterChip(
                                        selected = isSel,
                                        onClick = { editGroup = g },
                                        label = { Text(g, fontWeight = FontWeight.Bold) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = RedPrimary,
                                            selectedLabelColor = CleanWhite
                                        )
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = editPhone,
                            onValueChange = { editPhone = it },
                            label = { Text("Phone Number") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = editEmail,
                            onValueChange = { editEmail = it },
                            label = { Text("Email Address") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                viewModel.updateDonorProfile(
                                    name = editName,
                                    age = editAge,
                                    bloodGroup = editGroup,
                                    city = editCity,
                                    phone = editPhone,
                                    email = editEmail,
                                    daysAgoLastDonation = editDaysAgo,
                                    isAvailable = donor?.isAvailable ?: true,
                                    consentMaskPhone = donor?.consentMaskPhone ?: true
                                )
                                isEditing = false
                                Toast.makeText(context, "Donor Profile Updated!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = RedPrimary)
                        ) {
                            Icon(imageVector = Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save Profile Changes", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Donation History Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = RedPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Donation History",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "${history.size} records",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // History Items
        items(history, key = { it.id }) { item ->
            val dateStr = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date(item.dateEpochMillis))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = item.bloodBankOrHospital,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$dateStr • ${item.component}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = RedContainer
                    ) {
                        Text(
                            text = "+${item.unitsDonated} unit",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                            color = RedDark,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
