package jetbrains.datalore.base.numberFormat

import kotlin.test.Test
import kotlin.test.assertEquals

class NumberFormatSignTest {
    @Test
    fun padAfterSign() {
        assertEquals("+0", NumberFormat("=+1,d").apply(0))
        assertEquals("+0", NumberFormat("=+2,d").apply(0))
        assertEquals("+ 0", NumberFormat("=+3,d").apply(0))
        assertEquals("+   0", NumberFormat("=+5,d").apply(0))
        assertEquals("+      0", NumberFormat("=+8,d").apply(0))
        assertEquals("+           0", NumberFormat("=+13,d").apply(0))
        assertEquals("+                   0", NumberFormat("=+21,d").apply(0))
        assertEquals("+               1e+21", NumberFormat("=+21,d").apply(1e21))
    }

    @Test
    fun onlyUseSignForNegativeNumbers() {
        assertEquals("-1", NumberFormat("-1,d").apply(-1))
        assertEquals("0", NumberFormat("-1,d").apply(0))
        assertEquals(" 0", NumberFormat("-2,d").apply(0))
        assertEquals("  0", NumberFormat("-3,d").apply(0))
        assertEquals("    0", NumberFormat("-5,d").apply(0))
        assertEquals("       0", NumberFormat("-8,d").apply(0))
        assertEquals("            0", NumberFormat("-13,d").apply(0))
        assertEquals("                    0", NumberFormat("-21,d").apply(0))
    }
}