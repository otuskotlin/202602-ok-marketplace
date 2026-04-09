import kotlin.test.Test
import kotlin.test.assertEquals

// Тестовый класс, демонстрирующий вариантность (variance) в обобщениях Kotlin.
class GenericTest {

    // Тест для инвариантного (invariant) обобщённого типа.
    @Test
    fun invariant() {
        // Следующая строка закомментирована, потому что она НЕ КОМПИЛИРУЕТСЯ.
        // val obj: ISome<Number> = IntSome(1) // Не работает!!!
        // Причина: интерфейс ISome<T> объявлен с типом T, который не помечен ни out, ни in.
        // В Kotlin такие обобщения инвариантны: ISome<Number> не является супертипом для ISome<IntSome>,
        // даже если IntSome является подтипом Number? Но здесь T ограничен сверху (T : ISome<T>),
        // и ISome<IntSome> никак не связан с ISome<Number>. Это демонстрация инвариантности.
        assertEquals(3, (IntSome(1) + IntSome(2)).value)
    }

    // Тест для ковариантного (covariant) обобщённого типа.
    @Test
    fun covariant() {
        // Ковариантность позволяет присвоить IParse<Int> переменной типа IParse<Number>,
        // потому что out T означает, что T может быть только возвращаемым типом (producer).
        @Suppress("UNUSED_VARIABLE")
        val obj: IParse<Number> = CovariantCls() // IParse<Int> -- Работает!
        assertEquals(3, CovariantCls().parse("3"))
    }

    // Тест для контравариантного (contravariant) обобщённого типа.
    @Test
    fun contravariant() {
        // Контравариантность позволяет присвоить IToString<Number> переменной типа IToString<Int>,
        // потому что in T означает, что T может быть только типом аргумента (consumer).
        @Suppress("UNUSED_VARIABLE")
        val obj: IToString<Int> = ContravariantCls() // IToString<Number> -- Работает!
        assertEquals("3", ContravariantCls().toStr(3))
    }

    // Пример инвариантного интерфейса с рекурсивным ограничением типа (recursive type bound).
    // T должен быть подтипом ISome<T> (это позволяет методам возвращать T).
    private interface ISome<T : ISome<T>> {
        operator fun plus(other: T): T
    }

    // Реализация для Int. T здесь IntSome, что удовлетворяет ограничению (IntSome : ISome<IntSome>).
    private class IntSome(val value: Int) : ISome<IntSome> {
        override fun plus(other: IntSome): IntSome = IntSome(value + other.value)
    }

    // Ковариантный интерфейс: out T означает, что T может использоваться только в выходных позициях
    // (например, как возвращаемый тип метода parse). Это позволяет использовать IParse<Int>
    // там, где ожидается IParse<Number>.
    private interface IParse<out T : Number> {
        fun parse(str: String): T
    }

    // Реализация для Int, которая может быть присвоена переменной IParse<Number>.
    private class CovariantCls : IParse<Int> {
        override fun parse(str: String): Int = str.toInt()
    }

    // Контравариантный интерфейс: in T означает, что T может использоваться только во входных позициях
    // (например, как тип аргумента метода toStr). Это позволяет использовать IToString<Number>
    // там, где ожидается IToString<Int>.
    private interface IToString<in T> {
        fun toStr(i: T): String
    }

    // Реализация для Number, которая может быть присвоена переменной IToString<Int>.
    private class ContravariantCls : IToString<Number> {
        override fun toStr(i: Number): String = i.toString()
    }
}