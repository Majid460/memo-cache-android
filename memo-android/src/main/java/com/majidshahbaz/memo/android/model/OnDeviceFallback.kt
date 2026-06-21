package com.majidshahbaz.memo.android.model

import android.content.Context
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

/**
 * A production-ready, thread-safe, and non-blocking On-Device LLM Fallback client.
 * Engineered for library distribution to ensure it never blocks caller threads during setup or execution.
 */
class OnDeviceFallback(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AutoCloseable {

    private var modelPath: String? = null

    // Engine/Conversation instances protected behind a Mutex to prevent multi-thread racing
    private val initializationMutex = Mutex()
    private var engine: Engine? = null
    private var conversation: Conversation? = null

    private val isInitialized = AtomicBoolean(false)
    private val isClosed = AtomicBoolean(false)

    /**
     * Asynchronously initializes the LiteRT-LM engine and creates the session conversation.
     * Safe to call multiple times concurrently; initialization will only execute once.
     */
    suspend fun initialize(file: java.io.File): Unit = withContext(ioDispatcher) {
        if (isClosed.get()) return@withContext

        val newPath = file.absolutePath
        if (isInitialized.get() && modelPath == newPath) return@withContext

        initializationMutex.withLock {
            // If already initialized with a DIFFERENT model, close the old one first
            if (isInitialized.get() && modelPath != newPath) {
                closeInternal()
                isClosed.set(false)
            }

            modelPath = newPath
            val config = EngineConfig(modelPath = newPath)
            val nativeEngine = Engine(config)

            try {
                nativeEngine.initialize()
                engine = nativeEngine
                conversation = nativeEngine.createConversation()
                isInitialized.set(true)
            } catch (e: Exception) {
                nativeEngine.close()
                throw IllegalStateException("Failed to initialize LiteRT-LM engine fallback with $newPath", e)
            }
        }
    }

    /**
     * Safely executes an asynchronous streaming prompt. If the engine isn't initialized yet,
     * it automatically kicks off the initialization process in a thread-safe manner.
     */
    suspend fun generateResponseStream(prompt: String, file: java.io.File): Flow<String> {
        if (isClosed.get()) {
            throw IllegalStateException("Cannot generate responses on a closed OnDeviceFallback instance.")
        }

        if (!isInitialized.get() || modelPath != file.absolutePath) {
            initialize(file)
        }

        val activeConversation = conversation
            ?: throw IllegalStateException("Conversation failed to construct during initialization.")

        return activeConversation.sendMessageAsync(prompt)
            .map { message -> message.toString() }
            .catch { e -> emit("Inference error: ${e.localizedMessage}") }
            // Forces data emissions and stream maps to execute safely off the Main thread
            .flowOn(ioDispatcher)
    }

    /**
     * Closes the underlying LiteRT resources cleanly. Safe to call safely from any thread context.
     */
    override fun close() {
        if (isClosed.getAndSet(true)) return
        closeInternal()
    }

    private fun closeInternal() {
        try {
            conversation?.close()
            engine?.close()
        } catch (e: Exception) {
            // Log framework cleanup anomalies inside your library's logger here if needed
        } finally {
            conversation = null
            engine = null
            isInitialized.set(false)
        }
    }
}