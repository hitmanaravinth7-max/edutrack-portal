package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.data.model.BloodGroup
import com.example.ui.components.BloodGroupBadge
import com.example.ui.theme.BorderGray
import com.example.ui.theme.CleanWhite
import com.example.ui.theme.RedContainer
import com.example.ui.theme.RedDark
import com.example.ui.theme.RedPrimary
import com.example.ui.theme.StatusFulfilled
import com.example.ui.theme.StatusGreenLight
import com.example.ui.theme.UrgentCritical

data class MythFact(
    val myth: String,
    val fact: String
)

data class FaqItem(
    val question: String,
    val answer: String
)

@Composable
fun EducationScreen(
    modifier: Modifier = Modifier
) {
    val myths = listOf(
        MythFact(
            myth = "Donating blood makes you weak and takes weeks to recover.",
            fact = "Fact: Your body replenishes blood volume (plasma) within 24 to 48 hours. Red blood cells regenerate within a few weeks. Most donors feel completely normal right after their refreshment!"
        ),
        MythFact(
            myth = "You can contract infections like HIV or Hepatitis from the needle.",
            fact = "Fact: 100% false! Certified blood banks use brand new, sterile, disposable single-use needles and collection tubing. It is physically impossible to get an infection from donating."
        ),
        MythFact(
            myth = "Blood donation takes several painful hours.",
            fact = "Fact: The actual blood collection takes only 8 to 10 minutes. The whole visit including health screening, questionnaire, and refreshment is typically under 40 minutes."
        ),
        MythFact(
            myth = "Vegetarians or vegans have weak blood and cannot donate.",
            fact = "Fact: Diet does not disqualify anyone! As long as your routine hemoglobin test on donation day is above 12.5 g/dL, you can donate safely."
        ),
        MythFact(
            myth = "You only need to donate during major national emergencies.",
            fact = "Fact: Every 2 seconds, someone needs blood for childbirth, cancer treatments, organ transplants, and trauma injuries. Blood has a limited shelf-life (platelets only 5 days!), so consistent regular donations are critical."
        )
    )

    val faqs = listOf(
        FaqItem(
            question = "How often can I donate blood?",
            answer = "For whole blood donations, you must wait at least 90 days (3 months for men, and usually 3-4 months for women depending on national guidelines) to let iron stores recover fully."
        ),
        FaqItem(
            question = "Why is O- negative called the Universal Donor?",
            answer = "O- red blood cells lack A, B, and Rh antigens, meaning any recipient's immune system will accept O- blood without an adverse transfusion reaction in urgent emergency trauma situations."
        ),
        FaqItem(
            question = "What should I eat or drink before donating?",
            answer = "Drink 500ml of water or juice 1-2 hours prior. Have a light, healthy meal avoiding greasy fatty foods, and avoid alcohol for 24 hours."
        ),
        FaqItem(
            question = "Can I donate if I got a tattoo or piercing?",
            answer = "Generally, you can donate 6 months after getting a tattoo or body piercing, provided it was done in a state-regulated facility using sterile equipment."
        )
    )

    val eligibilityChecklist = listOf(
        "Age between 18 and 65 years old",
        "Body weight at least 50 kg (110 lbs)",
        "Hemoglobin level ≥ 12.5 g/dL (checked on site)",
        "At least 90 days since your last whole blood donation",
        "No fever, active cold, or flu symptoms in the last 48 hours",
        "Pulse rate normal (60 to 100 bpm) and blood pressure normal"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("education_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section: Eligibility Checklist
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(StatusGreenLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Rule,
                                contentDescription = null,
                                tint = StatusFulfilled,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Donor Eligibility Criteria",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    eligibilityChecklist.forEach { item ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = StatusFulfilled,
                                modifier = Modifier
                                    .size(18.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Section: Blood Compatibility Matrix Quick Reference
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(RedContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = RedDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Blood Compatibility Guide",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = RedDark
                            )
                            Text(
                                text = "Universal Donor: O- | Universal Recipient: AB+",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Compatibility rows
                    listOf(
                        "O-" to "Can donate to ALL (Universal Donor) • Can receive ONLY from O-",
                        "O+" to "Can donate to O+, A+, B+, AB+ • Can receive from O-, O+",
                        "A-" to "Can donate to A-, A+, AB-, AB+ • Can receive from O-, A-",
                        "A+" to "Can donate to A+, AB+ • Can receive from O-, O+, A-, A+",
                        "B-" to "Can donate to B-, B+, AB-, AB+ • Can receive from O-, B-",
                        "B+" to "Can donate to B+, AB+ • Can receive from O-, O+, B-, B+",
                        "AB-" to "Can donate to AB-, AB+ • Can receive from O-, A-, B-, AB-",
                        "AB+" to "Can donate ONLY to AB+ • Can receive from ALL (Universal Recipient)"
                    ).forEach { (group, desc) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BloodGroupBadge(bloodGroup = group, size = 36)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        HorizontalDivider(color = BorderGray.copy(alpha = 0.5f))
                    }
                }
            }
        }

        // Section: Myths vs Facts
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = null,
                    tint = RedPrimary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Busting Common Myths",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        items(myths) { mythFact ->
            var expanded by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (expanded) Color(0xFFFFF8E1) else MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "❌ Myth: ",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                                color = UrgentCritical
                            )
                            Text(
                                text = mythFact.myth,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = RedPrimary
                        )
                    }

                    AnimatedVisibility(visible = expanded) {
                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            HorizontalDivider(color = BorderGray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = mythFact.fact,
                                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Section: Frequently Asked Questions (FAQs)
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Help,
                    contentDescription = null,
                    tint = RedDark,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Frequently Asked Questions",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        items(faqs) { faq ->
            var expanded by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = faq.question,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = RedPrimary
                        )
                    }

                    AnimatedVisibility(visible = expanded) {
                        Column(modifier = Modifier.padding(top = 8.dp)) {
                            HorizontalDivider(color = BorderGray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = faq.answer,
                                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
