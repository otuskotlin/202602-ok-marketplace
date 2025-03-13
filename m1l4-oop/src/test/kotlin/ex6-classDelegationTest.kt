import kotlin.test.Test
import kotlin.test.assertEquals

// Тестовый класс, демонстрирующий механизм делегирования классов в Kotlin.
internal class ClassDelegationTest {

    // Первый тест: делегирование с частичным переопределением.
    @Test
    fun delegate() {
        val base = MyClass()
        val delegate = MyDelegate(base)

        println("Calling base")
        assertEquals("x", base.x())
        assertEquals("y", base.y())
        println("Calling delegate")
        // У делегата метод x() переопределён, а y() делегируется без изменений.
        assertEquals("delegate for (x)", delegate.x())
        assertEquals("y", delegate.y())
    }

    // Интерфейс, который будут реализовывать классы.
    interface IDelegate {
        fun x(): String
        fun y(): String
    }

    // Простая реализация интерфейса IDelegate.
    class MyClass() : IDelegate {
        override fun x(): String {
            println("MyClass.x()")
            return "x"
        }

        override fun y(): String {
            println("MyClass.x()") // Опечатка в выводе, но для примера оставим как есть.
            return "y"
        }
    }

    // Класс-делегат, использующий ключевое слово 'by' для автоматической делегации всех методов интерфейса IDelegate.
    // При этом метод x() переопределён вручную, а y() автоматически делегируется объекту del.
    class MyDelegate(
        private val del: IDelegate
    ) : IDelegate by del {  // 'by del' означает: все методы IDelegate, кроме переопределённых, будут вызваны у del.
        override fun x(): String {
            println("Calling x")
            val str = del.x()   // явный вызов, хотя можно было бы использовать просто x(), но здесь показано, как добавить логику.
            println("Calling x done")
            return "delegate for ($str)"
        }
    }

    // Далее пример множественной делегации: класс может реализовать несколько интерфейсов,
    // делегируя их разным объектам.

    // Интерфейс X с методом x().
    interface X {
        fun x(): String
    }

    // Реализация интерфейса X.
    class XImpl : X {
        override fun x(): String {
            println("Calling x in XImpl")
            return "x"
        }
    }

    // Интерфейс Y с методом y().
    interface Y {
        fun y(): String
    }

    // Реализация интерфейса Y.
    class YImpl : Y {
        override fun y(): String {
            println("Calling y() in YImpl")
            return "y"
        }
    }

    // Дополнительный интерфейс Z, который будет реализован непосредственно в классе XYZ.
    interface Z {
        fun z(): String
    }

    // Класс XYZ, использующий делегирование для интерфейсов X и Y.
    // Конструктор принимает объекты X и Y, и с помощью ключевого слова 'by' делегирует им реализацию соответствующих интерфейсов.
    // Таким образом, в классе не нужно вручную писать переопределения методов x() и y().
    class XYZ(
        x: X,
        y: Y,
    ) : X by x, Y by y, Z {  // делегируем X - объекту x, Y - объекту y
        // Метод z() реализуем напрямую.
        override fun z(): String = "z"
    }

    // Альтернативный вариант без использования делегации — пришлось бы вручную переопределять каждый метод.
    // Это демонстрирует, сколько кода экономит механизм 'by'.
    class XYZWithoutDelegation(
        private val x: X,
        private val y: Y,
    ) : X, Y, Z {

        override fun x(): String = x.x()   // ручная делегация
        override fun y(): String = y.y()   // ручная делегация
        override fun z(): String = "z"
    }

    // Тест для проверки работы множественной делегации.
    @Test
    fun xyz() {
        val x = XImpl()
        val y = YImpl()

        val xyz = XYZ(x, y)
        xyz.x()  // вызов метода, делегированного объекту x
    }
}
