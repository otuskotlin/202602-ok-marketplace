package functions

import kotlin.test.Test
import kotlin.test.assertEquals
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class VarargTest {

    fun printNames(vararg names: String) {
        for (name in names) {
            println(name)
        }
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
        printNames("Alice", "Bob", "Charlie")
        val nameList = arrayOf("David", "Eve")
        printNames(*nameList)
        printNames("Frank")
    }

    @Test
    fun `printNames with multiple arguments`() {
        val output = captureOutput { printNames("Alice", "Bob", "Charlie") }
        assertEquals("Alice\nBob\nCharlie\n", output)
    }

    @Test
    fun `printNames with one argument`() {
        val output = captureOutput { printNames("Frank") }
        assertEquals("Frank\n", output)
    }

    @Test
    fun `printNames with zero arguments`() {
        val output = captureOutput { printNames() }
        assertEquals("", output)
    }

    @Test
    fun `printNames with spread array`() {
        val nameList = arrayOf("David", "Eve")
        val output = captureOutput { printNames(*nameList) }
        assertEquals("David\nEve\n", output)
    }

    @Test
    fun `printNames with empty array spread`() {
        val emptyArray = emptyArray<String>()
        val output = captureOutput { printNames(*emptyArray) }
        assertEquals("", output)
    }

    @Test
    fun `printNames with mix of direct arguments and spread`() {
        val moreNames = arrayOf("Grace", "Heidi")
        val output = captureOutput { printNames("Alice", *moreNames, "Bob") }
        assertEquals("Alice\nGrace\nHeidi\nBob\n", output)
    }

    @Test
    fun `printNames does not modify original array`() {
        val originalArray = arrayOf("x", "y")
        captureOutput { printNames(*originalArray) }
        assertEquals("x", originalArray[0])
        assertEquals("y", originalArray[1])
    }

    @Test
    fun `main prints expected output`() {
        val output = captureOutput { mainDemo() }
        val lines = output.lines().filter { it.isNotEmpty() }
        assertEquals(6, lines.size)
        assertEquals("Alice", lines[0])
        assertEquals("Bob", lines[1])
        assertEquals("Charlie", lines[2])
        assertEquals("David", lines[3])
        assertEquals("Eve", lines[4])
        assertEquals("Frank", lines[5])
    }
}