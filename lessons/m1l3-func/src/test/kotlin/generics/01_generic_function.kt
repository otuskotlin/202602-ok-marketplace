package generics

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class GenericFunctionTest {

    fun <T> oneElementList(arg: T): List<T> = listOf(arg)

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
        val l1: List<String> = oneElementList("Hello")
        val l2: List<Int> = oneElementList(42)
        val l3: List<Double> = oneElementList(3.14)

        println(l1)
        println(l2)
        println(l3)
    }

    @Test
    fun `oneElementList with String`() {
        val list = oneElementList("Hello")
        assertEquals(1, list.size)
        assertEquals("Hello", list[0])
    }

    @Test
    fun `oneElementList with empty string`() {
        val list = oneElementList("")
        assertEquals(1, list.size)
        assertEquals("", list[0])
    }

    @Test
    fun `oneElementList with Int`() {
        val list = oneElementList(42)
        assertEquals(1, list.size)
        assertEquals(42, list[0])
    }

    @Test
    fun `oneElementList with zero`() {
        val list = oneElementList(0)
        assertEquals(1, list.size)
        assertEquals(0, list[0])
    }

    @Test
    fun `oneElementList with negative Int`() {
        val list = oneElementList(-5)
        assertEquals(1, list.size)
        assertEquals(-5, list[0])
    }

    @Test
    fun `oneElementList with large Int`() {
        val list = oneElementList(Int.MAX_VALUE)
        assertEquals(1, list.size)
        assertEquals(Int.MAX_VALUE, list[0])
    }

    @Test
    fun `oneElementList with Double`() {
        val list = oneElementList(3.14)
        assertEquals(1, list.size)
        assertEquals(3.14, list[0])
    }

    @Test
    fun `oneElementList with NaN`() {
        val list = oneElementList(Double.NaN)
        assertEquals(1, list.size)
        assertTrue(list[0].isNaN())
    }

    @Test
    fun `oneElementList with Boolean`() {
        val list = oneElementList(true)
        assertEquals(1, list.size)
        assertEquals(true, list[0])
    }

    @Test
    fun `oneElementList with custom class`() {
        data class Person(val name: String)
        val person = Person("Alice")
        val list = oneElementList(person)
        assertEquals(1, list.size)
        assertEquals(person, list[0])
    }

    @Test
    fun `oneElementList with null`() {
        val list = oneElementList<Any?>(null)
        assertEquals(1, list.size)
        assertEquals(null, list[0])
    }

    @Test
    fun `oneElementList with nullable type inference`() {
        val nullableString: String? = "test"
        val list = oneElementList(nullableString)
        assertEquals(1, list.size)
        assertEquals("test", list[0])
        assertTrue(list is List<String?>)
    }

    @Test
    fun `oneElementList returns new list each call`() {
        val list1 = oneElementList("a")
        val list2 = oneElementList("a")
        assertTrue(list1 !== list2)
    }

    @Test
    fun `main prints expected output`() {
        val output = captureOutput { mainDemo() }
        val lines = output.lines().filter { it.isNotEmpty() }
        assertEquals(3, lines.size)
        assertEquals("[Hello]", lines[0])
        assertEquals("[42]", lines[1])
        assertEquals("[3.14]", lines[2])
    }
}