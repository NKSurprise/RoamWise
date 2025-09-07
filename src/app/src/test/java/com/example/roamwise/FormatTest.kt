package com.example.roamwise

import org.junit.Test
import kotlin.test.assertEquals

class FormatTest {
    @Test fun `formats bytes`() {
        assertEquals("500 B", humanBytes(500))
    }
    @Test fun `formats KB MB GB`() {
        assertEquals("1.00 KB", humanBytes(1024))
        assertEquals("1.00 MB", humanBytes(1024L * 1024))
        assertEquals("1.00 GB", humanBytes(1024L * 1024 * 1024))
    }
}
