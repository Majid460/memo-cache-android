package com.majidshahbaz.memo.android.model

import android.content.Context
import com.majidshahbaz.memo.android.state.MemoNetworkState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class ModelFileManager(
    private val context: Context,
    private val customModelPath: String? = null
) {
    companion object {
        const val DEFAULT_MODEL_DIR = "llm"
        const val DEFAULT_MODEL_NAME =
            "Gemma3-1B-IT_multi-prefill-seq_q4_block128_ekv1280.task"
    }

    val modelFile: File
        get() = if (customModelPath != null) {
            File(customModelPath)
        } else {
            File(context.filesDir, "$DEFAULT_MODEL_DIR/$DEFAULT_MODEL_NAME")
        }

    fun isModelDownloaded(): Boolean = modelFile.exists() && modelFile.length() > 0

    fun downloadModel(downloadUrl: String): Flow<MemoNetworkState> = flow {
        emit(MemoNetworkState.DownloadingModel)

        try {
            modelFile.parentFile?.mkdirs()

            val connection = URL(downloadUrl).openConnection() as HttpURLConnection
            connection.connect()

            val totalBytes = connection.contentLengthLong
            var downloadedBytes = 0L

            connection.inputStream.use { input ->
                modelFile.outputStream().use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead

                        if (totalBytes > 0) {
                            val progress = ((downloadedBytes * 100) / totalBytes).toInt()
                            emit(MemoNetworkState.DownloadProgress(progress))
                        }
                    }
                }
            }

            emit(MemoNetworkState.OfflineReady)
        } catch (e: Exception) {
            modelFile.delete()
            emit(MemoNetworkState.OfflineNoModel)
        }
    }.flowOn(Dispatchers.IO)
}