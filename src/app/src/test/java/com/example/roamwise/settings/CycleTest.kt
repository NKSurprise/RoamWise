package com.example.roamwise.settings

import org.junit.Test
import kotlin.test.assertEquals

class CycleTest {

    // Helper to make a millis for YYYY,MM,DD at 12:00 to avoid TZ headaches
    private fun millisOf(y:Int, m:Int, d:Int): Long {
        val cal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.YEAR, y)
            set(java.util.Calendar.MONTH, m - 1) // 1..12 -> 0..11
            set(java.util.Calendar.DAY_OF_MONTH, d)
            set(java.util.Calendar.HOUR_OF_DAY, 12)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    @Test
    fun `when today is ON or AFTER cycle day, start is this month's cycle day`() {
        val now = millisOf(2025, 7, 20) // July 20
        val start = cycleStartMillis(cycleDay = 15, nowMillis = now)
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = start }
        assertEquals(2025, cal.get(java.util.Calendar.YEAR))
        assertEquals(java.util.Calendar.JULY, cal.get(java.util.Calendar.MONTH))
        assertEquals(15, cal.get(java.util.Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `when today is BEFORE cycle day, start is last month's cycle day`() {
        val now = millisOf(2025, 7, 10) // July 10
        val start = cycleStartMillis(cycleDay = 15, nowMillis = now)
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = start }
        assertEquals(2025, cal.get(java.util.Calendar.YEAR))
        assertEquals(java.util.Calendar.JUNE, cal.get(java.util.Calendar.MONTH))
        assertEquals(15, cal.get(java.util.Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `cycle day clamps in short months`() {
        // March 5, cycle day 31 -> February's max day (28 in non-leap year)
        val now = millisOf(2025, 3, 5)
        val start = cycleStartMillis(cycleDay = 31, nowMillis = now)
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = start }
        assertEquals(java.util.Calendar.FEBRUARY, cal.get(java.util.Calendar.MONTH))
        assertEquals(28, cal.get(java.util.Calendar.DAY_OF_MONTH))
    }

}
