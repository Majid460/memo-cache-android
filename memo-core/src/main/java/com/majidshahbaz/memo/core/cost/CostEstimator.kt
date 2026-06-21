package com.majidshahbaz.memo.core.cost

object CostEstimator {

    // price per 1,000 tokens, in USD — approximate, input-token pricing
    private val pricingPerThousandTokens = mapOf(
        "gpt-4" to 0.03,
        "gpt-4o" to 0.005,
        "gpt-3.5-turbo" to 0.0015,
        "gemini-1.5-pro" to 0.0035,
        "gemini-1.5-flash" to 0.00035,
        "gemini-2.5-flash" to 0.0003,
        "claude-3-opus" to 0.015,
        "claude-3-sonnet" to 0.003
    )

    private const val DEFAULT_PRICE_PER_THOUSAND = 0.005

    fun estimateSavings(model: String, tokenCount: Int): Double {
        val pricePerThousand = pricingPerThousandTokens[model] ?: DEFAULT_PRICE_PER_THOUSAND
        return (tokenCount / 1000.0) * pricePerThousand
    }

    fun estimateTokenCount(text: String): Int {
        // rough heuristic: ~4 characters per token, standard approximation
        return (text.length / 4).coerceAtLeast(1)
    }
}