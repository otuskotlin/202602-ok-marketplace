import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.test.Test
import kotlin.test.assertEquals

// Тестовый класс, демонстрирующий использование делегированных свойств в Kotlin.
internal class PropDelegationTest {

    // Тест для делегата только для чтения (read-only delegate).
    @Test
    fun roDelegate() {
        val example = DelegateExample()
        // Обращение к свойству constVal, делегированному ConstValue.
        println(example.constVal)
        assertEquals(example.constVal, 100501)
    }

    // Тест для делегата чтения/записи (read-write delegate).
    @Test
    fun rwDelegate() {
        val example = DelegateExample()
        // Установка значения свойства varVal через делегат VarValue.
        example.varVal = 15
        println(example.varVal)
        assertEquals(example.varVal, 15)
    }

    // Тест для ленивого делегата (lazy), предоставляемого стандартной библиотекой.
    @Test
    fun lazyDelegate() {
        val example = DelegateExample()
        // Первый доступ к lazyVal вызывает вычисление (вывод "calculate...").
        println(example.lazyVal)
        assertEquals(example.lazyVal, 42) // Повторный доступ возвращает уже вычисленное значение.
    }

    // Пользовательский делегат только для чтения, реализующий ReadOnlyProperty.
    // Он хранит фиксированное значение и возвращает его при getValue.
    private class ConstValue(private val value: Int) : ReadOnlyProperty<Any?, Int> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): Int {
            return value
        }
    }

    // Пользовательский делегат для чтения/записи, реализующий ReadWriteProperty.
    // Хранит изменяемое значение и позволяет его читать/изменять.
    private class VarValue(private var value: Int) : ReadWriteProperty<Any?, Int> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): Int {
            return value
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
            this.value = value
        }
    }

    // Класс, использующий различные виды делегированных свойств.
    private class DelegateExample {
        // Свойство constVal делегировано экземпляру ConstValue, который всегда возвращает 100501.
        val constVal by ConstValue(100501)

        // Свойство varVal делегировано экземпляру VarValue, начальное значение 100501,
        // но может быть изменено через setter.
        var varVal by VarValue(100501)

        // Ленивое свойство: инициализируется при первом обращении с помощью переданной лямбды.
        // Вычисления происходят только один раз, результат кэшируется.
        val lazyVal by lazy {
            println("calculate...") // Этот код выполнится только при первом доступе.
            42
        }
    }
}
