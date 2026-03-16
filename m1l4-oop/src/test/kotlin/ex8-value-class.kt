import kotlin.test.Test

// @JvmInline указывает, что это value class (в Kotlin - класс-обёртка для одного значения).
// Во время выполнения объект ProductId заменяется на его свойство (value) для оптимизации,
// но сохраняется типобезопасность на этапе компиляции.
@JvmInline
value class ProductId(
    val value: String   // единственное свойство, которое будет хранить значение
) {
    // Блок инициализации выполняется при создании экземпляра.
    // Здесь проверяется, что value соответствует регулярному выражению: цифры, двоеточие, цифры.
    init {
        require(value.contains("^\\d+:\\d+$".toRegex())) {
            "ProductId must be in format 'digits:digits'"
        }
    }

    // Функция, возвращающая первую часть до двоеточия.
    fun getFirstPart() = value.split(":").first()
}

// data class автоматически генерирует toString(), equals(), hashCode(), copy().
// Содержит два свойства: id типа ProductId и name.
data class Product(
    val id: ProductId,
    val name: String,
)

// Тестовый класс (опечатка в названии, но оставлено как есть).
class ValueClasTest() {

    @Test
    fun productTest() {
        // Создание экземпляра value class ProductId.
        // Валидация в init проверит корректность формата.
        val id = ProductId("123:14")
        // Вызов метода value class.
        id.getFirstPart()

        // Создание data class Product с использованием id.
        val product = Product(id, "Product")
    }
}
