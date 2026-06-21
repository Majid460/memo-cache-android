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

    fun getModelFile(tier: ModelTier): File {
        return if (customModelPath != null) {
            File(customModelPath)
        } else {
            File(context.filesDir, "${DEFAULT_MODEL_DIR}/${tier.fileName}")
        }
    }

    fun isModelDownloaded(tier: ModelTier): Boolean {
        val file = getModelFile(tier)
        return file.exists() && file.length() > 0
    }

    fun isAnyModelDownloaded(): Boolean {
        return ModelTier.values().any { isModelDownloaded(it) }
    }

    fun downloadModel(tier: ModelTier, downloadUrl: String): Flow<MemoNetworkState> = flow {
        emit(MemoNetworkState.DownloadingModel)

        try {
            val file = getModelFile(tier)
            file.parentFile?.mkdirs()

            val connection = URL(downloadUrl).openConnection() as HttpURLConnection
            connection.connect()

            val totalBytes = connection.contentLengthLong
            var downloadedBytes = 0L

            connection.inputStream.use { input ->
                file.outputStream().use { output ->
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
            getModelFile(tier).delete()
            emit(MemoNetworkState.OfflineNoModel)
        }
    }.flowOn(Dispatchers.IO)
}
