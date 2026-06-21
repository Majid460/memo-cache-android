package com.majidshahbaz.memo.android.model

enum class ModelTier(val label: String, val description: String, val fileName: String) {
    LITE(
        label = "Lite",
        description = "Smaller size, faster, lower quality.",
        fileName = "Gemma3-1B-IT_multi-prefill-seq_q4_block128_ekv1280.task"
    ),
    STANDARD(
        label = "Standard",
        description = "Larger size, slower, higher quality.",
        fileName = "Gemma3-3B-IT_multi-prefill-seq_q4_block128_ekv1280.task"
    )
}