package extensions

import kotlin.test.Test
import kotlin.test.assertEquals
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class LambdaExtensionsTest {

    val greet: String.() -> Unit = { println("Hello, $this") }

    val greetWithSurname: String.(String) -> Unit = { surname ->
        println("Hello, $this $surname")
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
        greet("Dee")
        "CoolGuy".greet()

        greetWithSurname("Ivan", "Gorky")
        "Ivan".greetWithSurname("Gorky")
    }

    @Test
    fun `greet called as extension on string`() {
        val output = captureOutput {
            "Alice".greet()
        }
        assertEquals("Hello, Alice\n", output)
    }

    @Test
    fun `greet called as function with receiver`() {
        val output = captureOutput {
            greet("Bob")
        }
        assertEquals("Hello, Bob\n", output)
    }

    @Test
    fun `greet with empty string`() {
        val output = captureOutput {
            "".greet()
        }
        assertEquals("Hello, \n", output)
    }

    @Test
    fun `greet with string containing spaces`() {
        val output = captureOutput {
            "John Doe".greet()
        }
        assertEquals("Hello, John Doe\n", output)
    }

    @Test
    fun `greet with special characters`() {
        val output = captureOutput {
            "O'Conner".greet()
        }
        assertEquals("Hello, O'Conner\n", output)
    }

    @Test
    fun `greetWithSurname called as extension`() {
        val output = captureOutput {
            "Alice".greetWithSurname("Smith")
        }
        assertEquals("Hello, Alice Smith\n", output)
    }

    @Test
    fun `greetWithSurname called as function with receiver`() {
        val output = captureOutput {
            greetWithSurname("Bob", "Johnson")
        }
        assertEquals("Hello, Bob Johnson\n", output)
    }

    @Test
    fun `greetWithSurname with empty name`() {
        val output = captureOutput {
            "".greetWithSurname("Doe")
        }
        assertEquals("Hello,  Doe\n", output)
    }

    @Test
    fun `greetWithSurname with empty surname`() {
        val output = captureOutput {
            "Alice".greetWithSurname("")
        }
        assertEquals("Hello, Alice \n", output)
    }

    @Test
    fun `greetWithSurname with both empty`() {
        val output = captureOutput {
            "".greetWithSurname("")
        }
        assertEquals("Hello,  \n", output)
    }

    @Test
    fun `greetWithSurname with spaces in name and surname`() {
        val output = captureOutput {
            "Mary Ann".greetWithSurname("Jane Doe")
        }
        assertEquals("Hello, Mary Ann Jane Doe\n", output)
    }

    @Test
    fun `main prints expected output`() {
        val output = captureOutput { mainDemo() }
        val lines = output.lines().filter { it.isNotEmpty() }
        assertEquals(4, lines.size)
        assertEquals("Hello, Dee", lines[0])
        assertEquals("Hello, CoolGuy", lines[1])
        assertEquals("Hello, Ivan Gorky", lines[2])
        assertEquals("Hello, Ivan Gorky", lines[3])
    }
}