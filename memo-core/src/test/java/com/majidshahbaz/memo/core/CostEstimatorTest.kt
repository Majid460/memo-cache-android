package com.majidshahbaz.memo.core

import com.majidshahbaz.memo.core.costanalyzer.CostEstimator

import org.junit.Test
import org.junit.Assert.*

class CostEstimatorTest {

    @Test
    fun `known model calculates correct savings`() {
        val savings = CostEstimator.estimateSavings("gpt-4", 1000)
        assertEquals(0.03, savings, 0.0001)
    }

    @Test
    fun `unknown model falls back to default pricing instead of crashing`() {
        val savings = CostEstimator.estimateSavings("some-future-model", 1000)
        assertEquals(0.005, savings, 0.0001)
    }

    @Test
    fun `zero tokens means zero savings`() {
        val savings = CostEstimator.estimateSavings("gpt-4", 0)
        assertEquals(0.0, savings, 0.0001)
    }

    @Test
    fun `token estimate is roughly proportional to text length`() {
        val shortText = "Hello"
        val longText = "Hello".repeat(100)

        val shortTokens = CostEstimator.estimateTokenCount(shortText)
        val longTokens = CostEstimator.estimateTokenCount(longText)

        assertTrue(longTokens > shortTokens)
    }

    @Test
    fun `even very short text counts as at least one token`() {
        val tokens = CostEstimator.estimateTokenCount("a")
        assertEquals(1, tokens)
    }
}