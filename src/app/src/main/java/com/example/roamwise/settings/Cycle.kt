package com.example.roamwise.settings

import java.util.Calendar

/** Start of current cycle based on [cycleDay] (shared for both SIMs). */
fun cycleStartMillis(cycleDay: Int, nowMillis: Long = System.currentTimeMillis()): Long {
    val day = cycleDay.coerceIn(1, 28)
    val cal = Calendar.getInstance().apply { timeInMillis = nowMillis
        set(Calendar.HOUR_OF_DAY,0); set(Calendar.MINUTE,0); set(Calendar.SECOND,0); set(Calendar.MILLISECOND,0)
    }
    val nowMonth = cal.get(Calendar.MONTH)
    val nowYear = cal.get(Calendar.YEAR)

    fun maxDay(year: Int, month: Int): Int {
        val tmp = Calendar.getInstance().apply { set(Calendar.YEAR, year); set(Calendar.MONTH, month); set(Calendar.DAY_OF_MONTH, 1) }
        return tmp.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    val today = cal.get(Calendar.DAY_OF_MONTH)
    val thisMonthDay = day.coerceIn(1, maxDay(nowYear, nowMonth))

    val (y, m, d) = if (today >= thisMonthDay) {
        Triple(nowYear, nowMonth, thisMonthDay)
    } else {
        val pm = if (nowMonth == 0) 11 else nowMonth - 1
        val py = if (nowMonth == 0) nowYear - 1 else nowYear
        Triple(py, pm, day.coerceIn(1, maxDay(py, pm)))
    }

    return Calendar.getInstance().apply {
        set(Calendar.YEAR, y); set(Calendar.MONTH, m); set(Calendar.DAY_OF_MONTH, d)
        set(Calendar.HOUR_OF_DAY,0); set(Calendar.MINUTE,0); set(Calendar.SECOND,0); set(Calendar.MILLISECOND,0)
    }.timeInMillis
}
