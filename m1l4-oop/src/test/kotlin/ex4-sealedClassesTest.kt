import kotlin.test.Test
import kotlin.test.assertEquals

// Sealed-интерфейс — определяет ограниченную иерархию типов.
// Все прямые реализации должны находиться в том же модуле (и обычно в том же файле).
sealed interface Base

// data object — объект-синглтон с автоматически сгенерированными
// toString(), equals(), hashCode() (как у data class, но для объекта).
data object ChildA : Base

// Обычный класс, реализующий sealed-интерфейс.
// Переопределяет equals и hashCode, чтобы сравнивать объекты по ссылке (===).
class ChildB : Base {
    override fun equals(other: Any?): Boolean {
        return this === other
    }

    override fun hashCode(): Int {
        return System.identityHashCode(this)
    }
}

// Простой объект-синглтон, реализующий Base.
object ChildC : Base

// Раскомментирование следующей строки вызовет ошибку компиляции,
// потому что sealed-интерфейс уже имеет реализацию с именем ChildC
// (нельзя создать ещё один класс или объект с тем же именем в том же файле).
//class ChildC : Base

// Тестовый класс, демонстрирующий работу с sealed-иерархией.
class SealedTest {
    @Test
    fun test() {
        // Полиморфное обращение: объект типа Base, фактически ChildA.
        val obj: Base = ChildA

        // when должен быть исчерпывающим (exhaustive), так как Base — sealed.
        // Компилятор проверяет, что все возможные типы обработаны.
        val result = when (obj) {
            is ChildA -> "a"
            is ChildB -> "b"
            is ChildC -> "c"
        }

        println(result) // Выведет "a"
        assertEquals(result, "a")
    }
}
