package functional_programming

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class AnonymousFunctionTest {

    val anonymousFun = fun(): Unit {
        println("I'm an anonymous function")
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
        anonymousFun()
    }

    @Test
    fun `anonymousFun has correct type`() {
        assertTrue(anonymousFun is () -> Unit)
    }

    @Test
    fun `anonymousFun prints expected message when called once`() {
        val output = captureOutput { anonymousFun() }
        assertEquals("I'm an anonymous function\n", output)
    }

    @Test
    fun `anonymousFun prints expected message when called multiple times`() {
        val output = captureOutput {
            anonymousFun()
            anonymousFun()
            anonymousFun()
        }
        val expected = "I'm an anonymous function\n".repeat(3)
        assertEquals(expected, output)
    }

    @Test
    fun `anonymousFun returns Unit`() {
        val result = anonymousFun()
        assertTrue(result is Unit)
        assertEquals(Unit, result)
    }

    @Test
    fun `anonymousFun does not throw exception`() {
        try {
            anonymousFun()
        } catch (e: Exception) {
            throw AssertionError("Should not throw exception", e)
        }
    }

    @Test
    fun `mainDemo prints expected output`() {
        val output = captureOutput { mainDemo() }
        assertEquals("I'm an anonymous function\n", output)
    }
}