package com.example.roamwise.stats

import kotlin.test.assertEquals
import org.junit.Test

class RoamingUsageCalculatorTest {

    @Test
    fun sums_only_roaming_buckets() {
        val buckets = listOf(
            DataBucket(0, 10, 100, 50,  true),  // 150
            DataBucket(10, 20, 200, 100, false),// 300 (ignored)
            DataBucket(20, 30, 10,  40,  true)  // 50
        )
        val calc = RoamingUsageCalculator(object : RoamingStatsSource {
            override fun queryBuckets(startMillis: Long, endMillis: Long) = buckets
        })
        val res = calc.getRoamingUsage(0, 30)
        assertEquals(200L, res.roamingBytes) // 150 + 50
    }

    @Test
    fun percent_used_clamps_0_to_100() {
        val calc = RoamingUsageCalculator(object : RoamingStatsSource {
            override fun queryBuckets(startMillis: Long, endMillis: Long) = emptyList<DataBucket>()
        })
        assertEquals(0,   calc.percentUsed(0, 0))
        assertEquals(50,  calc.percentUsed(50, 100))
        assertEquals(100, calc.percentUsed(150, 100))
    }
}
