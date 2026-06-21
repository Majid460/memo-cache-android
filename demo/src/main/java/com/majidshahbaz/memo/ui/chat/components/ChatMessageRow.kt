package com.majidshahbaz.memo.ui.chat.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.majidshahbaz.memo.data.models.ChatMessage
import com.majidshahbaz.memo.data.models.MessageSource
import com.majidshahbaz.memo.ui.chat.parsers.MarkdownBlock
import com.majidshahbaz.memo.ui.chat.parsers.parseMarkdownBlocks
import com.majidshahbaz.memo.ui.chat.parsers.parseMarkdownToAnnotatedString

import com.majidshahbaz.memo.ui.theme.*

@Composable
fun ChatMessageRow(
    message: ChatMessage,
    onEditClicked: (String) -> Unit
) {
    val isUser = message.isUser
    val context = LocalContext.current

    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val blocks = remember(message.text, message.isComplete) {
        if (message.isComplete) parseMarkdownBlocks(message.text) else emptyList()
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.widthIn(max = 310.dp)
        ) {
            Box(modifier = Modifier.padding(12.dp)) {
                if (!isUser && message.text.isEmpty() && (!message.isComplete || message.source == MessageSource.STREAM_STOPPED)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!message.isComplete && message.source != MessageSource.STREAM_STOPPED) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Loading",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp).rotate(rotationAngle)
                            )
                        }
                        Text(
                            text = when (message.source) {
                                MessageSource.CLOUD -> "Thinking..."
                                MessageSource.ON_DEVICE -> "Gemma is thinking..."
                                MessageSource.STREAM_STOPPED -> "Stream stopped"
                                else -> "Thinking..."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = LightGrayText
                        )
                    }
                } else if (!message.isComplete) {
                    // Streaming in progress — plain fast text, no parsing work at all
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isUser) PureWhite else MaterialTheme.colorScheme.tertiary
                    )
                } else {
                    Column {
                        blocks.forEach { block ->
                            when (block) {
                                is MarkdownBlock.Text -> {
                                    if (block.content.isNotBlank()) {
                                        Text(
                                            text = parseMarkdownToAnnotatedString(block.content),
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = if (isUser) PureWhite else MaterialTheme.colorScheme.tertiary
                                        )
                                    }
                                }
                                is MarkdownBlock.Table -> {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    MarkdownTableView(
                                        table = block.table,
                                        modifier = Modifier.widthIn(max = 280.dp) // explicit bound, matches Card minus padding
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        if (message.text.isNotEmpty() && message.isComplete) {
            Row(
                modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (isUser) {
                    TextButton(
                        onClick = { onEditClicked(message.text) },
                        modifier = Modifier.height(28.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            modifier = Modifier.size(14.dp).padding(end = 4.dp),
                            tint = GrayText
                        )
                        Text("Edit", style = MaterialTheme.typography.labelMedium, color = GrayText)
                    }
                } else {
                    TextButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("AI Response", message.text)
                            clipboard.setPrimaryClip(clip)
                        },
                        modifier = Modifier.height(28.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            modifier = Modifier.size(14.dp).padding(end = 4.dp),
                            tint = GrayText
                        )
                        Text("Copy", style = MaterialTheme.typography.labelMedium, color = GrayText)
                    }
                }
            }
        }
    }
}
