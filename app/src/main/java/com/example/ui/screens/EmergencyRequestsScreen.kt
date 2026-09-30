package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BloodComponent
import com.example.data.model.EmergencyRequestEntity
import com.example.data.model.UrgencyLevel
import com.example.ui.components.BloodGroupBadge
import com.example.ui.components.RequestStatusBadge
import com.example.ui.components.UrgencyBadge
import com.example.ui.theme.BorderGray
import com.example.ui.theme.CleanWhite
import com.example.ui.theme.RedContainer
import com.example.ui.theme.RedDark
import com.example.ui.theme.RedPrimary
import com.example.ui.theme.StatusFulfilled
import com.example.ui.theme.StatusGreenLight
import com.example.ui.theme.Urgent24h
import com.example.ui.theme.UrgentCritical
import com.example.ui.util.IntentHelper
import com.example.ui.util.StringsHelper
import com.example.ui.viewmodel.LifeLinkViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyRequestsScreen(
    viewModel: LifeLinkViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val language by viewModel.language.collectAsState()
    val allRequests by viewModel.allEmergencyRequests.collectAsState()
    val statusFilter by viewModel.emergencyStatusFilter.collectAsState()
    val broadcastSuccessMsg by viewModel.broadcastSuccessMessage.collectAsState()

    var showForm by remember { mutableStateOf(true) }

    // Form fields
    var patientName by remember { mutableStateOf("") }
    var selectedGroup by remember { mutableStateOf("O-") }
    var selectedComponent by remember { mutableStateOf(BloodComponent.WHOLE_BLOOD.displayName) }
    var unitsNeeded by remember { mutableIntStateOf(2) }
    var hospitalName by remember { mutableStateOf("") }
    var hospitalAddress by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Downtown Metro") }
    var urgency by remember { mutableStateOf(UrgencyLevel.CRITICAL) }
    var contactPerson by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val bloodGroups = listOf("O-", "O+", "A-", "A+", "B-", "B+", "AB-", "AB+")
    val components = listOf("Whole Blood", "Plasma", "Platelets")
    val cities = listOf("Downtown Metro", "West Hills", "Northside", "South Bay", "East Valley")

    val filteredRequests = remember(allRequests, statusFilter) {
        when (statusFilter) {
            "OPEN" -> allRequests.filter { it.status.equals("OPEN", ignoreCase = true) }
            "FULFILLED" -> allRequests.filter { it.status.equals("FULFILLED", ignoreCase = true) }
            else -> allRequests
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("emergency_requests_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Broadcast Success Alert Banner
        if (broadcastSuccessMsg != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("broadcast_success_banner"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = StatusGreenLight),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, StatusFulfilled)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = StatusFulfilled,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Emergency Alert Dispatched!",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = StatusFulfilled
                            )
                            Text(
                                text = broadcastSuccessMsg ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = { viewModel.clearBroadcastSuccessMessage() }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Dismiss")
                        }
                    }
                }
            }
        }

        // Emergency Request Form Header Card (Expandable)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("emergency_form_container_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, RedPrimary.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showForm = !showForm },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(RedContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Emergency,
                                    contentDescription = null,
                                    tint = UrgentCritical,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Send Emergency SOS Broadcast",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Black
                                    ),
                                    color = RedDark
                                )
                                Text(
                                    text = "Instantly alerts compatible donors within radius",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            imageVector = if (showForm) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (showForm) "Collapse" else "Expand",
                            tint = RedPrimary
                        )
                    }

                    AnimatedVisibility(
                        visible = showForm,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Patient Name
                            OutlinedTextField(
                                value = patientName,
                                onValueChange = { patientName = it },
                                label = { Text("Patient Full Name *") },
                                placeholder = { Text("e.g. David Miller") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("sos_patient_name_input"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            // Blood Group Selector
                            Column {
                                Text(
                                    text = "Blood Group Needed *",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    bloodGroups.forEach { group ->
                                        val isSel = selectedGroup == group
                                        FilterChip(
                                            selected = isSel,
                                            onClick = { selectedGroup = group },
                                            label = { Text(group, fontWeight = FontWeight.Black) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = RedPrimary,
                                                selectedLabelColor = CleanWhite
                                            ),
                                            shape = RoundedCornerShape(20.dp),
                                            modifier = Modifier.testTag("sos_group_$group")
                                        )
                                    }
                                }
                            }

                            // Component & Units Needed Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Component Selector
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Component",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        components.forEach { comp ->
                                            val isSel = selectedComponent == comp
                                            FilterChip(
                                                selected = isSel,
                                                onClick = { selectedComponent = comp },
                                                label = { Text(comp, fontSize = 12.sp) },
                                                shape = RoundedCornerShape(16.dp),
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = RedDark,
                                                    selectedLabelColor = CleanWhite
                                                )
                                            )
                                        }
                                    }
                                }

                                // Units Needed Counter
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Units Needed",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
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
                                                onClick = { if (unitsNeeded > 1) unitsNeeded-- },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease")
                                            }
                                            Text(
                                                text = "$unitsNeeded",
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Black
                                                ),
                                                modifier = Modifier.padding(horizontal = 8.dp)
                                            )
                                            IconButton(
                                                onClick = { if (unitsNeeded < 20) unitsNeeded++ },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Add, contentDescription = "Increase")
                                            }
                                        }
                                    }
                                }
                            }

                            // Urgency Level Selector
                            Column {
                                Text(
                                    text = "Urgency Level *",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    UrgencyLevel.entries.forEach { lvl ->
                                        val isSel = urgency == lvl
                                        val col = when (lvl) {
                                            UrgencyLevel.CRITICAL -> UrgentCritical
                                            UrgencyLevel.WITHIN_24H -> Urgent24h
                                            UrgencyLevel.PLANNED -> MaterialTheme.colorScheme.tertiary
                                        }
                                        FilterChip(
                                            selected = isSel,
                                            onClick = { urgency = lvl },
                                            label = {
                                                Text(
                                                    text = when (lvl) {
                                                        UrgencyLevel.CRITICAL -> "Critical"
                                                        UrgencyLevel.WITHIN_24H -> "< 24 Hours"
                                                        UrgencyLevel.PLANNED -> "Planned"
                                                    },
                                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = col,
                                                selectedLabelColor = CleanWhite
                                            ),
                                            shape = RoundedCornerShape(16.dp),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }

                            // Hospital Name & City
                            OutlinedTextField(
                                value = hospitalName,
                                onValueChange = { hospitalName = it },
                                label = { Text("Hospital / Medical Center *") },
                                placeholder = { Text("e.g. City General Trauma Center") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("sos_hospital_name_input"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            // City Row
                            Column {
                                Text(
                                    text = "City / District *",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    cities.forEach { c ->
                                        val isSel = city == c
                                        FilterChip(
                                            selected = isSel,
                                            onClick = { city = c },
                                            label = { Text(c) },
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                    }
                                }
                            }

                            // Contact Person & Phone
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = contactPerson,
                                    onValueChange = { contactPerson = it },
                                    label = { Text("Contact Person *") },
                                    placeholder = { Text("Family / Doctor") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("sos_contact_person_input"),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                OutlinedTextField(
                                    value = contactPhone,
                                    onValueChange = { contactPhone = it },
                                    label = { Text("Phone Number *") },
                                    placeholder = { Text("+1 (555)...") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("sos_contact_phone_input"),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            // Clinical Notes / Reason
                            OutlinedTextField(
                                value = notes,
                                onValueChange = { notes = it },
                                label = { Text("Clinical Reason / Instructions") },
                                placeholder = { Text("e.g. Accident trauma, surgery in Room 4B") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                maxLines = 3
                            )

                            // BIG RED BROADCAST BUTTON
                            Button(
                                onClick = {
                                    if (patientName.isBlank() || hospitalName.isBlank() || contactPhone.isBlank()) {
                                        Toast.makeText(context, "Please fill required fields (Patient, Hospital, Phone)", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    viewModel.broadcastEmergencyRequest(
                                        patientName = patientName,
                                        bloodGroup = selectedGroup,
                                        component = selectedComponent,
                                        unitsNeeded = unitsNeeded,
                                        hospitalName = hospitalName,
                                        hospitalAddress = hospitalAddress.ifBlank { "$hospitalName, $city" },
                                        city = city,
                                        urgency = urgency,
                                        contactPerson = contactPerson.ifBlank { "Emergency Contact" },
                                        contactPhone = contactPhone,
                                        notes = notes
                                    )
                                    // Reset form fields
                                    patientName = ""
                                    hospitalName = ""
                                    contactPerson = ""
                                    contactPhone = ""
                                    notes = ""
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .testTag("sos_broadcast_submit_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = UrgentCritical,
                                    contentColor = CleanWhite
                                ),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = CleanWhite,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "BROADCAST SOS ALERT NOW",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Feed Filter Header (All, Open, Fulfilled)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Live Emergency Feed (${filteredRequests.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("ALL", "OPEN", "FULFILLED").forEach { st ->
                        val isSel = statusFilter == st
                        FilterChip(
                            selected = isSel,
                            onClick = { viewModel.emergencyStatusFilter.value = st },
                            label = { Text(st, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.testTag("feed_filter_$st")
                        )
                    }
                }
            }
        }

        // Live Requests Feed Cards
        items(filteredRequests, key = { it.id }) { req ->
            EmergencyRequestCard(
                request = req,
                onCall = { IntentHelper.callPhone(context, req.contactPhone) },
                onShare = {
                    val shareMsg = """
                        🚨 URGENT BLOOD NEEDED: ${req.bloodGroup} (${req.unitsNeeded} units)
                        🏥 Hospital: ${req.hospitalName}, ${req.city}
                        👤 Patient: ${req.patientName}
                        ⚠️ Urgency: ${req.urgency}
                        📞 Contact: ${req.contactPerson} at ${req.contactPhone}
                        Please help share or donate via LifeLink!
                    """.trimIndent()
                    IntentHelper.shareEmergencySOS(context, shareMsg)
                },
                onCanDonate = {
                    val msg = "Hi, I saw your urgent LifeLink request for ${req.bloodGroup} blood for ${req.patientName} at ${req.hospitalName}. I am available to donate!"
                    IntentHelper.openWhatsApp(context, req.contactPhone, msg)
                },
                onMarkFulfilled = {
                    viewModel.markEmergencyFulfilled(req.id)
                }
            )
        }
    }
}

@Composable
fun EmergencyRequestCard(
    request: EmergencyRequestEntity,
    onCall: () -> Unit,
    onShare: () -> Unit,
    onCanDonate: () -> Unit,
    onMarkFulfilled: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isOpen = request.status.equals("OPEN", ignoreCase = true)
    val timeAgo = formatTimeAgo(request.createdAtMillis)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("emergency_request_card_${request.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = if (isOpen && request.urgency == UrgencyLevel.CRITICAL.name) {
            androidx.compose.foundation.BorderStroke(1.5.dp, UrgentCritical.copy(alpha = 0.5f))
        } else {
            androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
        }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Blood Group, Urgency & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BloodGroupBadge(
                        bloodGroup = request.bloodGroup,
                        isHighlighted = isOpen
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "${request.unitsNeeded} Units • ${request.component}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Patient: ${request.patientName}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    UrgencyBadge(urgencyName = request.urgency)
                    Spacer(modifier = Modifier.height(4.dp))
                    RequestStatusBadge(status = request.status)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = BorderGray)
            Spacer(modifier = Modifier.height(10.dp))

            // Hospital & Location
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocalHospital,
                    contentDescription = null,
                    tint = RedPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${request.hospitalName}, ${request.city}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (request.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "\"${request.notes}\"",
                    style = MaterialTheme.typography.bodySmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Matching Donors Alerted banner
            if (isOpen) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = RedContainer
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = RedDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${request.matchingDonorsAlerted} matching donors nearby alerted • $timeAgo",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = RedDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Actions: "I Can Donate" WhatsApp response, Call, Share, and Mark Fulfilled
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isOpen) {
                    Button(
                        onClick = onCanDonate,
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("can_donate_button_${request.id}"),
                        colors = ButtonDefaults.buttonColors(containerColor = RedPrimary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "I Can Donate",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = CleanWhite
                        )
                    }
                }

                OutlinedButton(
                    onClick = onCall,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("call_request_button_${request.id}"),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call",
                        modifier = Modifier.size(16.dp),
                        tint = RedPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Call", style = MaterialTheme.typography.labelMedium)
                }

                IconButton(
                    onClick = onShare,
                    modifier = Modifier
                        .size(42.dp)
                        .border(1.dp, BorderGray, RoundedCornerShape(10.dp))
                        .testTag("share_request_button_${request.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (isOpen) {
                    IconButton(
                        onClick = onMarkFulfilled,
                        modifier = Modifier
                            .size(42.dp)
                            .background(StatusGreenLight, RoundedCornerShape(10.dp))
                            .border(1.dp, StatusFulfilled.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .testTag("fulfill_request_button_${request.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Mark Fulfilled",
                            tint = StatusFulfilled
                        )
                    }
                }
            }
        }
    }
}
