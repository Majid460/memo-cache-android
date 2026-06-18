package com.majidshahbaz.memo.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.majidshahbaz.memo.android.OnDeviceFallback
import com.majidshahbaz.memo.data.models.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val onDeviceFallback = OnDeviceFallback(application.applicationContext)

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    fun askOfflineModel(prompt: String) {
        val trimmedPrompt = prompt.trim()
        if (trimmedPrompt.isBlank()) return

        // Instantly log the user's prompt
        val userMsg = ChatMessage(text = trimmedPrompt, isUser = true)
        val aiPlaceholderMsg = ChatMessage(text = "", isUser = false)
        _messages.value = _messages.value + userMsg + aiPlaceholderMsg

        viewModelScope.launch {
            try {
                // Check if the file is completely missing from the internal sandbox before bootup
                val expectedPath = File(
                    getApplication<Application>().filesDir,
                    "llm/Gemma3-1B-IT_multi-prefill-seq_q4_block128_ekv1280.task"
                )

                if (!expectedPath.exists()) {
                    throw FileNotFoundException("Model file not found at: ${expectedPath.absolutePath}. Did the ADB copy step fail?")
                }

                // Force Engine Loading
                onDeviceFallback.initialize()

                // Execute inference stream
                onDeviceFallback.generateResponseStream(trimmedPrompt).collect { token ->
                    withContext(Dispatchers.Main) {
                        val currentList = _messages.value.toMutableList()
                        if (currentList.isNotEmpty()) {
                            val lastIdx = currentList.lastIndex
                            val lastMsg = currentList[lastIdx]
                            currentList[lastIdx] = lastMsg.copy(text = lastMsg.text + token)
                            _messages.value = currentList
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    val currentList = _messages.value.toMutableList()
                    if (currentList.isNotEmpty()) {
                        currentList[currentList.lastIndex] =
                            ChatMessage("System Log: ${e.message ?: e.toString()}", false)
                        _messages.value = currentList
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        onDeviceFallback.close()
    }
}