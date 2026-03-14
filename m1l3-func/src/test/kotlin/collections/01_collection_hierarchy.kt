package collections

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class CollectionsHierarchyTestFixed {

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
        val list: List<Int> = listOf(1, 2, 3)
        val mutableList: MutableList<Int> = mutableListOf(1, 2, 3)

        val set: Set<String> = setOf("A", "B")
        val mutableSet: MutableSet<String> = mutableSetOf("A", "B")

        val map: Map<String, Int> = mapOf("A" to 1, "B" to 2)
        val mutableMap: MutableMap<String, Int> = mutableMapOf("A" to 1, "B" to 2)

        println("List: $list")
        println("MutableList: $mutableList")
        println("Set: $set")
        println("MutableSet: $mutableSet")
        println("Map: $map")
        println("MutableMap: $mutableMap")
    }

    @Test
    fun `read-only list creation`() {
        val list: List<Int> = listOf(1, 2, 3)
        assertEquals(3, list.size)
        assertEquals(1, list[0])
        assertEquals(2, list[1])
        assertEquals(3, list[2])
    }

    @Test
    fun `mutable list creation`() {
        val mutableList: MutableList<Int> = mutableListOf(1, 2, 3)
        assertEquals(3, mutableList.size)
        mutableList.add(4)
        assertEquals(4, mutableList.size)
        assertEquals(4, mutableList[3])
        mutableList.removeAt(0)
        assertEquals(3, mutableList.size)
        assertEquals(2, mutableList[0])
    }

    @Test
    fun `read-only set creation`() {
        val set: Set<String> = setOf("A", "B", "A")
        assertEquals(2, set.size)
        assertTrue(set.contains("A"))
        assertTrue(set.contains("B"))
    }

    @Test
    fun `mutable set creation`() {
        val mutableSet: MutableSet<String> = mutableSetOf("A", "B")
        assertEquals(2, mutableSet.size)
        mutableSet.add("C")
        assertEquals(3, mutableSet.size)
        mutableSet.add("A") // дубликат не добавится
        assertEquals(3, mutableSet.size)
        mutableSet.remove("B")
        assertEquals(2, mutableSet.size)
        assertTrue(mutableSet.contains("A"))
        assertTrue(mutableSet.contains("C"))
    }

    @Test
    fun `read-only map creation`() {
        val map: Map<String, Int> = mapOf("A" to 1, "B" to 2)
        assertEquals(2, map.size)
        assertEquals(1, map["A"])
        assertEquals(2, map["B"])
    }

    @Test
    fun `mutable map creation`() {
        val mutableMap: MutableMap<String, Int> = mutableMapOf("A" to 1, "B" to 2)
        assertEquals(2, mutableMap.size)
        mutableMap["C"] = 3
        assertEquals(3, mutableMap.size)
        assertEquals(3, mutableMap["C"])
        mutableMap["A"] = 10
        assertEquals(10, mutableMap["A"])
        mutableMap.remove("B")
        assertEquals(2, mutableMap.size)
        assertTrue(mutableMap.containsKey("A"))
        assertTrue(mutableMap.containsKey("C"))
    }

    @Test
    fun `main prints expected output`() {
        val output = captureOutput { mainDemo() }
        val lines = output.lines().filter { it.isNotEmpty() }
        assertEquals(6, lines.size)
        assertEquals("List: [1, 2, 3]", lines[0])
        assertEquals("MutableList: [1, 2, 3]", lines[1])
        assertEquals("Set: [A, B]", lines[2])
        assertEquals("MutableSet: [A, B]", lines[3])
        assertEquals("Map: {A=1, B=2}", lines[4])
        assertEquals("MutableMap: {A=1, B=2}", lines[5])
    }

    @Test
    fun `empty collections`() {
        val emptyList = listOf<Int>()
        assertTrue(emptyList.isEmpty())

        val emptySet = setOf<String>()
        assertTrue(emptySet.isEmpty())

        val emptyMap = mapOf<String, Int>()
        assertTrue(emptyMap.isEmpty())
    }

    @Test
    fun `listOf with vararg`() {
        val list = listOf(1, 2, 3, 4, 5)
        assertEquals(5, list.size)
        assertEquals(5, list.last())
    }

    @Test
    fun `setOf with duplicates`() {
        val set = setOf(1, 1, 2, 2, 3)
        assertEquals(3, set.size)
        assertTrue(set.contains(1))
        assertTrue(set.contains(2))
        assertTrue(set.contains(3))
    }

    @Test
    fun `mapOf with duplicate keys`() {
        val map = mapOf("A" to 1, "A" to 2)
        assertEquals(1, map.size)
        assertEquals(2, map["A"])
    }
}