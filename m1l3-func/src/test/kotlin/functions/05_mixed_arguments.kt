package functions

import kotlin.test.Test
import kotlin.test.assertEquals

class CountNewStepsTest {

    fun countNewSteps(
        currentStepCounter: Int,
        from: Int = 10,
        to: Int = 2
    ): Int = currentStepCounter + from - to

    @Test
    fun `all positional arguments`() {
        assertEquals(5 + 10 - 2, countNewSteps(5, 10, 2))
        assertEquals(5 + 10 - 2, countNewSteps(5, 10, 2))
    }

    @Test
    fun `positional with default to`() {
        assertEquals(5 + 10 - 2, countNewSteps(5, 10))
    }

    @Test
    fun `positional with default from and to`() {
        assertEquals(5 + 10 - 2, countNewSteps(5))
    }

    @Test
    fun `mixed arguments - currentStepCounter positional, from default, to named`() {
        assertEquals(5 + 10 - 10, countNewSteps(5, to = 10))
    }

    @Test
    fun `mixed arguments - currentStepCounter positional, from named, to default`() {
        assertEquals(5 + 15 - 2, countNewSteps(5, from = 15))
    }

    @Test
    fun `all named arguments`() {
        assertEquals(5 + 10 - 10, countNewSteps(currentStepCounter = 5, from = 10, to = 10))
    }

    @Test
    fun `all named arguments different order`() {
        assertEquals(5 + 8 - 3, countNewSteps(to = 3, currentStepCounter = 5, from = 8))
    }

    @Test
    fun `named arguments with some defaults`() {
        assertEquals(7 + 10 - 2, countNewSteps(currentStepCounter = 7))
        assertEquals(3 + 20 - 2, countNewSteps(currentStepCounter = 3, from = 20))
        assertEquals(4 + 10 - 5, countNewSteps(currentStepCounter = 4, to = 5))
        assertEquals(2 + 30 - 4, countNewSteps(currentStepCounter = 2, from = 30, to = 4))
    }

    @Test
    fun `zero and negative values`() {
        assertEquals(0 + 10 - 2, countNewSteps(0))
        assertEquals(-5 + 10 - 2, countNewSteps(-5))
        assertEquals(10 + (-3) - 7, countNewSteps(10, from = -3, to = 7))
        assertEquals(-8 + (-2) - (-5), countNewSteps(-8, from = -2, to = -5))
        assertEquals(0 + 0 - 0, countNewSteps(0, from = 0, to = 0))
    }

    @Test
    fun `large values`() {
        assertEquals(Int.MAX_VALUE, countNewSteps(Int.MAX_VALUE, from = 0, to = 0))
        assertEquals(Int.MAX_VALUE - 5, countNewSteps(Int.MAX_VALUE, from = 0, to = 5))
        val result = countNewSteps(Int.MAX_VALUE, from = 1, to = 0)
        assertEquals(Int.MIN_VALUE, result)
    }

    @Test
    fun `function does not modify inputs`() {
        val step = 5
        val from = 10
        val to = 3
        val result = countNewSteps(step, from, to)
        assertEquals(step + from - to, result)
        assertEquals(5, step)
        assertEquals(10, from)
        assertEquals(3, to)
    }

    @Test
    fun `main runs without exceptions`() {
        try {
            countNewSteps(5, 10)
            countNewSteps(5, to = 10)
            countNewSteps(currentStepCounter = 5, from = 10, to = 10)
        } catch (e: Exception) {
            throw AssertionError("Should not throw exception", e)
        }
    }
}