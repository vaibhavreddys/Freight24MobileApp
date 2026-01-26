package com.freight24.connect.feature.home

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.freight24.connect.R
import com.freight24.connect.enum.ConnectTab
import com.freight24.connect.feature.dashboard.views.DashboardScreen
import com.freight24.connect.feature.indents.views.IndentsScreen
import com.freight24.connect.feature.more.views.MoreScreen
import com.freight24.connect.feature.trips.views.TripsScreen
import com.freight24.connect.feature.trucks.views.FindTrucksScreen
import com.freight24.ui.components.AppTopBar
import com.freight24.ui.components.FreightBottomNavScaffold

@Composable
fun HomeScreen(
    onCreateIndentClick: () -> Unit
) {

    var selectedTab by rememberSaveable { mutableStateOf(ConnectTab.Home) }

    FreightBottomNavScaffold(
        items = ConnectTab.entries,
        selectedItem = selectedTab,
        onItemSelected = {
            selectedTab = it
        },
        topBar = {
            AppTopBar(
                title = selectedTab.name,
                showSearch = true,
            )
        },
        floatingActionButton = {
            if (selectedTab == ConnectTab.Indents) {
                FloatingActionButton(
                    shape = CircleShape,
                    onClick = {
                        onCreateIndentClick()
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.cd_add_indent)
                    )
                }
            }
        }
    )  { paddingValues, tab ->
        Crossfade(tab,  label = "TabTransition") { currentTab ->
            when (currentTab) {
                ConnectTab.Home -> DashboardScreen(paddingValues)
                ConnectTab.Indents -> IndentsScreen(Modifier.padding(paddingValues))
                ConnectTab.Trips -> TripsScreen(paddingValues)
                ConnectTab.Trucks -> FindTrucksScreen(paddingValues)
                ConnectTab.More -> MoreScreen(paddingValues)
            }
        }
    }
}