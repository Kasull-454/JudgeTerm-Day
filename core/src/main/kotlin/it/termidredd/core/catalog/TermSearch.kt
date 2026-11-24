package it.termidredd.core.catalog

import java.text.Normalizer

/**
 * Ricerca per parti di parola: "mem" trova "memoria", "Memorie di replica"...
 * Senza distinzione tra maiuscole e minuscole né accenti. Con più parole ("mem 171") devono
 * comparire tutte, in qualsiasi ordine. Cerca in descrizione, articolo, evento e categoria.
 */
object TermSearch {

    fun matches(term: ComputedTerm, query: String): Boolean {
        val tokens = normalize(query).split(' ').filter { it.isNotEmpty() }
        if (tokens.isEmpty()) return true
        val haystack = normalize(
            listOf(
                term.definition.label,
                term.definition.article,
                term.event.title,
                term.event.dateLabel,
                term.event.category.label,
            ).joinToString(" "),
        )
        return tokens.all { it in haystack }
    }

    fun filter(grouped: Map<Category, List<ComputedTerm>>, query: String): Map<Category, List<ComputedTerm>> =
        grouped.mapValues { (_, list) -> list.filter { matches(it, query) } }.filterValues { it.isNotEmpty() }

    private val MARKS = Regex("\\p{M}+")

    internal fun normalize(s: String): String =
        MARKS.replace(Normalizer.normalize(s.lowercase(), Normalizer.Form.NFD), "")
            .replace('-', ' ')
            .replace(Regex("\\s+"), " ")
            .trim()
}
