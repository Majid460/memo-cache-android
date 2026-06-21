package com.majidshahbaz.memo.ui.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.majidshahbaz.memo.android.model.ModelTier
import com.majidshahbaz.memo.ui.theme.*

@Composable
fun FirstTimeDownloadDialog(
    onTierSelected: (ModelTier) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SurfaceCardColor,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Welcome to Memo",
                    style = MaterialTheme.typography.headlineSmall,
                    color = PureWhite,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Which model would you like to download to start chatting offline?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = White85,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                ModelTier.entries.forEach { tier ->
                    Card(
                        onClick = { onTierSelected(tier) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = DarkBackground
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, White15)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = tier.label,
                                    color = NeonElectricPurple,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 18.sp
                                )
                                Icon(
                                    imageVector = Icons.Default.CloudDownload,
                                    contentDescription = null,
                                    tint = OfflineReadyDot
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = tier.description,
                                color = White85,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}
