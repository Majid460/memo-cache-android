package com.majidshahbaz.memo.android.state


sealed class MemoNetworkState {
    object Online : MemoNetworkState()
    object OfflineReady : MemoNetworkState()
    object OfflineNoModel : MemoNetworkState()
    object DownloadingModel : MemoNetworkState()
    object OfflineAuto : MemoNetworkState()
    data class DownloadProgress(val percent: Int) : MemoNetworkState()
}
