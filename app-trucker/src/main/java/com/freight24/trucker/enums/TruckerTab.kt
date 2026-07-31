package com.freight24.trucker.enums

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.ui.graphics.vector.ImageVector
import com.freight24.trucker.R
import com.freight24.ui.components.NavTabItem

enum class TruckerTab(
    override val id: String,
    override val titleRes: Int,
    override val icon: ImageVector,
) : NavTabItem {
    Loads("loads", R.string.tab_loads, Icons.Filled.Home),
    Trips("trips", R.string.tab_trips, Icons.Filled.SwapHoriz),
    Trucks("trucks", R.string.tab_trucks, Icons.Filled.LocalShipping),
    Profile("profile", R.string.tab_profile, Icons.Filled.Person),
}
