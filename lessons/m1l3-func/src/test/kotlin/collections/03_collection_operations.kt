package collections

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class CollectionOperationsTest {

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
        val numbers = listOf(1, 2, 3, 4, 5)

        val doubled = numbers.map { it * 2 }
        val squared = numbers.map { it * it }
        val filtered = numbers.filter { it % 2 == 0 }
        val grouped = numbers.groupBy { it % 2 }

        val sum = numbers.sum()
        val average = numbers.average()
        val count = numbers.count()

        println("Original: $numbers")
        println("Doubled: $doubled")
        println("Squared: $squared")
        println("Filtered: $filtered")
        println("Grouped: $grouped")
        println("Sum: $sum")
        println("Average: $average")
        println("Count: $count")
    }

    @Test
    fun `map doubles numbers`() {
        val numbers = listOf(1, 2, 3)
        assertEquals(listOf(2, 4, 6), numbers.map { it * 2 })
    }

    @Test
    fun `map squares numbers`() {
        val numbers = listOf(1, 2, 3)
        assertEquals(listOf(1, 4, 9), numbers.map { it * it })
    }

    @Test
    fun `filter even numbers`() {
        val numbers = listOf(1, 2, 3, 4)
        assertEquals(listOf(2, 4), numbers.filter { it % 2 == 0 })
    }

    @Test
    fun `filter odd numbers`() {
        val numbers = listOf(1, 2, 3, 4)
        assertEquals(listOf(1, 3), numbers.filter { it % 2 != 0 })
    }

    @Test
    fun `groupBy parity`() {
        val numbers = listOf(1, 2, 3, 4)
        val expected = mapOf(1 to listOf(1, 3), 0 to listOf(2, 4))
        assertEquals(expected, numbers.groupBy { it % 2 })
    }

    @Test
    fun `sum of numbers`() {
        val numbers = listOf(1, 2, 3, 4, 5)
        assertEquals(15, numbers.sum())
    }

    @Test
    fun `average of numbers`() {
        val numbers = listOf(1, 2, 3, 4, 5)
        assertEquals(3.0, numbers.average())
    }

    @Test
    fun `count of numbers`() {
        val numbers = listOf(1, 2, 3, 4, 5)
        assertEquals(5, numbers.count())
    }

    @Test
    fun `empty list map returns empty`() {
        val empty = emptyList<Int>()
        assertEquals(emptyList(), empty.map { it * 2 })
    }

    @Test
    fun `empty list filter returns empty`() {
        val empty = emptyList<Int>()
        assertEquals(emptyList(), empty.filter { it % 2 == 0 })
    }

    @Test
    fun `empty list groupBy returns empty map`() {
        val empty = emptyList<Int>()
        assertEquals(emptyMap(), empty.groupBy { it % 2 })
    }

    @Test
    fun `empty list sum returns zero`() {
        val empty = emptyList<Int>()
        assertEquals(0, empty.sum())
    }

    @Test
    fun `empty list average returns NaN`() {
        val empty = emptyList<Int>()
        assertTrue(empty.average().isNaN())
    }

    @Test
    fun `empty list count returns zero`() {
        val empty = emptyList<Int>()
        assertEquals(0, empty.count())
    }

    @Test
    fun `single element list map`() {
        val single = listOf(5)
        assertEquals(listOf(10), single.map { it * 2 })
    }

    @Test
    fun `single element list filter matches`() {
        val single = listOf(5)
        assertEquals(listOf(5), single.filter { it > 0 })
    }

    @Test
    fun `single element list filter no match`() {
        val single = listOf(5)
        assertEquals(emptyList(), single.filter { it < 0 })
    }

    @Test
    fun `single element list groupBy`() {
        val single = listOf(5)
        assertEquals(mapOf(1 to listOf(5)), single.groupBy { it % 2 })
    }

    @Test
    fun `single element list sum`() {
        val single = listOf(5)
        assertEquals(5, single.sum())
    }

    @Test
    fun `single element list average`() {
        val single = listOf(5)
        assertEquals(5.0, single.average())
    }

    @Test
    fun `negative numbers map`() {
        val negatives = listOf(-1, -2, -3)
        assertEquals(listOf(-2, -4, -6), negatives.map { it * 2 })
    }

    @Test
    fun `negative numbers filter odd`() {
        val negatives = listOf(-1, -2, -3)
        assertEquals(listOf(-1, -3), negatives.filter { it % 2 != 0 })
    }

    @Test
    fun `negative numbers groupBy parity`() {
        val negatives = listOf(-1, -2, -3)
        val expected = mapOf(-1 to listOf(-1, -3), 0 to listOf(-2))
        assertEquals(expected, negatives.groupBy { it % 2 })
    }

    @Test
    fun `original list unchanged`() {
        val original = listOf(1, 2, 3)
        original.map { it * 2 }
        original.filter { it % 2 == 0 }
        original.groupBy { it % 2 }
        original.sum()
        original.average()
        original.count()
        assertEquals(listOf(1, 2, 3), original)
    }

    @Test
    fun `main prints expected output`() {
        val output = captureOutput { mainDemo() }
        val lines = output.lines().filter { it.isNotEmpty() }
        assertEquals(8, lines.size)
        assertEquals("Original: [1, 2, 3, 4, 5]", lines[0])
        assertEquals("Doubled: [2, 4, 6, 8, 10]", lines[1])
        assertEquals("Squared: [1, 4, 9, 16, 25]", lines[2])
        assertEquals("Filtered: [2, 4]", lines[3])
        assertEquals("Grouped: {1=[1, 3, 5], 0=[2, 4]}", lines[4])
        assertEquals("Sum: 15", lines[5])
        assertEquals("Average: 3.0", lines[6])
        assertEquals("Count: 5", lines[7])
    }
}