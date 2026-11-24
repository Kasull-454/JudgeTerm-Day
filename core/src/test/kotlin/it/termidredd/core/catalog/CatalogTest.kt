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

class CatalogLibriTest {

    private val date = LocalDate.of(2027, 3, 18)

    @Test
    fun `ogni categoria ha almeno un evento`() {
        Category.entries.forEach { c -> assertTrue(Catalog.events.any { it.category == c }, "vuota: $c") }
    }

    @Test
    fun `lavoro, cautelare, sfratto e opposizioni esecutive non hanno sospensione feriale`() {
        val noFeriale = setOf(Category.LAVORO, Category.CAUTELARE, Category.SFRATTO)
        Catalog.events.filter { it.category in noFeriale }.flatMap { it.terms }
            .forEach { assertTrue(!it.feriale, "${it.article} ${it.label}") }
        Catalog.events.flatMap { it.terms }.filter { it.article.startsWith("617 ") }
            .forEach { assertTrue(!it.feriale) }
    }

    @Test
    fun `regolamento di competenza e termine a ritroso della ricusazione`() {
        val comp = Catalog.byId("incompetenza")!!.computeAll(date)
        val regolamento = comp.single { it.definition.article.startsWith("47 ") }
        assertEquals(LocalDate.of(2027, 4, 17), regolamento.result.nominal) // sabato
        assertEquals(LocalDate.of(2027, 4, 19), regolamento.result.legalDeadline) // lunedì
        val ric = Catalog.byId("udienza_generica")!!.computeAll(date)
            .single { it.definition.article.startsWith("52 ") }
        assertEquals(LocalDate.of(2027, 3, 16), ric.result.legalDeadline)
    }

    @Test
    fun `281-duodecies, memoria e replica nel semplificato`() {
        val out = Catalog.byId("semplificato_termini_281duodecies")!!.computeAll(date)
        assertEquals(listOf(LocalDate.of(2027, 4, 7), LocalDate.of(2027, 4, 17)), out.map { it.result.nominal })
        val found = TermSearch.filter(Catalog.computeAllGrouped(date), "281").values.flatten()
        assertTrue(found.any { it.definition.article.startsWith("281-duodecies") })
    }

    @Test
    fun `termini in anni e lodo arbitrale`() {
        val lodo = Catalog.byId("accettazione_arbitri")!!.computeAll(date).single()
        assertEquals(date.plusDays(240 + 31), lodo.result.nominal) // agosto sospeso

        val eredi = Catalog.byId("morte_parte")!!.computeAll(date).single()
        assertEquals(LocalDate.of(2028, 3, 18), eredi.result.nominal)
    }
}
