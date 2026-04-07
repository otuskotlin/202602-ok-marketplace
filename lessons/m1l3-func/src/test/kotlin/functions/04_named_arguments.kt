package functions

import kotlin.test.Test
import kotlin.test.assertEquals
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class NamedArgumentsTest {

    fun createUserProfile(
        name: String,
        age: Int = 18,
        city: String = "Unknown",
        isActive: Boolean = true
    ): String {
        return "User: $name, Age: $age, City: $city, Active: $isActive"
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
        println(createUserProfile("Alice", 25, "Moscow", true))
        println(createUserProfile(
            name = "Bob",
            age = 30,
            city = "Saint Petersburg",
            isActive = false
        ))
        println(createUserProfile("Charlie", age = 22, isActive = true))
    }

    @Test
    fun `all positional arguments`() {
        val result = createUserProfile("Alice", 25, "Moscow", true)
        assertEquals("User: Alice, Age: 25, City: Moscow, Active: true", result)
    }

    @Test
    fun `all named arguments in order`() {
        val result = createUserProfile(
            name = "Bob",
            age = 30,
            city = "Saint Petersburg",
            isActive = false
        )
        assertEquals("User: Bob, Age: 30, City: Saint Petersburg, Active: false", result)
    }

    @Test
    fun `all named arguments different order`() {
        val result = createUserProfile(
            isActive = true,
            city = "London",
            age = 40,
            name = "Dave"
        )
        assertEquals("User: Dave, Age: 40, City: London, Active: true", result)
    }

    @Test
    fun `mixed arguments positional and named`() {
        val result = createUserProfile("Charlie", age = 22, isActive = true)
        assertEquals("User: Charlie, Age: 22, City: Unknown, Active: true", result)
    }

    @Test
    fun `mixed arguments with city named`() {
        val result = createUserProfile("Eve", city = "Paris")
        assertEquals("User: Eve, Age: 18, City: Paris, Active: true", result)
    }

    @Test
    fun `only name argument`() {
        val result = createUserProfile("Frank")
        assertEquals("User: Frank, Age: 18, City: Unknown, Active: true", result)
    }

    @Test
    fun `name and age`() {
        val result = createUserProfile("Grace", age = 35)
        assertEquals("User: Grace, Age: 35, City: Unknown, Active: true", result)
    }

    @Test
    fun `name and city`() {
        val result = createUserProfile("Heidi", city = "Berlin")
        assertEquals("User: Heidi, Age: 18, City: Berlin, Active: true", result)
    }

    @Test
    fun `name and isActive`() {
        val result = createUserProfile("Ivan", isActive = false)
        assertEquals("User: Ivan, Age: 18, City: Unknown, Active: false", result)
    }

    @Test
    fun `name, age, city`() {
        val result = createUserProfile("Judy", 28, "Rome")
        assertEquals("User: Judy, Age: 28, City: Rome, Active: true", result)
    }

    @Test
    fun `name, age, isActive`() {
        val result = createUserProfile("Karl", age = 50, isActive = false)
        assertEquals("User: Karl, Age: 50, City: Unknown, Active: false", result)
    }

    @Test
    fun `name, city, isActive`() {
        val result = createUserProfile("Leo", city = "Madrid", isActive = false)
        assertEquals("User: Leo, Age: 18, City: Madrid, Active: false", result)
    }

    @Test
    fun `age zero`() {
        val result = createUserProfile("Mallory", age = 0)
        assertEquals("User: Mallory, Age: 0, City: Unknown, Active: true", result)
    }

    @Test
    fun `negative age`() {
        val result = createUserProfile("Nina", age = -5)
        assertEquals("User: Nina, Age: -5, City: Unknown, Active: true", result)
    }

    @Test
    fun `empty name`() {
        val result = createUserProfile("")
        assertEquals("User: , Age: 18, City: Unknown, Active: true", result)
    }

    @Test
    fun `empty city`() {
        val result = createUserProfile("Oscar", city = "")
        assertEquals("User: Oscar, Age: 18, City: , Active: true", result)
    }

    @Test
    fun `long strings`() {
        val longName = "A".repeat(1000)
        val longCity = "B".repeat(500)
        val result = createUserProfile(longName, city = longCity)
        assertEquals("User: $longName, Age: 18, City: $longCity, Active: true", result)
    }

    @Test
    fun `special characters in name and city`() {
        val result = createUserProfile("!@#$%", age = 33, city = "()[]{}")
        assertEquals("User: !@#$%, Age: 33, City: ()[]{}, Active: true", result)
    }

    @Test
    fun `main prints expected output`() {
        val output = captureOutput { mainDemo() }
        val lines = output.lines().filter { it.isNotEmpty() }
        assertEquals(3, lines.size)
        assertEquals("User: Alice, Age: 25, City: Moscow, Active: true", lines[0])
        assertEquals("User: Bob, Age: 30, City: Saint Petersburg, Active: false", lines[1])
        assertEquals("User: Charlie, Age: 22, City: Unknown, Active: true", lines[2])
    }
}