package com.example.roamwise.stats

data class DataBucket(
    val startMillis: Long,
    val endMillis: Long,
    val rxBytes: Long,
    val txBytes: Long,
    val roaming: Boolean
)

interface RoamingStatsSource {
    /**
     * Returns mobile data buckets between [startMillis, endMillis].
     * Each bucket indicates if it was marked as roaming by the OS.
     */
    fun queryBuckets(startMillis: Long, endMillis: Long): List<DataBucket>
}

/** Pure calculator that sums roaming-only buckets. Easy to unit test. */
class RoamingUsageCalculator(private val source: RoamingStatsSource) {
    data class Result(
        val roamingBytes: Long,
        val startMillis: Long,
        val endMillis: Long
    )

    fun getRoamingUsage(startMillis: Long, endMillis: Long): Result {
        val total = source.queryBuckets(startMillis, endMillis)
            .filter { it.roaming }
            .sumOf { it.rxBytes + it.txBytes }
        return Result(total, startMillis, endMillis)
    }

    fun percentUsed(roamingBytes: Long, quotaBytes: Long): Int {
        if (quotaBytes <= 0) return 0
        val pct = ((roamingBytes.toDouble() * 100.0) / quotaBytes).toInt()
        return pct.coerceIn(0, 100)
    }
}
