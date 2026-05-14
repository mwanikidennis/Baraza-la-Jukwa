package ke.jukwa.core.util

import android.app.ActivityManager
import android.content.Context
import javax.inject.Inject
import javax.inject.Singleton

enum class DeviceTier {
    LOW,
    STANDARD,
    HIGH
}

@Singleton
class DeviceTierManager @Inject constructor(
    private val context: Context
) {
    val tier: DeviceTier by lazy { detectTier() }

    private fun detectTier(): DeviceTier {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        if (activityManager.isLowRamDevice) {
            return DeviceTier.LOW
        }

        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        val totalRamGb = memoryInfo.totalMem / (1024.0 * 1024.0 * 1024.0)

        return when {
            totalRamGb <= 2.0 -> DeviceTier.LOW
            totalRamGb <= 4.0 -> DeviceTier.STANDARD
            else -> DeviceTier.HIGH
        }
    }
}
