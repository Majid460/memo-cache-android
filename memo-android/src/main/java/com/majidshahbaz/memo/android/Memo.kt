package com.majidshahbaz.memo.android

import android.content.Context
import com.majidshahbaz.memo.android.cache.MemoDatabase
import com.majidshahbaz.memo.android.cache.RoomCacheStore
import com.majidshahbaz.memo.android.model.ModelFileManager
import com.majidshahbaz.memo.android.model.OnDeviceFallback
import com.majidshahbaz.memo.android.state.MemoNetworkState
import com.majidshahbaz.memo.android.state.MemoStateManager
import com.majidshahbaz.memo.core.cache.CacheStore
import com.majidshahbaz.memo.core.cache.MemoCache
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class Memo private constructor(
    private val memoCache: MemoCache,
    private val stateManager: MemoStateManager,
    private val onDeviceFallback: OnDeviceFallback,
    private val modelFileManager: ModelFileManager
) : AutoCloseable {

    private val internalScope = kotlinx.coroutines.CoroutineScope(
        kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.IO
    )
    val networkState: StateFlow<MemoNetworkState> = stateManager.networkState

    val isModelDownloaded: Boolean
        get() = modelFileManager.isModelDownloaded()

    fun downloadModel() {
        internalScope.launch {
            stateManager.downloadModelIfNeeded().collect { /* updates flow into networkState internally */ }
        }
    }

    fun resolve(
        prompt: String,
        model: String = "gemini-1.5-flash",
        cloudApiCall: (suspend (String) -> String)? = null
    ): Flow<String> = flow {
        // 1. Check cache first, regardless of online/offline state
        val cachedHit = memoCache.peek(prompt, model)
        if (cachedHit != null) {
            emit(cachedHit)
            return@flow
        }

        val currentState = stateManager.networkState.value

        // 2. Online + dev provided a cloud call → use it, cache the result
        if (currentState == MemoNetworkState.Online && cloudApiCall != null) {
            val result = memoCache.call(
                prompt = prompt,
                model = model,
                apiCall = { cloudApiCall(prompt) }
            )
            emit(result.text)
            return@flow
        }

        // 3. Offline (or no cloud call provided) → use on-device model if available
        if (!modelFileManager.isModelDownloaded()) {
            throw IllegalStateException(
                "No network and no offline model available."
            )
        }

        onDeviceFallback.initialize()

        var fullResponse = ""
        onDeviceFallback.generateResponseStream(prompt).collect { token ->
            fullResponse += token
            emit(token)
        }

        // 4. Manually cache the completed on-device response for next time
        memoCache.call(
            prompt = prompt,
            model = model,
            apiCall = { fullResponse }
        )
    }

    override fun close() {
        onDeviceFallback.close()
        internalScope.cancel()
    }

    class Builder(private val context: Context) {
        private var autoDownload: Boolean = false
        private var downloadUrl: String? = null
        private var resolverEndpoint: String? = null
        private var customModelPath: String? = null
        private var cacheStore: CacheStore? = null

        fun autoDownloadModel(enabled: Boolean, downloadUrl: String? = null): Builder {
            this.autoDownload = enabled
            this.downloadUrl = downloadUrl
            return this
        }

        // New: point to a hardware-aware resolver backend instead of a fixed URL
        fun modelResolverEndpoint(endpoint: String): Builder {
            this.resolverEndpoint = endpoint
            return this
        }

        fun modelPath(path: String): Builder {
            this.customModelPath = path
            return this
        }

        fun cacheStore(store: CacheStore): Builder {
            this.cacheStore = store
            return this
        }

        fun build(): Memo {
            val store = cacheStore
                ?: RoomCacheStore(MemoDatabase.getInstance(context).cacheDao())
            val memoCache = MemoCache(store)
            val modelFileManager = ModelFileManager(context, customModelPath)
            val stateManager = MemoStateManager(
                context = context,
                modelFileManager = modelFileManager,
                autoDownloadModel = autoDownload,
                modelDownloadUrl = downloadUrl,
                modelResolverEndpoint = resolverEndpoint
            )
            val fallback = OnDeviceFallback(context)

            stateManager.startObserving()

            return Memo(
                memoCache = memoCache,
                stateManager = stateManager,
                onDeviceFallback = fallback,
                modelFileManager = modelFileManager
            )
        }
    }
}