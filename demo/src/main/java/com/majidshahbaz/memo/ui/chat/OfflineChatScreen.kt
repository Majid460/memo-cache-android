package com.majidshahbaz.memo.ui.chat

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.majidshahbaz.memo.android.state.MemoNetworkState
import com.majidshahbaz.memo.ui.chat.components.*
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun OfflineChatScreen(viewModel: ChatViewModel) {
    val chatMessages by viewModel.messages.collectAsState()
    val networkState by viewModel.networkState.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val showOnboarding by viewModel.showDownloadOnboarding.collectAsState()
    val activeTier by viewModel.activeTier.collectAsState()
    val storageUsage by viewModel.storageUsage.collectAsState()
    val hardwareProfile by viewModel.hardwareProfile.collectAsState()

    var isAutoScrollEnabled by remember { mutableStateOf(true) }
    var userIsManuallyScrolling by remember { mutableStateOf(false) }
    var showNoOfflineDialog by remember { mutableStateOf(false) }
    var showFirstTimeDialog by remember { mutableStateOf(!viewModel.isModelDownloaded) }

    // Single source of truth for auto-scroll during streaming —
    // throttled, with a re-check after the delay to respect manual scroll
    LaunchedEffect(chatMessages.lastOrNull()?.text?.length) {
        if (isAutoScrollEnabled && !userIsManuallyScrolling && chatMessages.isNotEmpty()) {
            delay(80.milliseconds)
            if (isAutoScrollEnabled && !userIsManuallyScrolling) {
                listState.scrollToItem(chatMessages.lastIndex)
            }
        }
    }


    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (showFirstTimeDialog && !viewModel.isModelDownloaded) {
                FirstTimeDownloadDialog(
                    onTierSelected = { tier ->
                        showFirstTimeDialog = false
                        viewModel.selectTier(tier)
                    },
                    onDismiss = { showFirstTimeDialog = false }
                )
            }
            if (showNoOfflineDialog) {
                NoOfflineSupportDialog(
                    onDownloadClick = {
                        showNoOfflineDialog = false
                        viewModel.downloadModel()
                    },
                    onDismiss = { showNoOfflineDialog = false }
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                ChatTopBar(
                    networkState = networkState,
                    isModelDownloaded = viewModel.isModelDownloaded,
                    selectedTier = activeTier,
                    storageUsage = storageUsage,
                    hardwareProfile = hardwareProfile,
                    onTierSelected = { viewModel.selectTier(it) },
                    onDownloadModelClick = { viewModel.downloadModel() },
                    title = "Memo"
                )
                DownloadOnboardingBanner(
                    isVisible = showOnboarding && !viewModel.isModelDownloaded,
                    onDismiss = { viewModel.dismissOnboarding() }
                )
                ChatTimeline(
                    chatMessages = chatMessages,
                    listState = listState,
                    onEditClicked = { originalText -> inputText = originalText },
                    modifier = Modifier.weight(1f)
                )
                DownloadProgressTile(networkState = networkState)
                ChatBottomInputConsole(
                    networkState = networkState,
                    inputText = inputText,
                    onValueChange = { inputText = it },
                    enabled = !isGenerating,
                    isGenerating = isGenerating,
                    onStop = {
                        viewModel.stopGeneration()
                    },
                    onSubmit = { prompt ->
                        if (networkState is MemoNetworkState.OfflineNoModel) {
                            showNoOfflineDialog = true
                        } else {
                            isAutoScrollEnabled = true
                            userIsManuallyScrolling = false
                            viewModel.askOfflineModel(prompt)
                            inputText = ""
                        }
                    }
                )
            }
        }
    }
}
