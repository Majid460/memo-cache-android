package com.majidshahbaz.memo.ui.chat.components


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.majidshahbaz.memo.android.state.MemoNetworkState
import com.majidshahbaz.memo.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatBottomInputConsole(
    networkState: MemoNetworkState,
    inputText: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean = true,
    onSubmit: (String) -> Unit,
    isGenerating: Boolean = false,
    onStop: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    val placeholderText = when (networkState) {
        is MemoNetworkState.Online -> "Ask anything..."
        is MemoNetworkState.OfflineReady -> "Ask Gemma 3 (offline)..."
        is MemoNetworkState.OfflineNoModel -> "Offline — no model available"
        is MemoNetworkState.DownloadingModel,
        is MemoNetworkState.DownloadProgress -> "Downloading offline model..."
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = onValueChange,
                placeholder = { Text(placeholderText, color = GrayText) },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(max = 140.dp),
                maxLines = 5,
                enabled = enabled,
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = PureWhite,
                    unfocusedTextColor = PureWhite,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = IndicatorColor
                ),
                trailingIcon = {
                    if (isGenerating) {
                        IconButton(onClick = onStop) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Stop generating",
                                tint = OfflineNoModelDot
                            )
                        }
                    } else {
                        IconButton(
                            onClick = {
                                val cleaned = inputText.trim()
                                if (cleaned.isNotBlank()) {
                                    onSubmit(cleaned)
                                    keyboardController?.hide()
                                }
                            },
                            enabled = inputText.isNotBlank()
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = if (inputText.isNotBlank()) MaterialTheme.colorScheme.primary else GrayText
                            )
                        }
                    }
                }

            )
        }
    }
}