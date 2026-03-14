package functional_programming

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class LambdaFunctionsTest {

    val lambda = { println("Hello from lambda") }

    val withArg = { str: String -> "My name is $str" }

    val withType: (Int, Int) -> Int = { a, b -> a + b }

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
        lambda()
        println(withArg("Alice"))
        println(withType(1, 2))
    }

    @Test
    fun `lambda prints correct message`() {
        val output = captureOutput { lambda() }
        assertEquals("Hello from lambda\n", output)
    }

    @Test
    fun `lambda can be called multiple times`() {
        val output = captureOutput {
            lambda()
            lambda()
        }
        assertEquals("Hello from lambda\nHello from lambda\n", output)
    }

    @Test
    fun `lambda returns Unit`() {
        val result = lambda()
        assertTrue(result is Unit)
        assertEquals(Unit, result)
    }

    @Test
    fun `withArg for regular string`() {
        assertEquals("My name is Alice", withArg("Alice"))
    }

    @Test
    fun `withArg for empty string`() {
        assertEquals("My name is ", withArg(""))
    }

    @Test
    fun `withArg for string with spaces`() {
        assertEquals("My name is John Doe", withArg("John Doe"))
    }

    @Test
    fun `withArg for special characters`() {
        assertEquals("My name is !@#", withArg("!@#"))
    }

    @Test
    fun `withArg for long string`() {
        val long = "a".repeat(1000)
        assertEquals("My name is $long", withArg(long))
    }

    @Test
    fun `withArg for numbers as string`() {
        assertEquals("My name is 123", withArg("123"))
    }

    @Test
    fun `withType adds positive numbers`() {
        assertEquals(5, withType(2, 3))
        assertEquals(10, withType(7, 3))
    }

    @Test
    fun `withType with zero`() {
        assertEquals(5, withType(5, 0))
        assertEquals(0, withType(0, 0))
        assertEquals(3, withType(0, 3))
    }

    @Test
    fun `withType with negative numbers`() {
        assertEquals(-1, withType(2, -3))
        assertEquals(-5, withType(-2, -3))
        assertEquals(0, withType(-5, 5))
    }

    @Test
    fun `withType with large numbers`() {
        assertEquals(2000000, withType(1000000, 1000000))
        assertEquals(Int.MAX_VALUE - 1, withType(Int.MAX_VALUE, -1))
        assertEquals(Int.MIN_VALUE + 1, withType(Int.MIN_VALUE, 1))
        assertEquals(0, withType(Int.MAX_VALUE, Int.MIN_VALUE + 1))
    }

    @Test
    fun `withType commutativity`() {
        assertEquals(withType(3, 5), withType(5, 3))
    }

    @Test
    fun `main prints expected output`() {
        val output = captureOutput { mainDemo() }
        val lines = output.lines().filter { it.isNotEmpty() }
        assertEquals(3, lines.size)
        assertEquals("Hello from lambda", lines[0])
        assertEquals("My name is Alice", lines[1])
        assertEquals("3", lines[2])
    }
}