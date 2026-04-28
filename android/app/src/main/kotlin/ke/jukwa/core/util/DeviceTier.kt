package ke.jukwa.core.util

import android.app.ActivityManager
import android.content.Context

/**
 * JUKWA Device Tier Detection
 * Source: Architecture §3.1
 * 
 * Tiers:
 * - LOW (<= 2GB RAM or LowRamDevice)
 * - STANDARD (3-4GB RAM)
 * - HIGH (4GB+ RAM)
 */

enum class DeviceTier {
    LOW,
    STANDARD,
    HIGH
}

class DeviceTierManager(private val context: Context) {
    
    fun getDeviceTier(): DeviceTier {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        
        // 1. Check for Low RAM device flag (Android Go / < 2GB)
        if (activityManager.isLowRamDevice) {
            return DeviceTier.LOW
        }
        
        // 2. Check total memory in GB
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        val totalRamGb = memoryInfo.totalMem / (1024 * 1024 * 1024.0)
        
        return when {
            totalRamGb <= 2.0 -> DeviceTier.LOW
            totalRamGb <= 4.0 -> DeviceTier.STANDARD
            else -> DeviceTier.HIGH
        }
    }
}
