package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UrgencyLevel
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
import com.example.ui.theme.UrgentPlanned
import com.example.ui.util.AppLanguage
import com.example.ui.util.StringsHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LifeLinkTopAppBar(
    language: AppLanguage,
    onToggleLanguage: () -> Unit,
    onSosClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier.testTag("lifelink_top_app_bar"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(RedPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "LifeLink Logo",
                        tint = CleanWhite,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = StringsHelper.t("app_title", language),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.5).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = StringsHelper.t("app_subtitle", language),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        },
        actions = {
            // Language Switcher Button
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .padding(end = 6.dp)
                    .clickable { onToggleLanguage() }
                    .testTag("language_switch_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = language.flag, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = language.name,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            // Quick SOS Button in Top Bar
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = UrgentCritical,
                modifier = Modifier
                    .clickable { onSosClick() }
                    .testTag("top_bar_sos_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Emergency,
                        contentDescription = "SOS",
                        tint = CleanWhite,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "SOS",
                        color = CleanWhite,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun BloodGroupBadge(
    bloodGroup: String,
    modifier: Modifier = Modifier,
    size: Int = 46,
    isHighlighted: Boolean = false
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(if (isHighlighted) RedPrimary else RedContainer)
            .border(
                width = 1.5.dp,
                color = if (isHighlighted) RedDark else RedPrimary.copy(alpha = 0.4f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = bloodGroup,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Black,
                fontSize = (size * 0.38).sp
            ),
            color = if (isHighlighted) CleanWhite else RedDark
        )
    }
}

@Composable
fun UrgencyBadge(
    urgencyName: String,
    modifier: Modifier = Modifier
) {
    val urgency = UrgencyLevel.fromString(urgencyName)
    val (bgColor, textColor, label) = when (urgency) {
        UrgencyLevel.CRITICAL -> Triple(UrgentCritical.copy(alpha = 0.12f), UrgentCritical, "CRITICAL")
        UrgencyLevel.WITHIN_24H -> Triple(Urgent24h.copy(alpha = 0.12f), Urgent24h, "WITHIN 24H")
        UrgencyLevel.PLANNED -> Triple(UrgentPlanned.copy(alpha = 0.12f), UrgentPlanned, "PLANNED")
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (urgency == UrgencyLevel.CRITICAL) 1.08f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "urgencyPulse"
    )

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        modifier = modifier.scale(pulseScale)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (urgency == UrgencyLevel.CRITICAL) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(UrgentCritical)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = label,
                color = textColor,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun RequestStatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val isOpen = status.equals("OPEN", ignoreCase = true)
    val bg = if (isOpen) RedContainer else StatusGreenLight
    val textCol = if (isOpen) StatusOpen else StatusFulfilled
    val label = if (isOpen) "OPEN SOS" else "FULFILLED"

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bg,
        modifier = modifier
    ) {
        Text(
            text = label,
            color = textCol,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun StockPill(
    units: Int,
    modifier: Modifier = Modifier
) {
    val (bg, fg, text) = when {
        units == 0 -> Triple(RedContainer, UrgentCritical, "Out of Stock")
        units < 5 -> Triple(Color(0xFFFFF3E0), Urgent24h, "$units units (Low)")
        else -> Triple(StatusGreenLight, StatusFulfilled, "$units units available")
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bg,
        modifier = modifier
    ) {
        Text(
            text = text,
            color = fg,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}
