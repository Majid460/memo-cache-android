package com.majidshahbaz.memo.ui.chat.components


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.majidshahbaz.memo.ui.chat.utils.formatChatTime
import java.util.Date

@Composable
fun ChatTimestampDivider(timestamp: Date, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = formatChatTime(timestamp),
            style = MaterialTheme.typography.labelMedium,
            color = com.majidshahbaz.memo.ui.theme.GrayText
        )
    }
}