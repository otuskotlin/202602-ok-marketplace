package advanced_functions

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TailrecFactorialTest {

    tailrec fun factorial(n: Int, acc: Int = 1): Int =
        if (n <= 1) acc else factorial(n - 1, n * acc)

    @Test
    fun `factorial of zero`() {
        assertEquals(1, factorial(0))
    }

    @Test
    fun `factorial of one`() {
        assertEquals(1, factorial(1))
    }

    @Test
    fun `factorial of two`() {
        assertEquals(2, factorial(2))
    }

    @Test
    fun `factorial of five`() {
        assertEquals(120, factorial(5))
    }

    @Test
    fun `factorial of negative numbers`() {
        // Для n <= 1 возвращается acc (по умолчанию 1)
        assertEquals(1, factorial(-1))
        assertEquals(1, factorial(-5))
    }

    @Test
    fun `factorial with explicit accumulator`() {
        assertEquals(12, factorial(3, 2))
        assertEquals(240, factorial(4, 10))
        assertEquals(42, factorial(-10, 42))
    }

    @Test
    fun `factorial of 12`() {
        assertEquals(479001600, factorial(12))
    }

    @Test
    fun `factorial of 13 overflows`() {
        assertEquals(1932053504, factorial(13))
    }

    @Test
    fun `factorial of large number does not stack overflow`() {
        val result = factorial(10000)
        assertTrue(result is Int)
    }
}
