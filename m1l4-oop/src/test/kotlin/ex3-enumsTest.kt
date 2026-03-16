import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

// Простой enum (перечисление) с двумя константами.
// Enum в Kotlin может содержать только имена констант без дополнительных данных.
enum class HighLowEnum {
    LOW,
    HIGH
}

// Enum с дополнительными данными (аналог enum с полями в Java).
// Каждая константа передаёт значения в первичный конструктор.
// level и description — свойства, доступные для каждой константы.
// @Suppress("unused") подавляет предупреждение о неиспользуемом параметре description.
enum class HighLowWithData(val level: Int, @Suppress("unused") val description: String) {
    LOW(10, "low level"),
    HIGH(20, "high level")
}

// Enum, реализующий интерфейс Iterable<FooBarEnum> (может быть использован в цикле for).
// Каждая константа определена с телом, переопределяющим абстрактный метод doSmth().
// Точка с запятой после последней константы обязательна, если после объявлений есть ещё члены.
enum class FooBarEnum : Iterable<FooBarEnum> {
    // Константа FOO с анонимным классом, переопределяющим doSmth()
    FOO {
        override fun doSmth() {
            println("do foo")
        }
    },

    // Константа BAR с анонимным классом, переопределяющим doSmth()
    BAR {
        override fun doSmth() {
            println("do bar")
        }
    };

    // Абстрактный метод, который должны реализовать все константы enum'а.
    abstract fun doSmth()

    // Переопределение метода iterator из интерфейса Iterable.
    // Возвращает итератор по списку всех констант (FOO, BAR).
    // Это позволяет, например, перебирать значения enum'а через for (value in FooBarEnum).
    override fun iterator(): Iterator<FooBarEnum> = listOf(FOO, BAR).iterator()
}

// Класс с тестами для enum'ов.
class EnumsTest {

    // Тест базового enum'а HighLowEnum.
    @Test
    fun enum() {
        // Прямое присваивание константы.
        var e = HighLowEnum.LOW
        println(e)

        // Получение константы по имени (чувствительно к регистру).
        // valueOf генерирует исключение, если константа не найдена.
        e = HighLowEnum.valueOf("HIGH")
        println(e)

        // ordinal — порядковый номер константы (начиная с 0).
        println(e.ordinal)
        assertEquals(1, e.ordinal) // HIGH имеет индекс 1

        // entries — современная замена values(), возвращает список всех констант.
        println(HighLowEnum.entries.joinToString())
    }

    // Тест enum'а с данными HighLowWithData.
    @Test
    fun enumWithData() {
        var e = HighLowWithData.LOW
        println(e)

        e = HighLowWithData.valueOf("HIGH")
        println(e)

        println(e.ordinal)
        assertEquals(1, e.ordinal)

        // Доступ к дополнительному свойству level.
        assertEquals(20, e.level)

        // Для сравнения выводим все константы простого enum'а.
        println(HighLowEnum.entries.joinToString())
    }

    // Тест enum'а, реализующего интерфейс (FooBarEnum).
    @Test
    fun interfacedEnums() {
        // Проверяем, что итератор, полученный от константы BAR, возвращает все значения.
        assertEquals(listOf(FooBarEnum.FOO, FooBarEnum.BAR),
            FooBarEnum.BAR.iterator().asSequence().toList())

        // То же самое от константы FOO.
        assertEquals(listOf(FooBarEnum.FOO, FooBarEnum.BAR),
            FooBarEnum.FOO.iterator().asSequence().toList())
    }

    // Тест обработки ошибок при работе с enum'ами.
    @Test
    fun enumFailures() {
        // assertFails проверяет, что блок кода выбрасывает исключение.
        assertFails {
            // valueOf с неправильным регистром ("high" вместо "HIGH") вызовет IllegalArgumentException.
            HighLowEnum.valueOf("high")
        }

        // runCatching выполняет блок и возвращает Result, из которого можно получить значение
        // или значение по умолчанию в случае ошибки (getOrDefault).
        val res = runCatching { HighLowEnum.valueOf("high") }
            .getOrDefault(HighLowEnum.HIGH)

        // В случае ошибки возвращается HIGH, поэтому утверждение проходит.
        assertEquals(HighLowEnum.HIGH, res)
    }
}
