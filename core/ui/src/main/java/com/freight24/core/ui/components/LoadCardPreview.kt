import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.freight24.core.ui.components.LoadCard
import com.freight24.core.ui.theme.Freight24Theme

@Preview(showBackground = true)
@Composable
private fun LoadCardPreview() {
    Freight24Theme {
        LoadCard(
            sourceCity = "Mumbai",
            destinationCity = "Delhi",
            postedTime = "2h ago",
            price = "₹1820/ton",
            truckInfo = "32 ft Multi-Axle",
            loadWeight = "20 Tons",
            commodity = "Steel",
            f24AgentName = "ABC Logistics",
            onCallClick = {},
            onBookmarkClick = {}
        )
    }
}