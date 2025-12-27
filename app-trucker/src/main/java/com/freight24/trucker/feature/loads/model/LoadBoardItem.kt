package com.freight24.trucker.feature.loads.model

import com.freight24.trucker.feature.loads.model.ui.LoadUi

sealed interface LoadBoardItem {

    data class CityItem(
        val cityId: String,
        val cityName: String,
        val totalLoads: Int,
        val expanded: Boolean
    ) : LoadBoardItem

    data class LoadItem(
        val load: LoadUi
    ) : LoadBoardItem

    data class ShowMoreItem(
        val cityId: String,
        val loadedCount: Int,
        val totalCount: Int
    ) : LoadBoardItem
}
