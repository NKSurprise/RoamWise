package com.example.roamwise.stats

import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService
import android.annotation.SuppressLint


@RequiresApi(28) // roaming flag support
class AndroidRoamingStatsSource(private val context: Context) : RoamingStatsSource {

    private fun resolveSubscriberId(): String? = try {
        val subMgr = context.getSystemService<SubscriptionManager>()
        val activeSubId = subMgr?.activeSubscriptionInfoList?.firstOrNull()?.subscriptionId
        val tel = context.getSystemService<TelephonyManager>()
        val tmForSub = if (activeSubId != null && activeSubId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
            if (Build.VERSION.SDK_INT >= 24) tel?.createForSubscriptionId(activeSubId) else tel
        } else tel
        @SuppressLint("MissingPermission")
        @Suppress("DEPRECATION")
        tmForSub?.subscriberId
    } catch (_: SecurityException) { null } catch (_: Throwable) { null }

    @Suppress("DEPRECATION") // TYPE_MOBILE + some bucket fields are deprecated
    override fun queryBuckets(startMillis: Long, endMillis: Long): List<DataBucket> {
        val nsm = context.getSystemService<NetworkStatsManager>()
            ?: error("NetworkStatsManager not available")

        val subId = resolveSubscriberId()
        val stats: NetworkStats = nsm.querySummary(
            ConnectivityManager.TYPE_MOBILE,
            subId,
            startMillis,
            endMillis
        )

        val out = mutableListOf<DataBucket>()
        val bucket = NetworkStats.Bucket()
        while (stats.hasNextBucket()) {
            stats.getNextBucket(bucket)
            val roaming = bucket.roaming == NetworkStats.Bucket.ROAMING_YES
            out += DataBucket(
                startMillis = bucket.startTimeStamp,
                endMillis = bucket.endTimeStamp,
                rxBytes = bucket.rxBytes,
                txBytes = bucket.txBytes,
                roaming = roaming
            )
        }
        stats.close()
        return out
    }
}
