package com.majidshahbaz.memo.ui.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.majidshahbaz.memo.ui.chat.parsers.MarkdownTable

import com.majidshahbaz.memo.ui.theme.*

@Composable
fun MarkdownTableView(table: MarkdownTable, modifier: Modifier = Modifier) {
    val columnWidth = 130.dp
    val columnCount = table.headers.size

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, TableBorderColor, RoundedCornerShape(10.dp))
    ) {
        LazyRow {
            items(columnCount) { colIndex ->
                Column(modifier = Modifier.width(columnWidth)) {
                    // Header cell
                    Box(
                        modifier = Modifier
                            .background(TableHeaderBackground)
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = table.headers[colIndex],
                            color = CodeHighlightColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }

                    // Data cells for this column, one per row
                    table.rows.forEachIndexed { rowIndex, row ->
                        Box(
                            modifier = Modifier
                                .background(
                                    if (rowIndex % 2 == 0) TableRowEvenBackground else SurfaceCardColor
                                )
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = row.getOrElse(colIndex) { "" },
                                color = White85,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
