package com.majidshahbaz.memo.ui.chat

import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.majidshahbaz.memo.ui.chat.components.ChatBottomInputConsole
import com.majidshahbaz.memo.ui.chat.components.ChatTimeline

@Composable
fun OfflineChatScreen(viewModel: ChatViewModel) {
    val chatMessages by viewModel.messages.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    var isAutoScrollEnabled by remember { mutableStateOf(true) }
    val lastMessageTextLength = chatMessages.lastOrNull()?.text?.length ?: 0

    LaunchedEffect(listState.interactionSource) {
        listState.interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is DragInteraction.Start, is PressInteraction.Press -> {
                    isAutoScrollEnabled = false
                }
            }
        }
    }

    val isAtAbsoluteBottom by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) true else {
                val lastVisibleItem = visibleItems.last()
                lastVisibleItem.index == layoutInfo.totalItemsCount - 1 &&
                        lastVisibleItem.offset + lastVisibleItem.size <= layoutInfo.viewportEndOffset
            }
        }
    }

    LaunchedEffect(isAtAbsoluteBottom) {
        if (isAtAbsoluteBottom) {
            isAutoScrollEnabled = true
        }
    }

    LaunchedEffect(chatMessages.size, lastMessageTextLength) {
        if (chatMessages.isNotEmpty()) {
            if (lastMessageTextLength <= 1) {
                isAutoScrollEnabled = true
                listState.scrollToItem(chatMessages.lastIndex)
            }
            else if (isAutoScrollEnabled) {
                listState.scrollToItem(chatMessages.lastIndex)
            }
        }
    }

    val darkBackground = Color(0xFF111214)
    val surfaceCardColor = Color(0xFF1E1F22)
    val neonElectricPurple = Color(0xFF9F7AEA)
    val userBubbleColor = Color(0xFF2B2D31)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = darkBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            ChatTimeline(
                chatMessages = chatMessages,
                listState = listState,
                onEditClicked = { originalText -> inputText = originalText },
                userBubbleColor = userBubbleColor,
                surfaceCardColor = surfaceCardColor,
                neonElectricPurple = neonElectricPurple,
                modifier = Modifier.weight(1f) // Look, no pointerInput modifiers competing for gestures!
            )

            ChatBottomInputConsole(
                inputText = inputText,
                onValueChange = { inputText = it },
                onSubmit = { prompt ->
                    isAutoScrollEnabled = true // Force reset lock when user sends a new message
                    viewModel.askOfflineModel(prompt)
                    inputText = ""
                },
                darkBackground = darkBackground,
                surfaceCardColor = surfaceCardColor,
                neonElectricPurple = neonElectricPurple
            )
        }
    }
}