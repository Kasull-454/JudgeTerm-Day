package it.termidredd.core.catalog

import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CatalogTest {

    @Test
    fun `id univoci e ogni evento ha almeno un termine`() {
        val ids = Catalog.events.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
        assertTrue(Catalog.events.all { it.terms.isNotEmpty() })
    }

    @Test
    fun `prima schermata e altri termini sono disgiunti`() {
        val others = Catalog.othersByCategory.values.flatten()
        assertTrue(Catalog.featured.isNotEmpty())
        assertTrue(Catalog.featured.none { it in others })
        assertEquals(Catalog.events.size, Catalog.featured.size + others.size)
    }

    @Test
    fun `udienza Cartabia calcola 163-bis, 166 e 171-ter a ritroso`() {
        val udienza = LocalDate.of(2027, 3, 18) // giovedì
        val out = Catalog.byId("udienza_citazione_cartabia")!!.computeAll(udienza)
        assertEquals(6, out.size)
        assertTrue(out.all { it.result.legalDeadline < udienza })
        val terza = out.single { it.definition.article.startsWith("171-ter n. 3") }
        assertEquals(LocalDate.of(2027, 3, 8), terza.result.nominal) // lunedì
    }
}
