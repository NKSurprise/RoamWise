package com.example.roamwise.stats

import org.junit.Test
import kotlin.test.assertEquals

private class FakeSubs(private val items: List<SubscriptionDescriptor>) : SubscriptionProvider {
    override fun list(): List<SubscriptionDescriptor> = items
}

private class FakeBytes(private val map: Map<Int, Long>) : RoamingBytesProvider {
    override fun roamingBytes(subId: Int, startMillis: Long, endMillis: Long): Long =
        map[subId] ?: 0L
}

class PerSimUsageCalculatorTest {

    @Test
    fun `returns usage per SIM without summing`() {
        val subs = FakeSubs(
            listOf(
                SubscriptionDescriptor(1, "SIM A"),
                SubscriptionDescriptor(2, "SIM B")
            )
        )
        val bytes = FakeBytes(mapOf(1 to 1500L, 2 to 2500L))

        val calc = PerSimUsageCalculator(subs, bytes)
        val list = calc.usages(startMillis = 0L, endMillis = 100L)

        assertEquals(2, list.size)
        // SIM A
        assertEquals(1, list[0].subscriptionId)
        assertEquals("SIM A", list[0].label)
        assertEquals(1500L, list[0].roamingBytes)
        // SIM B
        assertEquals(2, list[1].subscriptionId)
        assertEquals("SIM B", list[1].label)
        assertEquals(2500L, list[1].roamingBytes)
        // And importantly, no summation anywhere.
        assertEquals(4000L, list.sumOf { it.roamingBytes }) // sum here is just to check totals if needed
    }

    @Test
    fun `handles zero or missing SIMs gracefully`() {
        val calc = PerSimUsageCalculator(FakeSubs(emptyList()), FakeBytes(emptyMap()))
        val list = calc.usages(0L, 1L)
        assertEquals(0, list.size)
    }
}
