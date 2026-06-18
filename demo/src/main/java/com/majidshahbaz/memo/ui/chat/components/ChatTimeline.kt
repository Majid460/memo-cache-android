package com.majidshahbaz.memo.ui.chat.components


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.majidshahbaz.memo.data.models.ChatMessage


@Composable
fun ChatTimeline(
    chatMessages: List<ChatMessage>,
    listState: LazyListState,
    onEditClicked: (String) -> Unit,
    userBubbleColor: Color,
    surfaceCardColor: Color,
    neonElectricPurple: Color,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(chatMessages.size) { index ->
            val message = chatMessages[index]

            // Session timestamp separation rule (e.g., 3-minute gap)
            val showTimestamp = index == 0 || run {
                val previousMessage1 = chatMessages[index - 1]
                (message.timestamp.time - previousMessage1.timestamp.time) > 3 * 60 * 1000
            }

            if (showTimestamp) {
                ChatTimestampDivider(timestamp = message.timestamp)
            }

            ChatMessageRow(
                message = message,
                onEditClicked = onEditClicked,
                userBubbleColor = userBubbleColor,
                surfaceCardColor = surfaceCardColor,
                neonElectricPurple = neonElectricPurple
            )
        }
    }
}