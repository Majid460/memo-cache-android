package com.majidshahbaz.memo.ui.chat.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.majidshahbaz.memo.android.state.MemoNetworkState

import com.majidshahbaz.memo.ui.theme.*

@Composable
fun NetworkStatusBanner(
    state: MemoNetworkState,
    modifier: Modifier = Modifier
) {
    val (label, dotColor, gradient) = when (state) {
        is MemoNetworkState.Online -> Triple(
            "Online • Cloud Mode",
            OnlineDot,
            Brush.horizontalGradient(
                listOf(OnlineGradientStart, OnlineGradientEnd)
            )
        )
        is MemoNetworkState.OfflineReady -> Triple(
            "Offline Support Active",
            OfflineReadyDot,
            Brush.horizontalGradient(
                listOf(OfflineReadyGradientStart, OfflineReadyGradientEnd)
            )
        )
        is MemoNetworkState.OfflineNoModel -> Triple(
            "Offline • No Model Available",
            OfflineNoModelDot,
            Brush.horizontalGradient(
                listOf(OfflineNoModelGradientStart, OfflineNoModelGradientEnd)
            )
        )
        is MemoNetworkState.DownloadingModel -> Triple(
            "Downloading Offline Model…",
            DownloadingDot,
            Brush.horizontalGradient(
                listOf(DownloadingGradientStart, DownloadingGradientEnd)
            )
        )
        is MemoNetworkState.DownloadProgress -> Triple(
            "Downloading… ${state.percent}%",
            DownloadingDot,
            Brush.horizontalGradient(
                listOf(DownloadingGradientStart, DownloadingGradientEnd)
            )
        )
    }

    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(top = 12.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(gradient)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(dotColor)
                )
                Text(
                    text = label,
                    color = Color.White.copy(alpha = 0.92f),
                    fontSize = 13.sp
                )
            }
        }
    }
}