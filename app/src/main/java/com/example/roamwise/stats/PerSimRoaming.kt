package com.example.roamwise.stats

import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService
import android.annotation.SuppressLint


data class SimUsage(
    val subscriptionId: Int,
    val label: String,
    val roamingBytes: Long
)

@RequiresApi(28)
fun listActiveSubscriptions(context: Context): List<SubscriptionInfo> {
    val subMgr = context.getSystemService<SubscriptionManager>() ?: return emptyList()
    return try {
        subMgr.activeSubscriptionInfoList.orEmpty()
    } catch (_: SecurityException) {
        emptyList()
    } catch (_: Throwable) {
        emptyList()
    }
}

@RequiresApi(28)
@Suppress("DEPRECATION") // TYPE_MOBILE + subscriberId are deprecated
fun getRoamingBytesForSubscription(
    context: Context,
    subscriptionId: Int,
    startMillis: Long,
    endMillis: Long = System.currentTimeMillis()
): Long {
    val nsm = context.getSystemService<NetworkStatsManager>() ?: return 0L
    val tel = context.getSystemService<TelephonyManager>()

    @SuppressLint("MissingPermission")
    @Suppress("DEPRECATION")
    val subscriberId = try {
        val tmForSub = if (Build.VERSION.SDK_INT >= 24) tel?.createForSubscriptionId(subscriptionId) else tel
        tmForSub?.subscriberId
    } catch (_: SecurityException) { null } catch (_: Throwable) { null }

    val stats: NetworkStats = nsm.querySummary(
        ConnectivityManager.TYPE_MOBILE,
        subscriberId,
        startMillis,
        endMillis
    )
    var total = 0L
    val b = NetworkStats.Bucket()
    while (stats.hasNextBucket()) {
        stats.getNextBucket(b)
        if (b.roaming == NetworkStats.Bucket.ROAMING_YES) {
            total += b.rxBytes + b.txBytes
        }
    }
    stats.close()
    return total
}
@RequiresApi(28)
@Suppress("DEPRECATION")
fun getTotalMobileBytesForSubscription(
    context: Context,
    subscriptionId: Int,
    startMillis: Long,
    endMillis: Long = System.currentTimeMillis()
): Long {
    val nsm = context.getSystemService<NetworkStatsManager>() ?: return 0L
    val tel = context.getSystemService<TelephonyManager>()
    val subscriberId = try {
        val tmForSub = if (Build.VERSION.SDK_INT >= 24) tel?.createForSubscriptionId(subscriptionId) else tel
        @Suppress("DEPRECATION")
        tmForSub?.subscriberId
    } catch (_: SecurityException) { null } catch (_: Throwable) { null }

    val stats: NetworkStats = nsm.querySummary(
        ConnectivityManager.TYPE_MOBILE,
        subscriberId,
        startMillis,
        endMillis
    )
    var total = 0L
    val b = NetworkStats.Bucket()
    while (stats.hasNextBucket()) {
        stats.getNextBucket(b)
        total += b.rxBytes + b.txBytes   // NOTE: no roaming check here
    }
    stats.close()
    return total
}

