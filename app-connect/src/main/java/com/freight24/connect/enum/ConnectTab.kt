package com.freight24.connect.enum

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector
import com.freight24.connect.R
import com.freight24.ui.components.NavTabItem

enum class ConnectTab(
    override val id: String,
    override val titleRes: Int,
    override val icon: ImageVector,
) : NavTabItem {

    Home("home", R.string.tab_home, Icons.Filled.Home),
    Indents("indents", R.string.tab_indents, Icons.AutoMirrored.Outlined.Assignment),
    Trips("trips", R.string.tab_trips, Icons.Filled.LocalShipping),
    Trucks("find_trucks", R.string.tab_find_trucks, Icons.Filled.Search),
    More("more", R.string.tab_more, Icons.Filled.MoreHoriz),
}