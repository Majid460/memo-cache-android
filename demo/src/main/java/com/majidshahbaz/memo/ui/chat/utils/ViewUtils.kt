package com.majidshahbaz.memo.ui.chat.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Advanced token parser that converts Markdown formatting (**bold** and *italic*)
 * into clean Jetpack Compose AnnotatedStrings with zero dangling asterisks.
 */
fun parseMarkdownToAnnotatedString(text: String): AnnotatedString {
    return buildAnnotatedString {
        val lines = text.split("\n")

        lines.forEachIndexed { index, line ->
            val trimmedLine = line.trim()

            when {

                trimmedLine.startsWith("**") && trimmedLine.endsWith("**") -> {
                    val headingText = trimmedLine.removeSurrounding("**")
                    pushStyle(SpanStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFFE2E8F0)
                    ))
                    append(headingText)
                    pop()
                }


                trimmedLine.startsWith("*") -> {
                    var cleanLine = trimmedLine.removePrefix("*").trim()
                    cleanLine = cleanLine.replace("**", "")

                    if (cleanLine.contains(":")) {
                        val parts = cleanLine.split(":", limit = 2)
                        val titlePart = parts[0].trim()
                        val descriptionPart = parts[1]

                        append("• ")

                        pushStyle(SpanStyle(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = Color(0xFFBEE3F8)
                        ))
                        append(titlePart)
                        pop()

                        append(": ")
                        append(descriptionPart)
                    } else {
                        append("• $cleanLine")
                    }
                }

                else -> {
                    // Clean up any stray inline double asterisks scattered inside text blocks
                    val cleanBodyText = line.replace("**", "")
                    append(cleanBodyText)
                }
            }

            // Re-apply line breaks between parsed segments, except for the last line
            if (index < lines.lastIndex) {
                append("\n")
            }
        }
    }
}