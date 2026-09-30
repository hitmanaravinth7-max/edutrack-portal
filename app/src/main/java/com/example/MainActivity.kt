package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.LifeLinkTopAppBar
import com.example.ui.screens.AdminStockScreen
import com.example.ui.screens.BloodAvailabilityScreen
import com.example.ui.screens.DonorFinderScreen
import com.example.ui.screens.DonorProfileScreen
import com.example.ui.screens.EducationScreen
import com.example.ui.screens.EmergencyRequestsScreen
import com.example.ui.screens.InteractiveMapScreen
import com.example.ui.theme.BorderGray
import com.example.ui.theme.CleanWhite
import com.example.ui.theme.LifeLinkTheme
import com.example.ui.theme.RedContainer
import com.example.ui.theme.RedDark
import com.example.ui.theme.RedPrimary
import com.example.ui.theme.UrgentCritical
import com.example.ui.util.StringsHelper
import com.example.ui.viewmodel.LifeLinkViewModel
import com.example.ui.viewmodel.ScreenTab

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LifeLinkTheme {
                LifeLinkApp()
            }
        }
    }
}

@Composable
fun LifeLinkApp(
    viewModel: LifeLinkViewModel = viewModel()
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val language by viewModel.language.collectAsState()
    val allRequests by viewModel.allEmergencyRequests.collectAsState()
    val openRequestsCount = allRequests.count { it.status.equals("OPEN", ignoreCase = true) }

    // Handle Back Press to always return to Home/Availability if on secondary screen
    BackHandler(enabled = currentTab != ScreenTab.AVAILABILITY) {
        viewModel.setTab(ScreenTab.AVAILABILITY)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column {
                LifeLinkTopAppBar(
                    language = language,
                    onToggleLanguage = { viewModel.toggleLanguage() },
                    onSosClick = { viewModel.setTab(ScreenTab.EMERGENCY_SOS) }
                )

                // Quick Navigation Sub-Bar (Stock Admin & Education Guide chips)
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = currentTab == ScreenTab.ADMIN_STOCK,
                            onClick = { viewModel.setTab(ScreenTab.ADMIN_STOCK) },
                            label = { Text("🏥 Blood Bank Admin") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Inventory,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RedDark,
                                selectedLabelColor = CleanWhite
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.testTag("nav_chip_admin")
                        )

                        FilterChip(
                            selected = currentTab == ScreenTab.EDUCATION,
                            onClick = { viewModel.setTab(ScreenTab.EDUCATION) },
                            label = { Text("📖 Guide & Myths") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RedDark,
                                selectedLabelColor = CleanWhite
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.testTag("nav_chip_education")
                        )
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                // 1. Find Blood
                NavigationBarItem(
                    selected = currentTab == ScreenTab.AVAILABILITY,
                    onClick = { viewModel.setTab(ScreenTab.AVAILABILITY) },
                    icon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Find Blood")
                    },
                    label = {
                        Text(
                            text = StringsHelper.t("tab_availability", language),
                            maxLines = 1,
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = RedDark,
                        indicatorColor = RedContainer
                    ),
                    modifier = Modifier.testTag("tab_item_availability")
                )

                // 2. SOS Emergency
                NavigationBarItem(
                    selected = currentTab == ScreenTab.EMERGENCY_SOS,
                    onClick = { viewModel.setTab(ScreenTab.EMERGENCY_SOS) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (openRequestsCount > 0) {
                                    Badge(containerColor = UrgentCritical) {
                                        Text("$openRequestsCount", color = CleanWhite)
                                    }
                                }
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Emergency, contentDescription = "SOS Broadcast")
                        }
                    },
                    label = {
                        Text(
                            text = StringsHelper.t("tab_emergency", language),
                            maxLines = 1,
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = UrgentCritical,
                        indicatorColor = RedContainer
                    ),
                    modifier = Modifier.testTag("tab_item_emergency")
                )

                // 3. Donor Finder
                NavigationBarItem(
                    selected = currentTab == ScreenTab.DONOR_FINDER,
                    onClick = { viewModel.setTab(ScreenTab.DONOR_FINDER) },
                    icon = {
                        Icon(imageVector = Icons.Default.PersonSearch, contentDescription = "Donors")
                    },
                    label = {
                        Text(
                            text = StringsHelper.t("tab_donors", language),
                            maxLines = 1,
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = RedDark,
                        indicatorColor = RedContainer
                    ),
                    modifier = Modifier.testTag("tab_item_donors")
                )

                // 4. Map View
                NavigationBarItem(
                    selected = currentTab == ScreenTab.MAP_VIEW,
                    onClick = { viewModel.setTab(ScreenTab.MAP_VIEW) },
                    icon = {
                        Icon(imageVector = Icons.Default.Map, contentDescription = "Map")
                    },
                    label = {
                        Text(
                            text = StringsHelper.t("tab_map", language),
                            maxLines = 1,
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = RedDark,
                        indicatorColor = RedContainer
                    ),
                    modifier = Modifier.testTag("tab_item_map")
                )

                // 5. Donor Profile
                NavigationBarItem(
                    selected = currentTab == ScreenTab.DONOR_PROFILE,
                    onClick = { viewModel.setTab(ScreenTab.DONOR_PROFILE) },
                    icon = {
                        Icon(imageVector = Icons.Default.Person, contentDescription = "Profile")
                    },
                    label = {
                        Text(
                            text = StringsHelper.t("tab_profile", language),
                            maxLines = 1,
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = RedDark,
                        indicatorColor = RedContainer
                    ),
                    modifier = Modifier.testTag("tab_item_profile")
                )
            }
        },
        floatingActionButton = {
            if (currentTab != ScreenTab.EMERGENCY_SOS) {
                FloatingActionButton(
                    onClick = { viewModel.setTab(ScreenTab.EMERGENCY_SOS) },
                    containerColor = UrgentCritical,
                    contentColor = CleanWhite,
                    shape = CircleShape,
                    modifier = Modifier.testTag("fab_sos_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Emergency,
                            contentDescription = "SOS",
                            tint = CleanWhite,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SOS",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                ScreenTab.AVAILABILITY -> BloodAvailabilityScreen(viewModel = viewModel)
                ScreenTab.EMERGENCY_SOS -> EmergencyRequestsScreen(viewModel = viewModel)
                ScreenTab.DONOR_FINDER -> DonorFinderScreen(viewModel = viewModel)
                ScreenTab.MAP_VIEW -> InteractiveMapScreen(viewModel = viewModel)
                ScreenTab.DONOR_PROFILE -> DonorProfileScreen(viewModel = viewModel)
                ScreenTab.ADMIN_STOCK -> AdminStockScreen(viewModel = viewModel)
                ScreenTab.EDUCATION -> EducationScreen()
            }
        }
    }
}
