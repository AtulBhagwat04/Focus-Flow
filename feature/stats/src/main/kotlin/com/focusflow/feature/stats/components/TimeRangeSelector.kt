package com.focusflow.feature.stats.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.focusflow.feature.stats.StatsTimeRange

@Composable
fun TimeRangeSelector(
    selectedRange: StatsTimeRange,
    onRangeSelected: (StatsTimeRange) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StatsTimeRange.entries.forEach { range ->
            val label = when (range) {
                StatsTimeRange.DAY -> "Today"
                StatsTimeRange.WEEK -> "This Week"
                StatsTimeRange.MONTH -> "This Month"
            }
            FilterChip(
                selected = range == selectedRange,
                onClick = { onRangeSelected(range) },
                label = { Text(label) },
            )
        }
    }
}
