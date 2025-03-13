import kotlin.test.Ignore
import kotlin.test.Test

// Весь тестовый класс отключён с помощью @Ignore.
// Студентам нужно раскомментировать код в каждом тесте и реализовать соответствующие классы,
// чтобы тесты проходили успешно.
@Ignore
class Hw1KtTest {
    // Задание 1: создать класс Rectangle с полями width и height,
    // а также методом вычисления площади - area().
    // Раскомментируйте код в тесте ниже, чтобы проверить своё решение.
    @Test
    fun rectangleArea() {
        /*val r = Rectangle(10, 20)
        assertEquals(200, r.area())
        assertEquals(10, r.width)
        assertEquals(20, r.height)*/
    }

    // Задание 2: переопределить метод toString() у класса Rectangle.
    // Раскомментируйте код в тесте ниже.
    @Test
    fun rectangleToString() {
        /*val r = Rectangle(10, 20)
        assertEquals("Rectangle(10x20)", r.toString())
        */
    }

    // Задание 3: переопределить методы equals() и hashCode() для Rectangle.
    // Раскомментируйте код в тесте ниже.
    @Test
    fun rectangleEquals() {
        /*val a = Rectangle(10, 20)
        val b = Rectangle(10, 20)
        val c = Rectangle(20, 10)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertFalse (a === b)
        assertNotEquals(a, c)
        */
    }

    // Задание 4: создать класс Square (квадрат).
    // Раскомментируйте код в тесте ниже.
    @Test
    fun squareEquals() {
        /*val a = Square(10)
        val b = Square(10)
        val c = Square(20)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertFalse (a === b)
        assertNotEquals(a, c)
        println(a)
        */
    }

    // Задание 5: создать интерфейс Figure с методом area(),
    // и унаследовать от него Rectangle и Square.
    // Раскомментируйте код в тесте ниже.
    @Test
    fun figureArea() {
        /*var f : Figure = Rectangle(10, 20)
        assertEquals(f.area(), 200)

        f = Square(10)
        assertEquals(f.area(), 100)
        */
    }

    // Задание 6: реализовать функцию diffArea(a, b),
    // которая принимает две фигуры (Figure) и возвращает разность их площадей.
    // Раскомментируйте код в тесте ниже.
    @Test
    fun diffArea() {
        /*val a = Rectangle(10, 20)
        val b = Square(10)
        assertEquals(diffArea(a, b), 100)
        */
    }
}
