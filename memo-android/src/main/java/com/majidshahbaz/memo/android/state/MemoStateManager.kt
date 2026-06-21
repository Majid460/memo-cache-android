package com.majidshahbaz.memo.android.state

import android.content.Context
import com.majidshahbaz.memo.android.hardware.HardwareProfiler
import com.majidshahbaz.memo.android.model.ModelFileManager
import com.majidshahbaz.memo.android.model.ModelResolverApi
import com.majidshahbaz.memo.android.model.ModelTier
import com.majidshahbaz.memo.android.network.NetworkObserver
import com.majidshahbaz.memo.android.network.NetworkStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class MemoStateManager(
    private val context: Context,
    private val modelFileManager: ModelFileManager,
    private val autoDownloadModel: Boolean = true,
    private val modelDownloadUrl: String? = null,
    private val modelResolverEndpoint: String? = null
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val networkObserver = NetworkObserver(context)
    private val hardwareProfiler = HardwareProfiler(context)
    private val resolverApi = modelResolverEndpoint?.let { ModelResolverApi(it) }

    private var downloadJob: Job? = null

    private val _networkState = MutableStateFlow<MemoNetworkState>(
        MemoNetworkState.Online
    )
    val networkState: StateFlow<MemoNetworkState> = _networkState.asStateFlow()

    fun startObserving(currentTier: ModelTier) {
        scope.launch {
            networkObserver.observe().collect { status ->
                // Don't let network status changes overwrite an active download progress UI
                val isDownloading = downloadJob?.isActive == true
                if (!isDownloading) {
                    _networkState.value = when (status) {
                        NetworkStatus.Available -> MemoNetworkState.Online

                        NetworkStatus.Unavailable -> {
                            if (modelFileManager.isModelDownloaded(currentTier)) {
                                MemoNetworkState.OfflineReady
                            } else {
                                MemoNetworkState.OfflineNoModel
                            }
                        }
                    }
                }
            }
        }
    }

    fun downloadModelIfNeeded(tier: ModelTier): Flow<MemoNetworkState> {
        if (modelFileManager.isModelDownloaded(tier)) {
            return flowOf(MemoNetworkState.OfflineReady)
        }

        downloadJob?.cancel()

        // Priority: explicit URL > resolver endpoint > nothing available
        val directUrl = modelDownloadUrl
        if (directUrl != null) {
            val flow = modelFileManager.downloadModel(tier, directUrl)
            downloadJob = scope.launch {
                try {
                    flow.collect { state -> _networkState.value = state }
                } finally {
                    // Once download is done (success or failure), sync back with the current network status
                    val currentStatus = if (networkObserver.isNetworkAvailable()) {
                        MemoNetworkState.Online
                    } else {
                        if (modelFileManager.isModelDownloaded(tier)) {
                            MemoNetworkState.OfflineReady
                        } else {
                            MemoNetworkState.OfflineNoModel
                        }
                    }
                    _networkState.value = currentStatus
                }
            }
            return flow
        }

        if (resolverApi != null) {
            val flow = kotlinx.coroutines.flow.flow {
                emit(MemoNetworkState.DownloadingModel)

                val profile = hardwareProfiler.profile()
                val resolved = resolverApi.resolveModel(
                    totalRamMb = profile.totalRamMb,
                    availableStorageMb = profile.availableStorageMb,
                    requestedTier = tier.name
                )

                if (resolved == null) {
                    emit(MemoNetworkState.OfflineNoModel)
                    return@flow
                }

                modelFileManager.downloadModel(tier, resolved.downloadUrl).collect { state ->
                    emit(state)
                }
            }
            downloadJob = scope.launch {
                try {
                    flow.collect { state -> _networkState.value = state }
                } finally {
                    // Once download is done (success or failure), sync back with the current network status
                    val currentStatus = if (networkObserver.isNetworkAvailable()) {
                        MemoNetworkState.Online
                    } else {
                        if (modelFileManager.isModelDownloaded(tier)) {
                            MemoNetworkState.OfflineReady
                        } else {
                            MemoNetworkState.OfflineNoModel
                        }
                    }
                    _networkState.value = currentStatus
                }
            }
            return flow
        }

        return flowOf(MemoNetworkState.OfflineNoModel)
    }

    fun cancelDownload() {
        downloadJob?.cancel()
        downloadJob = null
        // Re-evaluate state after cancellation
        _networkState.value = if (networkObserver.isNetworkAvailable()) {
            MemoNetworkState.Online
        } else {
            MemoNetworkState.OfflineNoModel
        }
    }

    fun deleteModel(tier: ModelTier) {
        modelFileManager.deleteModel(tier)
        // If we deleted the active tier, update state
        if (_networkState.value == MemoNetworkState.OfflineReady || _networkState.value == MemoNetworkState.OfflineAuto) {
             _networkState.value = if (networkObserver.isNetworkAvailable()) {
                MemoNetworkState.Online
            } else {
                MemoNetworkState.OfflineNoModel
            }
        }
    }
}
