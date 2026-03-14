package scope_functions

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class ScopeFunctionsPracticalTest {

    data class Person(var name: String, var age: Int)

    inline fun <T, R> with(receiver: T, block: T.() -> R): R = receiver.block()

    inline fun <T> T.apply(block: T.() -> Unit): T {
        block()
        return this
    }

    inline fun <T> T.also(block: (T) -> Unit): T {
        block(this)
        return this
    }

    inline fun <T, R> T.run(block: T.() -> R): R = block()

    inline fun <T, R> T.let(block: (T) -> R): R = block(this)

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
        val result = with("445") { this + "6" }

        val person = Person("", 0).apply {
            name = "Alice"
            age = 33
        }

        val list = listOf(1, 2, 3).also { println("List created: $it") }

        val result2 = "445".run { this + "6" }

        val name: String? = "Alice"
        name?.let { println("Name is: $it") }

        println(result)
        println(person.name)
        println(list)
        println(result2)
    }

    @Test
    fun `with on String concatenates`() {
        val res = with("445") { this + "6" }
        assertEquals("4456", res)
    }

    @Test
    fun `with on Int computes square`() {
        val res = with(5) { this * this }
        assertEquals(25, res)
    }

    @Test
    fun `with on Person returns property`() {
        val person = Person("Bob", 25)
        val res = with(person) { "$name is $age" }
        assertEquals("Bob is 25", res)
    }

    @Test
    fun `with on nullable String`() {
        val s: String? = "hello"
        val res = with(s) { this?.length ?: -1 }
        assertEquals(5, res)
    }

    @Test
    fun `with on null`() {
        val s: String? = null
        val res = with(s) { this?.length ?: -1 }
        assertEquals(-1, res)
    }

    @Test
    fun `with returns last expression`() {
        val res = with(10) {
            val x = this * 2
            x + 3
        }
        assertEquals(23, res)
    }

    @Test
    fun `with block returning Unit`() {
        var side = 0
        val res = with(7) { side = this }
        assertTrue(res is Unit)
        assertEquals(7, side)
    }

    @Test
    fun `apply initializes Person`() {
        val person = Person("", 0).apply {
            name = "Alice"
            age = 33
        }
        assertEquals("Alice", person.name)
        assertEquals(33, person.age)
    }

    @Test
    fun `apply returns same object`() {
        val original = Person("X", 1)
        val result = original.apply { age = 99 }
        assertSame(original, result)
    }

    @Test
    fun `apply on immutable String does nothing`() {
        val str = "hello"
        val result = str.apply { }
        assertSame(str, result)
    }

    @Test
    fun `apply on null`() {
        val nullable: Person? = null
        val result = nullable.apply { }
        assertSame(null, result)
    }

    @Test
    fun `apply with side effect`() {
        var side = 0
        val obj = 5
        val result = obj.apply { side = this }
        assertSame(obj, result)
        assertEquals(5, side)
    }

    @Test
    fun `also logs and returns same object`() {
        var captured: List<Int>? = null
        val list = listOf(1, 2, 3)
        val result = list.also { captured = it }
        assertSame(list, result)
        assertEquals(list, captured)
    }

    @Test
    fun `also can modify object`() {
        val person = Person("", 0)
        val result = person.also { it.name = "Bob" }
        assertSame(person, result)
        assertEquals("Bob", person.name)
    }

    @Test
    fun `also on null`() {
        val nullable: Person? = null
        var captured: Person? = Person("tmp", 1)
        val result = nullable.also { captured = it }
        assertSame(null, result)
        assertEquals(null, captured)
    }

    @Test
    fun `also returns this even if block returns value`() {
        val obj = "test"
        val result = obj.also { it.length }
        assertSame(obj, result)
    }

    @Test
    fun `run concatenates string`() {
        val res = "445".run { this + "6" }
        assertEquals("4456", res)
    }

    @Test
    fun `run on Int computes`() {
        val res = 10.run { this * 3 }
        assertEquals(30, res)
    }

    @Test
    fun `run on Person`() {
        val person = Person("Charlie", 40)
        val res = person.run { "$name is $age" }
        assertEquals("Charlie is 40", res)
    }

    @Test
    fun `run on nullable`() {
        val s: String? = "hello"
        val res = s.run { this?.length ?: -1 }
        assertEquals(5, res)
    }

    @Test
    fun `run on null`() {
        val s: String? = null
        val res = s.run { this?.length ?: -1 }
        assertEquals(-1, res)
    }

    @Test
    fun `run returns last expression`() {
        val res = 7.run {
            val x = this * 2
            x + 1
        }
        assertEquals(15, res)
    }

    @Test
    fun `let transforms string`() {
        val res = "445".let { it + "6" }
        assertEquals("4456", res)
    }

    @Test
    fun `let on Int`() {
        val res = 5.let { it * it }
        assertEquals(25, res)
    }

    @Test
    fun `let on Person`() {
        val person = Person("Dave", 50)
        val res = person.let { "${it.name} is ${it.age}" }
        assertEquals("Dave is 50", res)
    }

    @Test
    fun `let on nullable with safe call`() {
        val name: String? = "Alice"
        var printed = ""
        name?.let { printed = "Name is: $it" }
        assertEquals("Name is: Alice", printed)
    }

    @Test
    fun `let on null does not execute`() {
        val name: String? = null
        var executed = false
        name?.let { executed = true }
        assertEquals(false, executed)
    }

    @Test
    fun `let returns result`() {
        val res = "test".let { it.length }
        assertEquals(4, res)
    }

    @Test
    fun `Person data class works`() {
        val p1 = Person("Alice", 30)
        val p2 = Person("Alice", 30)
        assertEquals(p1, p2)
        assertEquals("Person(name=Alice, age=30)", p1.toString())
    }

    @Test
    fun `main prints expected output`() {
        val output = captureOutput { mainDemo() }
        val lines = output.lines().filter { it.isNotEmpty() }
        assertEquals(6, lines.size)
        assertEquals("List created: [1, 2, 3]", lines[0])
        assertEquals("Name is: Alice", lines[1])
        assertEquals("4456", lines[2])
        assertEquals("Alice", lines[3])
        assertEquals("[1, 2, 3]", lines[4])
        assertEquals("4456", lines[5])
    }
}