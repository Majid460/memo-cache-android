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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.majidshahbaz.memo.data.models.ChatMessage
import com.majidshahbaz.memo.ui.chat.utils.parseMarkdownToAnnotatedString

@Composable
fun ChatMessageRow(
    message: ChatMessage,
    onEditClicked: (String) -> Unit,
    userBubbleColor: Color,
    surfaceCardColor: Color,
    neonElectricPurple: Color
) {
    val isUser = message.isUser
    val context = LocalContext.current

    // Infinite rotation configuration for the loading spinner
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
                containerColor = if (isUser) userBubbleColor else surfaceCardColor
            ),
            modifier = Modifier.widthIn(max = 310.dp)
        ) {
            Box(modifier = Modifier.padding(12.dp)) {
                if (!isUser && message.text.isEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Loading",
                            tint = neonElectricPurple,
                            modifier = Modifier.size(18.dp).rotate(rotationAngle)
                        )
                        Text(
                            text = "Gemma is thinking...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.LightGray
                        )
                    }
                } else {
                    Text(
                        text = parseMarkdownToAnnotatedString(message.text),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isUser) Color.White else Color(0xFFE3E3E3)
                    )
                }
            }
        }

        // Action Buttons Row (Edit / Copy)
        if (message.text.isNotEmpty()) {
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
                            tint = Color.Gray
                        )
                        Text("Edit", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
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
                            tint = Color.Gray
                        )
                        Text("Copy", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    }
                }
            }
        }
    }
}