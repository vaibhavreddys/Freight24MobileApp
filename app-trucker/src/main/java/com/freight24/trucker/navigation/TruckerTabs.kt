package com.freight24.trucker.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SyncAlt
import com.freight24.ui.navigation.BottomNavItem

val TruckerTabs =
    listOf(
        BottomNavItem(
            id = "loads",
            label = "Home",
            icon = Icons.Filled.Home,
        ),
        BottomNavItem(
            id = "trips",
            label = "Trips",
            icon = Icons.Filled.SyncAlt,
        ),
        BottomNavItem(
            id = "trucks",
            label = "Trucks",
            icon = Icons.Filled.LocalShipping,
        ),
        BottomNavItem(
            id = "profile",
            label = "Profile",
            icon = Icons.Filled.Person,
        ),
    )

// sealed class TruckerTab(val id: String, val titleRes: Int, val icon: ImageVector) {
//    data object Loads: TruckerTab("loads", R.string.tab_loads, Icons.Filled.Home)
//    data object Trips: TruckerTab("trips", R.string.tab_trips, Icons.Filled.SyncAlt)
//    data object Trucks: TruckerTab("trucks", R.string.tab_trucks, Icons.Filled.LocalShipping)
//    data object Profile: TruckerTab("profile", R.string.tab_profile, Icons.Filled.Person)
// }
