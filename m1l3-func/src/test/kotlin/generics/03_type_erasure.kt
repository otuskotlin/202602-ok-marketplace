package generics

import kotlin.test.Test
import kotlin.test.assertTrue
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class TypeErasureTest {

    fun <T : Any> typedGeneric(arg: T) {
        println("Value: $arg")
        println("Runtime type: ${arg::class.simpleName}")
        println("But generic type T is erased at runtime")
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
        typedGeneric(1)
        typedGeneric("hello")
        typedGeneric(listOf(1))
    }

    @Test
    fun `typedGeneric with Int`() {
        val output = captureOutput { typedGeneric(42) }
        val lines = output.lines().filter { it.isNotEmpty() }
        assertTrue(lines.size == 3)
        assertTrue(lines[0] == "Value: 42")
        assertTrue(lines[1] == "Runtime type: Int")
        assertTrue(lines[2] == "But generic type T is erased at runtime")
    }

    @Test
    fun `typedGeneric with zero Int`() {
        val output = captureOutput { typedGeneric(0) }
        val lines = output.lines().filter { it.isNotEmpty() }
        assertTrue(lines[0] == "Value: 0")
        assertTrue(lines[1] == "Runtime type: Int")
    }

    @Test
    fun `typedGeneric with negative Int`() {
        val output = captureOutput { typedGeneric(-7) }
        assertTrue(output.contains("Value: -7"))
        assertTrue(output.contains("Runtime type: Int"))
    }

    @Test
    fun `typedGeneric with String`() {
        val output = captureOutput { typedGeneric("hello") }
        val lines = output.lines().filter { it.isNotEmpty() }
        assertTrue(lines[0] == "Value: hello")
        assertTrue(lines[1] == "Runtime type: String")
        assertTrue(lines[2] == "But generic type T is erased at runtime")
    }

    @Test
    fun `typedGeneric with empty string`() {
        val output = captureOutput { typedGeneric("") }
        assertTrue(output.contains("Value: "))
        assertTrue(output.contains("Runtime type: String"))
    }

    @Test
    fun `typedGeneric with Double`() {
        val output = captureOutput { typedGeneric(3.14) }
        assertTrue(output.contains("Value: 3.14"))
        assertTrue(output.contains("Runtime type: Double"))
    }

    @Test
    fun `typedGeneric with Boolean`() {
        val output = captureOutput { typedGeneric(true) }
        assertTrue(output.contains("Value: true"))
        assertTrue(output.contains("Runtime type: Boolean"))
    }

    @Test
    fun `typedGeneric with List of Int`() {
        val list = listOf(1, 2, 3)
        val output = captureOutput { typedGeneric(list) }
        assertTrue(output.contains("Value: [1, 2, 3]"))
        val runtimeLine = output.lines().first { it.startsWith("Runtime type:") }
        assertTrue(runtimeLine.contains("ArrayList") || runtimeLine.contains("List") || runtimeLine.contains("AbstractList"))
    }

    @Test
    fun `typedGeneric with custom class`() {
        class Person(val name: String)
        val person = Person("Alice")
        val output = captureOutput { typedGeneric(person) }
        assertTrue(output.contains("Value: ${person.toString()}"))
        assertTrue(output.contains("Runtime type: Person"))
    }

    @Test
    fun `typedGeneric with data class`() {
        data class Point(val x: Int, val y: Int)
        val point = Point(10, 20)
        val output = captureOutput { typedGeneric(point) }
        assertTrue(output.contains("Value: Point(x=10, y=20)"))
        assertTrue(output.contains("Runtime type: Point"))
    }

    @Test
    fun `typedGeneric with array`() {
        val array = arrayOf(1, 2, 3)
        val output = captureOutput { typedGeneric(array) }
        assertTrue(output.contains("Runtime type: Array"))
    }

    @Test
    fun `typedGeneric with large number`() {
        val output = captureOutput { typedGeneric(Int.MAX_VALUE) }
        assertTrue(output.contains("Value: 2147483647"))
        assertTrue(output.contains("Runtime type: Int"))
    }

    @Test
    fun `typedGeneric always prints the same generic message`() {
        val outputInt = captureOutput { typedGeneric(1) }
        val outputStr = captureOutput { typedGeneric("a") }
        val linesInt = outputInt.lines().filter { it.isNotEmpty() }
        val linesStr = outputStr.lines().filter { it.isNotEmpty() }
        assertTrue(linesInt[2] == "But generic type T is erased at runtime")
        assertTrue(linesStr[2] == linesInt[2])
    }

    @Test
    fun `main prints expected lines`() {
        val output = captureOutput { mainDemo() }
        val lines = output.lines().filter { it.isNotEmpty() }
        assertTrue(lines.size == 9)
        assertTrue(lines[0] == "Value: 1")
        assertTrue(lines[1] == "Runtime type: Int")
        assertTrue(lines[2] == "But generic type T is erased at runtime")
        assertTrue(lines[3] == "Value: hello")
        assertTrue(lines[4] == "Runtime type: String")
        assertTrue(lines[5] == "But generic type T is erased at runtime")
        assertTrue(lines[6] == "Value: [1]")
        assertTrue(lines[7].startsWith("Runtime type:"))
        assertTrue(lines[8] == "But generic type T is erased at runtime")
    }
}