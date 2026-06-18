package com.majidshahbaz.memo.data.models

import java.util.Date

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Date = Date()
)