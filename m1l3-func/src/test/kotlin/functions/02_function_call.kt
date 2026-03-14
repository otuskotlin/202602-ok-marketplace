package functions

import kotlin.test.Test
import kotlin.test.assertEquals
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class SingleExpressionFunctionTest {

    fun double(x: Int): Int = x * x

    private fun captureOutput(block: () -> Unit): String {
        val out = ByteArrayOutputStream()
        val originalOut = System.out
        System.setOut(PrintStream(out))
        try {
            block()
        } finally {
            System.setOut(originalOut)
        }
        return out.toString()
    }

    private fun mainDemo() {
        println(double(4))
    }

    @Test
    fun `double of zero returns zero`() {
        assertEquals(0, double(0))
    }

    @Test
    fun `double of one returns one`() {
        assertEquals(1, double(1))
    }

    @Test
    fun `double of positive numbers`() {
        assertEquals(4, double(2))
        assertEquals(9, double(3))
        assertEquals(16, double(4))
        assertEquals(25, double(5))
    }

    @Test
    fun `double of negative numbers`() {
        assertEquals(4, double(-2))
        assertEquals(9, double(-3))
        assertEquals(16, double(-4))
        assertEquals(25, double(-5))
    }

    @Test
    fun `double of large numbers without overflow`() {
        assertEquals(2_147_395_600, double(46340))
        assertEquals(2_147_395_600, double(-46340))
    }

    @Test
    fun `double of numbers causing overflow`() {
        assertEquals(-2_147_479_015, double(46341))
        assertEquals(-2_147_479_015, double(-46341))
        assertEquals(1, double(Int.MAX_VALUE))
        assertEquals(0, double(Int.MIN_VALUE))
    }

    @Test
    fun `double property holds for arbitrary values`() {
        val values = listOf(7, -11, 123, -456, 999, -10000)
        for (x in values) {
            assertEquals(x * x, double(x))
        }
    }

    @Test
    fun `main prints expected output`() {
        val output = captureOutput { mainDemo() }
        assertEquals("16\n", output)
    }
}