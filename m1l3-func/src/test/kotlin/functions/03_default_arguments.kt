package functions

import kotlin.test.Test
import kotlin.test.assertEquals
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class DefaultArgumentsTest {

    fun greet(name: String, greeting: String = "Hello") {
        println("$greeting, $name!")
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
        greet("Alice")
        greet("Bob", "Hi")
    }

    @Test
    fun `greet with default greeting`() {
        val output = captureOutput { greet("Alice") }
        assertEquals("Hello, Alice!\n", output)
    }

    @Test
    fun `greet with custom greeting`() {
        val output = captureOutput { greet("Bob", "Hi") }
        assertEquals("Hi, Bob!\n", output)
    }

    @Test
    fun `greet with empty name`() {
        val output = captureOutput { greet("") }
        assertEquals("Hello, !\n", output)
    }

    @Test
    fun `greet with empty name and custom greeting`() {
        val output = captureOutput { greet("", "Hey") }
        assertEquals("Hey, !\n", output)
    }

    @Test
    fun `greet with empty greeting string`() {
        val output = captureOutput { greet("Alice", "") }
        assertEquals(", Alice!\n", output)
    }

    @Test
    fun `greet with both empty`() {
        val output = captureOutput { greet("", "") }
        assertEquals(", !\n", output)
    }

    @Test
    fun `greet with name containing spaces`() {
        val output = captureOutput { greet("John Doe") }
        assertEquals("Hello, John Doe!\n", output)
    }

    @Test
    fun `greet with special characters in name`() {
        val output = captureOutput { greet("!@#$%") }
        assertEquals("Hello, !@#$%!\n", output)
    }

    @Test
    fun `greet with special characters in greeting`() {
        val output = captureOutput { greet("Alice", "***") }
        assertEquals("***, Alice!\n", output)
    }

    @Test
    fun `greet with long strings`() {
        val longName = "A".repeat(1000)
        val longGreeting = "B".repeat(500)
        val output = captureOutput { greet(longName, longGreeting) }
        assertEquals("$longGreeting, $longName!\n", output)
    }

    @Test
    fun `greet with named arguments`() {
        val output = captureOutput { greet(greeting = "Good morning", name = "Charlie") }
        assertEquals("Good morning, Charlie!\n", output)
    }

    @Test
    fun `greet with named arguments default`() {
        val output = captureOutput { greet(name = "Dave") }
        assertEquals("Hello, Dave!\n", output)
    }

    @Test
    fun `greet multiple calls`() {
        val output = captureOutput {
            greet("First")
            greet("Second", "Welcome")
        }
        assertEquals("Hello, First!\nWelcome, Second!\n", output)
    }

    @Test
    fun `main prints expected output`() {
        val output = captureOutput { mainDemo() }
        val lines = output.lines().filter { it.isNotEmpty() }
        assertEquals(2, lines.size)
        assertEquals("Hello, Alice!", lines[0])
        assertEquals("Hi, Bob!", lines[1])
    }
}