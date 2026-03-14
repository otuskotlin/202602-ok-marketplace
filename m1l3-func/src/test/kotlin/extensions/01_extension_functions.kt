package extensions

import kotlin.test.Test
import kotlin.test.assertEquals
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class ExtensionsTestFixed {

    fun Int.toCustomString() = "This is integer $this"
    fun Double.toCustomString() = "This is double $this"
    fun Int.toLong(): String = "Convert int to Long"
    fun Any?.toCustomString(): String {
        return if (this == null) {
            "Nothing to return"
        } else {
            "$this this is custom string"
        }
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
        println(10.toCustomString())
        println(10.toLong())
        println(10.3.toCustomString())
        println(null.toCustomString())
    }

    @Test
    fun `Int toCustomString for zero`() {
        assertEquals("This is integer 0", 0.toCustomString())
    }

    @Test
    fun `Int toCustomString for positive`() {
        assertEquals("This is integer 42", 42.toCustomString())
    }

    @Test
    fun `Int toCustomString for negative`() {
        assertEquals("This is integer -5", (-5).toCustomString())
    }

    @Test
    fun `Int toCustomString for max int`() {
        assertEquals("This is integer 2147483647", Int.MAX_VALUE.toCustomString())
    }

    @Test
    fun `Int toCustomString for min int`() {
        assertEquals("This is integer -2147483648", Int.MIN_VALUE.toCustomString())
    }

    @Test
    fun `Double toCustomString for zero`() {
        assertEquals("This is double 0.0", 0.0.toCustomString())
    }

    @Test
    fun `Double toCustomString for positive`() {
        assertEquals("This is double 3.14", 3.14.toCustomString())
    }

    @Test
    fun `Double toCustomString for negative`() {
        assertEquals("This is double -2.71", (-2.71).toCustomString())
    }

    @Test
    fun `Double toCustomString for NaN`() {
        assertEquals("This is double NaN", Double.NaN.toCustomString())
    }

    @Test
    fun `Double toCustomString for Infinity`() {
        assertEquals("This is double Infinity", Double.POSITIVE_INFINITY.toCustomString())
    }


    @Test
    fun `Any? toCustomString on null`() {
        assertEquals("Nothing to return", null.toCustomString())
    }

    @Test
    fun `Any? toCustomString on non-null Int`() {
        assertEquals("42 this is custom string", (42 as Any?).toCustomString())
    }

    @Test
    fun `Any? toCustomString on non-null Double`() {
        assertEquals("3.14 this is custom string", (3.14 as Any?).toCustomString())
    }

    @Test
    fun `Any? toCustomString on non-null String`() {
        assertEquals("Hello this is custom string", "Hello".toCustomString())
    }

    class SampleClass

    @Test
    fun `Any? toCustomString on custom class`() {
        val obj = SampleClass()
        assertEquals("$obj this is custom string", obj.toCustomString())
    }

    @Test
    fun `Any? toCustomString on list`() {
        val list = listOf(1, 2, 3)
        assertEquals("$list this is custom string", list.toCustomString())
    }

}