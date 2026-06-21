package com.majidshahbaz.memo.android.hardware

import android.app.ActivityManager
import android.content.Context
import android.os.StatFs

data class HardwareProfile(
    val availableStorageMb: Long,
    val totalRamMb: Long,
    val availableRamMb: Long
) {
    val isHighEnd: Boolean
        get() = totalRamMb >= 6000 && availableStorageMb >= 2000

    val isMidRange: Boolean
        get() = totalRamMb >= 3000 && availableStorageMb >= 1000

    val isLowEnd: Boolean
        get() = !isHighEnd && !isMidRange
}

class HardwareProfiler(private val context: Context) {

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

        return HardwareProfile(
            availableStorageMb = availableStorageMb,
            totalRamMb = totalRamMb,
            availableRamMb = availableRamMb
        )
    }
}