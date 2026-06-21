package com.majidshahbaz.memo.ui.chat.parsers

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.majidshahbaz.memo.ui.theme.*

/**
 * Removes inline markdown markers (** and *) from the text.
 */
private fun cleanInlineMarkdown(text: String): String {
    return text.replace("**", "").replace("*", "")
}

/**
 * Advanced token parser that converts Markdown formatting (# headings, bullets, **bold**)
 * into clean Jetpack Compose AnnotatedStrings with zero dangling asterisks.
 */
fun parseMarkdownToAnnotatedString(text: String): AnnotatedString {
    return buildAnnotatedString {
        val lines = text.split("\n")

        lines.forEachIndexed { index, line ->
            val trimmedLine = line.trim()

            when {
                trimmedLine.startsWith("###") -> {
                    val headingText = cleanInlineMarkdown(trimmedLine.removePrefix("###").trim())
                    pushStyle(SpanStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = HeadingColor
                    ))
                    append(headingText)
                    pop()
                }
                trimmedLine.startsWith("##") -> {
                    val headingText = cleanInlineMarkdown(trimmedLine.removePrefix("##").trim())
                    pushStyle(SpanStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = HeadingColor
                    ))
                    append(headingText)
                    pop()
                }
                trimmedLine.startsWith("#") -> {
                    val headingText = cleanInlineMarkdown(trimmedLine.removePrefix("#").trim())
                    pushStyle(SpanStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = HeadingColor
                    ))
                    append(headingText)
                    pop()
                }
                trimmedLine.startsWith("**") && trimmedLine.endsWith("**") -> {
                    val headingText = cleanInlineMarkdown(trimmedLine.removeSurrounding("**"))
                    pushStyle(SpanStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = HeadingColor
                    ))
                    append(headingText)
                    pop()
                }
                trimmedLine.startsWith("*") || trimmedLine.startsWith("-") || trimmedLine.startsWith("+") -> {
                    val isBullet = trimmedLine.length == 1 || trimmedLine[1] == ' '
                    if (isBullet) {
                        var cleanLine = trimmedLine.substring(1).trim()
                        cleanLine = cleanInlineMarkdown(cleanLine)

                        if (cleanLine.contains(":")) {
                            val parts = cleanLine.split(":", limit = 2)
                            val titlePart = parts[0].trim()
                            val descriptionPart = parts[1]

                            append("• ")

                            pushStyle(SpanStyle(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = CodeHighlightColor
                            ))
                            append(titlePart)
                            pop()

                            append(": ")
                            append(descriptionPart)
                        } else {
                            append("• $cleanLine")
                        }
                    } else {
                        // Not a bullet, likely inline formatting at start of line
                        append(cleanInlineMarkdown(line))
                    }
                }
                else -> {
                    // Clean up any stray inline asterisks scattered inside text blocks
                    append(cleanInlineMarkdown(line))
                }
            }

            // Re-apply line breaks between parsed segments, except for the last line
            if (index < lines.lastIndex) {
                append("\n")
            }
        }
    }
}