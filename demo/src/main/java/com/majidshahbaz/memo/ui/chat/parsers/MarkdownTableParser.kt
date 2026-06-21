package com.majidshahbaz.memo.ui.chat.parsers

data class MarkdownTable(
    val headers: List<String>,
    val rows: List<List<String>>
)

sealed class MarkdownBlock {
    data class Text(val content: String) : MarkdownBlock()
    data class Table(val table: MarkdownTable) : MarkdownBlock()
}

/**
 * Removes stray markdown bold/italic markers from table cell content.
 * Table cells render as plain Text() with no AnnotatedString parsing,
 * so any "**bold**" or "*italic*" markers must be stripped here directly
 * rather than relying on parseMarkdownToAnnotatedString (which only
 * handles full-line formatting, not inline mid-line markers).
 */
private fun cleanCellText(raw: String): String {
    return raw
        .replace("**", "")
        .replace("*", "")
        .trim()
}

/**
 * Splits raw markdown into a sequence of blocks — plain text segments
 * and detected table segments — so each can be rendered with the
 * appropriate Composable (AnnotatedString text vs a real grid layout).
 */
fun parseMarkdownBlocks(text: String): List<MarkdownBlock> {
    val lines = text.split("\n")
    val blocks = mutableListOf<MarkdownBlock>()
    val currentTextBuffer = StringBuilder()
    var i = 0

    fun flushTextBuffer() {
        if (currentTextBuffer.isNotEmpty()) {
            blocks.add(MarkdownBlock.Text(currentTextBuffer.toString().trimEnd('\n')))
            currentTextBuffer.clear()
        }
    }

    while (i < lines.size) {
        val line = lines[i].trim()
        val isTableHeaderRow = line.startsWith("|") && line.endsWith("|")
        val nextLine = lines.getOrNull(i + 1)?.trim() ?: ""
        val isSeparatorRow = nextLine.matches(Regex("^\\|[\\s\\-:|]+\\|$"))

        if (isTableHeaderRow && isSeparatorRow) {
            flushTextBuffer()

            val headers = line.trim('|').split("|").map { cleanCellText(it) }
            val rows = mutableListOf<List<String>>()
            i += 2 // skip header row and separator row

            while (i < lines.size) {
                val rowLine = lines[i].trim()
                if (rowLine.startsWith("|") && rowLine.endsWith("|")) {
                    val cells = rowLine.trim('|').split("|").map { cleanCellText(it) }
                    rows.add(cells)
                    i++
                } else {
                    break
                }
            }

            blocks.add(MarkdownBlock.Table(MarkdownTable(headers, rows)))
            continue
        }

        currentTextBuffer.append(lines[i])
        currentTextBuffer.append("\n")
        i++
    }

    flushTextBuffer()
    return blocks
}