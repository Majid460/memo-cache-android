package com.majidshahbaz.memo.ui.chat.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.majidshahbaz.memo.android.hardware.HardwareProfile
import com.majidshahbaz.memo.android.model.ModelTier
import com.majidshahbaz.memo.android.state.MemoNetworkState
import com.majidshahbaz.memo.ui.chat.StorageUsage
import com.majidshahbaz.memo.ui.theme.*

@Composable
fun ChatTopBar(
    networkState: MemoNetworkState,
    isModelDownloaded: Boolean,
    selectedTier: ModelTier?,
    storageUsage: StorageUsage,
    hardwareProfile: HardwareProfile?,
    onTierSelected: (ModelTier) -> Unit,
    onDownloadModelClick: () -> Unit,
    title: String = "Memo",
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

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
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = PureWhite
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.background(SurfaceCardColor)
                ) {
                    // Task 2: Move Cloud Action Into Menu
                    val showDownloadButton = !isModelDownloaded &&
                            (networkState is MemoNetworkState.OfflineNoModel || networkState is MemoNetworkState.Online)

                    if (showDownloadButton) {
                        DropdownMenuItem(
                            text = { Text("Download Cloud Model", color = PureWhite) },
                            onClick = {
                                menuExpanded = false
                                onDownloadModelClick()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, tint = OfflineReadyDot)
                            }
                        )
                        HorizontalDivider(color = White15)
                    }

                    // Task 3: Model Management Menu
                    Text(
                        "Models",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = OfflineReadyDot
                    )

                    ModelTier.entries.forEach { tier ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(tier.label, color = if (selectedTier == tier) NeonElectricPurple else PureWhite)
                                    Text(tier.description, style = MaterialTheme.typography.bodySmall, color = White85)
                                }
                            },
                            onClick = {
                                menuExpanded = false
                                onTierSelected(tier)
                            },
                            trailingIcon = {
                                RadioButton(
                                    selected = selectedTier == tier,
                                    onClick = null,
                                    colors = RadioButtonDefaults.colors(selectedColor = NeonElectricPurple)
                                )
                            }
                        )
                    }

                    HorizontalDivider(color = White15)

                    // Task 5: Storage and Resource Usage Section
                    Text(
                        "Storage & Resources",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = OfflineReadyDot
                    )

                    DropdownMenuItem(
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                ResourceRow("Models", "${storageUsage.modelsSizeMb} MB")
                                ResourceRow("Cache", "${storageUsage.cacheSizeMb} MB")
                                ResourceRow("Total", "${storageUsage.totalSizeMb} MB", fontWeight = FontWeight.Bold)
                                if (hardwareProfile != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    ResourceRow("CPU Usage", "${hardwareProfile.cpuUsagePercent}%")
                                    LinearProgressIndicator(
                                        progress = { hardwareProfile.cpuUsagePercent / 100f },
                                        modifier = Modifier.fillMaxWidth().height(4.dp),
                                        color = if (hardwareProfile.cpuUsagePercent > 80) ErrorRed else NeonElectricPurple,
                                        trackColor = White15
                                    )
                                }
                            }
                        },
                        onClick = { },
                        enabled = false
                    )
                }
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
        }
    }
}

@Composable
private fun ResourceRow(label: String, value: String, fontWeight: FontWeight = FontWeight.Normal) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = White85, style = MaterialTheme.typography.bodySmall)
        Text(value, color = PureWhite, style = MaterialTheme.typography.bodySmall, fontWeight = fontWeight)
    }
}
