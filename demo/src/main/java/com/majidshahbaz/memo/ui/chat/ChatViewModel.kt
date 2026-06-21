package com.majidshahbaz.memo.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.majidshahbaz.memo.BuildConfig
import com.majidshahbaz.memo.android.Memo
import com.majidshahbaz.memo.android.state.MemoNetworkState
import com.majidshahbaz.memo.data.models.ChatMessage
import com.majidshahbaz.memo.data.models.MessageSource
import com.majidshahbaz.memo.data.network.GroqApiClient
import com.majidshahbaz.memo.data.preferences.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    /**
     * NOTE FOR DEVELOPERS INTEGRATING MEMO:
     * The resolver endpoint below points to THIS DEMO APP's own example
     * backend (see /memo-backend in this repo). It is NOT required by
     * the Memo library itself.
     *
     * Memo ships with its own default model and works with zero backend
     * setup. Use .modelResolverEndpoint(...) only if you want hardware-aware
     * tiered model selection via your own backend — see README "Configuration".
     *
     * This value is read from local.properties via BuildConfig, never
     * hardcoded, so it's safe to keep out of version control.
     */
    private val memo: Memo = Memo.Builder(application.applicationContext)
        .modelResolverEndpoint(BuildConfig.MEMO_MODEL_RESOLVER_ENDPOINT)
        .autoDownloadModel(true)
        .build()

    /**
     * Demo-only cloud client (Groq). Also sourced from local.properties via
     * BuildConfig — never hardcoded. Demonstrates the `cloudApiCall` parameter
     * of Memo.resolve(); any OpenAI-compatible or custom provider works the same way.
     */
    private val groqClient = GroqApiClient(BuildConfig.GROQ_API_KEY)

    private val userPreferences = UserPreferences(application.applicationContext)
    val showDownloadOnboarding = userPreferences.downloadOnboardingShown
        .map { shown -> !shown }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    val networkState: StateFlow<MemoNetworkState> = memo.networkState

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating = _isGenerating.asStateFlow()
    val isModelDownloaded: Boolean
        get() = memo.isModelDownloaded
    private var generationJob: Job? = null

    fun askOfflineModel(prompt: String) {
        val trimmedPrompt = prompt.trim()
        if (trimmedPrompt.isBlank() || _isGenerating.value) return

        _isGenerating.value = true

        val userMsg = ChatMessage(text = trimmedPrompt, isUser = true)
        val isOnline = networkState.value is MemoNetworkState.Online
        val source = if (isOnline) MessageSource.CLOUD else MessageSource.ON_DEVICE

        val aiPlaceholderMsg = ChatMessage(text = "", isUser = false, isComplete = false, source = source)
        _messages.value = _messages.value + userMsg + aiPlaceholderMsg

        generationJob = viewModelScope.launch {
            try {
                memo.resolve(
                    prompt = trimmedPrompt,
                    model = "llama-3.3-70b-versatile",
                    cloudApiCall = { p -> groqClient.chat(p) }
                ).collect { token ->
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

                withContext(Dispatchers.Main) {
                    val currentList = _messages.value.toMutableList()
                    if (currentList.isNotEmpty()) {
                        val lastIdx = currentList.lastIndex
                        currentList[lastIdx] = currentList[lastIdx].copy(isComplete = true)
                        _messages.value = currentList
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    val currentList = _messages.value.toMutableList()
                    if (currentList.isNotEmpty()) {
                        currentList[currentList.lastIndex] =
                            ChatMessage("System Log: ${e.message ?: e.toString()}", false, isComplete = true)
                        _messages.value = currentList
                    }
                }
            } finally {
                _isGenerating.value = false
            }
        }
    }
    fun dismissOnboarding() {
        viewModelScope.launch {
            userPreferences.setDownloadOnboardingShown(true)
        }
    }

    fun stopGeneration() {
        _isGenerating.value = false
        generationJob?.cancel()
        generationJob = null

        val currentList = _messages.value.toMutableList()
        if (currentList.isNotEmpty()) {
            val lastIdx = currentList.lastIndex
            val lastMsg = currentList[lastIdx]
            if (!lastMsg.isUser && !lastMsg.isComplete) {
                currentList[lastIdx] = lastMsg.copy(
                    isComplete = true,
                    source = MessageSource.STREAM_STOPPED
                )
                _messages.value = currentList
            }
        }
    }
    /**
     * Triggers a model download if one isn't already present.
     * Progress and completion are observed via [networkState] —
     * no manual Flow collection needed by the caller.
     */
    fun downloadModel() {
        if (memo.isModelDownloaded) {
            return
        }
        memo.downloadModel()
    }

    override fun onCleared() {
        memo.close()
        groqClient.close()
    }
}