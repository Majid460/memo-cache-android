package com.majidshahbaz.memo.utils

import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp



private fun Modifier.maxHeightIn(max: Dp) = this.then(
    Modifier.heightIn(max = max)
)