package com.majidshahbaz.memo.ui.chat.components


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatBottomInputConsole(
    inputText: String,
    onValueChange: (String) -> Unit,
    onSubmit: (String) -> Unit,
    darkBackground: Color,
    surfaceCardColor: Color,
    neonElectricPurple: Color,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = darkBackground,
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
                placeholder = { Text("Ask Gemma 3...", color = Color.Gray) },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(max = 140.dp),
                maxLines = 5,
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = surfaceCardColor,
                    unfocusedContainerColor = surfaceCardColor,
                    disabledContainerColor = surfaceCardColor,
                    focusedIndicatorColor = neonElectricPurple,
                    unfocusedIndicatorColor = Color(0xFF3F4147)
                ),
                trailingIcon = {
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
                            tint = if (inputText.isNotBlank()) neonElectricPurple else Color.Gray
                        )
                    }
                }
            )
        }
    }
}