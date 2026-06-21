package com.majidshahbaz.memo.android.hardware

import android.app.ActivityManager
import android.content.Context
import android.os.StatFs

data class HardwareProfile(
    val availableStorageMb: Long,
    val totalRamMb: Long,
    val availableRamMb: Long,
    val cpuUsagePercent: Int = 0
) {
    val isHighEnd: Boolean
        get() = totalRamMb >= 6000 && availableStorageMb >= 2000

    val isMidRange: Boolean
        get() = totalRamMb >= 3000 && availableStorageMb >= 1000

    val isLowEnd: Boolean
        get() = !isHighEnd && !isMidRange
}

class HardwareProfiler(private val context: Context) {

    private var lastCpuTime = 0L
    private var lastSampleTime = 0L

    fun profile(): HardwareProfile {
        val activityManager =
            context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)

        val statFs = StatFs(context.filesDir.absolutePath)
        val availableBytes = statFs.availableBlocksLong * statFs.blockSizeLong
        val availableStorageMb = availableBytes / (1024 * 1024)

        val totalRamMb = memInfo.totalMem / (1024 * 1024)
        val availableRamMb = memInfo.availMem / (1024 * 1024)

        val currentCpuTime = android.os.Process.getElapsedCpuTime()
        val currentSampleTime = System.currentTimeMillis()

        val cpuUsage = if (lastSampleTime > 0) {
            val cpuDiff = currentCpuTime - lastCpuTime
            val timeDiff = currentSampleTime - lastSampleTime
            if (timeDiff > 0) {
                ((cpuDiff.toFloat() / timeDiff.toFloat()) * 100).toInt().coerceIn(0, 100)
            } else 0
        } else 0

        lastCpuTime = currentCpuTime
        lastSampleTime = currentSampleTime

        return HardwareProfile(
            availableStorageMb = availableStorageMb,
            totalRamMb = totalRamMb,
            availableRamMb = availableRamMb,
            cpuUsagePercent = cpuUsage
        )
    }
}
