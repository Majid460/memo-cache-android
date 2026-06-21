package com.majidshahbaz.memo.ui.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.majidshahbaz.memo.android.state.MemoNetworkState

import com.majidshahbaz.memo.ui.theme.*

@Composable
fun ChatTopBar(
    networkState: MemoNetworkState,
    isModelDownloaded: Boolean,
    title: String = "Memo",
    onDownloadModelClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val (statusLabel, dotColor) = when (networkState) {
        is MemoNetworkState.Online ->
            "Online • Cloud Mode" to OnlineDot
        is MemoNetworkState.OfflineReady ->
            "Offline Support Active" to OfflineReadyDot
        is MemoNetworkState.OfflineNoModel ->
            "Offline • No Model Available" to OfflineNoModelDot
        is MemoNetworkState.DownloadingModel ->
            "Downloading Offline Model…" to DownloadingDot
        is MemoNetworkState.DownloadProgress ->
            "Downloading… ${networkState.percent}%" to DownloadingDot
    }

    val showDownloadButton = !isModelDownloaded &&
            (networkState is MemoNetworkState.OfflineNoModel || networkState is MemoNetworkState.Online)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkBackground)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(NeonElectricPurple, OfflineReadyDot)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "M",
                    color = PureWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Text(
                text = title,
                color = PureWhite,
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Download offline model button — only shown when relevant

            AnimatedContent(
                targetState = statusLabel to dotColor,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "network-status"
            ) { (label, color) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceCardColor)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(RoundedCornerShape(50))
                            .background(color)
                    )
                    Text(
                        text = label,
                        color = White85,
                        fontSize = 11.sp
                    )
                }
            }
            if (showDownloadButton && onDownloadModelClick != null) {
                IconButton(
                    onClick = onDownloadModelClick,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceCardColor)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = "Download offline model",
                        tint = OfflineReadyDot,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

        }
    }
}
