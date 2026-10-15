package com.focusflow.feature.timer.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusflow.core.designsystem.theme.FocusDarkText
import com.focusflow.core.designsystem.theme.FocusForestGreenDeep

/**
 * Frosted translucent quote card floating over the scenic lake background.
 */
@Composable
fun ScenicQuoteCard(
    quote: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth(0.90f),
        shape = RoundedCornerShape(22.dp),
        color = Color(0xF2FBF9F6), // Soft warm off-white translucent frost
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.90f)),
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "“",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = FocusForestGreenDeep,
                modifier = Modifier.padding(bottom = 2.dp),
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = quote,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = FocusDarkText,
                textAlign = TextAlign.Start,
                lineHeight = 16.sp,
            )
        }
    }
}
