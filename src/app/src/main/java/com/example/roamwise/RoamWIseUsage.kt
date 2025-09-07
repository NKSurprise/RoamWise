package com.example.roamwise

import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService
import java.util.Calendar

data class RoamingUsageResult(
    val roamingBytes: Long,
    val startMillis: Long,
    val endMillis: Long
)

@RequiresApi(28) // Android 9+
fun getRoamingBytesThisCycle(
    context: Context,
    cycleStartMillis: Long,
    endMillis: Long = System.currentTimeMillis()
): RoamingUsageResult {
    val nsm = context.getSystemService<NetworkStatsManager>()
        ?: error("NetworkStatsManager not available")

    val subscriberId: String? = try {
        val subMgr = context.getSystemService<SubscriptionManager>()
        val activeSubId = subMgr?.activeSubscriptionInfoList?.firstOrNull()?.subscriptionId
        val tel = context.getSystemService<TelephonyManager>()
        val tmForSub = if (activeSubId != null && activeSubId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
            if (Build.VERSION.SDK_INT >= 24) tel?.createForSubscriptionId(activeSubId) else tel
        } else tel
        @Suppress("DEPRECATION")
        tmForSub?.subscriberId
    } catch (_: SecurityException) { null } catch (_: Throwable) { null }

    var roamingBytes = 0L
    val stats: NetworkStats = nsm.querySummary(
        ConnectivityManager.TYPE_MOBILE,
        subscriberId,
        cycleStartMillis,
        endMillis
    )
    val bucket = NetworkStats.Bucket()
    while (stats.hasNextBucket()) {
        stats.getNextBucket(bucket)
        val isRoaming = if (Build.VERSION.SDK_INT >= 28)
            bucket.roaming == NetworkStats.Bucket.ROAMING_YES else false
        if (isRoaming) roamingBytes += bucket.rxBytes + bucket.txBytes
    }
    stats.close()

    return RoamingUsageResult(roamingBytes, cycleStartMillis, endMillis)
}

fun humanBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val units = arrayOf("KB","MB","GB","TB")
    var v = bytes.toDouble()
    var i = 0
    while (v >= 1024 && i < units.lastIndex) { v /= 1024.0; i++ }
    return String.format("%.2f %s", v, units[i])
}

fun defaultCycleStartFirstOfMonth(): Long {
    val now = Calendar.getInstance()
    now.set(Calendar.DAY_OF_MONTH, 1)
    now.set(Calendar.HOUR_OF_DAY, 0)
    now.set(Calendar.MINUTE, 0)
    now.set(Calendar.SECOND, 0)
    now.set(Calendar.MILLISECOND, 0)
    return now.timeInMillis
}
