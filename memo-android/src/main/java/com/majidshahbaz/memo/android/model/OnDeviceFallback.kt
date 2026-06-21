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

    private val modelPath = File(
        context.filesDir,
        "llm/Gemma3-1B-IT_multi-prefill-seq_q4_block128_ekv1280.task"
    ).absolutePath

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
    suspend fun initialize(): Unit = withContext(ioDispatcher) {
        if (isInitialized.get() || isClosed.get()) return@withContext

        initializationMutex.withLock {
            // Double-checked locking pattern inside Mutex
            if (isInitialized.get() || isClosed.get()) return@withLock

            val config = EngineConfig(modelPath = modelPath)
            val nativeEngine = Engine(config)

            try {
                // Initialize is a heavy JNI operation; executed cleanly on the I/O thread pool
                nativeEngine.initialize()
                engine = nativeEngine
                conversation = nativeEngine.createConversation()
                isInitialized.set(true)
            } catch (e: Exception) {
                // Prevent resource leaks if conversation setup fails after engine initialization
                nativeEngine.close()
                throw IllegalStateException("Failed to initialize LiteRT-LM engine fallback", e)
            }
        }
    }

    /**
     * Safely executes an asynchronous streaming prompt. If the engine isn't initialized yet,
     * it automatically kicks off the initialization process in a thread-safe manner.
     */
    suspend fun generateResponseStream(prompt: String): Flow<String> {
        if (isClosed.get()) {
            throw IllegalStateException("Cannot generate responses on a closed OnDeviceFallback instance.")
        }

        // Implicitly initialize if the library consumer forgot to call initialize() explicitly
        if (!isInitialized.get()) {
            initialize()
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

        try {
            conversation?.close()
            engine?.close()
        } catch (e: Exception) {
            // Log framework cleanup anomalies inside your library's logger here if needed
        } finally {
            conversation = null
            engine = null
        }
    }
}