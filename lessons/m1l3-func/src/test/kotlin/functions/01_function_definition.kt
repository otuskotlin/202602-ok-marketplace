package functions

import kotlin.test.Test
import kotlin.test.assertEquals
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class FunctionDefinitionTest {

    fun double(x: Int): Int = x * x

    fun greet(name: String) {
        println("Hello, $name!")
    }

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
        val result = double(4)
        println("Квадрат числа 4 равен: $result")
        greet("Kotlin")
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
    fun `double property holds`() {
        val values = listOf(7, -11, 123, -456, 1000, -9999)
        for (x in values) {
            assertEquals(x * x, double(x))
        }
    }

    @Test
    fun `greet with regular name prints correct message`() {
        val output = captureOutput { greet("Alice") }
        assertEquals("Hello, Alice!\n", output)
    }

    @Test
    fun `greet with empty string prints Hello, !`() {
        val output = captureOutput { greet("") }
        assertEquals("Hello, !\n", output)
    }

    @Test
    fun `greet with name containing spaces`() {
        val output = captureOutput { greet("John Doe") }
        assertEquals("Hello, John Doe!\n", output)
    }

    @Test
    fun `greet with special characters`() {
        val output = captureOutput { greet("!@#$%") }
        assertEquals("Hello, !@#$%!\n", output)
    }

    @Test
    fun `greet with long name`() {
        val longName = "A".repeat(1000)
        val output = captureOutput { greet(longName) }
        assertEquals("Hello, $longName!\n", output)
    }

    @Test
    fun `greet can be called multiple times`() {
        val output = captureOutput {
            greet("First")
            greet("Second")
        }
        assertEquals("Hello, First!\nHello, Second!\n", output)
    }

    @Test
    fun `main prints expected output`() {
        val output = captureOutput { mainDemo() }
        val lines = output.lines().filter { it.isNotEmpty() }
        assertEquals(2, lines.size)
        assertEquals("Квадрат числа 4 равен: 16", lines[0])
        assertEquals("Hello, Kotlin!", lines[1])
    }
}