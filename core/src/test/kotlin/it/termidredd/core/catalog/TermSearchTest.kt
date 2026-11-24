package it.termidredd.core.catalog

import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TermSearchTest {

    private val all = Catalog.computeAllGrouped(LocalDate.of(2027, 3, 18))
    private fun labels(q: String) = TermSearch.filter(all, q).values.flatten().map { it.definition.label }

    @Test
    fun `mem trova memorie integrative e di replica`() {
        val found = labels("mem")
        assertTrue(found.isNotEmpty())
        assertTrue(found.all { "memori" in it.lowercase() })
        assertTrue("Memorie di replica" in found)
        assertEquals(4, found.size) // 3 x 171-ter + 189 n. 3
    }

    @Test
    fun `maiuscole, accenti e piu parole`() {
        assertEquals(labels("MEM"), labels("mem"))
        assertTrue(labels("perdità").contains("Perdita di efficacia del precetto"))
        assertEquals(listOf("Terza memoria integrativa"), labels("terza mem"))
    }

    @Test
    fun `cerca anche per articolo e categoria`() {
        assertTrue(labels("171-ter").size == 3)
        assertTrue(labels("lavoro").isNotEmpty())
    }

    @Test
    fun `query vuota non filtra, senza risultati rimuove le categorie`() {
        assertEquals(all, TermSearch.filter(all, "  "))
        assertTrue(TermSearch.filter(all, "zzzz").isEmpty())
    }
}
