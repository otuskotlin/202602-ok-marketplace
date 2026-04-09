package advanced_functions

import kotlin.test.Test
import kotlin.test.assertEquals

class InlineFunctionTest {

    inline fun square(x: Int): Int = x * x

    @Test
    fun `square of zero returns zero`() {
        assertEquals(0, square(0))
    }

    @Test
    fun `square of positive numbers`() {
        assertEquals(1, square(1))
        assertEquals(4, square(2))
        assertEquals(9, square(3))
        assertEquals(16, square(4))
    }

    @Test
    fun `square of negative numbers`() {
        assertEquals(1, square(-1))
        assertEquals(4, square(-2))
        assertEquals(9, square(-3))
        assertEquals(16, square(-4))
    }

    @Test
    fun `square of large values without overflow`() {
        assertEquals(2_147_395_600, square(46340))
        assertEquals(2_147_395_600, square(-46340))
    }

    @Test
    fun `square of values causing overflow`() {
        assertEquals(-2_147_479_015, square(46341))
        assertEquals(-2_147_479_015, square(-46341))

        assertEquals(1, square(Int.MAX_VALUE))

        assertEquals(0, square(Int.MIN_VALUE))
    }

    @Test
    fun `square property holds for arbitrary values`() {
        val testValues = listOf(5, -7, 42, -100, 12345, -54321)
        for (x in testValues) {
            assertEquals(x * x, square(x))
        }
    }
}