package com.freight24.trucker.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.freight24.core.ui.navigation.BottomNavItem

val TruckerTabs = listOf(
    BottomNavItem(
        id = "loads",
        label = "Home",
        icon = Icons.Filled.Home
    ),
    BottomNavItem(
        id = "trips",
        label = "Trips",
        icon = Icons.Filled.SyncAlt
    ),
    BottomNavItem(
        id = "trucks",
        label = "Trucks",
        icon = Icons.Filled.LocalShipping
    ),
    BottomNavItem(
        id = "profile",
        label = "Profile",
        icon = Icons.Filled.Person
    )
)
