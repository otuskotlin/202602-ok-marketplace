import kotlin.test.Test

// Интерфейс-маркер, не содержит методов. Используется для указания,
// что класс реализует этот интерфейс (полиморфизм).
interface IClass {
}

// Абстрактный класс. Не может быть создан напрямую, только через наследование.
abstract class BaseClass {

    // open - метод может быть переопределён в наследниках.
    open fun openMethod() {}

    // Без open - метод закрыт для переопределения (final по умолчанию).
    fun closeMethod() {}
}

// Первый класс-наследник BaseClass.
class FirstClass : BaseClass() {

    // Переопределение open-метода обязательно с ключевым словом override.
    override fun openMethod() {}
}

// @Suppress("unused") - подавляет предупреждение компилятора о том,
// что класс или его члены нигде не используются.
@Suppress("unused")
// Класс InheritedClass реализует интерфейс IClass и наследуется от BaseClass.
// В первичном конструкторе объявлены:
// - arg: String (параметр конструктора, не является свойством класса,
//   если не используется в теле или не помечен val/var).
// - val prop: String = arg - свойство класса с публичным геттером,
//   значение по умолчанию берётся из аргумента конструктора arg.
class InheritedClass(
    arg: String,
    val prop: String = arg
) : IClass, BaseClass() {

    // Свойство x, инициализируемое значением arg.
    val x: String = arg

    // Блок инициализации, выполняется сразу после первичного конструктора
    // при создании объекта.
    init {
        println("Init in constructor with $arg")
    }

    // Обычный метод класса.
    fun some() {
        // Использование свойства prop с ключевым словом this (можно опустить).
        println("Some is called with: ${this.prop}")
    }
}

// Класс с тестом (используется библиотека kotlin.test).
class BaseTest() {

    // @Test - аннотация, указывающая, что функция является тестовым методом.
    @Test
    fun baseTest() {
        // Создание объекта InheritedClass с аргументом "some".
        val obj = InheritedClass("some")
        // Вызов метода some у созданного объекта.
        obj.some()
    }
}