package com.example.roamwise.stats

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi

class AndroidSubscriptionProvider(private val context: Context) : SubscriptionProvider {
    @RequiresApi(28)
    override fun list(): List<SubscriptionDescriptor> =
        listActiveSubscriptions(context).map {
            SubscriptionDescriptor(
                id = it.subscriptionId,
                label = (it.displayName ?: it.carrierName ?: "SIM ${it.subscriptionId}").toString()
            )
        }
}

class AndroidRoamingBytesProvider(private val context: Context) : RoamingBytesProvider {
    @RequiresApi(28)
    override fun roamingBytes(subId: Int, startMillis: Long, endMillis: Long): Long =
        getRoamingBytesForSubscription(context, subId, startMillis, endMillis)
}
