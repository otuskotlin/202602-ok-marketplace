// Импорты для работы с денежными суммами: BigDecimal (точные вычисления),
// NumberFormat (форматирование чисел), Currency (валюта), Locale (локализация).
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

// Импорты для тестирования из библиотеки kotlin.test.
import kotlin.test.Test
import kotlin.test.assertEquals

// Класс, представляющий денежную сумму в определённой валюте.
// amount - сумма (публичное свойство только для чтения).
// currency - валюта (приватное свойство, недоступно напрямую извне).
class Cash(
    val amount: BigDecimal,
    private val currency: Currency
) {
    // Вторичный конструктор, принимающий сумму в виде строки.
    // Преобразует строку в BigDecimal и вызывает первичный конструктор.
    constructor(
        amount: String,
        currency: Currency
    ) : this(BigDecimal(amount), currency)

    // Метод форматирования суммы в соответствии с локалью.
    // Использует NumberFormat.getCurrencyInstance для получения форматера валюты,
    // устанавливает нужную валюту и возвращает отформатированную строку.
    fun format(locale: Locale): String {
        val formatter = NumberFormat.getCurrencyInstance(locale)
        formatter.currency = currency
        return formatter.format(amount)
    }

    // Перегрузка оператора минус (operator fun minus).
    // Позволяет вычитать одно денежное значение из другого, если валюты совпадают.
    // require генерирует исключение IllegalArgumentException, если валюты разные.
    // Возвращает новый объект Cash с разностью сумм и той же валютой.
    operator fun minus(other: Cash): Cash {
        require(currency == other.currency) {
            "Summand should be of the same currency"
        }
        return Cash(amount - other.amount, currency)
    }

    // Companion object - аналог статических членов в Java.
    // Здесь определена константа NONE, представляющая нулевую сумму в рублях.
    companion object {
        val NONE = Cash(BigDecimal.ZERO, Currency.getInstance("RUR"))
    }
}

// Класс с тестами для Cash.
class CashTest {
    // Аннотация @Test указывает, что функция является тестовым методом.
    @Test
    fun test() {
        // Создание двух объектов Cash с долларами США через вторичный конструктор.
        val a = Cash("10", Currency.getInstance("USD"))
        val b = Cash("20", Currency.getInstance("USD"))

        // Использование оператора минус, определённого в классе.
        val c = b - a

        // Попытка изменить amount приведёт к ошибке компиляции,
        // так как amount объявлен как val (immutable).
        // c.amount = BigDecimal.TEN; // ERROR!

        // Вывод значений в консоль для демонстрации.
        println(c.amount)        // 10
        println(a)                // ссылка на объект
        println(c.format(Locale.FRANCE)) // форматирование для Франции (например, "10,00 €")

        // Проверка, что сумма c равна 10 с помощью assertEquals.
        assertEquals(c.amount, BigDecimal.TEN)

        // @Suppress("RedundantCompanionReference") подавляет предупреждение о том,
        // что обращение к Companion излишне (можно писать Cash.NONE напрямую).
        @Suppress("RedundantCompanionReference")
        assertEquals(Cash.Companion.NONE, Cash.NONE) // проверка равенства констант
    }
}
