package com.freight24.trucker.feature.loads.data

import com.freight24.trucker.feature.loads.model.ui.LoadUi

object DummyLoadData {

    fun loadsForCity(city: String, start: Int, count: Int): List<LoadUi> {
        return (start until start + count).map {
            LoadUi(
                id = "$city-$it",
                sourceCity = city,
                destinationCity = "Delhi",
                postedTime = "2h ago",
                price = "₹${1800 + it * 10}/ton",
                truckInfo = "32 ft Multi-Axle",
                loadWeight = "20 Tons",
                commodity = "Steel",
                f24AgentName = "Ram Kumar"
            )
        }
    }
}
