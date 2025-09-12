package com.example.roamwise.stats

data class SubscriptionDescriptor(val id: Int, val label: String)

/** Lists active SIMs (id + label). */
interface SubscriptionProvider {
    fun list(): List<SubscriptionDescriptor>
}

/** Returns roaming bytes for a given subId in [startMillis, endMillis]. */
interface RoamingBytesProvider {
    fun roamingBytes(subId: Int, startMillis: Long, endMillis: Long): Long
}

class PerSimUsageCalculator(
    private val subs: SubscriptionProvider,
    private val bytes: RoamingBytesProvider
) {
    fun usages(startMillis: Long, endMillis: Long): List<SimUsage> =
        subs.list().map { sub ->
            SimUsage(
                subscriptionId = sub.id,
                label = sub.label,
                roamingBytes = bytes.roamingBytes(sub.id, startMillis, endMillis)
            )
        }
}
