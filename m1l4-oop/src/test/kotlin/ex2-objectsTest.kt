import kotlin.test.Test

// Пример, демонстрирующий работу object и companion object в Kotlin.
class ObjectsExample {

    // Companion object — объект, связанный с классом. Его члены вызываются через имя класса,
    // как статические методы в Java. Инициализируется при первой загрузке класса
    // (при первом обращении к классу или его companion).
    companion object {
        // Блок инициализации companion object. Выполняется один раз при загрузке класса.
        init {
            println("companion inited") // будет выведено при первом обращении к ObjectsExample
        }

        fun doSmth() {
            println("companion object")
        }
    }

    // Объявление object — создание синглтона (единственного экземпляра).
    // Инициализация происходит лениво: при первом обращении к объекту A.
    object A {
        init {
            println("A inited") // выполнится один раз при первом доступе к A
        }

        fun doSmth() {
            println("object A")
        }
    }
}

// Тестовый класс для проверки порядка инициализации.
class ObjectsTest {

    @Test
    fun test() {
        // 1. Создание экземпляра класса ObjectsExample.
        // При первом обращении к классу загружается его companion object,
        // поэтому сначала выведется "companion inited".
        ObjectsExample()

        // 2. Вызов метода companion object через имя класса.
        // Companion object уже проинициализирован, поэтому просто вызов метода.
        ObjectsExample.doSmth()

        // 3. Первое обращение к объекту A. Происходит его ленивая инициализация:
        // выполнится init-блок объекта A ("A inited"), затем метод doSmth().
        ObjectsExample.A.doSmth()

        // 4. Повторное обращение к объекту A. Инициализация уже была,
        // поэтому только вызов метода.
        ObjectsExample.A.doSmth()

        // Ожидаемый вывод:
        // companion inited
        // companion object
        // A inited
        // object A
        // object A
    }
}