import groovy.util.GroovyTestCase.assertEquals
import org.junit.Test

class SumClassTest {

	private val sumClass = SumClass()

	@Test
	fun sum() {
		val expected = 3
		val result = sumClass.sum(1, 2)

		assertEquals(expected, result)
	}
}