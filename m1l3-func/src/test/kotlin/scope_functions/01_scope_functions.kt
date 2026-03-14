package scope_functions

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ScopeFunctionsTest {

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

    data class Person(var name: String, var age: Int)

    @Test
    fun `with on String returns block result`() {
        val result = with("hello") { this + " world" }
        assertEquals("hello world", result)
    }

    @Test
    fun `with on Int returns block result`() {
        val result = with(5) { this * 2 }
        assertEquals(10, result)
    }

    @Test
    fun `with on custom object can access properties`() {
        val person = Person("Alice", 30)
        val result = with(person) { "$name is $age years old" }
        assertEquals("Alice is 30 years old", result)
    }

    @Test
    fun `with on null receiver`() {
        val nullable: String? = null
        val result = with(nullable) { this?.length ?: -1 }
        assertEquals(-1, result)
    }

    @Test
    fun `with on nullable receiver with non-null block`() {
        val nullable: String? = "test"
        val result = with(nullable) { this!!.length }
        assertEquals(4, result)
    }

    @Test
    fun `with returns last expression`() {
        val result = with(10) {
            val x = this * 2
            x + 5
        }
        assertEquals(25, result)
    }

    @Test
    fun `with block returning Unit`() {
        var sideEffect = 0
        val result = with(5) {
            sideEffect = this
        }
        assertTrue(result is Unit)
        assertEquals(5, sideEffect)
    }

    @Test
    fun `apply on mutable object modifies and returns same object`() {
        val person = Person("Alice", 30)
        val result = person.apply {
            name = "Bob"
            age = 25
        }
        assertSame(person, result)
        assertEquals("Bob", result.name)
        assertEquals(25, result.age)
    }

    @Test
    fun `apply on immutable object (String) does nothing but returns same object`() {
        val str = "hello"
        val result = str.apply {
            // this is str, but cannot modify
        }
        assertSame(str, result)
    }

    @Test
    fun `apply on null`() {
        val nullable: Person? = null
        val result = nullable.apply {
            // this is null
        }
        assertSame(null, result)
    }

    @Test
    fun `apply with side effect`() {
        var sideEffect = 0
        val obj = 10
        val result = obj.apply {
            sideEffect = this
        }
        assertSame(obj, result)
        assertEquals(10, sideEffect)
    }

    @Test
    fun `apply returns this even if block returns something`() {
        val obj = "test"
        val result = obj.apply {
            "ignored"
        }
        assertSame(obj, result)
    }

    @Test
    fun `also on object passes it and returns same object`() {
        val person = Person("Alice", 30)
        var captured: Person? = null
        val result = person.also {
            captured = it
            it.name = "Bob"
        }
        assertSame(person, result)
        assertEquals(person, captured)
        assertEquals("Bob", person.name)
    }

    @Test
    fun `also on immutable object returns same object`() {
        val str = "hello"
        var captured: String? = null
        val result = str.also {
            captured = it
        }
        assertSame(str, result)
        assertEquals("hello", captured)
    }

    @Test
    fun `also on null`() {
        val nullable: String? = null
        var captured: String? = "initial"
        val result = nullable.also {
            captured = it
        }
        assertSame(null, result)
        assertEquals(null, captured)
    }

    @Test
    fun `also returns this even if block returns something`() {
        val obj = 42
        val result = obj.also {
            it * 2
        }
        assertSame(obj, result)
    }

    @Test
    fun `run on String returns block result`() {
        val result = "hello".run { this + " world" }
        assertEquals("hello world", result)
    }

    @Test
    fun `run on Int returns block result`() {
        val result = 5.run { this * 2 }
        assertEquals(10, result)
    }

    @Test
    fun `run on custom object`() {
        val person = Person("Alice", 30)
        val result = person.run { "$name is $age" }
        assertEquals("Alice is 30", result)
    }

    @Test
    fun `run on null receiver`() {
        val nullable: String? = null
        val result = nullable.run { this?.length ?: -1 }
        assertEquals(-1, result)
    }

    @Test
    fun `run returns last expression`() {
        val result = 10.run {
            val x = this * 2
            x + 5
        }
        assertEquals(25, result)
    }

    @Test
    fun `run block returning Unit`() {
        var sideEffect = 0
        val result = 5.run {
            sideEffect = this
        }
        assertTrue(result is Unit)
        assertEquals(5, sideEffect)
    }

    @Test
    fun `let on String passes it and returns block result`() {
        val result = "hello".let { it + " world" }
        assertEquals("hello world", result)
    }

    @Test
    fun `let on Int passes it`() {
        val result = 5.let { it * 2 }
        assertEquals(10, result)
    }

    @Test
    fun `let on custom object`() {
        val person = Person("Alice", 30)
        val result = person.let { "${it.name} is ${it.age}" }
        assertEquals("Alice is 30", result)
    }

    @Test
    fun `let on null`() {
        val nullable: String? = null
        val result = nullable.let { it?.length ?: -1 }
        assertEquals(-1, result)
    }

    @Test
    fun `let returns last expression`() {
        val result = 10.let {
            val x = it * 2
            x + 5
        }
        assertEquals(25, result)
    }

    @Test
    fun `let block returning Unit`() {
        var sideEffect = 0
        val result = 5.let {
            sideEffect = it
        }
        assertTrue(result is Unit)
        assertEquals(5, sideEffect)
    }

    @Test
    fun `with returns type R`() {
        val res: Int = with(5) { this + 1 }
        assertEquals(6, res)
    }

    @Test
    fun `apply returns type T`() {
        val obj: String = "hello".apply {}
        assertEquals("hello", obj)
    }

    @Test
    fun `also returns type T`() {
        val obj: String = "hello".also {}
        assertEquals("hello", obj)
    }

    @Test
    fun `run returns type R`() {
        val res: Int = "5".run { length }
        assertEquals(1, res)
    }

    @Test
    fun `let returns type R`() {
        val res: Int = "5".let { it.length }
        assertEquals(1, res)
    }

    @Test
    fun `main example`() {
        val result = with("445") { this + "6" }
        assertEquals("4456", result)
    }
}