package it.termidredd.core.catalog

import it.termidredd.core.Direction.BACKWARD
import it.termidredd.core.Direction.FORWARD
import it.termidredd.core.TermUnit.DAYS
import it.termidredd.core.TermUnit.MONTHS
import it.termidredd.core.TermUnit.YEARS
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HalvedTermsTest {

    private fun def(amount: Int, unit: it.termidredd.core.TermUnit, dir: it.termidredd.core.Direction) =
        TermDefinition("1 c.p.c.", "x", amount, unit, dir)

    @Test
    fun `giorni pari e dispari con arrotondamento prudenziale`() {
        assertEquals(20, def(40, DAYS, BACKWARD).halved().amount)
        assertEquals(7, def(15, DAYS, FORWARD).halved().amount)   // in avanti per difetto
        assertEquals(8, def(15, DAYS, BACKWARD).halved().amount)  // a ritroso per eccesso
        assertEquals(1, def(1, DAYS, FORWARD).halved().amount)
    }

    @Test
    fun `mesi e anni`() {
        assertEquals(def(3, MONTHS, FORWARD), def(6, MONTHS, FORWARD).halved())
        assertEquals(def(45, DAYS, FORWARD), def(3, MONTHS, FORWARD).halved())
        assertEquals(def(15, DAYS, FORWARD), def(1, MONTHS, FORWARD).halved())
        assertEquals(def(6, MONTHS, FORWARD), def(1, YEARS, FORWARD).halved())
    }

    @Test
    fun `catalogo dimezzato conserva il termine di legge`() {
        val date = LocalDate.of(2027, 3, 18)
        val normal = Catalog.computeAllGrouped(date).values.flatten()
        val half = Catalog.computeAllGrouped(date, halved = true).values.flatten()
        assertEquals(normal.size, half.size)
        assertTrue(normal.all { it.halvedFrom == null })
        assertTrue(half.all { it.halvedFrom != null })
        val terza = half.single { it.definition.article.startsWith("171-ter n. 3") }
        assertEquals(5, terza.definition.amount)
        assertEquals(10, terza.halvedFrom!!.amount)
        assertNull(normal.single { it.definition.article.startsWith("171-ter n. 3") }.halvedFrom)
    }
}
