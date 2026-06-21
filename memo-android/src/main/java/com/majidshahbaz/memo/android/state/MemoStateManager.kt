package com.majidshahbaz.memo.android.state

import android.content.Context
import com.majidshahbaz.memo.android.hardware.HardwareProfiler
import com.majidshahbaz.memo.android.model.ModelFileManager
import com.majidshahbaz.memo.android.model.ModelResolverApi
import com.majidshahbaz.memo.android.network.NetworkObserver
import com.majidshahbaz.memo.android.network.NetworkStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

    private val _networkState = MutableStateFlow<MemoNetworkState>(
        MemoNetworkState.Online
    )
    val networkState: StateFlow<MemoNetworkState> = _networkState.asStateFlow()

    fun startObserving() {
        scope.launch {
            networkObserver.observe().collect { status ->
                _networkState.value = when (status) {
                    NetworkStatus.Available -> MemoNetworkState.Online

                    NetworkStatus.Unavailable -> {
                        if (modelFileManager.isModelDownloaded()) {
                            MemoNetworkState.OfflineReady
                        } else {
                            MemoNetworkState.OfflineNoModel
                        }
                    }
                }
            }
        }
    }

    fun downloadModelIfNeeded(): Flow<MemoNetworkState> {
        if (modelFileManager.isModelDownloaded()) {
            return flowOf(MemoNetworkState.OfflineReady)
        }

        // Priority: explicit URL > resolver endpoint > nothing available
        val directUrl = modelDownloadUrl
        if (directUrl != null) {
            return modelFileManager.downloadModel(directUrl).also { flow ->
                scope.launch {
                    flow.collect { state -> _networkState.value = state }
                }
            }
        }

        if (resolverApi != null) {
            return kotlinx.coroutines.flow.flow {
                emit(MemoNetworkState.DownloadingModel)

                val profile = hardwareProfiler.profile()
                val resolved = resolverApi.resolveModel(
                    totalRamMb = profile.totalRamMb,
                    availableStorageMb = profile.availableStorageMb
                )

                if (resolved == null) {
                    emit(MemoNetworkState.OfflineNoModel)
                    return@flow
                }

                modelFileManager.downloadModel(resolved.downloadUrl).collect { state ->
                    emit(state)
                }
            }.also { flow ->
                scope.launch {
                    flow.collect { state -> _networkState.value = state }
                }
            }
        }

        return flowOf(MemoNetworkState.OfflineNoModel)
    }
}