package it.termidredd.core.catalog

/**
 * Ordina i riferimenti normativi come nel codice: prima c.p.c. (per numero, poi bis/ter/..., poi n.),
 * poi le disposizioni di attuazione, poi le altre fonti (c.p.a., D.Lgs., ...).
 *
 * Esempi: "163-bis c.p.c." < "166 c.p.c." < "171-ter n. 1 c.p.c." < "171-ter n. 3 c.p.c." < "92 c.p.a."
 */
object ArticleOrder : Comparator<String> {

    private val PATTERN = Regex("""^(\d+)(?:-([a-z]+))?(?:\.(\d+))?(?:\s+n\.\s*(\d+))?\s*(.*)$""")

    private val LATIN = listOf(
        "bis", "ter", "quater", "quinquies", "sexies", "septies", "octies", "novies", "decies",
        "undecies", "duodecies", "terdecies", "quaterdecies", "quinquiesdecies", "sexiesdecies",
    ).withIndex().associate { (i, s) -> s to i + 2 }

    private data class Key(val source: Int, val sourceName: String, val number: Int, val suffix: Int, val dot: Int, val n: Int)

    private fun key(article: String): Key {
        val m = PATTERN.matchEntire(article.trim()) ?: return Key(3, article, Int.MAX_VALUE, 0, 0, 0)
        val (num, suffix, dot, n, rest) = m.destructured
        val source = when {
            rest.startsWith("c.p.c") -> 0
            rest.startsWith("disp. att") || rest.startsWith("disp.att") -> 1
            else -> 2
        }
        return Key(
            source = source,
            sourceName = rest,
            number = num.toInt(),
            suffix = if (suffix.isEmpty()) 0 else LATIN[suffix] ?: 99,
            dot = dot.toIntOrNull() ?: 0,
            n = n.toIntOrNull() ?: 0,
        )
    }

    private val byKey = compareBy<Key>({ it.source }, { it.sourceName }, { it.number }, { it.suffix }, { it.dot }, { it.n })

    override fun compare(a: String, b: String): Int = byKey.compare(key(a), key(b))
}
