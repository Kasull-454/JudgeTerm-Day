package it.termidredd.core.catalog

import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ArticleOrderTest {

    @Test
    fun `ordine numerico, latino e per numero interno`() {
        val shuffled = listOf(
            "92 c.p.a.", "171-ter n. 3 c.p.c.", "166 c.p.c.", "50 c.p.c.", "171-ter n. 1 c.p.c.",
            "163-bis c.p.c.", "163 c.p.c.", "669-terdecies c.p.c.", "669-octies c.p.c.", "103 disp. att. c.p.c.",
        )
        assertEquals(
            listOf(
                "50 c.p.c.", "163 c.p.c.", "163-bis c.p.c.", "166 c.p.c.", "171-ter n. 1 c.p.c.", "171-ter n. 3 c.p.c.",
                "669-octies c.p.c.", "669-terdecies c.p.c.", "103 disp. att. c.p.c.", "92 c.p.a.",
            ),
            shuffled.sortedWith(ArticleOrder),
        )
    }

    @Test
    fun `tutte le scadenze raggruppate per categoria e ordinate per articolo`() {
        val grouped = Catalog.computeAllGrouped(LocalDate.of(2027, 3, 18))
        assertEquals(Category.entries.filter { c -> Catalog.events.any { it.category == c } }, grouped.keys.toList())
        assertEquals(Catalog.events.sumOf { it.terms.size }, grouped.values.sumOf { it.size })
        grouped.values.forEach { list ->
            val articles = list.map { it.definition.article }
            assertEquals(articles.sortedWith(ArticleOrder), articles)
        }
        assertTrue(grouped.getValue(Category.GENERALI).first().definition.article.startsWith("47 "))
        assertTrue(grouped.getValue(Category.COGNIZIONE).first().definition.article.startsWith("38 "))
    }
}
