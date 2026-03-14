package collections

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class CreateCollectionsTestFinal {

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
        val l1 = listOf(1, 2, 3)
        val l2 = mutableListOf(1, 2, 3)

        val m1 = mapOf(1 to "str 1", 2 to "str 2")
        val m2 = mutableMapOf(1 to "str 1", 2 to "str 2")

        val r1: IntRange = 1..10
        val p1: IntProgression = 100 downTo 10 step 2
        val p2: IntProgression = 1 until 10 step 3

        val seq = sequenceOf(1, 2, 3)

        println("l1: $l1")
        println("l2: $l2")
        println("m1: $m1")
        println("m2: $m2")
        println("r1: $r1")
        println("p1: $p1")
        println("p2: $p2")
        println("seq: $seq")
    }

    @Test
    fun `listOf creates read-only list`() {
        val l1 = listOf(1, 2, 3)
        assertEquals(3, l1.size)
        assertEquals(1, l1[0])
        assertEquals(2, l1[1])
        assertEquals(3, l1[2])
    }

    @Test
    fun `mutableListOf creates mutable list`() {
        val l2 = mutableListOf(1, 2, 3)
        assertEquals(3, l2.size)
        l2.add(4)
        assertEquals(4, l2.size)
        assertEquals(4, l2[3])
        l2.removeAt(0)
        assertEquals(3, l2.size)
        assertEquals(2, l2[0])
    }

    @Test
    fun `read-only list throws on modification attempt`() {
        val l1 = listOf(1, 2, 3)
        assertFailsWith<UnsupportedOperationException> {
            (l1 as MutableList<Int>).add(4)
        }
    }

    @Test
    fun `mapOf creates read-only map`() {
        val m1 = mapOf(1 to "str 1", 2 to "str 2")
        assertEquals(2, m1.size)
        assertEquals("str 1", m1[1])
        assertEquals("str 2", m1[2])
    }

    @Test
    fun `mutableMapOf creates mutable map`() {
        val m2 = mutableMapOf(1 to "str 1", 2 to "str 2")
        assertEquals(2, m2.size)
        m2[3] = "str 3"
        assertEquals(3, m2.size)
        assertEquals("str 3", m2[3])
        m2[1] = "new str"
        assertEquals("new str", m2[1])
        m2.remove(2)
        assertEquals(2, m2.size)
        assertTrue(m2.containsKey(1))
        assertTrue(m2.containsKey(3))
    }


    @Test
    fun `intRange properties`() {
        val r1: IntRange = 1..10
        assertEquals(1, r1.first)
        assertEquals(10, r1.last)
        assertEquals(1, r1.start)
        assertEquals(10, r1.endInclusive)
        assertTrue(r1.contains(5))
        assertTrue(5 in r1)
        assertFalse(r1.contains(0))
        assertFalse(r1.contains(11))
    }

    @Test
    fun `intRange iteration`() {
        val r1 = 1..5
        val list = r1.toList()
        assertEquals(listOf(1, 2, 3, 4, 5), list)
    }

    @Test
    fun `downTo progression`() {
        val p1: IntProgression = 100 downTo 10 step 2
        assertEquals(100, p1.first)
        assertEquals(10, p1.last)
        assertEquals(-2, p1.step)
        val list = p1.toList()
        val expected = (100 downTo 10 step 2).toList()
        assertEquals(expected, list)
        assertTrue(50 in p1)
        assertFalse(51 in p1)
    }

    @Test
    fun `until progression`() {
        val p2: IntProgression = 1 until 10 step 3
        assertEquals(1, p2.first)
        assertEquals(7, p2.last)
        assertEquals(3, p2.step)
        val list = p2.toList()
        assertEquals(listOf(1, 4, 7), list)
        assertTrue(4 in p2)
        assertFalse(8 in p2)
    }

    @Test
    fun `sequence creation`() {
        val seq = sequenceOf(1, 2, 3)
        val list = seq.toList()
        assertEquals(listOf(1, 2, 3), list)
    }

    @Test
    fun `sequence is lazy`() {
        var counter = 0
        val seq = sequence {
            yield(1)
            counter++
            yield(2)
            counter++
        }

        val iterator = seq.iterator()

        assertEquals(0, counter)

        val first = iterator.next()
        assertEquals(1, first)
        assertEquals(0, counter)

        val second = iterator.next()
        assertEquals(2, second)
        assertEquals(1, counter)

        val hasNext = iterator.hasNext()
        assertFalse(hasNext)
        assertEquals(2, counter)
    }

    @Test
    fun `main prints expected output`() {
        val output = captureOutput { mainDemo() }
        val lines = output.lines().filter { it.isNotEmpty() }
        assertEquals(8, lines.size)
        assertEquals("l1: [1, 2, 3]", lines[0])
        assertEquals("l2: [1, 2, 3]", lines[1])
        assertEquals("m1: {1=str 1, 2=str 2}", lines[2])
        assertEquals("m2: {1=str 1, 2=str 2}", lines[3])
        assertEquals("r1: 1..10", lines[4])
        assertEquals("p1: 100 downTo 10 step 2", lines[5])
        assertEquals("p2: 1..7 step 3", lines[6])
        assertTrue(lines[7].startsWith("seq: "))
    }
}