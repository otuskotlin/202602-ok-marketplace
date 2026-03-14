package generics

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class SafeOneElementListTest {

    // ---------- Тестируемая функция (копия из исходного кода) ----------
    fun <T : Any> safeOneElementList(arg: T): List<T> = listOf(arg)

    // ---------- Вспомогательная функция для захвата вывода ----------
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

    // ---------- Функция, воспроизводящая логику исходного main ----------
    private fun mainDemo() {
        val l1: List<String> = safeOneElementList("text")
        val l2: List<Int> = safeOneElementList(42)
        println(l1)
        println(l2)
    }

    // ---------- Тесты ----------
    @Test
    fun `safeOneElementList with String`() {
        val list = safeOneElementList("hello")
        assertEquals(1, list.size)
        assertEquals("hello", list[0])
    }

    @Test
    fun `safeOneElementList with empty string`() {
        val list = safeOneElementList("")
        assertEquals(1, list.size)
        assertEquals("", list[0])
    }

    @Test
    fun `safeOneElementList with Int`() {
        val list = safeOneElementList(123)
        assertEquals(1, list.size)
        assertEquals(123, list[0])
    }

    @Test
    fun `safeOneElementList with zero`() {
        val list = safeOneElementList(0)
        assertEquals(1, list.size)
        assertEquals(0, list[0])
    }

    @Test
    fun `safeOneElementList with negative Int`() {
        val list = safeOneElementList(-5)
        assertEquals(1, list.size)
        assertEquals(-5, list[0])
    }

    @Test
    fun `safeOneElementList with large Int`() {
        val list = safeOneElementList(Int.MAX_VALUE)
        assertEquals(1, list.size)
        assertEquals(Int.MAX_VALUE, list[0])
    }

    @Test
    fun `safeOneElementList with Double`() {
        val list = safeOneElementList(3.14)
        assertEquals(1, list.size)
        assertEquals(3.14, list[0])
    }

    @Test
    fun `safeOneElementList with NaN`() {
        val list = safeOneElementList(Double.NaN)
        assertEquals(1, list.size)
        assertTrue(list[0].isNaN())
    }

    @Test
    fun `safeOneElementList with Infinity`() {
        val list = safeOneElementList(Double.POSITIVE_INFINITY)
        assertEquals(1, list.size)
        assertEquals(Double.POSITIVE_INFINITY, list[0])
    }

    @Test
    fun `safeOneElementList with Boolean`() {
        val list = safeOneElementList(false)
        assertEquals(1, list.size)
        assertEquals(false, list[0])
    }

    @Test
    fun `safeOneElementList with custom class`() {
        data class Person(val name: String)
        val person = Person("Alice")
        val list = safeOneElementList(person)
        assertEquals(1, list.size)
        assertEquals(person, list[0])
    }

    @Test
    fun `safeOneElementList with array`() {
        val array = arrayOf(1, 2, 3)
        val list = safeOneElementList(array)
        assertEquals(1, list.size)
        assertTrue(list[0] contentEquals array) // для массивов нужно contentEquals
    }

    @Test
    fun `safeOneElementList returns new list each call`() {
        val list1 = safeOneElementList("a")
        val list2 = safeOneElementList("a")
        assertTrue(list1 !== list2)
    }

    // Проверка, что тип возвращаемого списка соответствует типу аргумента
    @Test
    fun `safeOneElementList returns List of correct type`() {
        val list = safeOneElementList("test")
        assertTrue(list is List<String>)
        // Проверка, что элементы имеют ожидаемый тип
        val element: String = list[0]
        assertEquals("test", element)
    }

    // Тест для проверки, что функция не принимает null (нельзя проверить в рантайме, но можно проверить, что компилятор не позволяет)
    // В тесте мы можем только убедиться, что вызов с non-null работает, а попытка передать null вызовет ошибку компиляции,
    // что не проверяется в рантайме. Поэтому этот аспект остаётся на усмотрение компилятора.

    // ---------- Проверка вывода main ----------
    @Test
    fun `main prints expected output`() {
        val output = captureOutput { mainDemo() }
        val lines = output.lines().filter { it.isNotEmpty() }
        assertEquals(2, lines.size)
        assertEquals("[text]", lines[0])
        assertEquals("[42]", lines[1])
    }
}