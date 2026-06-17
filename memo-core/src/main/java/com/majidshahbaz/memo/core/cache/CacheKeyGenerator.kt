package com.majidshahbaz.memo.core.cache


import java.security.MessageDigest

object CacheKeyGenerator {
    fun generate(prompt: String, model: String, temperature: Double = 0.0): String {
        val normalized = prompt.trim().lowercase()
        val raw = "$model|$normalized|$temperature"
        val bytes = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}