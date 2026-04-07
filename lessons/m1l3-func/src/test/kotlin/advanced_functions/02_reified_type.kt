package advanced_functions

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class ReifiedFunctionTestFixed {

    inline fun <reified T : Any> printType(arg: T) {
        println("Argument type: ${arg::class.simpleName}")
        println("Generic type T: ${T::class}")
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

    private fun assertGenericTypeLine(line: String, expectedTypeNames: List<String>) {
        assertTrue(line.startsWith("Generic type T: class "), "Line should start with 'Generic type T: class '")
        val content = line.removePrefix("Generic type T: class ").trim()
        val className = content.substringBefore(" (").trim() // отрезаем возможный суффикс
        val match = expectedTypeNames.any { className.contains(it) }
        assertTrue(match, "Expected one of $expectedTypeNames, but got: $className")
    }

    @Test
    fun `printType with Int`() {
        val output = captureOutput { printType(123) }
        val lines = output.lines().filter { it.isNotEmpty() }
        assertEquals(2, lines.size)

        assertTrue(lines[0].startsWith("Argument type: "))
        assertTrue(lines[0].contains("Int"))

        assertGenericTypeLine(lines[1], listOf("kotlin.Int", "java.lang.Integer", "Int"))
    }

    @Test
    fun `printType with String`() {
        val output = captureOutput { printType("hello") }
        val lines = output.lines().filter { it.isNotEmpty() }
        assertEquals(2, lines.size)

        assertTrue(lines[0].startsWith("Argument type: "))
        assertTrue(lines[0].contains("String"))

        assertGenericTypeLine(lines[1], listOf("kotlin.String", "java.lang.String", "String"))
    }

    @Test
    fun `printType with Double`() {
        val output = captureOutput { printType(3.14) }
        val lines = output.lines().filter { it.isNotEmpty() }
        assertEquals(2, lines.size)

        assertTrue(lines[0].startsWith("Argument type: "))
        assertTrue(lines[0].contains("Double"))

        assertGenericTypeLine(lines[1], listOf("kotlin.Double", "java.lang.Double", "Double"))
    }

    @Test
    fun `printType with Boolean`() {
        val output = captureOutput { printType(true) }
        val lines = output.lines().filter { it.isNotEmpty() }
        assertEquals(2, lines.size)

        assertTrue(lines[0].startsWith("Argument type: "))
        assertTrue(lines[0].contains("Boolean"))

        assertGenericTypeLine(lines[1], listOf("kotlin.Boolean", "java.lang.Boolean", "Boolean"))
    }

    @Test
    fun `printType with Unit`() {
        val output = captureOutput { printType(Unit) }
        val lines = output.lines().filter { it.isNotEmpty() }
        assertEquals(2, lines.size)

        assertTrue(lines[0].startsWith("Argument type: "))
        assertTrue(lines[0].contains("Unit"))

        assertGenericTypeLine(lines[1], listOf("kotlin.Unit", "Unit"))
    }

    class SampleClass

    @Test
    fun `printType with custom class`() {
        val output = captureOutput { printType(SampleClass()) }
        val lines = output.lines().filter { it.isNotEmpty() }
        assertEquals(2, lines.size)

        assertTrue(lines[0].startsWith("Argument type: "))
        assertTrue(lines[0].contains("SampleClass"))

        val expectedName = SampleClass::class.qualifiedName ?: "SampleClass"
        assertGenericTypeLine(lines[1], listOf(expectedName, "SampleClass"))
    }

    @Test
    fun `printType with multiple calls preserves output order`() {
        val output = captureOutput {
            printType(1)
            printType("two")
        }
        val lines = output.lines().filter { it.isNotEmpty() }
        assertEquals(4, lines.size)

        assertTrue(lines[0].startsWith("Argument type: "))
        assertTrue(lines[0].contains("Int"))
        assertGenericTypeLine(lines[1], listOf("kotlin.Int", "java.lang.Integer", "Int"))

        assertTrue(lines[2].startsWith("Argument type: "))
        assertTrue(lines[2].contains("String"))
        assertGenericTypeLine(lines[3], listOf("kotlin.String", "java.lang.String", "String"))
    }
}