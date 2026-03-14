package functional_programming

import kotlin.test.Test
import kotlin.test.assertEquals
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class FunctionTypesTest {

    val greet: () -> Unit = { println("Hello!") }

    val multiply: (Int, Int) -> Int = { a, b -> a * b }

    val format: (String) -> String = { "Formatted: $it" }

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
        greet()
        println(multiply(2, 3))
        println(format("text"))
    }

    @Test
    fun `greet prints Hello`() {
        val output = captureOutput { greet() }
        assertEquals("Hello!\n", output)
    }

    @Test
    fun `greet can be called multiple times`() {
        val output = captureOutput {
            greet()
            greet()
        }
        assertEquals("Hello!\nHello!\n", output)
    }

    @Test
    fun `multiply positive numbers`() {
        assertEquals(6, multiply(2, 3))
        assertEquals(20, multiply(4, 5))
        assertEquals(1, multiply(1, 1))
    }

    @Test
    fun `multiply with zero`() {
        assertEquals(0, multiply(0, 5))
        assertEquals(0, multiply(7, 0))
        assertEquals(0, multiply(0, 0))
    }

    @Test
    fun `multiply with negative numbers`() {
        assertEquals(-6, multiply(-2, 3))
        assertEquals(6, multiply(-2, -3))
        assertEquals(-20, multiply(4, -5))
    }

    @Test
    fun `format with regular string`() {
        assertEquals("Formatted: hello", format("hello"))
    }

    @Test
    fun `format with empty string`() {
        assertEquals("Formatted: ", format(""))
    }

    @Test
    fun `format with string containing spaces`() {
        assertEquals("Formatted: a b c", format("a b c"))
    }

    @Test
    fun `format with special characters`() {
        assertEquals("Formatted: !@#", format("!@#"))
    }

    @Test
    fun `format with long string`() {
        val long = "a".repeat(1000)
        assertEquals("Formatted: $long", format(long))
    }

    @Test
    fun `format with numbers as string`() {
        assertEquals("Formatted: 123", format("123"))
    }

    @Test
    fun `main prints expected output`() {
        val output = captureOutput { mainDemo() }
        val lines = output.lines().filter { it.isNotEmpty() }
        assertEquals(3, lines.size)
        assertEquals("Hello!", lines[0])
        assertEquals("6", lines[1])
        assertEquals("Formatted: text", lines[2])
    }

    @Test
    fun `multiply and format combined`() {
        val result = multiply(3, 4)
        val formatted = format(result.toString())
        assertEquals("Formatted: 12", formatted)
    }

    @Test
    fun `greet does not affect return values`() {
        val output = captureOutput {
            greet()
            val x = multiply(2, 3)
            println(format(x.toString()))
        }
        assertEquals("Hello!\nFormatted: 6\n", output)
    }
}