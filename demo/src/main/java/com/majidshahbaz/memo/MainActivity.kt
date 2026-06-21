package com.majidshahbaz.memo

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.majidshahbaz.memo.ui.chat.ChatViewModel
import com.majidshahbaz.memo.ui.chat.OfflineChatScreen
import com.majidshahbaz.memo.ui.theme.MemoTheme
import kotlin.getValue

class MainActivity : AppCompatActivity() {
    private val chatViewModel: ChatViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MemoTheme {
                // Your streaming Compose UI layer runs natively here
                OfflineChatScreen(viewModel = chatViewModel)
            }
        }
    }
}

