package com.focusflow.feature.timer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.focusflow.core.domain.model.SubjectTag

@Composable
fun TagSelectorRow(
    tags: List<SubjectTag>,
    selectedTag: SubjectTag?,
    enabled: Boolean,
    onSelectTag: (SubjectTag) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (tags.isEmpty()) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        tags.forEach { tag ->
            val isSelected = tag.id == selectedTag?.id
            val tagColor = try {
                Color(android.graphics.Color.parseColor(tag.colorHex))
            } catch (_: IllegalArgumentException) {
                MaterialTheme.colorScheme.primary
            }

            FilterChip(
                selected = isSelected,
                onClick = { onSelectTag(tag) },
                enabled = enabled,
                leadingIcon = {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(tagColor),
                    )
                },
                label = { Text(tag.name) },
            )
        }
    }
}
