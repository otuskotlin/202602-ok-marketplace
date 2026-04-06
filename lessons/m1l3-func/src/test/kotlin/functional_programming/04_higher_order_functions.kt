package functional_programming

import kotlin.test.Test
import kotlin.test.assertEquals
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class HigherOrderFunctionTest {

    fun processNumber(num: Int, operation: (Int) -> Int): Int = operation(num)

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
        val result1 = processNumber(10) { it * 2 }
        val result2 = processNumber(10) { it + 5 }
        println(result1)
        println(result2)
    }

    @Test
    fun `processNumber with multiplication`() {
        assertEquals(20, processNumber(10) { it * 2 })
        assertEquals(0, processNumber(0) { it * 5 })
        assertEquals(-10, processNumber(-5) { it * 2 })
        assertEquals(100, processNumber(10) { it * it })
    }

    @Test
    fun `processNumber with addition`() {
        assertEquals(15, processNumber(10) { it + 5 })
        assertEquals(5, processNumber(0) { it + 5 })
        assertEquals(0, processNumber(-5) { it + 5 })
        assertEquals(Int.MAX_VALUE, processNumber(Int.MAX_VALUE) { it + 0 })
    }

    @Test
    fun `processNumber with subtraction`() {
        assertEquals(5, processNumber(10) { it - 5 })
        assertEquals(-5, processNumber(0) { it - 5 })
        assertEquals(-10, processNumber(-5) { it - 5 })
    }

    @Test
    fun `processNumber with division`() {
        assertEquals(5, processNumber(10) { it / 2 })
        assertEquals(0, processNumber(5) { it / 10 })
        assertEquals(-2, processNumber(-10) { it / 5 })
    }

    @Test
    fun `processNumber with identity`() {
        assertEquals(42, processNumber(42) { it })
        assertEquals(-7, processNumber(-7) { it })
        assertEquals(0, processNumber(0) { it })
    }

    @Test
    fun `processNumber with constant function`() {
        assertEquals(100, processNumber(123) { 100 })
        assertEquals(0, processNumber(999) { 0 })
    }

    @Test
    fun `processNumber does not modify original number`() {
        val original = 7
        val result = processNumber(original) { it * 2 }
        assertEquals(7, original)
        assertEquals(14, result)
    }

    @Test
    fun `processNumber with large numbers`() {
        assertEquals(Int.MAX_VALUE, processNumber(Int.MAX_VALUE) { it })
        assertEquals(Int.MAX_VALUE - 1, processNumber(Int.MAX_VALUE) { it - 1 })
        assertEquals(Int.MIN_VALUE, processNumber(Int.MIN_VALUE) { it })
        assertEquals(Int.MIN_VALUE + 1, processNumber(Int.MIN_VALUE) { it + 1 })
    }

    @Test
    fun `main prints expected output`() {
        val output = captureOutput { mainDemo() }
        val lines = output.lines().filter { it.isNotEmpty() }
        assertEquals(2, lines.size)
        assertEquals("20", lines[0])
        assertEquals("15", lines[1])
    }
}