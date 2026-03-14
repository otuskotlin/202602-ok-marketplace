package extensions

import kotlin.test.Test
import kotlin.test.assertEquals
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class InfixFunctionTest {

    infix fun String.withNum(num: Int) = "$this with ($num)"

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
        val res = "My string" withNum 22
        println(res)
    }

    @Test
    fun `empty string with number`() {
        val result = "" withNum 5
        assertEquals(" with (5)", result)
    }

    @Test
    fun `regular string with number`() {
        val result = "Hello" withNum 42
        assertEquals("Hello with (42)", result)
    }

    @Test
    fun `string with spaces with number`() {
        val result = "My string" withNum 22
        assertEquals("My string with (22)", result)
    }

    @Test
    fun `string with special characters with number`() {
        val result = "!@#$%^&*()" withNum 0
        assertEquals("!@#$%^&*() with (0)", result)
    }

    @Test
    fun `string with number zero`() {
        val result = "Zero" withNum 0
        assertEquals("Zero with (0)", result)
    }

    @Test
    fun `string with negative number`() {
        val result = "Negative" withNum -10
        assertEquals("Negative with (-10)", result)
    }

    @Test
    fun `string with max int`() {
        val result = "Max" withNum Int.MAX_VALUE
        assertEquals("Max with (2147483647)", result)
    }

    @Test
    fun `string with min int`() {
        val result = "Min" withNum Int.MIN_VALUE
        assertEquals("Min with (-2147483648)", result)
    }

    @Test
    fun `long string with number`() {
        val longString = "A".repeat(1000)
        val result = longString withNum 123
        assertEquals("${longString} with (123)", result)
    }

    @Test
    fun `infix call is equivalent to regular call`() {
        val infixResult = "Test" withNum 7
        val regularResult = "Test".withNum(7)
        assertEquals(infixResult, regularResult)
    }

    @Test
    fun `function does not modify original string`() {
        val original = "Original"
        val result = original withNum 99
        assertEquals("Original", original)
        assertEquals("Original with (99)", result)
    }

    @Test
    fun `main prints expected output`() {
        val output = captureOutput { mainDemo() }
        assertEquals("My string with (22)\n", output)
    }
}