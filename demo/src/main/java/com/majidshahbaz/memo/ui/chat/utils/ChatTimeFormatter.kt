package com.majidshahbaz.memo.ui.chat.utils

import java.text.SimpleDateFormat
import java.util.Locale


fun formatChatTime(date: java.util.Date): String {
    val formatter = SimpleDateFormat("h:mm a", Locale.getDefault())
    return formatter.format(date)
}