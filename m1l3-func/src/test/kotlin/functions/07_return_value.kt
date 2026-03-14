package functions

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class ReturnValuesTest {

    fun square(x: Int): Int = x * x

    fun cube(x: Int): Int = x * x * x

    fun classify(x: Int): String = when (x) {
        0 -> "Zero"
        in 1..10 -> "Small"
        else -> "Large"
    }

    fun greet(): Unit = println("Hello!")

    fun sayHi(): Unit = println("Hi!")

    fun reportError(): Nothing = throw RuntimeException("Something went wrong")

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
        println(square(4))
        println(cube(2))
        println(classify(5))
        greet()
        sayHi()
        // reportError()
    }

    @Test
    fun `square of zero returns zero`() {
        assertEquals(0, square(0))
    }

    @Test
    fun `square of one returns one`() {
        assertEquals(1, square(1))
    }

    @Test
    fun `square of positive numbers`() {
        assertEquals(4, square(2))
        assertEquals(9, square(3))
        assertEquals(16, square(4))
        assertEquals(25, square(5))
    }

    @Test
    fun `square of negative numbers`() {
        assertEquals(4, square(-2))
        assertEquals(9, square(-3))
        assertEquals(16, square(-4))
        assertEquals(25, square(-5))
    }

    @Test
    fun `square of large numbers without overflow`() {
        assertEquals(2_147_395_600, square(46340))
        assertEquals(2_147_395_600, square(-46340))
    }

    @Test
    fun `square of numbers causing overflow`() {
        assertEquals(-2_147_479_015, square(46341))
        assertEquals(-2_147_479_015, square(-46341))
        assertEquals(1, square(Int.MAX_VALUE))
        assertEquals(0, square(Int.MIN_VALUE))
    }

    @Test
    fun `cube of zero returns zero`() {
        assertEquals(0, cube(0))
    }

    @Test
    fun `cube of one returns one`() {
        assertEquals(1, cube(1))
    }

    @Test
    fun `cube of positive numbers`() {
        assertEquals(8, cube(2))
        assertEquals(27, cube(3))
        assertEquals(64, cube(4))
        assertEquals(125, cube(5))
    }

    @Test
    fun `cube of negative numbers`() {
        assertEquals(-8, cube(-2))
        assertEquals(-27, cube(-3))
        assertEquals(-64, cube(-4))
        assertEquals(-125, cube(-5))
    }

    @Test
    fun `cube of large numbers without overflow`() {
        assertEquals(1_000_000_000, cube(1000))
        assertEquals(-1_000_000_000, cube(-1000))
    }

    @Test
    fun `classify zero returns Zero`() {
        assertEquals("Zero", classify(0))
    }

    @Test
    fun `classify numbers less than 1 returns Large`() {
        val values = listOf(-1, -100, Int.MIN_VALUE)
        for (x in values) {
            assertEquals("Large", classify(x))
        }
    }

    @Test
    fun `classify numbers greater than 10 returns Large`() {
        val values = listOf(11, 100, Int.MAX_VALUE)
        for (x in values) {
            assertEquals("Large", classify(x))
        }
    }

    @Test
    fun `greet prints Hello`() {
        val output = captureOutput { greet() }
        assertEquals("Hello!\n", output)
    }

    @Test
    fun `greet returns Unit`() {
        val result = greet()
        assertEquals(Unit, result)
    }

    @Test
    fun `sayHi prints Hi`() {
        val output = captureOutput { sayHi() }
        assertEquals("Hi!\n", output)
    }

    @Test
    fun `sayHi returns Unit`() {
        val result = sayHi()
        assertEquals(Unit, result)
    }

    @Test
    fun `reportError throws RuntimeException`() {
        assertFailsWith<RuntimeException> {
            reportError()
        }
    }

    @Test
    fun `reportError throws with correct message`() {
        val exception = assertFailsWith<RuntimeException> {
            reportError()
        }
        assertEquals("Something went wrong", exception.message)
    }

    @Test
    fun `main prints expected output`() {
        val output = captureOutput { mainDemo() }
        val lines = output.lines().filter { it.isNotEmpty() }
        assertEquals(5, lines.size)
        assertEquals("16", lines[0])
        assertEquals("8", lines[1])
        assertEquals("Small", lines[2])
        assertEquals("Hello!", lines[3])
        assertEquals("Hi!", lines[4])
    }
}