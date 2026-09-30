package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BloodBankEntity
import com.example.data.model.DonorEntity
import com.example.ui.components.BloodGroupBadge
import com.example.ui.theme.BorderGray
import com.example.ui.theme.CleanWhite
import com.example.ui.theme.RedContainer
import com.example.ui.theme.RedDark
import com.example.ui.theme.RedPrimary
import com.example.ui.theme.StatusFulfilled
import com.example.ui.theme.StatusGreenLight
import com.example.ui.theme.UrgentCritical
import com.example.ui.util.GeoUtil
import com.example.ui.util.IntentHelper
import com.example.ui.viewmodel.LifeLinkViewModel

data class MapPin(
    val id: String,
    val isBank: Boolean,
    val title: String,
    val subtitle: String,
    val bloodGroupOrUnits: String,
    val normalizedX: Float, // 0.1f .. 0.9f
    val normalizedY: Float, // 0.1f .. 0.9f
    val bankRef: BloodBankEntity? = null,
    val donorRef: DonorEntity? = null
)

@Composable
fun InteractiveMapScreen(
    viewModel: LifeLinkViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allBanks by viewModel.allBloodBanks.collectAsState()
    val allDonors by viewModel.allDonors.collectAsState()

    var filterType by remember { mutableStateOf("ALL") } // ALL, BANKS, DONORS
    var selectedPin by remember { mutableStateOf<MapPin?>(null) }

    // Map latitude/longitude to normalized 2D canvas coordinates
    // Center: Lat 37.7749, Lng -122.4194 (Downtown)
    val pins = remember(allBanks, allDonors, filterType) {
        val list = mutableListOf<MapPin>()

        val minLat = 37.7300
        val maxLat = 37.8200
        val minLng = -122.4600
        val maxLng = -122.3800

        fun normX(lng: Double): Float {
            return ((lng - minLng) / (maxLng - minLng)).toFloat().coerceIn(0.12f, 0.88f)
        }

        fun normY(lat: Double): Float {
            // Note: Canvas Y is top-to-bottom, so maxLat is at the top
            return (1f - ((lat - minLat) / (maxLat - minLat)).toFloat()).coerceIn(0.12f, 0.88f)
        }

        if (filterType == "ALL" || filterType == "BANKS") {
            allBanks.forEach { bank ->
                list.add(
                    MapPin(
                        id = "bank_${bank.id}",
                        isBank = true,
                        title = bank.name,
                        subtitle = "${bank.city} • Verified Center",
                        bloodGroupOrUnits = "Blood Bank",
                        normalizedX = normX(bank.longitude),
                        normalizedY = normY(bank.latitude),
                        bankRef = bank
                    )
                )
            }
        }

        if (filterType == "ALL" || filterType == "DONORS") {
            allDonors.forEach { donor ->
                list.add(
                    MapPin(
                        id = "donor_${donor.id}",
                        isBank = false,
                        title = donor.fullName,
                        subtitle = "${donor.city} • ${donor.age} yrs",
                        bloodGroupOrUnits = donor.bloodGroup,
                        normalizedX = normX(donor.longitude),
                        normalizedY = normY(donor.latitude),
                        donorRef = donor
                    )
                )
            }
        }

        list
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("interactive_map_screen")
    ) {
        // Map Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(pins) {
                    detectTapGestures { tapOffset ->
                        val w = size.width
                        val h = size.height

                        // Find closest pin within 32.dp tap target
                        var closest: MapPin? = null
                        var minDistanceSq = 36f * 36f * density * density

                        pins.forEach { pin ->
                            val px = pin.normalizedX * w
                            val py = pin.normalizedY * h
                            val dx = tapOffset.x - px
                            val dy = tapOffset.y - py
                            val distSq = dx * dx + dy * dy
                            if (distSq < minDistanceSq) {
                                minDistanceSq = distSq
                                closest = pin
                            }
                        }
                        selectedPin = closest
                    }
                }
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Background City Grid Map Texture
            drawRect(color = Color(0xFFF1F5F9))

            // Map grid roads
            val roadColor = Color(0xFFE2E8F0)
            val highwayColor = Color(0xFFFFE0B2)

            for (i in 0..12) {
                val y = canvasHeight * (i / 12f)
                drawLine(
                    color = roadColor,
                    start = Offset(0f, y),
                    end = Offset(canvasWidth, y),
                    strokeWidth = 3f
                )
            }
            for (i in 0..8) {
                val x = canvasWidth * (i / 8f)
                drawLine(
                    color = roadColor,
                    start = Offset(x, 0f),
                    end = Offset(x, canvasHeight),
                    strokeWidth = 3f
                )
            }

            // Diagonal Express Highways
            drawLine(
                color = highwayColor,
                start = Offset(0f, canvasHeight * 0.25f),
                end = Offset(canvasWidth, canvasHeight * 0.75f),
                strokeWidth = 10f
            )
            drawLine(
                color = highwayColor,
                start = Offset(canvasWidth * 0.15f, 0f),
                end = Offset(canvasWidth * 0.85f, canvasHeight),
                strokeWidth = 8f
            )

            // User Location Pulse Center (Downtown)
            val userX = canvasWidth * 0.5f
            val userY = canvasHeight * 0.5f
            drawCircle(
                color = Color(0x330288D1),
                radius = 32f,
                center = Offset(userX, userY)
            )
            drawCircle(
                color = Color(0xFF0288D1),
                radius = 10f,
                center = Offset(userX, userY)
            )
            drawCircle(
                color = CleanWhite,
                radius = 4f,
                center = Offset(userX, userY)
            )

            // Draw Pins
            pins.forEach { pin ->
                val px = pin.normalizedX * canvasWidth
                val py = pin.normalizedY * canvasHeight
                val isSelected = selectedPin?.id == pin.id

                val pinRadius = if (isSelected) 22f else 16f
                val pinColor = if (pin.isBank) RedDark else RedPrimary

                // Outer aura when selected
                if (isSelected) {
                    drawCircle(
                        color = RedPrimary.copy(alpha = 0.35f),
                        radius = pinRadius + 14f,
                        center = Offset(px, py)
                    )
                }

                // Pin base circle
                drawCircle(
                    color = pinColor,
                    radius = pinRadius,
                    center = Offset(px, py)
                )
                drawCircle(
                    color = CleanWhite,
                    radius = pinRadius,
                    center = Offset(px, py),
                    style = Stroke(width = 3f)
                )

                if (pin.isBank) {
                    // White Medical Cross in Pin
                    val armW = pinRadius * 0.3f
                    val armL = pinRadius * 0.65f
                    // Horizontal
                    drawRoundRect(
                        color = CleanWhite,
                        topLeft = Offset(px - armL, py - armW),
                        size = Size(armL * 2, armW * 2),
                        cornerRadius = CornerRadius(2f, 2f)
                    )
                    // Vertical
                    drawRoundRect(
                        color = CleanWhite,
                        topLeft = Offset(px - armW, py - armL),
                        size = Size(armW * 2, armL * 2),
                        cornerRadius = CornerRadius(2f, 2f)
                    )
                } else {
                    // White Blood Droplet in Pin
                    val dotRadius = pinRadius * 0.42f
                    drawCircle(
                        color = CleanWhite,
                        radius = dotRadius,
                        center = Offset(px, py)
                    )
                }
            }
        }

        // Top Filter Floating Chips
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            shadowElevation = 4.dp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "ALL" to "All Locations",
                    "BANKS" to "🏥 Blood Banks",
                    "DONORS" to "🩸 Donors"
                ).forEach { (type, label) ->
                    val isSel = filterType == type
                    FilterChip(
                        selected = isSel,
                        onClick = {
                            filterType = type
                            selectedPin = null
                        },
                        label = { Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RedPrimary,
                            selectedLabelColor = CleanWhite
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }
        }

        // Map Legend Indicator
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            shadowElevation = 2.dp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp, top = 76.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0288D1))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "You are here", style = MaterialTheme.typography.labelSmall)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(RedDark)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Blood Bank (+)", style = MaterialTheme.typography.labelSmall)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(RedPrimary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Donor (🩸)", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        // Selected Pin Detail Bottom Card
        AnimatedVisibility(
            visible = selectedPin != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            selectedPin?.let { pin ->
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (pin.isBank) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(RedContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocalHospital,
                                            contentDescription = null,
                                            tint = RedDark,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                } else {
                                    BloodGroupBadge(
                                        bloodGroup = pin.bloodGroupOrUnits,
                                        size = 46,
                                        isHighlighted = true
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = pin.title,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = pin.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            IconButton(onClick = { selectedPin = null }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Action Buttons based on type
                        if (pin.isBank && pin.bankRef != null) {
                            val bank = pin.bankRef
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { IntentHelper.callPhone(context, bank.phone) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Call, contentDescription = "Call", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Call")
                                }
                                Button(
                                    onClick = {
                                        IntentHelper.openDirections(context, bank.latitude, bank.longitude, bank.name)
                                    },
                                    modifier = Modifier.weight(1.3f),
                                    colors = ButtonDefaults.buttonColors(containerColor = RedPrimary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Directions, contentDescription = "Directions", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Directions")
                                }
                            }
                        } else if (!pin.isBank && pin.donorRef != null) {
                            val donor = pin.donorRef
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { IntentHelper.callPhone(context, donor.phone) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Call, contentDescription = "Call", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Call")
                                }
                                Button(
                                    onClick = {
                                        val msg = "Hi ${donor.fullName}, saw your profile on LifeLink. Are you available for a ${donor.bloodGroup} donation?"
                                        IntentHelper.openWhatsApp(context, donor.phone, msg)
                                    },
                                    modifier = Modifier.weight(1.3f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Message, contentDescription = "WhatsApp", modifier = Modifier.size(16.dp), tint = CleanWhite)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("WhatsApp", color = CleanWhite)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
