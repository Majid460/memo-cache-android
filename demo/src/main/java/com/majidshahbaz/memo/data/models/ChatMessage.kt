package com.majidshahbaz.memo.data.models

import java.util.Date

enum class MessageSource {
    USER,
    CLOUD,
    ON_DEVICE,
    STREAM_STOPPED
}

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val isComplete: Boolean = true,
    val timestamp: Date = Date(),
    val source: MessageSource = MessageSource.CLOUD
)